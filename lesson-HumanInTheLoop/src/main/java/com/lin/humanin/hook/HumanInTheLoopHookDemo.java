package com.lin.humanin.hook;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.hook.Hook;
import io.agentscope.core.hook.HookEvent;
import io.agentscope.core.hook.PostReasoningEvent;
import io.agentscope.core.message.*;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.file.WriteFileTool;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

public class HumanInTheLoopHookDemo {

    private static final BufferedReader reader =
            new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) throws IOException {

        // 使用Hook来判断是否调用到敏感工具
        Hook confirmationHook = new Hook() {
            private static final List<String> SENSITIVE_TOOLS = List.of("insert_text_file", "write_text_file");

            @Override
            public <T extends HookEvent> Mono<T> onEvent(T event) {
                if (event instanceof PostReasoningEvent e) {
                    Msg reasoningMsg = e.getReasoningMessage();
                    List<ToolUseBlock> toolCalls = reasoningMsg.getContentBlocks(ToolUseBlock.class);

                    // 如果包含敏感工具，暂停等待确认
                    boolean hasSensitive = toolCalls.stream()
                            .anyMatch(t -> SENSITIVE_TOOLS.contains(t.getName()));

                    if (hasSensitive) {
                        System.out.println("本次需要调用到敏感工具，工具信息如下：");
                        e.stopAgent();
                    }
                }
                return Mono.just(event);
            }
        };

        // 使用内置的WriteFileTool工具
        URL resource = ClassLoader.getSystemResource("");
        File file = new File(resource.getPath());
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new WriteFileTool(file.getPath()));

        // 创建智能体并内联配置模型
        ReActAgent agent = ReActAgent.builder()
                .name("Assistant")
                .sysPrompt("你是一个有帮助的 AI 助手。")
                .model(DashScopeChatModel.builder()
                        .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                        .modelName(System.getenv("QWEN_MODEL"))
                        .build())
                .toolkit(toolkit)
                .hook(confirmationHook)
                .build();

        // 让其收集信息，写入到文件中
        Msg response = agent.call(Msg.builder()
                .name("user")
                .role(MsgRole.USER)
                .textContent("帮我将阿里巴巴的信息归纳为100个字写入到ali.md文件")
                .build()).block();

        // 检查是否有待确认的工具调用
        while (response.hasContentBlocks(ToolUseBlock.class)) {
            // 展示待执行的工具
            List<ToolUseBlock> pending = response.getContentBlocks(ToolUseBlock.class);
            for (ToolUseBlock tool : pending) {
                System.out.println("工具: " + tool.getName());
                System.out.println("参数: " + tool.getInput());
            }

            if (userConfirms()) {
                // 用户确认，继续执行
                response = agent.call().block();
            } else {
                // 用户拒绝，返回取消信息
                Msg cancelResult = Msg.builder()
                        .role(MsgRole.TOOL)
                        .content(pending.stream()
                                .map(t -> ToolResultBlock.of(t.getId(), t.getName(),
                                        TextBlock.builder().text("操作已取消").build()))
                                .toArray(ToolResultBlock[]::new))
                        .build();
                response = agent.call(cancelResult).block();
            }
        }

        // 最终响应
        System.out.println(response.getTextContent());
    }

    // 来自控制台的输入，用于判断用户回到
    private static boolean userConfirms() throws IOException {
        System.out.print("You> ");
        String input = reader.readLine();
        // 这里只是简单判断“可以”两个字
        if("可以".equals(input)){
            return true;
        }
        return false;

    }
}
