package com.lin.error;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.core.tool.Toolkit;

public class ToolErrorDemo {

    public static void main(String[] args) {
        // 设置工具
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new ToolErrorDemo());
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
        String userInput = "广州的天气情况？";
        Msg response = agent.call(Msg.builder()
                .textContent(userInput)
                .build())
                .block();
        // 打印输出结果
        System.out.println(response==null?null:response.getTextContent());
    }

    @Tool(description = "获取指定城市的天气")
    public String getWeather(
            @ToolParam(name = "city", description = "城市名称") String city) {
        System.out.println("=====执行工具=======");
        if(city.equals("广州"))
            throw new RuntimeException("执行工具错误"); // 模拟抛出异常
        return city + " 的天气：晴天，25°C";
    }
}
