package com.sm.inspection.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sm.common.core.exception.ServiceException;
import com.sm.common.core.utils.StringUtils;
import com.sm.common.json.utils.JsonUtils;
import com.sm.common.satoken.utils.LoginHelper;
import com.sm.inspection.constant.InspectionStatus;
import com.sm.inspection.domain.InspectionDefect;
import com.sm.inspection.domain.InspectionFeedback;
import com.sm.inspection.domain.InspectionReport;
import com.sm.inspection.domain.InspectionTask;
import com.sm.inspection.mapper.InspectionDefectMapper;
import com.sm.inspection.mapper.InspectionFeedbackMapper;
import com.sm.inspection.mapper.InspectionProductMapper;
import com.sm.inspection.mapper.InspectionReportMapper;
import com.sm.inspection.mapper.InspectionTaskMapper;
import com.sm.inspection.perception.InspectionAgent;
import com.sm.inspection.perception.QcTools;
import com.sm.inspection.perception.VisionClient;
import io.agentscope.core.model.Model;
import com.sm.inspection.rule.GradeRuleEngine;
import com.sm.inspection.service.IInspectionService;
import com.sm.message.service.INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 智能质检Service业务层处理
 *
 * <p>流程编排(对应《系统详细设计》3.2/3.3):</p>
 * 提交 → MQ → ①拍照合规校验 → ②机型识别 → ③缺陷分析 → ④⑤规则引擎(分级+定价)
 * → 生成报告 → 回写商品 → 通知卖家;不达标转补拍,异常转失败。</p>
 *
 * @author sm
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class InspectionServiceImpl implements IInspectionService {

    /** 质检任务 Topic */
    public static final String TOPIC_INSPECTION_TASK = "sm_inspection_task";
    /** 可提交质检的商品状态 */
    private static final List<String> SUBMITTABLE_STATUS = List.of("DRAFT", "OFF_SHELF", "WAITING_QC");

    private final InspectionTaskMapper taskMapper;
    private final InspectionReportMapper reportMapper;
    private final InspectionDefectMapper defectMapper;
    private final InspectionFeedbackMapper feedbackMapper;
    private final InspectionProductMapper productMapper;
    private final INotificationService notificationService;
    private final VisionClient visionClient;
    private final Model agentModel;
    private final RocketMQTemplate rocketMQTemplate;
    private final TransactionTemplate transactionTemplate;

    @Value("${inspection.max-rounds:3}")
    private int maxRounds;

    @Override
    public Long submit(Long productId, List<String> photoUrls) {
        Map<String, Object> product = requireOwnedProduct(productId);
        String status = String.valueOf(product.get("status"));
        if (!SUBMITTABLE_STATUS.contains(status)) {
            throw new ServiceException("当前商品状态不允许提交质检");
        }
        if (photoUrls == null || photoUrls.isEmpty()) {
            throw new ServiceException("请至少上传 1 张照片");
        }
        long total = taskMapper.selectCount(Wrappers.<InspectionTask>lambdaQuery()
            .eq(InspectionTask::getProductId, productId));
        if (total >= maxRounds) {
            throw new ServiceException("该商品质检次数已达上限(" + maxRounds + " 次)");
        }
        int round = nextRound(productId);
        return createAndDispatch(productId, photoUrls, round);
    }

    private Map<String, Object> requireOwnedProduct(Long productId) {
        Map<String, Object> product = productMapper.selectProductInfo(productId);
        if (product == null) {
            throw new ServiceException("商品不存在");
        }
        Long sellerId = Long.valueOf(String.valueOf(product.get("sellerId")));
        if (!LoginHelper.getUserId().equals(sellerId)) {
            throw new ServiceException("无权操作该商品");
        }
        return product;
    }

    private int nextRound(Long productId) {
        InspectionTask latest = taskMapper.selectOne(Wrappers.<InspectionTask>lambdaQuery()
            .eq(InspectionTask::getProductId, productId)
            .orderByDesc(InspectionTask::getRoundNo)
            .last("limit 1"));
        return latest == null || latest.getRoundNo() == null ? 1 : latest.getRoundNo() + 1;
    }

    private Long createAndDispatch(Long productId, List<String> photoUrls, int round) {
        InspectionTask task = new InspectionTask();
        task.setProductId(productId);
        task.setRoundNo(round);
        task.setStatus(InspectionStatus.SUBMITTED);
        task.setPhotoUrls(JsonUtils.toJsonString(photoUrls));
        task.setModelName(visionClient.modelName());
        task.setCreateTime(new Date());
        taskMapper.insert(task);

        // 商品进入待质检(允许从 DRAFT/OFF_SHELF 进入)
        for (String from : SUBMITTABLE_STATUS) {
            if (productMapper.updateProductStatusCas(productId, from, "WAITING_QC") > 0) {
                break;
            }
        }
        sendInspectionMessage(task);
        return task.getId();
    }

    private void sendInspectionMessage(InspectionTask task) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("taskId", task.getId());
        message.put("productId", task.getProductId());
        message.put("roundNo", task.getRoundNo());
        message.put("submitTime", new Date());
        try {
            rocketMQTemplate.syncSend(TOPIC_INSPECTION_TASK,
                MessageBuilder.withPayload(JsonUtils.toJsonString(message)).build());
        } catch (Exception e) {
            log.error("质检任务消息发送失败 taskId={}", task.getId(), e);
        }
    }

    @Override
    public void executeTask(Long taskId) {
        InspectionTask task = taskMapper.selectById(taskId);
        if (task == null || !InspectionStatus.SUBMITTED.equals(task.getStatus())) {
            return; // 幂等:已处理或不存在
        }
        if (updateTaskCas(taskId, InspectionStatus.SUBMITTED, InspectionStatus.PROCESSING) <= 0) {
            return;
        }
        long start = System.currentTimeMillis();
        Map<String, Object> product = productMapper.selectProductInfo(task.getProductId());
        Long sellerId = product == null ? null : Long.valueOf(String.valueOf(product.get("sellerId")));
        try {
            List<String> photos = parsePhotos(task.getPhotoUrls());

            // ReAct 智能体编排:由模型自主决定调用哪些工具、顺序与是否补拍
            InspectionAgent.Result agentResult = InspectionAgent.run(
                visionClient, agentModel, task.getProductId(), photos);
            QcTools tools = agentResult.tools();

            // ① 合规校验结果(智能体第一步;未执行视为异常)
            VisionClient.ComplianceResult compliance = tools.getCompliance();
            if (compliance == null) {
                handleFailed(task, sellerId, "智能体未完成拍照合规校验");
                return;
            }
            if (!compliance.passed()) {
                handleReshootNeeded(task, sellerId, compliance);
                return;
            }

            // ②③ 感知结果
            VisionClient.ModelIdentifyResult modelInfo = tools.getModelInfo();
            if (modelInfo == null) {
                modelInfo = new VisionClient.ModelIdentifyResult("未知品牌", "未识别机型", null, null, 0.5);
            }
            List<GradeRuleEngine.DefectItem> defects = tools.getDefects();
            if (defects == null) {
                handleFailed(task, sellerId, "智能体未完成缺陷分析");
                return;
            }

            // ④⑤ 成色分级与定价:优先采用智能体调用的规则引擎工具结果;
            // 若智能体未调用,则由服务端用同一规则引擎兜底(裁决逻辑始终在代码侧)
            GradeRuleEngine.GradeResult grade = tools.getGrade();
            if (grade == null) {
                BigDecimal basePrice = null;
                if (modelInfo.marketPriceMin() != null && modelInfo.marketPriceMax() != null) {
                    basePrice = modelInfo.marketPriceMin().add(modelInfo.marketPriceMax())
                        .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
                }
                grade = GradeRuleEngine.gradeAndPrice(defects, basePrice);
            }

            Long reportId = saveSuccess(task, modelInfo, defects, grade, sellerId,
                System.currentTimeMillis() - start);

            notifySeller(sellerId, "质检完成", "商品质检完成:成色 " + grade.grade() + " 级,建议价 ¥"
                + (grade.priceMin() == null ? "-" : grade.priceMin() + " ~ " + grade.priceMax()),
                reportId);

        } catch (Exception e) {
            log.error("质检任务执行失败 taskId={}", taskId, e);
            handleFailed(task, sellerId, e.getMessage());
        }
    }

    private List<String> parsePhotos(String photoUrls) {
        if (StringUtils.isBlank(photoUrls)) {
            return List.of();
        }
        return JsonUtils.parseArray(photoUrls, String.class);
    }

    private void handleReshootNeeded(InspectionTask task, Long sellerId, VisionClient.ComplianceResult compliance) {
        String suggestion = compliance.suggestion() == null ? "" : compliance.suggestion();
        String missing = compliance.missingAngles() == null ? "" : String.join("、", compliance.missingAngles());
        String reason = "照片不达标" + (missing.isEmpty() ? "" : "(缺少:" + missing + ")") + "。" + suggestion;

        transactionTemplate.executeWithoutResult(status -> {
            taskMapper.update(null, Wrappers.<InspectionTask>lambdaUpdate()
                .set(InspectionTask::getStatus, InspectionStatus.NEED_RESHOOT)
                .set(InspectionTask::getErrorMsg, truncate(reason, 480))
                .set(InspectionTask::getUpdateTime, new Date())
                .eq(InspectionTask::getId, task.getId()));
            // 商品退回草稿,等待卖家补拍
            productMapper.updateProductStatusCas(task.getProductId(), "WAITING_QC", "DRAFT");
        });
        notifySeller(sellerId, "质检需补拍", reason, null);
    }

    private void handleFailed(InspectionTask task, Long sellerId, String error) {
        transactionTemplate.executeWithoutResult(status -> {
            taskMapper.update(null, Wrappers.<InspectionTask>lambdaUpdate()
                .set(InspectionTask::getStatus, InspectionStatus.FAILED)
                .set(InspectionTask::getErrorMsg, truncate(error == null ? "质检服务异常" : error, 480))
                .set(InspectionTask::getUpdateTime, new Date())
                .eq(InspectionTask::getId, task.getId()));
            productMapper.updateProductStatusCas(task.getProductId(), "WAITING_QC", "DRAFT");
        });
        notifySeller(sellerId, "质检失败", "质检服务异常,请稍后重新提交", null);
    }

    private Long saveSuccess(InspectionTask task, VisionClient.ModelIdentifyResult modelInfo,
                             List<GradeRuleEngine.DefectItem> defects, GradeRuleEngine.GradeResult grade,
                             Long sellerId, long durationMs) {
        Long[] reportIdHolder = new Long[1];
        transactionTemplate.executeWithoutResult(status -> {
            // 报告
            InspectionReport report = new InspectionReport();
            report.setTaskId(task.getId());
            report.setProductId(task.getProductId());
            report.setQualityGrade(grade.grade());
            report.setBrandModel(modelInfo.brand() + " " + modelInfo.model());
            report.setDefectSummary(buildDefectSummary(defects));
            report.setPriceMin(grade.priceMin());
            report.setPriceMax(grade.priceMax());
            report.setConfidence(BigDecimal.valueOf(modelInfo.confidence() * 100)
                .setScale(1, RoundingMode.HALF_UP));
            report.setDisclaimer("AI 辅助质检,仅供参考");
            report.setStatus("VALID");
            report.setCreateBy(sellerId);
            report.setCreateTime(new Date());
            report.setUpdateTime(new Date());
            reportMapper.insert(report);
            reportIdHolder[0] = report.getId();

            // 缺陷明细
            if (defects != null) {
                int sort = 0;
                for (GradeRuleEngine.DefectItem defect : defects) {
                    InspectionDefect entity = new InspectionDefect();
                    entity.setReportId(report.getId());
                    entity.setDefectType(defect.type());
                    entity.setPosition(defect.position());
                    entity.setSeverity(defect.severity());
                    entity.setDescription(describe(defect));
                    entity.setSort(sort++);
                    defectMapper.insert(entity);
                }
            }

            // 任务成功
            taskMapper.update(null, Wrappers.<InspectionTask>lambdaUpdate()
                .set(InspectionTask::getStatus, InspectionStatus.SUCCESS)
                .set(InspectionTask::getConfidence, report.getConfidence())
                .set(InspectionTask::getDurationMs, durationMs)
                .set(InspectionTask::getUpdateTime, new Date())
                .eq(InspectionTask::getId, task.getId()));

            // 商品回写质检结果,并通过质检自动上架
            productMapper.updateProductReport(task.getProductId(), grade.grade(), report.getId());
            productMapper.updateProductStatusCas(task.getProductId(), "WAITING_QC", "ON_SALE");
        });
        return reportIdHolder[0];
    }

    private String buildDefectSummary(List<GradeRuleEngine.DefectItem> defects) {
        if (defects == null || defects.isEmpty()) {
            return "未检出明显外观缺陷";
        }
        return "共检出 " + defects.size() + " 处缺陷,详见缺陷明细";
    }

    private String describe(GradeRuleEngine.DefectItem defect) {
        String typeName = switch (defect.type()) {
            case "SCRATCH" -> "划痕";
            case "CRACK" -> "裂纹";
            case "DENT" -> "磕碰";
            case "PAINT_LOSS" -> "掉漆";
            case "WATER_DAMAGE" -> "进水痕";
            default -> "其他";
        };
        String severityName = switch (defect.severity()) {
            case "MILD" -> "轻微";
            case "MODERATE" -> "中等";
            case "SEVERE" -> "严重";
            default -> defect.severity();
        };
        return (defect.position() == null ? "" : defect.position()) + typeName + "(" + severityName + ")";
    }

    private int updateTaskCas(Long taskId, String from, String to) {
        LambdaUpdateWrapper<InspectionTask> uw = Wrappers.lambdaUpdate();
        uw.set(InspectionTask::getStatus, to);
        uw.set(InspectionTask::getUpdateTime, new Date());
        uw.eq(InspectionTask::getId, taskId);
        uw.eq(InspectionTask::getStatus, from);
        return taskMapper.update(null, uw);
    }

    @Override
    public Long reshoot(Long taskId, List<String> photoUrls) {
        InspectionTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new ServiceException("质检任务不存在");
        }
        if (!InspectionStatus.NEED_RESHOOT.equals(task.getStatus())) {
            throw new ServiceException("当前任务不需要补拍");
        }
        Map<String, Object> product = requireOwnedProduct(task.getProductId());
        if (photoUrls == null || photoUrls.isEmpty()) {
            throw new ServiceException("请至少上传 1 张照片");
        }
        long total = taskMapper.selectCount(Wrappers.<InspectionTask>lambdaQuery()
            .eq(InspectionTask::getProductId, task.getProductId()));
        if (total >= maxRounds) {
            throw new ServiceException("该商品质检次数已达上限(" + maxRounds + " 次)");
        }
        return createAndDispatch(task.getProductId(), photoUrls, task.getRoundNo() + 1);
    }

    @Override
    public Map<String, Object> queryTask(Long taskId) {
        InspectionTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new ServiceException("质检任务不存在");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("task", task);
        InspectionReport report = reportMapper.selectOne(Wrappers.<InspectionReport>lambdaQuery()
            .eq(InspectionReport::getTaskId, taskId).last("limit 1"));
        if (report != null) {
            result.put("reportId", report.getId());
        }
        return result;
    }

    @Override
    public Map<String, Object> queryReport(Long reportId) {
        InspectionReport report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new ServiceException("质检报告不存在");
        }
        return buildReportDetail(report);
    }

    @Override
    public Map<String, Object> queryProductReport(Long productId) {
        InspectionReport report = reportMapper.selectOne(Wrappers.<InspectionReport>lambdaQuery()
            .eq(InspectionReport::getProductId, productId)
            .eq(InspectionReport::getStatus, "VALID")
            .orderByDesc(InspectionReport::getCreateTime)
            .last("limit 1"));
        if (report == null) {
            return null;
        }
        return buildReportDetail(report);
    }

    private Map<String, Object> buildReportDetail(InspectionReport report) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("report", report);
        result.put("defects", defectMapper.selectList(Wrappers.<InspectionDefect>lambdaQuery()
            .eq(InspectionDefect::getReportId, report.getId())
            .orderByAsc(InspectionDefect::getSort)));
        Map<String, Object> product = productMapper.selectProductInfo(report.getProductId());
        if (product != null) {
            result.put("productTitle", product.get("title"));
            result.put("productId", product.get("id"));
        }
        return result;
    }

    @Override
    public void feedback(Long reportId, String content) {
        if (StringUtils.isBlank(content)) {
            throw new ServiceException("反馈内容不能为空");
        }
        InspectionReport report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new ServiceException("质检报告不存在");
        }
        InspectionFeedback feedback = new InspectionFeedback();
        feedback.setReportId(reportId);
        feedback.setUserId(LoginHelper.getUserId());
        feedback.setContent(content.length() > 500 ? content.substring(0, 500) : content);
        feedback.setStatus("PENDING");
        feedback.setCreateTime(new Date());
        feedbackMapper.insert(feedback);
    }

    @Override
    public List<Map<String, Object>> guide(Long categoryId) {
        List<Map<String, Object>> guides = new ArrayList<>();
        guides.add(guideItem("正面", "整机正面,屏幕完整入镜,光线充足"));
        guides.add(guideItem("背面", "整机背面,后盖与摄像头完整入镜"));
        guides.add(guideItem("侧面", "侧边框与按键细节,重点拍摄磨损处"));
        guides.add(guideItem("屏幕点亮", "点亮屏幕显示主界面,便于检查坏点与亮线"));
        guides.add(guideItem("序列号标签", "拍摄机身上的序列号/型号标签,用于机型识别"));
        return guides;
    }

    private Map<String, Object> guideItem(String angle, String tip) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("angle", angle);
        item.put("tip", tip);
        return item;
    }

    private void notifySeller(Long sellerId, String title, String content, Long reportId) {
        if (sellerId == null) {
            return;
        }
        String link = reportId == null ? "/trade/my" : "/inspection/report?id=" + reportId;
        try {
            notificationService.send(sellerId, "INSPECTION", title, content, link);
        } catch (Exception e) {
            log.warn("质检通知发送失败 sellerId={}", sellerId, e);
        }
    }

    private String truncate(String text, int max) {
        if (text == null) {
            return null;
        }
        return text.length() > max ? text.substring(0, max) : text;
    }
}
