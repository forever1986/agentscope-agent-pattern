package com.lin.thinking;

import com.alibaba.cloud.ai.agent.agentscope.AgentScopeAgent;
import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LoopAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SequentialAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.loop.LoopMode;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.model.DashScopeChatModel;
import org.springframework.ai.chat.messages.Message;

import java.util.List;
import java.util.Optional;

public class SelfReflectionDemo {

    private static final double QUALITY_THRESHOLD = 0.85;

    private static final String SEARCH_PROMPT =
            """
            您是一个搜索专家，负责检索相关资料。通过搜索工具能力，检索出用户问题的相关资料，并将每一个资料内容简短总结，然后返回结果。
            """;

    private static final String REFLECTION_PROMPT =
            """
            你是一名评估专家。根据用户的请求以及检索的结果之间的相关性，
            输出一个介于 0 到 1 之间的单个浮点数分数。该分数表示 检索的结果 与用户意图的匹配程度。
            仅输出数字，不要其他文字。示例：0.85
            """;

    private static final String SUMMARY_PROMPT =
            """
            你是一名总结专家。根据用户的请求以及检索的结果，总结出不超过200个字的内容。
            """;

    private static final String SEARCH_KEY = "search_key";
    private static final String REFLECTION_KEY = "reflection_key";
    private static final String SUMMARY_KEY = "summary_key";

    public static void main(String[] args) throws GraphRunnerException {

        // 用户输入
        String userInput = "关于阿里巴巴公司2025年第三季度的财报相关资料。";

        // 负责搜索的ReActAgent
        ReActAgent.Builder searchBuilder =
                ReActAgent.builder()
                        .name("search_agent")
                        .model(DashScopeChatModel.builder()
                                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                                .modelName(System.getenv("QWEN_MODEL"))
                                .enableSearch(true) // 开启检索功能
                                .build())
                        .description("您是一个搜索专家，负责检索相关资料")
                        .sysPrompt(SEARCH_PROMPT)
                        .memory(new InMemoryMemory());
        // 封装ReActAgent让其兼容FlowAgent框架
        AgentScopeAgent searchAgent =
                AgentScopeAgent.fromBuilder(searchBuilder)
                        .name("search_agent")
                        .description("您是一个搜索专家，负责检索相关资料")
                        .instruction("{input}")
                        .includeContents(false)
                        .outputKey(SEARCH_KEY)
                        .build();

        // 对检索结果进行评分的ReActAgent
        ReActAgent.Builder reflectionBuilder =
                ReActAgent.builder()
                        .name("reflection")
                        .model(DashScopeChatModel.builder()
                                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                                .modelName(System.getenv("QWEN_MODEL"))
                                .build())
                        .description("对检索的内容评判与用户问题的相关性")
                        .sysPrompt(REFLECTION_PROMPT)
                        .memory(new InMemoryMemory());
        // 封装ReActAgent让其兼容FlowAgent框架
        AgentScopeAgent reflectionAgent =
                AgentScopeAgent.fromBuilder(reflectionBuilder)
                        .name("reflection")
                        .description("对检索的内容评判与用户问题的相关性")
                        .instruction(
                                "这里是检索的结果:\n"
                                        + " {search_key}.\n\n"
                                        + " 这是用户原始的问题:\n"
                                        + " {input}.")
                        .includeContents(false)
                        .outputKey(REFLECTION_KEY)
                        .build();

        // 将上面两个Agent组装为一个顺序的subAgent
        SequentialAgent sequentialAgent =
                SequentialAgent.builder()
                        .name("问题检索")
                        .description("检索问题并评估其质量")
                        .subAgents(List.of(searchAgent, reflectionAgent))
                        .build();

        // 使用LoopAgent进行循环
        LoopAgent loopAgent=  LoopAgent.builder()
                .name("loop_search_refinement_agent")
                .description(
                        "不断对检索进行优化，直至质量评分超过一定标准。 " + QUALITY_THRESHOLD)
                .subAgent(sequentialAgent)
                .loopStrategy(
                        LoopMode.condition(
                                messages -> {
                                    if (messages == null || messages.isEmpty()) {
                                        return false;
                                    }
                                    String text = messages.get(messages.size() - 1).getText();
                                    if (text == null || text.isBlank()) {
                                        return false;
                                    }
                                    try {
                                        double score = Double.parseDouble(text.trim());
                                        boolean satisfied = score > QUALITY_THRESHOLD;
                                        if (satisfied) {
                                            System.out.println(
                                                    "检索得分 {" + score + "} 超过了 {"+ QUALITY_THRESHOLD + "},"
                                                            + " 停止循环");
                                        }else {
                                            System.out.println(
                                                    "检索得分 {" + score + "} 没有超过 {"+ QUALITY_THRESHOLD + "},"
                                                            + " 继续循环");
                                        }
                                        return satisfied;
                                    } catch (NumberFormatException e) {
                                        System.err.println("无法格式化得到分数: {" + text +"}");
                                        return false;
                                    }
                                }))
                .build();

        // 对总结的ReActAgent
        ReActAgent.Builder summaryBuilder =
                ReActAgent.builder()
                        .name("summary")
                        .model(DashScopeChatModel.builder()
                                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                                .modelName(System.getenv("QWEN_MODEL"))
                                .build())
                        .description("根据检索问题和用户的提问，将结果为200个字总结")
                        .sysPrompt(SUMMARY_PROMPT)
                        .memory(new InMemoryMemory());
        // 封装ReActAgent让其兼容FlowAgent框架
        AgentScopeAgent summaryAgent =
                AgentScopeAgent.fromBuilder(summaryBuilder)
                        .name("summary")
                        .description("根据检索问题和用户的提问，将结果为200个字总结")
                        .instruction(
                                "这里是检索的结果:\n"
                                        + " {search_key}.\n\n"
                                        + " 这是用户原始的问题:\n"
                                        + " {input}.")
                        .includeContents(false)
                        .outputKey(SUMMARY_KEY)
                        .build();


        // 将loopAgent和summaryAgent组装为一个顺序的subAgent
        SequentialAgent resultAgent =
                SequentialAgent.builder()
                        .name("问题检索")
                        .description("检索问题并评估其质量")
                        .subAgents(List.of(loopAgent, summaryAgent))
                        .build();

        // 输出结果
        Optional<OverAllState> resultOpt = resultAgent.invoke(userInput);
        if (resultOpt.isEmpty()) {
            System.err.println("无结果!");
            return;
        }
        OverAllState state = resultOpt.get();
        String search_content = extractText(state.value(SEARCH_KEY));
        String summary_content = extractText(state.value(SUMMARY_KEY));
        String score = extractText(state.value(REFLECTION_KEY));
        System.out.println("用户输入: " + userInput);
        System.out.println("检索评分结果: " + score);
        System.out.println("=============分割线======================");
        System.out.println("检索内容: " + search_content);
        System.out.println("=============分割线======================");
        System.out.println("总结结果: " + summary_content);

        // 打印出StateGraph的流程图
        System.out.println();
        System.out.println();
        GraphRepresentation representation = resultAgent.getGraph().getGraph(GraphRepresentation.Type.PLANTUML,
                "expander flow");
        System.out.println(representation.content());
    }

    private static String extractText(Optional<Object> valueOpt) {
        if (valueOpt.isEmpty()) {
            return null;
        }
        Object v = valueOpt.get();
        if (v instanceof Message message) {
            return message.getText();
        }
        return v.toString();
    }

}
