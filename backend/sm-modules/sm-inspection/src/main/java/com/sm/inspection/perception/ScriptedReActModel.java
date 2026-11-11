package com.sm.inspection.perception;

import io.agentscope.core.message.ContentBlock;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.message.ToolResultBlock;
import io.agentscope.core.message.ToolUseBlock;
import io.agentscope.core.model.ChatResponse;
import io.agentscope.core.model.ChatUsage;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.model.Model;
import io.agentscope.core.model.ToolSchema;
import io.agentscope.core.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 离线 ReAct 模型(演示模式)
 *
 * <p>实现 AgentScope {@link Model} 接口,按"已执行工具 + 观察结果"决定下一步动作,
 * 让 ReAct 循环在无多模态 API Key 时也能真实运行(Thought→Action→Tool→Observation)。</p>
 *
 * <p>决策逻辑与真实模型一致:合规未通过直接结束并要求补拍;通过后依次识别机型、
 * 分析缺陷,并把缺陷清单交给规则引擎工具裁决。</p>
 *
 * @author sm
 */
@Slf4j
public class ScriptedReActModel implements Model {

    @Override
    public Flux<ChatResponse> stream(List<Msg> messages, List<ToolSchema> tools, GenerateOptions options) {
        Set<String> calledTools = new HashSet<>();
        StringBuilder observations = new StringBuilder();
        if (messages != null) {
            for (Msg msg : messages) {
                if (msg.getContent() == null) {
                    continue;
                }
                for (ContentBlock block : msg.getContent()) {
                    collectBlock(block, calledTools, observations);
                }
            }
        }
        String obs = observations.toString();
        ChatResponse response;
        if (!calledTools.contains("check_compliance")) {
            response = toolCall("check_compliance", Map.of());
        } else if (!calledTools.contains("recognize_model")) {
            if (obs.contains("\"passed\":false") || obs.contains("\"passed\": false")) {
                response = text("照片合规校验未通过,请按提示补拍后重新提交质检。");
            } else {
                response = toolCall("recognize_model", Map.of());
            }
        } else if (!calledTools.contains("analyze_defects")) {
            response = toolCall("analyze_defects", Map.of());
        } else if (!calledTools.contains("grade_and_price")) {
            String defectsJson = extractDefectsArray(obs);
            response = toolCall("grade_and_price", Map.of("defects_json", defectsJson));
        } else {
            response = text("质检流程完成,成色等级与参考价格已由规则引擎裁决生成。");
        }
        log.debug("[ScriptedReActModel] called={}, action={}", calledTools, response.getContent());
        return Flux.just(response);
    }

    private void collectBlock(ContentBlock block, Set<String> calledTools, StringBuilder observations) {
        if (block instanceof ToolUseBlock use) {
            calledTools.add(use.getName());
            return;
        }
        if (block instanceof ToolResultBlock result) {
            if (result.getOutput() != null) {
                for (ContentBlock out : result.getOutput()) {
                    if (out instanceof TextBlock textBlock) {
                        observations.append(textBlock.getText()).append('\n');
                    }
                }
            }
            return;
        }
        if (block instanceof TextBlock text) {
            observations.append(text.getText()).append('\n');
        }
    }

    /**
     * 从观察历史中提取最近一次缺陷清单 JSON 数组
     */
    private String extractDefectsArray(String observations) {
        int anchor = observations.lastIndexOf("\"defects\"");
        if (anchor < 0) {
            return "[]";
        }
        int arrayStart = observations.indexOf('[', anchor);
        if (arrayStart < 0) {
            return "[]";
        }
        int arrayEnd = observations.indexOf(']', arrayStart);
        if (arrayEnd < 0) {
            return "[]";
        }
        return observations.substring(arrayStart, arrayEnd + 1);
    }

    @Override
    public String getModelName() {
        return "scripted-react-model";
    }

    private static ChatResponse text(String value) {
        return ChatResponse.builder()
            .id("msg_" + UUID.randomUUID())
            .content(List.of(TextBlock.builder().text(value).build()))
            .usage(new ChatUsage(5, 10, 15))
            .build();
    }

    private static ChatResponse toolCall(String toolName, Map<String, Object> arguments) {
        Map<String, Object> args = arguments == null ? new HashMap<>() : arguments;
        return ChatResponse.builder()
            .id("msg_" + UUID.randomUUID())
            .content(List.of(
                ToolUseBlock.builder()
                    .name(toolName)
                    .id(UUID.randomUUID().toString())
                    .input(args)
                    .content(JsonUtils.getJsonCodec().toJson(args))
                    .build()))
            .usage(new ChatUsage(8, 15, 23))
            .build();
    }
}
