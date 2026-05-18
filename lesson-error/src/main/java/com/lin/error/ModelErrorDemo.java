package com.lin.error;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;
import reactor.core.publisher.Mono;

public class ModelErrorDemo {

    public static void main(String[] args) {
        // 创建智能体并内联配置模型
        ReActAgent agent = ReActAgent.builder()
                .name("Assistant")
                .sysPrompt("你是一个有帮助的 AI 助手。")
                .model(DashScopeChatModel.builder()
                        .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                        .modelName(System.getenv("QWEN_MODEL"))
                        .build())
                .build();
        // 调用call方法返回数据
        String userInput = "广州的天气情况？";
        Msg response = agent.call(Msg.builder()
                .textContent(userInput)
                .build())
                // 处理模型执行出错
                .onErrorResume(ex -> {
                    // 1. 记录错误（生产必加）
                    System.err.println("ReActAgent 执行失败，输入: " + userInput +", 异常: {" + ex.getMessage() + "}");
                    // 2. 构建兜底响应（友好提示）
                    Msg fallbackMsg = Msg.builder()
                            .textContent("抱歉，我暂时无法回答你的问题，请稍后再试。")
                            .build();
                    // 3. 返回新的 Mono（终止原流，启动备用流）
                    return Mono.just(fallbackMsg);
                })
                .block();
        // 打印输出结果
        System.out.println(response==null?null:response.getTextContent());
    }

}
