package com.sm.inspection.service;

import java.util.List;
import java.util.Map;

/**
 * 智能质检Service接口
 *
 * @author sm
 */
public interface IInspectionService {

    /**
     * 提交质检(创建任务并投递 MQ)
     */
    Long submit(Long productId, List<String> photoUrls);

    /**
     * 执行质检任务(MQ 消费入口,幂等)
     */
    void executeTask(Long taskId);

    /**
     * 补拍(基于 NEED_RESHOOT 任务创建新一轮)
     */
    Long reshoot(Long taskId, List<String> photoUrls);

    /**
     * 查询任务状态(前端轮询)
     */
    Map<String, Object> queryTask(Long taskId);

    /**
     * 报告详情(含缺陷明细)
     */
    Map<String, Object> queryReport(Long reportId);

    /**
     * 商品最新有效报告(公开)
     */
    Map<String, Object> queryProductReport(Long productId);

    /**
     * 报告反馈
     */
    void feedback(Long reportId, String content);

    /**
     * 拍照引导清单(按品类)
     */
    List<Map<String, Object>> guide(Long categoryId);
}
