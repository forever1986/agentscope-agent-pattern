package com.lin.supervisor;

import com.lin.supervisor.tools.CalendarStubTools;
import com.lin.supervisor.tools.DateTools;
import com.lin.supervisor.tools.EmailStubTools;
import com.lin.supervisor.tools.OrgTools;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.tool.Toolkit;

public class SupervisorDemo {

    private static final String CALENDAR_AGENT_PROMPT =
            """
            您是日程安排助手。
            将自然语言中的日程安排请求（例如“下周二下午 2 点”）解析为正确的 ISO 日期时间格式。
            在需要时使用 get_available_time_slots 来检查可用时间。
            使用 create_calendar_event 来安排活动。
            在最终回复中务必确认已安排的日程内容。
            """;

    private static final String EMAIL_AGENT_PROMPT =
            """
            你是一名电子邮件助手。
            根据自然语言的请求来撰写专业邮件。
            提取收件人信息，并拟定恰当的邮件主题行和正文内容。
            使用“send_email”来发送邮件。
            在最终回复中务必确认已发送的内容。
            """;

    private static final String SUPERVISOR_PROMPT =
            """
            你是一名乐于助人的个人助手。
            你可以安排日程中的活动并发送电子邮件，如果用户没有强调发送邮件，则无需发送邮件。
            将用户请求分解为适当的工具调用，并协调结果。
            当请求包含多个操作时，依次使用多个工具。
            """;

    public static void main(String[] args) {

        // 1. 定义基础大语言模型
        DashScopeChatModel model = DashScopeChatModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName(System.getenv("QWEN_MODEL"))
                .build();

        // 2. 定义日历智能体
        Toolkit calendarToolkit = new Toolkit();
        calendarToolkit.registerTool(new CalendarStubTools());
        ReActAgent calendarReActAgent = ReActAgent.builder()
                    .name("schedule_event")
                    .description("日程安排助手")
                    .sysPrompt(CALENDAR_AGENT_PROMPT)
                    .model(model)
                    .toolkit(calendarToolkit)
                    .build();

        // 3. 定义邮件智能体
        Toolkit emailToolkit = new Toolkit();
        emailToolkit.registerTool(new EmailStubTools());
        ReActAgent emailReActAgent = ReActAgent.builder()
                .name("manage_email")
                .description("电子邮件助手")
                .sysPrompt(EMAIL_AGENT_PROMPT)
                .model(model)
                .toolkit(emailToolkit)
                .memory(new InMemoryMemory())
                .build();

        // 4. 定义Supervisor智能体
        Toolkit supervisorToolkit = new Toolkit();
        supervisorToolkit.registration().subAgent(() -> calendarReActAgent).apply();
        supervisorToolkit.registration().subAgent(() -> emailReActAgent).apply();
        supervisorToolkit.registerTool(new DateTools());
        supervisorToolkit.registerTool(new OrgTools());
        ReActAgent supervisorAgent = ReActAgent.builder()
                .name("personal_assistant")
                .sysPrompt(SUPERVISOR_PROMPT)
                .model(model)
                .toolkit(supervisorToolkit)
                .memory(new InMemoryMemory())
                .build();

        // 示例1: 让 "Supervisor智能体" 调用 "日历智能体"
        String query1 = "安排开发部在明天上午9点召开站立会议。";
        System.out.println("用户: " + query1);
        System.out.println("---");
        Msg response = supervisorAgent.call(Msg.builder().role(MsgRole.USER).textContent(query1).build()).block();
        System.out.println("助手: " + (response != null && response.getTextContent()!=null && !response.getTextContent().isEmpty()
                ? response.getTextContent()
                : "(No response)"));
        System.out.println("=================================");
        // 示例2: 让 "Supervisor智能体" 调用 "日历智能体" + "邮件智能体"
        String query2 ="下周二下午2点安排一次与设计部的会议，持续1小时，并给他们发送一封邮件提醒，告知他们要对新的模型进行审查。";
        System.out.println("用户: " + query2);
        System.out.println("---");
        response = supervisorAgent.call(Msg.builder().role(MsgRole.USER).textContent(query2).build()).block();
        System.out.println("助手: " + (response != null && response.getTextContent()!=null && !response.getTextContent().isEmpty()
                ? response.getTextContent()
                : "(No response)"));
    }
}
