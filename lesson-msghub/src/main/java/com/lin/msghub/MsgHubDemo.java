package com.lin.msghub;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.formatter.dashscope.DashScopeMultiAgentFormatter;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.pipeline.MsgHub;


public class MsgHubDemo {

    public static void main(String[] args) {
        // 创建模型，使用 MultiAgentFormatter
        DashScopeChatModel model = DashScopeChatModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName(System.getenv("QWEN_MODEL"))
                .stream(false)
                .formatter(new DashScopeMultiAgentFormatter()) // 必须使用DashScopeMultiAgentFormatter
                .build();

        // 创建智能体
        ReActAgent alice = ReActAgent.builder()
                .name("Alice")
                .sysPrompt("你是 Alice，一位友好的老师。回答请简洁。")
                .model(model)
                .memory(new InMemoryMemory())
                .build();

        ReActAgent bob = ReActAgent.builder()
                .name("Bob")
                .sysPrompt("你是 Bob，一位好奇的学生。回答请简洁。")
                .model(model)
                .memory(new InMemoryMemory())
                .build();

        ReActAgent charlie = ReActAgent.builder()
                .name("Charlie")
                .sysPrompt("你是 Charlie，一位深思熟虑的观察者。回答请简洁。")
                .model(model)
                .memory(new InMemoryMemory())
                .build();

        // 创建公告消息
        Msg announcement = Msg.builder()
                .name("system")
                .role(MsgRole.SYSTEM)
                .content(TextBlock.builder()
                        .text("欢迎来到讨论！请简短地介绍一下自己。")
                        .build())
                .build();

        // 使用 try-with-resources 管理 MsgHub，Hub 自动关闭，订阅者被清理
        try (MsgHub hub = MsgHub.builder()
                .name("Introduction")
                .participants(alice, bob, charlie)
                .announcement(announcement)
                .enableAutoBroadcast(true)  // 默认为 true
                .build()) {

            // 进入 Hub（向所有参与者广播公告）
            hub.enter().block();

            // 每个智能体自我介绍
            // 他们的回复会自动广播给其他人
            Msg aliceReply = alice.call().block();
            System.out.println("Alice: " + aliceReply.getTextContent());

            Msg bobReply = bob.call().block();
            System.out.println("Bob: " + bobReply.getTextContent());

            Msg charlieReply = charlie.call().block();
            System.out.println("Charlie: " + charlieReply.getTextContent());
        }
    }
}
