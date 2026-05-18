package com.lin.thinking;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.hook.Hook;
import io.agentscope.core.hook.HookEvent;
import io.agentscope.core.hook.ReasoningChunkEvent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.ThinkingBlock;
import io.agentscope.core.model.DashScopeChatModel;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

public class ThinkingDemo {

    public static void main(String[] args) {
        // 创建智能体并内联配置模型
        ReActAgent agent = ReActAgent.builder()
                .name("Assistant")
                .sysPrompt("你是一个有帮助的 AI 助手。")
                .model(DashScopeChatModel.builder()
                        .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                        .modelName(System.getenv("QWEN_MODEL"))
                        .enableThinking(true) // 启用大模型的thinking能力
                        .build())
                .hook(new ThinkingHook()) // 在推理内容到达时逐段打印大模型的思考过程
                .build();
        // 调用call方法返回数据
        Msg response = agent.call(Msg.builder()
                .textContent("""
                        已知甲、乙、丙三个工程队合作完成一项工程，甲队单独做需要12天完成，乙队单独做需要15天完成，丙队单独做需要20天完成。
                        现在甲队先做3天，剩下的工程由乙、丙两队合作，一段时间后，甲队重新加入，三个队一起合作2天完成了全部工程。请问乙、丙两队单独合作了多少天？
                        """)
                .build()).block();
        // 打印输出结果
        System.out.println();
        System.out.println("==================输出结果===================");
        System.out.println(response==null?null:response.getTextContent());
    }

    static class ThinkingHook implements Hook{

        @Override
        public <T extends HookEvent> Mono<T> onEvent(T event) {
            if (event instanceof ReasoningChunkEvent reasoningChunk) {
                // 在推理内容到达时逐段打印大模型的思考过程
                Msg chunk = reasoningChunk.getIncrementalChunk();
                String text = getTextContent(chunk);
                if (!text.isEmpty()) {
                    System.out.print(text);
                }
            }
            return Mono.just(event);
        }

        private static String getTextContent(Msg msg) {
            String thinking =
                    msg.getContent().stream()
                            .filter(block -> block instanceof ThinkingBlock)
                            .map(block -> ((ThinkingBlock) block).getThinking())
                            .collect(Collectors.joining("\n"));

            if (!thinking.isEmpty()) {
                return thinking;
            } else {
                return "";
            }
        }
    }
}
