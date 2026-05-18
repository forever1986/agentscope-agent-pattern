package com.lin.hook;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.formatter.dashscope.DashScopeChatFormatter;
import io.agentscope.core.hook.*;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.message.ThinkingBlock;
import io.agentscope.core.message.ToolResultBlock;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolEmitter;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.core.tool.Toolkit;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

public class HookDemo {

    public static void main(String[] args) throws Exception {
        // 创建一个用于监控的 hook
        Hook monitoringHook = new MonitoringHook();

        // 创建工具Toolkit
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new ProgressTools());

        // Create Agent with hook
        ReActAgent agent =
                ReActAgent.builder()
                        .name("HookAgent")
                        .sysPrompt("你是一名乐于助人的助手。在处理数据时，请使用“process_data”这个工具。")
                        .model(DashScopeChatModel.builder()
                                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                                .modelName(System.getenv("QWEN_MODEL"))
                                .stream(true)
                                .enableThinking(true) // 开启思考模式
                                .formatter(new DashScopeChatFormatter())
                                .build())
                        .toolkit(toolkit)
                        .memory(new InMemoryMemory())
                        .hook(monitoringHook)
                        .build();

        // 调用call方法返回数据
        Msg response = agent.call(Msg.builder()
                .textContent("请处理财务数据！")
                .build()).block();
        // 打印输出结果
        System.out.println("最终回复："+(response==null?null:response.getTextContent()));
    }

    /**
     * 用于记录所有代理执行事件的日志监控Hook
     */
    static class MonitoringHook implements Hook {

        @Override
        public <T extends HookEvent> Mono<T> onEvent(T event) {
            if (event instanceof PreCallEvent preCall) {
                System.out.println("[HOOK PreCallEvent] - Agent started: " + preCall.getAgent().getName());

            } else if (event instanceof ReasoningChunkEvent reasoningChunk) {
                // 在推理内容到达时逐段打印（增量块）
                Msg chunk = reasoningChunk.getIncrementalChunk();
                String text = getTextContent(chunk);
                if (!text.isEmpty()) {
                    System.out.print(text);
                }
            } else if (event instanceof PreReasoningEvent preReasoning) {
                System.out.println(
                        "[HOOK PreReasoningEvent] - Before Reasoning");

            } else if (event instanceof PreActingEvent preActing) {
                System.out.println(
                        "\n[HOOK PreActingEvent] - Tool: "
                                + preActing.getToolUse().getName()
                                + ", Input: "
                                + preActing.getToolUse().getInput());

            } else if (event instanceof ActingChunkEvent actingChunk) {
                // 接收来自 ToolEmitter 的进度更新信息
                ToolResultBlock chunk = actingChunk.getChunk();
                String output =
                        chunk.getOutput().isEmpty() ? "" : chunk.getOutput().get(0).toString();
                System.out.println(
                        "[HOOK ActingChunkEvent] - Tool: "
                                + actingChunk.getToolUse().getName()
                                + ", Progress: "
                                + output);

            } else if (event instanceof PostActingEvent postActing) {
                ToolResultBlock result = postActing.getToolResult();
                String output =
                        result.getOutput().isEmpty() ? "" : result.getOutput().get(0).toString();
                System.out.println(
                        "[HOOK PostActingEvent] - Tool: "
                                + postActing.getToolUse().getName()
                                + ", Result: "
                                + output);
            }  else if (event instanceof PostReasoningEvent postReasoning) {
                System.out.println(
                        "\n[HOOK PostReasoningEvent] - After Reasoning");

            } else if (event instanceof PostCallEvent) {
                System.out.println("\n[HOOK PostCallEvent] - Agent execution finished\n");
            }

            // 保持Event原样返回
            return Mono.just(event);
        }
    }

    /** 使用“ToolEmitter”来报告进度的工具 */
    public static class ProgressTools {

        @Tool(name = "数据处理", description = "处理数据和财报工具")
        public String processData(
                @ToolParam(name = "dataset_name", description = "要处理的数据集名称")
                String datasetName,
                ToolEmitter emitter) {

            System.out.println(
                    "[TOOL] Starting to process dataset: "
                            + datasetName
                            + " (this will take a few seconds)");

            try {
                // 模拟处理过程，并提供进度更新信息。
                for (int i = 1; i <= 5; i++) {
                    Thread.sleep(800);
                    int progress = i * 20;

                    // 发出进度信息
                    emitter.emit(
                            ToolResultBlock.text(
                                    String.format("Processed %d%% of %s", progress, datasetName)));
                }

                return String.format(
                        "Successfully processed dataset '%s'. Total: 1000 records analyzed.",
                        datasetName);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Processing interrupted";
            }
        }
    }

    private static String getTextContent(Msg msg) {
        String thinking =
                msg.getContent().stream()
                        .filter(block -> block instanceof ThinkingBlock)
                        .map(block -> ((ThinkingBlock) block).getThinking())
                        .collect(Collectors.joining("\n"));

        String text =
                msg.getContent().stream()
                        .filter(block -> block instanceof TextBlock)
                        .map(block -> ((TextBlock) block).getText())
                        .collect(Collectors.joining("\n"));

        if (!thinking.isEmpty() && !text.isEmpty()) {
            return thinking + "\n\n" + text;
        } else if (!thinking.isEmpty()) {
            return thinking;
        } else if (!text.isEmpty()) {
            return text;
        } else {
            return "[No response]";
        }
    }
}