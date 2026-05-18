package com.lin.memory.shortmemory;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.autocontext.AutoContextConfig;
import io.agentscope.core.memory.autocontext.AutoContextHook;
import io.agentscope.core.memory.autocontext.AutoContextMemory;
import io.agentscope.core.memory.autocontext.ContextOffloadTool;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.tool.Toolkit;

public class AutoContextMemoryStrategy2Demo {

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
                .lastKeep(1) // 保持未压缩的最近消息数量（仅在策略 1 和策略 2 中生效）
                .largePayloadThreshold(100) // 设置消息的最大阈值，超过该阈值会触发策略2、策略3和策略5都会使用
                .offloadSinglePreview(100) // 最大消息保留阈值，如果最大消息超过largePayloadThreshold，就只会保留offloadSinglePreview个token
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
                .hook(new AutoContextHook())  // 自动注册 ContextOffloadTool 并附加 PlanNotebook
                .toolkit(toolkit)
                .build();

        System.out.println("============第一次的结果==================");
        agent.call(Msg.builder()
                .textContent("推荐5部电影，并说说推荐理由！在200个字以内")
                .build()).block();
        memory.getMessages().forEach((msg) -> {
            System.out.println(msg.getRole().name() + ":" + msg.getTextContent());
        });

        System.out.println("============第二次的结果==================");
        agent.call(Msg.builder()
                .textContent("除了这五部之外，再推荐一部！")
                .build()).block();
        memory.getMessages().forEach((msg) -> {
            System.out.println(msg.getRole().name() + ":" + msg.getTextContent());
        });

        System.out.println("============第三次的结果==================");
        agent.call(Msg.builder()
                .textContent("再推荐一部！")
                .build()).block();
        memory.getMessages().forEach((msg) -> {
            System.out.println(msg.getRole().name() + ":" + msg.getTextContent());
        });

    }
}
