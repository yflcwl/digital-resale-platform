package com.sm.inspection.perception;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.ContentBlock;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.Model;
import io.agentscope.core.state.InMemoryAgentStateStore;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.harness.agent.HarnessAgent;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 质检智能体(AgentScope HarnessAgent)
 *
 * <p>单一智能体 + 四工具(check_compliance / recognize_model / analyze_defects / grade_and_price),
 * 由模型在 ReAct 循环中自主决定调用顺序与是否补拍;分级定价固定由规则引擎工具裁决。</p>
 *
 * <p>系统提示词、任务模板与视觉提示词均外置于 classpath:prompts/ 目录,由 {@link PromptLoader} 加载。</p>
 *
 * @author sm
 */
@Slf4j
public final class InspectionAgent {

    /** 单任务最大推理轮次 */
    private static final int MAX_ITERS = 8;
    /** 单任务执行超时 */
    private static final Duration TIMEOUT = Duration.ofSeconds(180);
    /** Harness 工作目录(构建产物目录下,避免污染项目根目录) */
    private static final Path WORKSPACE = Path.of("target", "agentscope-workspace");

    private InspectionAgent() {
    }

    /**
     * 执行一次质检任务
     */
    public static Result run(VisionClient visionClient, Model model, Long productId, List<String> photos) {
        prepareWorkspace();
        QcTools tools = new QcTools(visionClient, productId, photos);
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(tools);

        HarnessAgent agent = HarnessAgent.builder()
            .name("QcInspector")
            .description("二手数码商品质检智能体")
            .sysPrompt(PromptLoader.load("inspection-system"))
            .model(model)
            .toolkit(toolkit)
            .maxIters(MAX_ITERS)
            .workspace(WORKSPACE)
            .stateStore(new InMemoryAgentStateStore())
            .disableSessionPersistence()
            .disableFilesystemTools()
            .disableShellTool()
            .disableWebTools()
            .disableSubagents()
            .disableMemoryTools()
            .disableMemoryHooks()
            .disableWorkspaceContext()
            .disableAtPathExpansion()
            .disableDefaultWorkspaceSkills()
            .disableDynamicSkills()
            .disableDynamicSubagents()
            .disableToolsConfig()
            .build();

        Msg input = Msg.builder()
            .name("user")
            .role(MsgRole.USER)
            .textContent(PromptLoader.format("inspection-task",
                Map.of("productId", String.valueOf(productId),
                    "photoCount", String.valueOf(photos == null ? 0 : photos.size()))))
            .build();

        Msg reply = agent.call(input, RuntimeContext.builder()
            .userId("inspection")
            .sessionId("qc-" + productId + "-" + System.nanoTime())
            .build()).block(TIMEOUT);
        String finalText = extractText(reply);
        log.info("[InspectionAgent] task finished productId={}, compliance={}, defects={}, grade={}, reply={}",
            productId,
            tools.getCompliance() == null ? null : tools.getCompliance().passed(),
            tools.getDefects() == null ? 0 : tools.getDefects().size(),
            tools.getGrade() == null ? null : tools.getGrade().grade(),
            finalText);
        return new Result(tools, finalText);
    }

    private static void prepareWorkspace() {
        try {
            Files.createDirectories(WORKSPACE);
        } catch (IOException e) {
            throw new UncheckedIOException("质检工作目录创建失败: " + WORKSPACE.toAbsolutePath(), e);
        }
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
