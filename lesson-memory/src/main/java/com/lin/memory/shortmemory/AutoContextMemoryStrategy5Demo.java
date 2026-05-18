package com.lin.memory.shortmemory;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.hook.Hook;
import io.agentscope.core.hook.HookEvent;
import io.agentscope.core.hook.PreReasoningEvent;
import io.agentscope.core.memory.autocontext.AutoContextConfig;
import io.agentscope.core.memory.autocontext.AutoContextHook;
import io.agentscope.core.memory.autocontext.AutoContextMemory;
import io.agentscope.core.memory.autocontext.ContextOffloadTool;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.tool.Toolkit;
import reactor.core.publisher.Mono;

import java.util.List;

public class AutoContextMemoryStrategy5Demo {

    public static void main(String[] args) {

        // 模型
        DashScopeChatModel model = DashScopeChatModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName(System.getenv("QWEN_MODEL"))
                .stream(false)
                .build();

        // 配置
        AutoContextConfig config = AutoContextConfig.builder()
                .msgThreshold(2) // 触发压缩的消息数量
                .largePayloadThreshold(100) // 设置消息的最大阈值，超过该阈值会触发策略2、策略3和策略5都会使用
                .build();

        // 创建内存
        AutoContextMemory memory = new AutoContextMemory(config, model);

        // 注册上下文重载工具
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new ContextOffloadTool(memory));// 注册AutoContextHook就会自动注册ContextOffloadTool工具，这里只是显式告诉大家注册了一个工具

        // 创建智能体并内联配置模型
        ReActAgent agent = ReActAgent.builder()
                .name("Assistant")
                .sysPrompt("你是一个有帮助的 AI 助手。")
                .model(model)
                .memory(memory) // 增加AutoContextMemory记忆
                .toolkit(toolkit)
                .hook(new StrategyHook(memory)) // 模拟在访问Agent之前加入一条超过限制字数的ASSISTANT信息
                .hook(new AutoContextHook())  // 自动注册 ContextOffloadTool 并附加 PlanNotebook
                .build();

        System.out.println("============第一次的结果==================");
        agent.call(Msg.builder()
                .textContent("我需要一份A公司的财务总结。100个字以内")
                .build()).block();
        memory.getMessages().forEach((msg) -> {
            System.out.println(msg.getRole().name() + ":" + msg.getTextContent());
        });

    }

    static class StrategyHook implements Hook{

        private AutoContextMemory autoContextMemory;

        public StrategyHook(AutoContextMemory autoContextMemory) {
            this.autoContextMemory = autoContextMemory;
        }

        @Override
        public <T extends HookEvent> Mono<T> onEvent(T event) {
            if(event instanceof PreReasoningEvent preReasoningEvent){
                // 模拟一段ASSISTANT信息，让策略5触发
                Msg msg = Msg.builder()
                        .name("system")
                        .role(MsgRole.ASSISTANT)
                        .content(
                                TextBlock.builder()
                                        .text("""
                                    以下是找到的A公司2025的财务明细
                                    1. 2025年1月份，收入100万，支出80万。
                                    2. 2025年2月份，收入70万，支出40万。
                                    3. 2025年3月份，收入150万，支出100万。
                                    4. 2025年4月份，收入180万，支出130万。
                                    5. 2025年5月份，收入160万，支出100万。
                                    6. 2025年6月份，收入80万，支出80万。
                                    7. 2025年7月份，收入75万，支出60万。
                                    8. 2025年8月份，收入60万，支出70万。
                                    9. 2025年9月份，收入50万，支出75万。
                                    10. 2025年10月份，收入170万，支出100万。
                                    11. 2025年11月份，收入140万，支出120万。
                                    12. 2025年12月份，收入150万，支出110万。
                                    """)
                                        .build()).build();
                List<Msg> list = preReasoningEvent.getInputMessages();
                list.add(msg);
                autoContextMemory.addMessage(msg);
                preReasoningEvent.setInputMessages(list);

            }
            return Mono.just(event);
        }

        @Override
        public int priority() {
            // 优先级为0，跟AutoContextHook一样，并且在AutoContextHook之前加入
            return 0;
        }
    }

}

