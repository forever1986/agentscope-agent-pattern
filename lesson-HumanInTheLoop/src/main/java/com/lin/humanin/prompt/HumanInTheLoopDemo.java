package com.lin.humanin.prompt;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.file.WriteFileTool;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class HumanInTheLoopDemo {

    public static void main(String[] args) throws IOException {

        //使用WriteFileTool工具
        URL resource = ClassLoader.getSystemResource("");
        File file = new File(resource.getPath());
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new WriteFileTool(file.getPath()));

        // 创建智能体并内联配置模型
        ReActAgent agent = ReActAgent.builder()
                .name("Assistant")
                // 通过提示词的方式，让用户确认
                .sysPrompt("你是一个有帮助的 AI 助手。必须遵守的规则，你使用到敏感工具：insert_text_file, write_text_file。必须咨询用户，得到用户允许才能执行工具")
                .model(DashScopeChatModel.builder()
                        .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                        .modelName(System.getenv("QWEN_MODEL"))
                        .build())
                .toolkit(toolkit)
                .build();

        // 使用官方的ExampleUtils来在控制台方式输入（也就是claude code方式）
        ExampleUtils.startChat(agent);
    }
}
