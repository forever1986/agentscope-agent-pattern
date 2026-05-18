package com.lin.plan;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.hook.Hook;
import io.agentscope.core.hook.HookEvent;
import io.agentscope.core.hook.PostActingEvent;
import io.agentscope.core.hook.PostReasoningEvent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.plan.PlanNotebook;
import io.agentscope.core.plan.model.Plan;
import io.agentscope.core.plan.model.SubTask;
import reactor.core.publisher.Mono;

import java.util.List;

public class PlanDemo {

    public static void main(String[] args) {

        // 定义计划PlanNotebook
        PlanNotebook planNotebook = PlanNotebook.builder()
                .needUserConfirm(false) // 设置不需要询问用户就可以直接执行计划
                .build();
        Hook planVisualizationHook =
                new Hook() {
                    @Override
                    public <T extends HookEvent> Mono<T> onEvent(T event) {
                        if (event instanceof PostActingEvent postActing) {
                            // 在每次调用规划工具后打印规划状态信息
                            String toolName = postActing.getToolUse().getName();
                            printPlanState(planNotebook, "After " + toolName);
                        }else  if (event instanceof PostReasoningEvent postReasoning){
                            // 在每次推理之后，打印出大模型输出结果
                            // 这里是简单输出生成的结果，示例是生成一个网站，实际可以加入文件写入和读取工具，让大模型调用工具写入到本地
                            System.out.println("结果输出：" + postReasoning.getReasoningMessage().getTextContent());
                        }
                        return Mono.just(event);
                    }
                };

        // 创建支持计划的智能体
        ReActAgent agent = ReActAgent.builder()
                .name("PlanAgent")
                .sysPrompt("你是一款系统化的助手，能够将复杂的任务分解成具体的计划。")
                .model(DashScopeChatModel.builder()
                        .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                        .modelName(System.getenv("QWEN_MODEL"))
                        .stream(false)
                        .build())
                .hook(planVisualizationHook)
//                .enablePlan() // 也可以直接使用enablePlan方法启用
                .planNotebook(planNotebook)
                .build();



        // 给智能体分配复杂任务
        Msg task = Msg.builder()
                .role(MsgRole.USER)
                .content(List.of(TextBlock.builder()
                        .text("使用 HTML、CSS 和 JavaScript 构建一个简单的计算器网页应用程序。计划好后就直接执行该计划，无需询问。")
                        .build()))
                .build();

        // 智能体会自动创建计划并逐步执行
        Msg response = agent.call(task).block();
//        System.out.println(response.getTextContent());
    }

    private static void printPlanState(PlanNotebook notebook, String event) {
        Plan currentPlan = notebook.getCurrentPlan();
        if (currentPlan == null) {
            System.out.println("\n📋 [" + event + "] No active plan");
            return;
        }

        System.out.println("\n" + "=".repeat(70));
        System.out.println("📋 PLAN STATE [" + event + "]");
        System.out.println("=".repeat(70));
        System.out.println("Plan: " + currentPlan.getName());
        System.out.println("State: " + currentPlan.getState());
        System.out.println("\nSubtasks:");

        for (int i = 0; i < currentPlan.getSubtasks().size(); i++) {
            SubTask subtask = currentPlan.getSubtasks().get(i);
            String icon =
                    switch (subtask.getState()) {
                        case TODO -> "⏸️";
                        case IN_PROGRESS -> "▶️";
                        case DONE -> "✅";
                        case ABANDONED -> "❌";
                    };
            System.out.printf(
                    "  %s [%d] %s - %s%n", icon, i, subtask.getName(), subtask.getState());
        }
        System.out.println("=".repeat(70) + "\n");
    }
}
