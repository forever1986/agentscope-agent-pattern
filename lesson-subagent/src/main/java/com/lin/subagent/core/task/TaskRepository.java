package com.lin.subagent.core.task;

import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * 使用线程池作为后台执行的实现方式来构建任务存储库
 */
public class TaskRepository {

    private final Map<String, BackgroundTask> backgroundTasks = new ConcurrentHashMap<>();

    private final ExecutorService executor;

    private final boolean ownsExecutor;

    public TaskRepository() {
        this(
                Executors.newCachedThreadPool(
                        r -> {
                            Thread thread = new Thread(r);
                            thread.setDaemon(true);
                            thread.setName("background-task-" + thread.getId());
                            return thread;
                        }),
                true);
    }

    public TaskRepository(ExecutorService executor) {
        this(executor, false);
    }

    public TaskRepository(ExecutorService executor, boolean ownsExecutor) {
        this.executor = executor;
        this.ownsExecutor = ownsExecutor;
    }

    public BackgroundTask getTask(String taskId) {
        return this.backgroundTasks.get(taskId);
    }

    public BackgroundTask putTask(String taskId, Supplier<String> taskExecution) {
        CompletableFuture<String> future =
                CompletableFuture.supplyAsync(taskExecution, this.executor);
        BackgroundTask backgroundTask = new BackgroundTask(taskId, future);
        this.backgroundTasks.put(taskId, backgroundTask);
        return backgroundTask;
    }

    public void removeTask(String taskId) {
        this.backgroundTasks.remove(taskId);
    }

    public void clear() {
        this.backgroundTasks.clear();
    }

    /**
     * 从Repository中移除已完成的任务
     */
    public void clearCompletedTasks() {
        this.backgroundTasks.entrySet().removeIf(entry -> entry.getValue().isCompleted());
    }

    /**
     * 如果此Repository拥有executor服务，则应关闭该executor服务。
     */
    public void shutdown() {
        if (this.ownsExecutor && this.executor != null) {
            this.executor.shutdown();
            try {
                if (!this.executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    this.executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                this.executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
