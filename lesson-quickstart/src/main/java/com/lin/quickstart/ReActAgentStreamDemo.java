package com.lin.quickstart;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.ContentBlock;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.message.ThinkingBlock;
import io.agentscope.core.model.DashScopeChatModel;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ReActAgentStreamDemo {

    public static void main(String[] args) {
        // 创建智能体并内联配置模型
        ReActAgent agent = ReActAgent.builder()
                .name("Assistant")
                .sysPrompt("你是一个有帮助的 AI 助手。")
                .model(DashScopeChatModel.builder()
                        .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                        .modelName("qwen3-max-2026-01-23")
//                        .stream(false) // 设置模型的输出方式
                        .build())
                .build();

        AtomicBoolean hasPrintedThinkingHeader = new AtomicBoolean(false); // 是否打印Thinking：
        AtomicBoolean hasPrintedTextHeader = new AtomicBoolean(false);// 是否打印Text：
        AtomicBoolean hasPrintedTextSeparator = new AtomicBoolean(false);// 是否在Thinking和Text直接增加2个换行
        AtomicReference<String> lastThinkingContent = new AtomicReference<>(""); // 保留上次一次输出Thinking内容
        AtomicReference<String> lastTextContent = new AtomicReference<>(""); // 保留上一次的输出Text内容

        agent.stream(Msg.builder()
                .textContent("你好！")
                .build())
                // 通过doOnNext方法处理stream返回的每一个回复
                .doOnNext(
                        event -> {
                            Msg msg = event.getMessage();
                            for (ContentBlock block : msg.getContent()) {
                                if (block instanceof ThinkingBlock) {
                                    // 处理Thinking数据
                                    printStreamContent(
                                            ((ThinkingBlock) block).getThinking(),
                                            lastThinkingContent,
                                            hasPrintedThinkingHeader,
                                            "> Thinking: ",
                                            null);
                                } else if (block instanceof TextBlock) {
                                    // 处理Text数据
                                    printStreamContent(
                                            ((TextBlock) block).getText(),
                                            lastTextContent,
                                            hasPrintedTextHeader,
                                            "Text: ",
                                            () -> {
                                                if (hasPrintedThinkingHeader.get()
                                                        && !hasPrintedTextSeparator
                                                        .get()) {
                                                    System.out.print("\n\n");
                                                    hasPrintedTextSeparator.set(true);
                                                }
                                            });
                                }
                            }
                        })
                .blockLast();;
    }

    private static void printStreamContent(
            String content,
            AtomicReference<String> lastContentRef,
            AtomicBoolean hasPrintedHeaderRef,
            String header,
            Runnable prePrintAction) {
        String lastContent = lastContentRef.get();
        String toPrint;

        // 检查输出方式是累加还是递增
        if (content.startsWith(lastContent)) {
            // 累积
            toPrint = content.substring(lastContent.length());
            lastContentRef.set(content);
        } else {
            // 递增
            toPrint = content;
            lastContentRef.set(lastContent + content);
        }

        if (!toPrint.isEmpty()) {
            // 打印Thinking和Text的换行
            if (prePrintAction != null) {
                prePrintAction.run();
            }

            // 打印Thinking：和Text：
            if (!hasPrintedHeaderRef.get()) {
                System.out.print(header);
                hasPrintedHeaderRef.set(true);
            }
            // 打印输出
            // TODO: 这里为了显示与call方法不一样，特意每次都换行来显示每次的输出结果
            System.out.println(toPrint);
            System.out.flush();
        }
    }
}
