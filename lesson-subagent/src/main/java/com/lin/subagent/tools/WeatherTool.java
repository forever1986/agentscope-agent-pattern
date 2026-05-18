package com.lin.subagent.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;

/**
 * 模拟获取景点信息工具
 */
public class WeatherTool {

    @Tool(description = "获取指定城市的天气")
    public String getWeather(
            @ToolParam(name = "city", description = "城市名称") String city) {
        System.out.println("=====执行getWeather工具=======");
        return city + " 的天气：晴天，25°C";
    }

}
