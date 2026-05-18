package com.lin.pipeline;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.pipeline.Pipelines;

import java.util.List;

public class FanoutPipelineDemo {

    public static void main(String[] args) {

        // 创建模型
        DashScopeChatModel model = DashScopeChatModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName(System.getenv("QWEN_MODEL"))
                .build();

        // 创建具有不同视角的智能体
        ReActAgent optimist = ReActAgent.builder()
                .name("Optimist")
                .sysPrompt("你是一个乐观主义者。分析主题的积极方面。")
                .model(model)
                .build();

        ReActAgent pessimist = ReActAgent.builder()
                .name("Pessimist")
                .sysPrompt("你是一个悲观主义者。分析潜在的风险和挑战。")
                .model(model)
                .build();

        ReActAgent realist = ReActAgent.builder()
                .name("Realist")
                .sysPrompt("你是一个现实主义者。提供平衡的分析。")
                .model(model)
                .build();

        // 创建输入消息
        Msg input = Msg.builder()
                .name("user")
                .role(MsgRole.USER)
                .content(TextBlock.builder().text("分析一下人工智能在医疗领域的应用，用100个字总结").build())
                .build();

        // 执行扇出管道（默认并发）
        List<Msg> results = Pipelines.fanout(
                List.of(optimist, pessimist, realist),
                input
        ).block();

//        // 使用Builder方式创建
//        FanoutPipeline concurrentPipeline = FanoutPipeline.builder()
//                .addAgent(optimist)
//                .addAgent(pessimist)
//                .addAgent(realist)
//                .sequential()  // 默认模式 也可以改为sequential顺序模式
//                .build();
//        List<Msg> results = concurrentPipeline.execute(input).block();

        // 处理所有结果
        for (Msg result : results) {
            System.out.println(result.getName() + ": " + result.getTextContent());
        }
    }
}
