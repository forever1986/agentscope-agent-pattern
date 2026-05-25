package com.lin.supervisor.tools;

import io.agentscope.core.tool.Tool;

import java.util.Date;

public class DateTools {

    @Tool(
            name = "get_current_date",
            description = "获取当前日期。如果涉及到日期，请使用该工具获取当前日期")
    public String getCurrentDate() {
        System.out.println("==调用getCurrentDate工具==");
        // 模拟发送邮件
        return new Date().toString();
    }
}
