package com.lin.router;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.DashScopeChatModel;

public class RouterDemo {

    public static void main(String[] args) {

        // 创建模型
        DashScopeChatModel model = DashScopeChatModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName(System.getenv("QWEN_MODEL"))
                .build();

        // 创建不同阶段的智能体
        ReActAgent router = ReActAgent.builder()
                .name("Router")
                .sysPrompt("""
                        分析用户的请求并确定哪个专家处理程序应处理它。
                        如果请求与预订航班或酒店相关，输出 'booker'。
                        对于所有其他一般信息问题，输出 'info'。
                        如果请求不清楚或不适合任一类别，输出 'unclear'。
                        只输出一个词：'booker'、'info' 或 'unclear'。
                        """)
                .model(model)
                .build();

        // 定义不同的业务分支（比如：根据用户问题类型，选择不同的AI处理逻辑）
        // 分支1：处理预定问题
        Runnable<String, String> bookingHandlerBranch = input -> {
            // 模拟预订智能体请求。
            System.out.println("--- 委托给预订处理程序 ---");
            return "预订处理程序处理了请求：'" + input + "'。结果：模拟预订操作。";
        };

        // 分支2：处理信息问题
        Runnable<String, String> infoHandlerBranch = input -> {
            // 模拟信息智能体请求
            System.out.println("--- 委托给信息处理程序 ---");
            return "信息处理程序处理了请求：'" + input + "'。结果：模拟信息检索。";
        };

        // 分支3：默认分支（处理未知类型）
        Runnable<String, String> unclearHandlerBranch = input -> {
            // 处理无法委托的请求
            System.out.println("--- 处理不清楚的请求 ---");
            return "协调器无法委托请求：'" + input + "'。请澄清。";
        };


        // 构建 RunnableBranch（条件路由）
        RunnableBranch<String, String> questionRouter = RunnableBranch.<String, String>builder()
                .addBranch(input -> input.contains("booker"), bookingHandlerBranch) // 条件1：包含booker
                .addBranch(input -> input.contains("info"), infoHandlerBranch) // 条件2：包含info
                .defaultBranch(unclearHandlerBranch) // 默认分支
                .build();

        // 测试不同输入的分支路由效果
        // 创建输入消息
        Msg input = Msg.builder()
                .name("user")
                .role(MsgRole.USER)
                .content(TextBlock.builder().text("给我预订去伦敦的航班。").build())
                .build();
        System.out.println(questionRouter.invoke(router.call(input).block().getTextContent()));

        input = Msg.builder()
                .name("user")
                .role(MsgRole.USER)
                .content(TextBlock.builder().text("意大利的首都是什么？").build())
                .build();
        System.out.println(questionRouter.invoke(router.call(input).block().getTextContent()));

        input = Msg.builder()
                .name("user")
                .role(MsgRole.USER)
                .content(TextBlock.builder().text("帮我拿一杯水。").build())
                .build();
        System.out.println(questionRouter.invoke(router.call(input).block().getTextContent()));
    }
}
