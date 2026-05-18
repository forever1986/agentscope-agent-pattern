//package com.lin.router;
//
//import com.alibaba.cloud.ai.agent.agentscope.AgentScopeAgent;
//import com.alibaba.cloud.ai.agent.agentscope.flow.AgentScopeRoutingAgent;
//import com.alibaba.cloud.ai.graph.GraphRepresentation;
//import com.alibaba.cloud.ai.graph.OverAllState;
//import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
//import io.agentscope.core.ReActAgent;
//import io.agentscope.core.model.DashScopeChatModel;
//import org.springframework.ai.chat.messages.Message;
//
//import java.util.List;
//import java.util.Optional;
//
//public class RounterGraphDemo {
//
//    private static final String BOOKER_KEY = "booker_key";
//    private static final String INFO_KEY = "info_key";
//    private static final String OTHER_KEY = "other_key";
//
//    public static void main(String[] args) throws GraphRunnerException {
//        // 模型
//        DashScopeChatModel model = DashScopeChatModel.builder()
//                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
//                .modelName(System.getenv("QWEN_MODEL"))
//                .build();
//
//        ReActAgent.Builder bookerBuilder =
//                ReActAgent.builder()
//                        .name("booker")
//                        .description("你是一个预订机票专家，专门处理机票预订")
//                        .sysPrompt("""
//                                您是 预订机票 方面的专家。通过用户的提问，确认用户需要定几号的机票。
//                                请回复以下请求：{booker_input}
//                                """)
//                        .model(model);
//        AgentScopeAgent bookerAgent = AgentScopeAgent.fromBuilder(bookerBuilder)
//                .name("booker")
//                .description("你是一个预订机票专家，专门处理机票预订")
//                .instruction("请回复以下请求：{booker_input}")
//                .outputKey(BOOKER_KEY)
//                .build();
//
//        ReActAgent.Builder infoBuilder =
//                ReActAgent.builder()
//                        .name("info")
//                        .description("你是一个信息检索专家，专门处理信息检索")
//                        .sysPrompt("""
//                                您是 信息检索 方面的专家。通过用户的提问，检索出需要内容。
//                                请回复以下请求：{info_input}
//                                """)
//                        .model(model);
//        AgentScopeAgent infoAgent = AgentScopeAgent.fromBuilder(infoBuilder)
//                .name("booker")
//                .description("你是一个信息检索专家，专门处理信息检索")
//                .instruction("请回复以下请求：{info_input}")
//                .outputKey("info_key")
//                .build();
//
//        ReActAgent.Builder otherBuilder =
//                ReActAgent.builder()
//                        .name("other")
//                        .description("你是一个全能专家，专门处理除了机票预订和信息检索的问题")
//                        .sysPrompt("""
//                                您是 全能 方面的专家。通过用户的提问，回答用户问题。
//                                请回复以下请求：{other_input}
//                                """)
//                        .model(model);
//        AgentScopeAgent otherAgent = AgentScopeAgent.fromBuilder(otherBuilder)
//                .name("booker")
//                .description("你是一个全能专家，专门处理除了机票预订和信息检索的问题")
//                .instruction("请回复以下请求：{other_input}")
//                .outputKey("other_key")
//                .build();
//        ExAgentScopeRoutingAgent routingAgent = ExAgentScopeRoutingAgent.builder()
//                .name("router")
//                .model(model)
//                .description("""
//                        分析用户的请求并确定哪个专家处理程序应处理它
//                        根据相关性将查询路由至 booker、info 或 other 专家处。
//                        """)
//                .subAgents(List.of(bookerAgent, infoAgent, otherAgent))
//                .build();
//
//
//        // 输出结果
//        Optional<OverAllState> resultOpt = routingAgent.invoke("给我预订12号去伦敦的航班。");
//        if (resultOpt.isEmpty()) {
//            System.err.println("No result!");
//            return;
//        }
//        OverAllState state = resultOpt.get();
//        String booker_key = extractText(state.value(BOOKER_KEY));
//        String info_key = extractText(state.value(INFO_KEY));
//        String other_key = extractText(state.value(OTHER_KEY));
//        System.out.println("预订机票专家输出: " + booker_key);
//        System.out.println("信息检索专家输出: " + info_key);
//        System.out.println("全能专家输出: " + other_key);
//
//        // 打印出StateGraph的流程图
//        System.out.println();
//        System.out.println();
//        GraphRepresentation representation = routingAgent.getGraph().getGraph(GraphRepresentation.Type.PLANTUML,
//                "expander flow");
//        System.out.println(representation.content());
//    }
//
//    private static String extractText(Optional<Object> valueOpt) {
//        if (valueOpt.isEmpty()) {
//            return null;
//        }
//        Object v = valueOpt.get();
//        if (v instanceof Message message) {
//            return message.getText();
//        }
//        return v.toString();
//    }
//}
