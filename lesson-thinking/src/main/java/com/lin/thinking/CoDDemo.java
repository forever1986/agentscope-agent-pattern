package com.lin.thinking;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.formatter.dashscope.DashScopeMultiAgentFormatter;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.pipeline.MsgHub;

public class CoDDemo {

    public static class JudgeResult {
        public boolean finished;
        public String correctAnswer;
    }

    public static void main(String[] args) {
        // 主题
        String topic = """
            两个圆外切且没有相对滑动。圆 A 的半径是圆 B 半径的 1/3。
            圆 A 绕圆 B 滚动一圈回到起点。圆 A 总共会自转多少圈？
            """;

        // 创建模型
        DashScopeChatModel model = DashScopeChatModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName(System.getenv("QWEN_MODEL"))
                .formatter(new DashScopeMultiAgentFormatter())
                .build();

        // 创建辩论者
        ReActAgent alice = ReActAgent.builder()
                .name("Alice")
                .sysPrompt("你是辩论者 Alice。主题：" + topic)
                .model(model)
                .memory(new InMemoryMemory())
                .build();

        ReActAgent bob = ReActAgent.builder()
                .name("Bob")
                .sysPrompt("你是辩论者 Bob。主题：" + topic)
                .model(model)
                .memory(new InMemoryMemory())
                .build();

        // 创建主持人
        ReActAgent moderator = ReActAgent.builder()
                .name("Moderator")
                .sysPrompt("你是主持人，评估关于以下主题的辩论：" + topic)
                .model(model)
                .memory(new InMemoryMemory())
                .build();

        // 运行辩论
        for (int round = 1; round <= 5; round++) {
            System.out.println("\n=== 第 " + round + " 轮 ===\n");

            try (MsgHub hub = MsgHub.builder()
                    .participants(alice, bob, moderator)
                    .build()) {

                hub.enter().block();

                Msg aliceMsg = alice.call(Msg.builder()
                        .name("user")
                        .role(MsgRole.USER)
                        .content(TextBlock.builder()
                                .text("发表你的观点。")
                                .build())
                        .build()).block();
                System.out.println("Alice: " + aliceMsg.getTextContent());

                Msg bobMsg = bob.call(Msg.builder()
                        .name("user")
                        .role(MsgRole.USER)
                        .content(TextBlock.builder()
                                .text("回应 Alice 并发表你的观点。")
                                .build())
                        .build()).block();
                System.out.println("Bob: " + bobMsg.getTextContent());
            }

            // 主持人判决
            Msg judgeMsg = moderator.call(
                    Msg.builder()
                            .name("user")
                            .role(MsgRole.USER)
                            .content(TextBlock.builder()
                                    .text("评估辩论。是否有正确答案？")
                                    .build())
                            .build(),
                    JudgeResult.class
            ).block();

            JudgeResult result = judgeMsg.getStructuredData(JudgeResult.class);

            if (result.finished) {
                System.out.println("\n=== 辩论结束 ===");
                System.out.println("答案：" + result.correctAnswer);
                break;
            }
        }
    }
}
