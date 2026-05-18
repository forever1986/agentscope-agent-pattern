package com.lin.mcp;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.mcp.McpClientBuilder;
import io.agentscope.core.tool.mcp.McpClientWrapper;

public class MCPStdIODemo {

    public static void main(String[] args) {
        // StdIO 传输 - 连接到本地 MCP 服务器
        McpClientWrapper mcpClient = McpClientBuilder.create("playwright-mcp")
                .stdioTransport("npx.cmd", "@playwright/mcp@latest")
                .buildAsync()
                .block();

        Toolkit toolkit = new Toolkit();
        // 注册 MCP 服务器的所有工具
        toolkit.registerMcpClient(mcpClient).block();

        ReActAgent agent = ReActAgent.builder()
                .name("Assistant")
                .model(DashScopeChatModel.builder()
                        .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                        .modelName(System.getenv("QWEN_MODEL"))
                        .build())
                .toolkit(toolkit)  // MCP 工具现已可用
                .memory(new InMemoryMemory())
                .build();

        // 调用call方法返回数据
        Msg response = agent.call(Msg.builder()
                .textContent("开浏览器访问baidu并搜索关键字周星驰")
                .build()).block();
        // 打印输出结果
        System.out.println(response==null?null:response.getTextContent());
    }
}
