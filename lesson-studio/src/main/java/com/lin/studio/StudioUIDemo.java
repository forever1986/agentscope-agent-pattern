package com.lin.studio;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.studio.StudioManager;
import io.agentscope.core.studio.StudioMessageHook;
import io.agentscope.core.studio.StudioUserAgent;

public class StudioUIDemo {

    public static void main(String[] args) {
        // 初始化 Studio 连接
        StudioManager.init()
                .studioUrl("http://localhost:3000")
                .project("JavaExamples")
                .runName("demo_" + System.currentTimeMillis())
                .initialize()
                .block();

        // 创建带 Hook 的 Agent
        // 创建用户 Agent
        StudioUserAgent user = StudioUserAgent.builder()
                .name("User")
                .studioClient(StudioManager.getClient())
                .webSocketClient(StudioManager.getWebSocketClient())
                .build();
        try {
            // 创建 Agent（带 Studio Hook）
            ReActAgent agent = ReActAgent.builder()
                    .name("Assistant")
                    .sysPrompt("你是一个有帮助的 AI 助手。")
                    .model(DashScopeChatModel.builder()
                            .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                            .modelName(System.getenv("QWEN_MODEL"))
                            .build())
                    .hook(new StudioMessageHook(StudioManager.getClient()))
                    .build();

            // 对话循环
            System.out.println("Starting conversation (type 'exit' to quit)");
            System.out.println("Open http://localhost:3000 to interact\n");

            Msg msg = null;
            int turn = 1;
            while (true) {
                System.out.println("[Turn " + turn + "] Waiting for user input...");
                // 从Studio接入WEB UI方式
                msg = user.call(msg).block();

                // 如果msg是空或者消息体=exit，则退出
                if (msg == null || "exit".equalsIgnoreCase(msg.getTextContent())) {
                    System.out.println("\nConversation ended");
                    break;
                }

                // 将用户消息给Agent调用
                System.out.println("[Turn " + turn + "] User: " + msg.getTextContent());
                msg = agent.call(msg).block();

                // 输出Agent返回结果
                if (msg != null) {
                    System.out.println("[Turn " + turn + "] Agent: "
                            + msg.getTextContent() + "\n");
                }

                turn++;
            }

        } finally {
            System.out.println("\nShutting down...");
            // 清理资源
            StudioManager.shutdown();
            System.out.println("Done\n");
        }
    }
}
