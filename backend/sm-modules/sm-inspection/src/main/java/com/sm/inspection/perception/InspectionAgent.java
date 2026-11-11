package com.sm.inspection.perception;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.ContentBlock;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.Model;
import io.agentscope.core.tool.Toolkit;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;

/**
 * 质检智能体(AgentScope ReAct)
 *
 * <p>单一智能体 + 四工具(check_compliance / recognize_model / analyze_defects / grade_and_price),
 * 由模型在 ReAct 循环中自主决定调用顺序与是否补拍;分级定价固定由规则引擎工具裁决。</p>
 *
 * @author sm
 */
@Slf4j
public final class InspectionAgent {

    /** 单任务最大推理轮次 */
    private static final int MAX_ITERS = 8;
    /** 单任务执行超时 */
    private static final Duration TIMEOUT = Duration.ofSeconds(180);

    private static final String SYS_PROMPT = """
        你是二手数码商品质检智能体,负责编排一次完整的质检流程。

        可用工具:
        1. check_compliance:拍照合规校验(角度是否齐全、清晰度是否达标)。
        2. recognize_model:机型识别(品牌/型号/配置/二手行情价区间)。
        3. analyze_defects:外观缺陷分析(类型/位置/严重度/置信度)。
        4. grade_and_price:按系统规则引擎计算成色分、等级(S/A/B/C/D)与参考价区间,入参 defects_json 为缺陷数组。

        执行规则:
        - 第一步必须调用 check_compliance。
        - 若 check_compliance 返回 passed=false,直接输出需要补拍的原因与建议,不再调用其他工具。
        - 合规通过后,依次调用 recognize_model、analyze_defects;调用 grade_and_price 时把 analyze_defects 返回的缺陷数组作为 defects_json 传入。
        - 成色等级与价格只能由 grade_and_price 裁决,你不得自行推断或修改数值。
        - 全部工具完成后,用一句话总结本次质检结论。
        """;

    private InspectionAgent() {
    }

    /**
     * 执行一次质检任务
     */
    public static Result run(VisionClient visionClient, Model model, Long productId, List<String> photos) {
        QcTools tools = new QcTools(visionClient, productId, photos);
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(tools);

        ReActAgent agent = ReActAgent.builder()
            .name("QcInspector")
            .sysPrompt(SYS_PROMPT)
            .model(model)
            .toolkit(toolkit)
            .maxIters(MAX_ITERS)
            .build();

        Msg input = Msg.builder()
            .name("user")
            .role(MsgRole.USER)
            .textContent("请对商品 #" + productId + " 的 " + (photos == null ? 0 : photos.size())
                + " 张照片执行质检流程,并给出最终结论。")
            .build();

        Msg reply = agent.call(input).block(TIMEOUT);
        String finalText = extractText(reply);
        log.info("[InspectionAgent] task finished productId={}, compliance={}, defects={}, grade={}, reply={}",
            productId,
            tools.getCompliance() == null ? null : tools.getCompliance().passed(),
            tools.getDefects() == null ? 0 : tools.getDefects().size(),
            tools.getGrade() == null ? null : tools.getGrade().grade(),
            finalText);
        return new Result(tools, finalText);
    }

    private static String extractText(Msg reply) {
        if (reply == null || reply.getContent() == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (ContentBlock block : reply.getContent()) {
            if (block instanceof TextBlock text) {
                sb.append(text.getText());
            }
        }
        return sb.toString();
    }

    /**
     * 执行结果:工具实例(含各阶段结构化结果)+ 智能体最终结论
     */
    public record Result(QcTools tools, String finalText) {
    }
}
