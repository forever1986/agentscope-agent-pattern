package com.lin.tool;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.*;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.core.tool.ToolSuspendException;
import io.agentscope.core.tool.Toolkit;

import java.util.List;

public class ToolCallSuspendDemo {

    public static void main(String[] args) {

        // 定义工具
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new ToolCallSuspendDemo());

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
                .textContent("检索海贼王的信息？")
                .build()).block();

        // 检查是否被挂起
        if (response.getGenerateReason() == GenerateReason.TOOL_SUSPENDED) {
            // 获取待执行的工具调用
            List<ToolUseBlock> pendingTools = response.getContentBlocks(ToolUseBlock.class);

            // 外部执行后，提供结果
            Msg toolResult = Msg.builder()
                    .role(MsgRole.TOOL)
                    .content(ToolResultBlock.of(pendingTools.getFirst().getId(), pendingTools.getFirst().getName(),
                            TextBlock.builder().text("海贼王是一部中国漫画").build()))
                    .build();
            // 恢复执行
            response = agent.call(toolResult).block();

            // 打印输出结果
            System.out.println(response==null?null:response.getTextContent());
        }

    }

    @Tool(name = "external_api", description = "调用检索外部 API")
    public ToolResultBlock callExternalApi(
            @ToolParam(name = "url") String url) {
        // 抛出异常，暂停执行
        throw new ToolSuspendException("等待外部 API 响应: " + url);
    }
}
