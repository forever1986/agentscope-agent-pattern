package com.lin.memory.shortmemory;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.memory.Memory;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;

public class InMemoryMemoryDemo {

    public static void main(String[] args) {
        // 创建短期记忆存储
        Memory memory = new InMemoryMemory();

        // 创建智能体并内联配置模型
        ReActAgent agent = ReActAgent.builder()
                .name("Assistant")
                .sysPrompt("你是一个有帮助的 AI 助手。")
                .model(DashScopeChatModel.builder()
                        .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                        .modelName(System.getenv("QWEN_MODEL"))
                        .build())
                .memory(memory) // 增加记忆内容（默认也会加入）
                .build();

        // 调用call方法返回数据
        agent.call(Msg.builder()
                .textContent("你好！")
                .build()).block();
        agent.call(Msg.builder()
                .textContent("说一个笑话！")
                .build()).block();

        // 打印输出结果
        memory.getMessages().forEach((msg) -> {
            System.out.println(msg.getRole().name() + ":" + msg.getTextContent());
        });
    }

}
