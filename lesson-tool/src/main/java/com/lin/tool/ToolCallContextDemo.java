package com.lin.tool;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolExecutionContext;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.core.tool.Toolkit;

public class ToolCallContextDemo {

    public static void main(String[] args) {

        // 定义工具
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new ToolCallContextDemo());
        toolkit.registerMetaTool();

        // 创建上下文
        ToolExecutionContext context = ToolExecutionContext.builder()
                .register(new UserContext("user-123"))
                .build();

        // 创建智能体并内联配置模型
        ReActAgent agent = ReActAgent.builder()
                .name("Assistant")
                .sysPrompt("你是一个有帮助的 AI 助手。")
                .model(DashScopeChatModel.builder()
                        .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                        .modelName(System.getenv("QWEN_MODEL"))
                        .build())
                // 加入工具
                .toolkit(toolkit)
                // 加入上下文
                .toolExecutionContext(context)
                .build();

        // 调用call方法返回数据
        Msg response = agent.call(Msg.builder()
                .textContent("北京今天天气如何？")
                .build()).block();

        // 打印输出结果
        System.out.println(response==null?null:response.getTextContent());
    }

    @Tool(description = "异步获取指定城市的天气")
    public String getWeather(
            @ToolParam(name = "city", description = "城市名称") String city,
            UserContext ctx) {  // 自动注入，无需 @ToolParam
        System.out.println("=====执行getWeather工具=======");
        System.out.println("执行者：" + ctx.getUserId());
        return city + " 的天气：晴天，25°C";
    }

}

// 定义工具上下文传递类
class UserContext {
    private final String userId;
    public UserContext(String userId) { this.userId = userId; }
    public String getUserId() { return userId; }
}
