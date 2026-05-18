package com.lin.subagent.core.task;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 使用 CompletableFuture 来管理后台任务的执行
 */
public class BackgroundTask {

    private final String taskId;

    private final CompletableFuture<String> future;

    public BackgroundTask(String taskId, CompletableFuture<String> future) {
        this.taskId = taskId;
        this.future = future;
    }

    /**
     * 检查该任务是否已完成执行
     */
    public boolean isCompleted() {
        return this.future.isDone();
    }

    /**
     * 获取任务执行的结果（非阻塞方式）
     */
    public String getResult() {
        try {
            return this.future.getNow(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取任务执行过程中出现的任何错误信息
     */
    public Exception getError() {
        if (this.future.isCompletedExceptionally()) {
            try {
                this.future.getNow(null);
            } catch (Exception e) {
                if (e.getCause() instanceof Exception cause) {
                    return cause;
                }
                return e;
            }
        }
        return null;
    }

    /**
     * 获取该任务的人类可读状态描述
     */
    public String getStatus() {
        if (this.future.isCompletedExceptionally()) {
            Exception error = getError();
            return "Failed: " + (error != null ? error.getMessage() : "Unknown error");
        }
        return this.future.isDone() ? "Completed" : "Running";
    }

    /**
     * 等待任务在指定的超时时间内完成
     */
    public boolean waitForCompletion(long timeoutMs) throws InterruptedException {
        if (this.future.isDone()) {
            return true;
        }
        try {
            this.future.get(timeoutMs, TimeUnit.MILLISECONDS);
            return true;
        } catch (InterruptedException e) {
            throw e;
        } catch (TimeoutException e) {
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 如果任务尚未完成，取消它
     */
    public boolean cancel(boolean mayInterruptIfRunning) {
        return this.future.cancel(mayInterruptIfRunning);
    }

    /**
     * 获取任务ID
     */
    public String getTaskId() {
        return this.taskId;
    }
}
