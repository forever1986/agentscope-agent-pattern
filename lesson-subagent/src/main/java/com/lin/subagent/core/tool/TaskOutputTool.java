package com.lin.subagent.core.tool;

import com.lin.subagent.core.task.BackgroundTask;
import com.lin.subagent.core.task.TaskRepository;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;

/**
 * AgentScope 工具用于获取正在运行或已完成的后台任务的输出信息。
 */
public class TaskOutputTool {

    private static final String DEFAULT_DESCRIPTION =
            """
            从正在运行或已完成的后台任务（子代理）中获取到任务的输出。
            当任务是以 run_in_background=true 的方式启动时使用此方法。
            参数：task_id（必填）、block（等待完成，默认为 true）、timeout（最大等待毫秒数，默认为 30000）。
            """;

    private final TaskRepository taskRepository;

    public TaskOutputTool(TaskRepository taskRepository) {
        if(taskRepository==null)
            throw new RuntimeException("taskRepository must not be null");
        this.taskRepository = taskRepository;
    }

    @Tool(name = "TaskOutput", description = DEFAULT_DESCRIPTION)
    public String taskOutput(
            @ToolParam(
                            name = "task_id",
                            description =
                                    "当“run_in_background”设置为“true”时，任务工具返回的任务ID",
                            required = true)
                    String taskId,
            @ToolParam(
                            name = "block",
                            description = "是否等待完成（默认值：true）",
                            required = false)
                    Boolean block,
            @ToolParam(
                            name = "timeout",
                            description =
                                    "最大等待时间（以毫秒为单位，单位默认为毫秒，最大值为 600000）",
                            required = false)
                    Long timeout) {
        System.out.println("=====执行taskOutput工具=======");
        BackgroundTask bgTask = taskRepository.getTask(taskId);

        if (bgTask == null) {
            return "Error: 未找到任务ID=" + taskId + " 的后台任务";
        }

        boolean shouldBlock = block == null || block;
        long timeoutMs = timeout != null ? Math.min(timeout, 600000) : 30000;

        if (shouldBlock && !bgTask.isCompleted()) {
            try {
                bgTask.waitForCompletion(timeoutMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Error: Wait for task interrupted";
            }
        }

        StringBuilder result = new StringBuilder();
        result.append("Task ID: ").append(taskId).append("\n");
        result.append("Status: ").append(bgTask.getStatus()).append("\n\n");

        if (bgTask.isCompleted() && bgTask.getResult() != null) {
            result.append("Result:\n").append(bgTask.getResult());
        } else if (bgTask.getError() != null) {
            result.append("Error:\n").append(bgTask.getError().getMessage());
            if (bgTask.getError().getCause() != null) {
                result.append("\nCause: ").append(bgTask.getError().getCause().getMessage());
            }
        } else if (!bgTask.isCompleted()) {
            result.append("Task still running...");
        }

        return result.toString();
    }
}
