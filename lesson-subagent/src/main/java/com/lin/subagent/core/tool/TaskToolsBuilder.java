package com.lin.subagent.core.tool;

import com.lin.subagent.core.loader.AgentSpec;
import com.lin.subagent.core.loader.AgentSpecLoader;
import com.lin.subagent.core.loader.AgentSpecReActAgentFactory;
import com.lin.subagent.core.task.TaskRepository;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.CallableAgent;
import io.agentscope.core.model.Model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 构建 AgentScope TaskTool 和 TaskOutputTool两个工具，并从文件中加载子Agent
 */
public final class TaskToolsBuilder {

    private TaskRepository taskRepository = new TaskRepository();
    private final Map<String, CallableAgent> subAgents = new HashMap<>();
    private final List<File> agentResources = new ArrayList<>();
    private Model model;
    private Map<String, Object> defaultToolsByName = Map.of();

    private TaskToolsBuilder() {}

    public static TaskToolsBuilder builder() {
        return new TaskToolsBuilder();
    }

    /**
     * 设置TaskRepository（这是进行后台执行所必需的）
     */
    public TaskToolsBuilder taskRepository(TaskRepository taskRepository) {
        if(taskRepository==null)
            throw new RuntimeException("taskRepository must not be null");
        this.taskRepository = taskRepository;
        return this;
    }

    /**
     * 设置用于根据Agent Spec资源创建 ReActAgent 的模型
     */
    public TaskToolsBuilder model(Model model) {
        this.model = model;
        return this;
    }

    /**
     * 根据 Spec 设定由子Agent构建而成的所使用的默认工具名称。键值必须与之匹配。
     */
    public TaskToolsBuilder defaultToolsByName(Map<String, Object> defaultToolsByName) {
        this.defaultToolsByName =
                defaultToolsByName != null ? Map.copyOf(defaultToolsByName) : Map.of();
        return this;
    }

    /**
     * 通过编程方式加入加载Agent 规范
     */
    public TaskToolsBuilder subAgent(String type, ReActAgent agent) {
        if(type==null|| type.isEmpty())
            throw new RuntimeException("type must not be empty");
        if(agent==null)
            throw new RuntimeException("agent must not be null");
        this.subAgents.put(type, agent);
        return this;
    }

    /**
     * 从一个 markdown文件加载Agent 规范
     */
    public TaskToolsBuilder addAgentResource(File file) {
        if (file != null) {
            this.agentResources.add(file);
        }
        return this;
    }

    /**
     * 构建并返回任务和任务输出工具。从markdown文件中解析子Agent
     */
    public TaskToolsResult build() {
        if(taskRepository==null)
            throw new RuntimeException("taskRepository must be provided");

        // 初始一个子Agent的Map，将已经有的subAgents赋值到该map
        Map<String, CallableAgent> resolved = new HashMap<>(this.subAgents);
        // 从markdown方式注册的subAgents也一起放到Map
        loadFromResourcesAndMerge(resolved);

        if( resolved.isEmpty() )
            throw new RuntimeException("At least one sub-agent must be configured (via subAgent or addAgentResource)");

        // 返回TaskTool工具
        TaskTool taskTool = new TaskTool(resolved, taskRepository);
        // 返回TaskOutputTool工具
        TaskOutputTool taskOutputTool = new TaskOutputTool(taskRepository);
        return new TaskToolsResult(taskTool, taskOutputTool);
    }

    private void loadFromResourcesAndMerge(Map<String, CallableAgent> into) {
        if (agentResources.isEmpty()) {
            return;
        }
        if(model==null)
            throw new RuntimeException("model must be set when using addAgentResource");
        if(defaultToolsByName==null || defaultToolsByName.isEmpty())
            throw new RuntimeException("defaultToolsByName must be set when using addAgentResource");

        AgentSpecReActAgentFactory factory =
                new AgentSpecReActAgentFactory(model, defaultToolsByName);

        for (File file : agentResources) {
            try {
                if (!file.exists() || !file.canRead()) {
                    continue;
                }
                AgentSpec spec = AgentSpecLoader.loadFromFile(file.toPath());
                if (spec != null && spec.name()!=null && !spec.name().isEmpty()) {
                    into.put(spec.name(), factory.create(spec));
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to load agent spec from " + file.getName(), e);
            }
        }
    }

    public record TaskToolsResult(TaskTool taskTool, TaskOutputTool taskOutputTool) {}
}
