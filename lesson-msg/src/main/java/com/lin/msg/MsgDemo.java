package com.lin.msg;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.*;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.core.tool.Toolkit;

public class MsgDemo {

    public static void main(String[] args) {
        // 工具：这里为了演示不同消息体引入工具，先看看如何使用即可，后续讲到工具会细讲
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new MsgDemo());
        // 创建智能体并内联配置模型
        ReActAgent agent = ReActAgent.builder()
                .name("Assistant")
                .sysPrompt("你是一个有帮助的 AI 助手。")
                .model(DashScopeChatModel.builder()
                        .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                        .modelName(System.getenv("QWEN_MODEL"))
                        .enableThinking(true) // 启动思考模式
                        .build())
                .toolkit(toolkit) // 注入工具
                .build();
        // 调用call方法返回数据
        Msg response = agent.call(Msg.builder()
                .textContent("今天广州天气如何？")
                .build())
                .block();
        agent.getMemory().getMessages().forEach( msg ->{
            msg.getContent().forEach(content ->{
                if(content instanceof TextBlock textBlock){
                    System.out.println("[Role]: +" + msg.getRole() + ";    [TextBlock]:" + textBlock.getText());
                }else if(content instanceof ThinkingBlock thinkingBlock){
                    System.out.println("[Role]: +" + msg.getRole() + ";    [ThinkingBlock]:" + thinkingBlock.getThinking());
                }else if(content instanceof ToolUseBlock toolUseBlock){
                    System.out.println("[Role]: +" + msg.getRole() + ";    [ToolUseBlock]:" + toolUseBlock.getContent());
                }else if(content instanceof ToolResultBlock toolResultBlock){
                    System.out.println("[Role]: +" + msg.getRole() + ";    [ToolResultBlock]:" + toolResultBlock.getOutput().getLast());
                }
            });
        });
    }

    // 模拟查询天气的工具
    @Tool(description = "获取指定城市的天气")
    public String getWeather(
            @ToolParam(name = "city", description = "城市名称") String city) {
        return city + " 的天气：晴天，25°C";
    }
}
