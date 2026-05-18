package com.lin.memory.shortmemory;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.autocontext.AutoContextConfig;
import io.agentscope.core.memory.autocontext.AutoContextHook;
import io.agentscope.core.memory.autocontext.AutoContextMemory;
import io.agentscope.core.memory.autocontext.ContextOffloadTool;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.Toolkit;

public class AutoContextMemoryStrategy1Demo {

    public static void main(String[] args) {

        // 模型
        DashScopeChatModel model = DashScopeChatModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName(System.getenv("QWEN_MODEL"))
                .stream(false)
                .build();

        // 配置
        AutoContextConfig config = AutoContextConfig.builder()
                .msgThreshold(7) // 触发压缩的消息数量
                .lastKeep(1) // 保持未压缩的最近消息数量（仅在策略 1 和策略 2 中生效）
                .minConsecutiveToolMessages(3) // 压缩所需的最小连续工具消息数量
                .minCompressionTokenThreshold(200) // 设置压缩操作开始所需的最小令牌数量。如果原始令牌数量少于此值，则将跳过压缩操作。默认值为 5000 个令牌。在策略1和策略4都会使用
                .build();

        // 创建内存
        AutoContextMemory memory = new AutoContextMemory(config, model);

        // 注册上下文重载工具
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new ContextOffloadTool(memory)); // 注册AutoContextHook就会自动注册ContextOffloadTool工具，这里只是显式告诉大家注册了一个工具
        toolkit.registerTool(new AutoContextMemoryStrategy1Demo());

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
                .textContent("推荐五部好电影！并从中推荐最好的一部电影和最差的一部电影！")
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

    }

    @Tool(description = "获取最好的5部电影")
    public String getTheTopFiveBestMovie() {
        return "肖申克的救赎，盗梦空间，泰坦尼克号，阿甘正传，星际穿越";
    }

    @Tool(description = "获取最好的1部电影")
    public String getTheBestMovie() {
        return "肖申克的救赎";
    }

    @Tool(description = "获取最差的1部电影")
    public String getTheWorstMovie() {
        return "泰坦尼克号";
    }
}
