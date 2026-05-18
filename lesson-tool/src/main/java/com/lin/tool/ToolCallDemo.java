package com.lin.tool;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.tool.Toolkit;

public class ToolCallDemo {

    public static void main(String[] args) {

        // 定义工具
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new WeatherService());

        // 创建智能体并内联配置模型
        ReActAgent agent = ReActAgent.builder()
                .name("Assistant")
                .sysPrompt("你是一个有帮助的 AI 助手。")
                .model(DashScopeChatModel.builder()
                        .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                        .modelName(System.getenv("QWEN_MODEL"))
                        .build())
                .toolkit(toolkit)
                .build();

        // 调用call方法返回数据
        Msg response = agent.call(Msg.builder()
                .textContent("北京今天天气如何？")
                .build()).block();

        // 打印输出结果
        System.out.println(response==null?null:response.getTextContent());
    }
}
