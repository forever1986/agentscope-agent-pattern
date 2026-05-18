package com.lin.reflection;

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

public class ReflectionDemo {

    private static final double QUALITY_THRESHOLD = 0.5;

    private static final String SQL_GENERATOR_PROMPT =
            """
            You are a MySQL database expert. Given the user's natural language request, output the corresponding SQL statement.
            Only output valid MySQL SQL. Do not include explanations.
            """;

    private static final String SQL_RATER_PROMPT =
            """
            You are a SQL quality reviewer. Given the user's natural language request and the generated SQL,
            output a single float score between 0 and 1. The score indicates how well the SQL matches the user intent.
            Output ONLY the number, no other text. Example: 0.85
            """;

    private static final String SQL_KEY = "sql";
    private static final String SCORE_KEY = "score";

    public static void main(String[] args) throws GraphRunnerException {

        // 用户输入
        String userInput = "找出 2024 年下单超过 3 次的客户。";

        // 模型
        DashScopeChatModel model = DashScopeChatModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName(System.getenv("QWEN_MODEL"))
                .build();

        // 将自然语言转换为SQL的ReActAgent
        ReActAgent.Builder sqlGenBuilder =
                ReActAgent.builder()
                        .name("sql_generator")
                        .model(model)
                        .description("Converts natural language to MySQL SQL")
                        .sysPrompt(SQL_GENERATOR_PROMPT)
                        .memory(new InMemoryMemory());
        // 封装ReActAgent让其兼容FlowAgent框架
        AgentScopeAgent sqlGenerateAgent =
                AgentScopeAgent.fromBuilder(sqlGenBuilder)
                        .name("sql_generator")
                        .description("Converts natural language to MySQL SQL")
                        .instruction("{input}")
                        .includeContents(false)
                        .outputKey(SQL_KEY)
                        .build();

        // 对SQL进行评分的ReActAgent
        ReActAgent.Builder sqlRaterBuilder =
                ReActAgent.builder()
                        .name("sql_rater")
                        .model(model)
                        .description("Scores SQL against user intent")
                        .sysPrompt(SQL_RATER_PROMPT)
                        .memory(new InMemoryMemory());
        // 封装ReActAgent让其兼容FlowAgent框架
        AgentScopeAgent sqlRatingAgent =
                AgentScopeAgent.fromBuilder(sqlRaterBuilder)
                        .name("sql_rater")
                        .description("Scores SQL against user intent")
                        .instruction(
                                "Here's the generated SQL:\n"
                                        + " {sql}.\n\n"
                                        + " Here's the original user request:\n"
                                        + " {input}.")
                        .includeContents(false)
                        .outputKey(SCORE_KEY)
                        .build();

        // 将上面两个Agent组装为一个顺序的subAgent
        SequentialAgent sqlAgent =
                SequentialAgent.builder()
                        .name("sql_agent")
                        .description("Generates SQL and scores its quality")
                        .subAgents(List.of(sqlGenerateAgent, sqlRatingAgent))
                        .build();

        // 使用LoopAgent进行循环
        LoopAgent loopAgent=  LoopAgent.builder()
                .name("loop_sql_refinement_agent")
                .description(
                        "Iteratively refines SQL until quality score exceeds " + QUALITY_THRESHOLD)
                .subAgent(sqlAgent)
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
                                                    "SQL quality score {" + score + "} exceeds threshold {"+ QUALITY_THRESHOLD + "},"
                                                            + " stopping loop");
                                        }else {
                                            System.out.println(
                                                    "SQL quality score {" + score + "} did not exceeds threshold {"+ QUALITY_THRESHOLD + "},"
                                                            + " continue loop");
                                        }
                                        return satisfied;
                                    } catch (NumberFormatException e) {
                                        System.err.println("Could not parse score from: {" + text +"}");
                                        return false;
                                    }
                                }))
                .build();

        // 输出结果
        Optional<OverAllState> resultOpt = loopAgent.invoke(userInput);
        if (resultOpt.isEmpty()) {
            System.err.println("No result!");
            return;
        }
        OverAllState state = resultOpt.get();
        String sql = extractText(state.value(SQL_KEY));
        String score = extractText(state.value(SCORE_KEY));
        System.out.println("用户输入: " + userInput);
        System.out.println("生成SQL评分: " + score);
        System.out.println("sql结果: " + sql);

        // 打印出StateGraph的流程图
        System.out.println();
        System.out.println();
        GraphRepresentation representation = loopAgent.getGraph().getGraph(GraphRepresentation.Type.PLANTUML,
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
