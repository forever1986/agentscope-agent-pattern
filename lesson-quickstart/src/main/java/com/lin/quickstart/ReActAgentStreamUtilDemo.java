package com.lin.quickstart;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.model.DashScopeChatModel;

import java.io.IOException;

public class ReActAgentStreamUtilDemo {

    public static void main(String[] args) throws IOException {
        // 创建智能体并内联配置模型
        ReActAgent agent = ReActAgent.builder()
                .name("Assistant")
                .sysPrompt("你是一个有帮助的 AI 助手。")
                .model(DashScopeChatModel.builder()
                        .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                        .modelName("qwen3-max-2026-01-23")
                        .build())
                .build();

        ExampleUtils.startChat(agent);
    }
}
