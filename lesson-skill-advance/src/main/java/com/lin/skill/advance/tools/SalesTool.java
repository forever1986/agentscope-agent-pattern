package com.lin.skill.advance.tools;

import io.agentscope.core.message.TextBlock;
import io.agentscope.core.message.ToolResultBlock;
import io.agentscope.core.tool.AgentTool;
import io.agentscope.core.tool.ToolCallParam;
import reactor.core.publisher.Mono;

import java.util.Map;

// 仅做测试使用
public class SalesTool implements AgentTool {

    @Override
    public String getName() {
        return "getSalesData";
    }

    @Override
    public String getDescription() {
        return "获取销售数据";
    }

    @Override
    public Map<String, Object> getParameters() {
        return Map.of();
    }

    @Override
    public Mono<ToolResultBlock> callAsync(ToolCallParam param) {
        System.out.println("=====执行“获取销售数据”工具=======");
        return Mono.just(ToolResultBlock.builder()
                        .name(this.getName())
                        .output(TextBlock.builder().text("无数据").build())
                .build());
    }
}
