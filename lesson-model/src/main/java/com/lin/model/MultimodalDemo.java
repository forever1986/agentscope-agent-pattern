package com.lin.model;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.formatter.dashscope.DashScopeChatFormatter;
import io.agentscope.core.message.*;
import io.agentscope.core.model.DashScopeChatModel;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;

public class MultimodalDemo {

    public static void main(String[] args) throws IOException {

        ReActAgent agent = ReActAgent.builder()
                .name("VisionAssistant")
                .sysPrompt("你是一个具有视觉能力的 AI 助手。")
                .model(DashScopeChatModel.builder()
                        .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                        .modelName("qwen3-vl-flash-2026-01-22")
                        .stream(true)
                        .formatter(new DashScopeChatFormatter())  // 必需
                        .build())
                .build();

        // 图像：Base64 方式（推荐）
        URL resource = ClassLoader.getSystemResource("color.png");
        File file = new File(resource.getPath());
        String base64Image = Base64.getEncoder().encodeToString(
                Files.readAllBytes(file.toPath())
        );
        ImageBlock imageBlock = ImageBlock.builder()
                .source(Base64Source.builder()
                        .data(base64Image)
                        .mediaType("image/png")
                        .build())
                .build();
        // 构建消息
        Msg singleImageMsg = Msg.builder()
                .role(MsgRole.USER)
                .content(List.of(
                        TextBlock.builder().text("这张图片是什么颜色？").build(),
                        imageBlock
                ))
                .build();

        // 发送请求
        Msg response = agent.call(singleImageMsg).block();
        System.out.println(response.getTextContent());

    }
}
