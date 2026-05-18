package com.lin.pipeline;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.pipeline.Pipelines;

import java.util.List;

public class SequentialPipelineDemo {

    public static void main(String[] args) {
        // 创建模型
        DashScopeChatModel model = DashScopeChatModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName(System.getenv("QWEN_MODEL"))
                .build();

        // 创建不同阶段的智能体
        ReActAgent researcher = ReActAgent.builder()
                .name("Researcher")
                .sysPrompt("你是一名研究员。分析主题并提供关键发现。")
                .model(model)
                .build();

        ReActAgent writer = ReActAgent.builder()
                .name("Writer")
                .sysPrompt("你是一名作家。根据研究发现撰写简洁的摘要。")
                .model(model)
                .build();

        ReActAgent editor = ReActAgent.builder()
                .name("Editor")
                .sysPrompt("你是一名编辑。润色并定稿摘要。")
                .model(model)
                .build();
        // 创建输入消息
        Msg input = Msg.builder()
                .name("user")
                .role(MsgRole.USER)
                .content(TextBlock.builder().text("人工智能在医疗领域的应用").build())
                .build();

        // 执行顺序管道
        // Researcher → Writer → Editor
        Msg result = Pipelines.sequential(List.of(researcher, writer, editor), input).block();

        // 不使用Pipelines工厂模式创建，直接通过SequentialPipeline实现
//        // 创建可复用的管道
//        SequentialPipeline pipeline = SequentialPipeline.builder()
//                .addAgent(researcher)
//                .addAgent(writer)
//                .addAgent(editor)
//                .build();
//        // 执行管道
//        Msg result1 = pipeline.execute("人工智能在医疗领域的应用").block();

        System.out.println("最终结果: " + result.getTextContent());
    }
}
