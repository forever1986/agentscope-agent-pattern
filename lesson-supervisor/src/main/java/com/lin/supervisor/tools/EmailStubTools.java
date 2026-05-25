package com.lin.supervisor.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;

import java.util.List;

/**
 * 用于主管个人助理的简易电子邮件 API 工具示例。
 */
public class EmailStubTools {

    @Tool(
            name = "send_email",
            description = "通过电子邮件 API 发送电子邮件。需要提供格式正确的收件人地址。")
    public String sendEmail(
            @ToolParam(name = "to", description = "收件人电子邮件地址列表")
            List<String> to,
            @ToolParam(name = "subject", description = "电子邮件主题") String subject,
            @ToolParam(name = "body", description = "电子邮件正文\n") String body,
            @ToolParam(name = "cc", description = "抄送人邮件地址", required = false)
            List<String> cc) {
        System.out.println("==调用sendEmail工具==");
        // 模拟发送邮件
        return String.format(
                "邮件已经发送给 %s - 主题: %s",
                String.join(", ", to != null ? to : List.of()), subject);
    }
}
