package com.lin.subagent.core.tool;

import com.lin.subagent.core.task.TaskRepository;
import io.agentscope.core.agent.CallableAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

/**
 * AgentScope 工具可调用子代理来处理复杂且独立的任务。支持后台执行
 */
public class TaskTool {

    private static final Logger logger = LoggerFactory.getLogger(TaskTool.class);

    private final Map<String, CallableAgent> subAgents;
    private final TaskRepository taskRepository;

    public TaskTool(Map<String, CallableAgent> subAgents, TaskRepository taskRepository) {
        this.subAgents = Map.copyOf(subAgents);
        this.taskRepository = taskRepository;
    }

    @Tool(
            name = "Task",
            description =
                    """
                    启动一个专门的sub-agent来自主完成复杂的多步骤任务。
                    当任务需要出门穿搭推荐、旅游规划或进行多步骤执行时使用此功能。
                    参数包括：description（简短摘要）、prompt（子代理的任务）、subagent_type（必填）、run_in_background（可选）。
                    当 run_in_background=true 时，会返回一个任务 ID；使用 TaskOutput tool 结合该任务 ID 获取结果。
                    """)
    public String task(
            @ToolParam(
                            name = "description",
                            description = "该代理人将要执行的任务简述（3-5 个词）")
                    String description,
            @ToolParam(
                            name = "prompt",
                            description = "该代理方需执行的具体任务",
                            required = true)
                    String prompt,
            @ToolParam(
                            name = "subagent_type",
                            description = "专业Agent的类型",
                            required = true)
                    String subagentType,
            @ToolParam(
                            name = "run_in_background",
                            description =
                                    "将值设为“true”以实现异步运行；使用“TaskOutput”来获取结果",
                            required = false)
                    Boolean runInBackground) {
        System.out.println("=====执行task工具=======");
        if (subagentType == null || subagentType.isEmpty()) {
            return "Error: subagent_type is required";
        }
        // 判断子agent的map里面有没有对应的子agent
        if (!subAgents.containsKey(subagentType)) {
            return "Error: Unknown subagent type: "
                    + subagentType
                    + ". Allowed types: "
                    + subAgents.keySet();
        }
        if (prompt == null || prompt.isEmpty()) {
            return "Error: prompt is required";
        }

        // 获取子Agent
        CallableAgent subAgent = subAgents.get(subagentType);

        if (Boolean.TRUE.equals(runInBackground)) {
            // 异步执行子Agent
            String taskId = "task_" + UUID.randomUUID();
            taskRepository.putTask(taskId, () -> executeSubAgent(subAgent, prompt));
            return String.format(
                    "任务 ID：%s%n%n 后台已启动该任务。请使用“TaskOutput”工具并指定任务 ID 为 '%s' 来获取结果。",
                    taskId, taskId);
        }

        // 执行子Agent
        return executeSubAgent(subAgent, prompt);
    }

    private String executeSubAgent(CallableAgent subAgent, String prompt) {
        try {
            Msg userMsg = Msg.builder().role(MsgRole.USER).textContent(prompt).build();
            Msg response = subAgent.call(userMsg).block();
            if (response == null) {
                return "Sub-agent 没有回复";
            }
            String text = response.getTextContent();
            return text!= null&&!text.isEmpty() ? text : "Sub-agent 返回了空内容";
        } catch (Exception e) {
            logger.warn("Sub-agent execution failed: {}", e.getMessage());
            return "Error executing sub-agent: " + e.getMessage();
        }
    }
}
