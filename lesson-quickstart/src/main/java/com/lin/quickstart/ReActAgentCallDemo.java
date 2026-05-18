package com.lin.quickstart;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;

public class ReActAgentCallDemo {

    public static void main(String[] args) {
        // 创建智能体并内联配置模型
        ReActAgent agent = ReActAgent.builder()
                .name("Assistant")
                .sysPrompt("你是一个有帮助的 AI 助手。")
                .model(DashScopeChatModel.builder()
                        .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                        .modelName(System.getenv("QWEN_MODEL"))
                        .build())
                .build();
        // 调用call方法返回数据
        Msg response = agent.call(Msg.builder()
                .textContent("你好！")
                .build())
                .block();
        // 打印输出结果
        System.out.println(response==null?null:response.getTextContent());
    }
}
