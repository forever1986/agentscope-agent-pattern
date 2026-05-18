package com.lin.agentastool;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.hook.Hook;
import io.agentscope.core.hook.HookEvent;
import io.agentscope.core.hook.PreActingEvent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.tool.Toolkit;
import reactor.core.publisher.Mono;

public class AgentAsToolDemo {

    public static void main(String[] args) {

        // 创建模型
        DashScopeChatModel model = DashScopeChatModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName(System.getenv("QWEN_MODEL"))
                .build();
        // 加入hook用于验证是否通过工具调用子智能体
        Hook hook = new Hook() {
            @Override
            public <T extends HookEvent> Mono<T> onEvent(T event) {
                if (event instanceof PreActingEvent preActing) {
                    System.out.println(
                            "\n[HOOK PreActingEvent] - Tool: "
                                    + preActing.getToolUse().getName()
                                    + ", Input: "
                                    + preActing.getToolUse().getInput());

                }
                // 保持Event原样返回
                return Mono.just(event);
            }
        };
        // 创建子智能体的 Provider（工厂）
        // 注意：必须使用 lambda 表达式，确保每次调用创建新实例
        Toolkit toolkit = new Toolkit();
        toolkit.registration()
                .subAgent(() -> ReActAgent.builder()
                        .name("Expert")
                        .sysPrompt("你是一个数学领域专家，负责回答专业问题。")
                        .model(model)
                        .build())
                .apply();

        // 创建主智能体，配置工具
        ReActAgent mainAgent = ReActAgent.builder()
                .name("Coordinator")
                .sysPrompt("你是一个协调员。当遇到数学领域的专业问题时，调用 call_expert 工具咨询专家。")
                .model(model)
                .toolkit(toolkit)
                .hook(hook)
                .build();

        // 主智能体会在需要时自动调用专家智能体
        Msg response = mainAgent.call(Msg.builder().textContent("请使用100个字给我介绍勾股定理").build())
                .block();

        // 打印输出结果
        System.out.println(response==null?null:response.getTextContent());
    }
}
