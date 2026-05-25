package com.lin.supervisor.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;

import java.util.List;

/**
 * 为“主管个人助理”示例提供的日历 API 工具。
 */
public class CalendarStubTools {

    @Tool(
            name = "create_calendar_event",
            description = "创建日历事件。需要精确的ISO日期时间格式。")
    public String createCalendarEvent(
            @ToolParam(name = "title", description = "事件标题") String title,
            @ToolParam(
                    name = "startTime",
                    description = "开始时间，采用 ISO 格式，例如：2024-01-15T14:00:0")
            String startTime,
            @ToolParam(
                    name = "endTime",
                    description = "结束时间，采用 ISO 格式，例如：2024-01-15T15:00:0")
            String endTime,
            @ToolParam(name = "attendees", description = "参会者电子邮件地址列表")
            List<String> attendees,
            @ToolParam(name = "location", description = "事件场所", required = false)
            String location) {
        System.out.println("==调用createCalendarEvent工具==");
        // 模拟创建日历事件
        return String.format(
                "时间创建: %s 从 %s 到 %s 。 参会人员： %d ",
                title, startTime, endTime, attendees != null ? attendees.size() : 0);
    }

    @Tool(
            name = "get_available_time_slots",
            description = "查询给定参会者的特定日期内可用时间。")
    public String getAvailableTimeSlots(
            @ToolParam(name = "attendees", description = "参会者电子邮件地址列表")
            List<String> attendees,
            @ToolParam(name = "date", description = "日期，采用 ISO 格式，例如：2024-01-15")
            String date,
            @ToolParam(name = "durationMinutes", description = "持续时间（以分钟计）")
            int durationMinutes) {
        System.out.println("==调用getAvailableTimeSlots工具==");
        // 模拟返回时间
        return "[\"09:00\", \"14:00\", \"16:00\"]";
    }
}
