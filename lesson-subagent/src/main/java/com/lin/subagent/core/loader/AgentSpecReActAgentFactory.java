package com.lin.subagent.core.loader;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.model.Model;
import io.agentscope.core.tool.Toolkit;

import java.util.List;
import java.util.Map;

/**
 * 将AgentSpec从md文件读取到的Agent配置）构建AgentScope的ReActAgent实例
 */
public final class AgentSpecReActAgentFactory {

    private final Model model;
    private final Map<String, Object> defaultToolsByName;

    public AgentSpecReActAgentFactory(Model model, Map<String, Object> defaultToolsByName) {
        if(model==null)
            throw new RuntimeException("model must not be null");
        this.model = model;
        this.defaultToolsByName =
                defaultToolsByName != null ? Map.copyOf(defaultToolsByName) : Map.of();
    }

    /**
     * 根据给定的Spec创建一个 ReActAgent
     */
    public ReActAgent create(AgentSpec spec) {
        if(spec==null)
            throw new RuntimeException("spec must not be null");

        Toolkit toolkit = new Toolkit();
        List<String> toolNames = spec.toolNames();
        if (toolNames==null || toolNames.isEmpty()) {
            defaultToolsByName.values().forEach(toolkit::registerTool);
        } else {
            for (String name : toolNames) {
                Object tool = defaultToolsByName.get(name);
                if (tool != null) {
                    toolkit.registerTool(tool);
                }
            }
        }

        return ReActAgent.builder()
                .name(spec.name())
                .description(spec.description())
                .sysPrompt(spec.systemPrompt() != null ? spec.systemPrompt() : "")
                .model(model)
                .toolkit(toolkit)
                .memory(new InMemoryMemory())
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Model model;
        private Map<String, Object> defaultToolsByName = Map.of();

        public Builder model(Model model) {
            this.model = model;
            return this;
        }

        public Builder defaultToolsByName(Map<String, Object> defaultToolsByName) {
            this.defaultToolsByName = defaultToolsByName != null ? defaultToolsByName : Map.of();
            return this;
        }

        public AgentSpecReActAgentFactory build() {
            if(model==null)
                throw new RuntimeException("model must be provided");
            return new AgentSpecReActAgentFactory(model, defaultToolsByName);
        }
    }
}
