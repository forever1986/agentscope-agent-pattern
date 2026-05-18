package com.lin.router;

import java.util.function.Predicate;

public class RunnableBranch<I, O> implements Runnable<I, O>{

    // 分支列表：[条件判断, 对应执行的Runnable]
    private final java.util.List<java.util.AbstractMap.SimpleEntry<Predicate<I>, Runnable<I, O>>> branches;
    // 默认分支：所有条件都不满足时执行
    private final Runnable<I, O> defaultBranch;

    // 构造方法：初始化分支和默认分支
    public RunnableBranch(java.util.List<java.util.AbstractMap.SimpleEntry<Predicate<I>, Runnable<I, O>>> branches,
                          Runnable<I, O> defaultBranch) {
        this.branches = branches;
        this.defaultBranch = defaultBranch;
    }

    // 核心方法：根据输入条件选择分支执行
    @Override
    public O invoke(I input) {
        // 遍历分支，找到第一个满足条件的分支执行
        for (var branch : branches) {
            Predicate<I> condition = branch.getKey();
            Runnable<I, O> runnable = branch.getValue();
            if (condition.test(input)) {
                return runnable.invoke(input);
            }
        }
        // 没有满足的条件，执行默认分支
        return defaultBranch.invoke(input);
    }

    // 便捷构建方法（链式调用，对齐 Python 的用法）
    public static <I, O> Builder<I, O> builder() {
        return new Builder<>();
    }

    // 构建器：简化分支添加
    static class Builder<I, O> {
        private final java.util.List<java.util.AbstractMap.SimpleEntry<Predicate<I>, Runnable<I, O>>> branches = new java.util.ArrayList<>();
        private Runnable<I, O> defaultBranch;

        public Builder<I, O> addBranch(Predicate<I> condition, Runnable<I, O> runnable) {
            branches.add(new java.util.AbstractMap.SimpleEntry<>(condition, runnable));
            return this;
        }

        public Builder<I, O> defaultBranch(Runnable<I, O> defaultBranch) {
            this.defaultBranch = defaultBranch;
            return this;
        }

        public RunnableBranch<I, O> build() {
            if (defaultBranch == null) {
                throw new IllegalArgumentException("默认分支不能为空");
            }
            return new RunnableBranch<>(branches, defaultBranch);
        }
    }
}
