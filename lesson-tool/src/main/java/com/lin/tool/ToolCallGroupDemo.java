package com.lin.tool;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.core.tool.Toolkit;

import java.util.List;

public class ToolCallGroupDemo {

    public static void main(String[] args) {

        // 定义工具
        Toolkit toolkit = new Toolkit();

        // 创建工具组
        toolkit.createToolGroup("query", "查询工具", true);   // 默认激活
        toolkit.createToolGroup("generate", "生成工具", false);  // 默认停用

        // 注册到工具组
        toolkit.registration()
                .tool(new WeatherService())
                .group("query")
                .apply();

        toolkit.registration()
                .tool(new ToolCallGroupDemo())
                .group("generate")
                .apply();

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
                .build();

        // 调用call方法返回数据
        Msg response = agent.call(Msg.builder()
                .textContent("北京今天天气如何？")
                .build()).block();

        // 打印输出结果
        System.out.println(response==null?null:response.getTextContent());

        // 动态切换
        // TODO：注意，切换Toolkit的group，一定要从Agent中获取，而不是使用上面的toolkit变量，因为ReActAgent在build的时候是进行了拷贝toolkit(见ReActAgent的1356行代码）
        agent.getToolkit().updateToolGroups(List.of("query"), false);  // 停用
        agent.getToolkit().updateToolGroups(List.of("generate"), true);  // 激活

        // 调用call方法返回数据
        response = agent.call(Msg.builder()
                .textContent("广州今天天气如何？")
                .build()).block();

        // 打印输出结果
        System.out.println(response==null?null:response.getTextContent());
    }

    @Tool(description = "生成财报数据")
    public String generate(
            @ToolParam(name = "count", description = "整理财报的数量" ) int count) {
        return "已经完成"+ count +"份财报数据";
    }

}
