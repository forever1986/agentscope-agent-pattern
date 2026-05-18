package com.lin.subagent.core.loader;

import java.util.List;

/**
 * 从带有 YAML 前置信息的 Markdown 文件中解析出的子Agent Specification
 */
public record AgentSpec(
        /**
         * 子Agent的唯一标识符（在任务工具中用作“subagent_type”）
         */
        String name,

        /**
         * 对如何使用此Agent的自然语言描述，包括使用的时机和方式。
         */
        String description,

        /**
         * 系统Prompt（Markdown中的body内容，用作 ReactAgent 系统提示）。
         */
        String systemPrompt,

        /**
         * 此Agent可使用的工具名称的可选列表。为空表示可使用所有工具
         */
        List<String> toolNames,

        /**
         * 可选的模型
         */
        String model) {


    public static AgentSpec of(String name, String description, String systemPrompt) {
        return new AgentSpec(name, description, systemPrompt, List.of(), null);
    }
}
