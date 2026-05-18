package com.lin.handoffs;

import com.alibaba.cloud.ai.agent.agentscope.AgentScopeAgent;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.lin.handoffs.actions.RouteAfterSalesAction;
import com.lin.handoffs.actions.RouteAfterSupportAction;
import com.lin.handoffs.actions.RouteInitialAction;
import com.lin.handoffs.state.AgentScopeStateConstants;
import com.lin.handoffs.tools.TransferToSalesTool;
import com.lin.handoffs.tools.TransferToSupportTool;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.tool.Toolkit;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;

public class HandoffsDemo {

    private static final String SALES_PROMPT =
            """
            您是一个销售代理。请协助处理销售咨询、报价以及产品库存情况。
            如果客户询问技术问题、故障排除或账户相关问题，请使用“transfer_to_support”工具将问题转交至支持代理。
            """;

    private static final String SUPPORT_PROMPT =
            """
            您是一名客服专员。负责解决技术问题、故障排查以及账户相关问题。
            如果客户询问价格、购买事宜或产品供应情况，请使用“transfer_to_sales”工具将问题转交至销售专员。
            """;

    public static void main(String[] args) throws GraphStateException {

        // 初始化基础模型
        DashScopeChatModel model = DashScopeChatModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName(System.getenv("QWEN_MODEL"))
                .build();

        // 创建sales Agent
        Toolkit salesToolkit = new Toolkit();
        salesToolkit.registerTool(TransferToSupportTool.create());

        ReActAgent.Builder salesReActBuilder =
                ReActAgent.builder()
                        .name(AgentScopeStateConstants.SALES_AGENT)
                        .description("负责定价、产品供应情况及销售咨询的销售代理人员")
                        .sysPrompt(SALES_PROMPT)
                        .model(model)
                        .toolkit(salesToolkit) // 加入工具，这个工作主要作用是转换参数active_agent为support_agent
                        .memory(new InMemoryMemory());

        AgentScopeAgent salesAgentScopeAgent =  AgentScopeAgent.fromBuilder(salesReActBuilder)
                .name(AgentScopeStateConstants.SALES_AGENT)
                .description("负责定价、产品供应情况及销售咨询的销售代理人员")
                .instruction("请帮助客户解答他们的销售相关问题：{input}。")
                .includeContents(true)
                .returnReasoningContents(true)
                .build();

        // 创建support Agent
        Toolkit supportToolkit = new Toolkit();
        supportToolkit.registerTool(TransferToSalesTool.create());

        ReActAgent.Builder supportReActBuilder =
                ReActAgent.builder()
                        .name(AgentScopeStateConstants.SUPPORT_AGENT)
                        .description("技术支持人员，负责处理技术问题及故障排查工作")
                        .sysPrompt(SUPPORT_PROMPT)
                        .model(model)
                        .toolkit(supportToolkit)// 加入工具，这个工作主要作用是转换参数active_agent为sales_agent
                        .memory(new InMemoryMemory());

        AgentScopeAgent supportAgentScopeAgent =  AgentScopeAgent.fromBuilder(supportReActBuilder)
                .name(AgentScopeStateConstants.SUPPORT_AGENT)
                .description("技术支持人员，负责处理技术问题及故障排查工作")
                .instruction(
                        "请协助客户解决其产品技术方面的问题：{input}。")
                .includeContents(true)
                .returnReasoningContents(true)
                .build();


        // 组装工作流图
        StateGraph graph =
                new StateGraph(
                        "agent_scope_handoffs",
                        () -> {
                            Map<String, KeyStrategy> strategies = new HashMap<>();
                            // 用户消息使用追加方式
                            strategies.put("messages", new AppendStrategy(false));
                            // active agent使用替换方式
                            strategies.put(
                                    AgentScopeStateConstants.ACTIVE_AGENT, new ReplaceStrategy());
                            return strategies;
                        });

        // 将sales Agent和support Agent加入到图的节点上
        graph.addNode(AgentScopeStateConstants.SALES_AGENT, salesAgentScopeAgent.asNode());
        graph.addNode(AgentScopeStateConstants.SUPPORT_AGENT, supportAgentScopeAgent.asNode());

        // 增加初始节点到sales Agent和support Agent两个节点的条件边
        // 1）如果参数active_agent=sales_agent，就从开始节点跳转到sales Agent
        // 2）否则，则从开始节点跳转到support Agent
        graph.addConditionalEdges(
                START,
                new RouteInitialAction(),
                Map.of(AgentScopeStateConstants.SALES_AGENT, AgentScopeStateConstants.SALES_AGENT,
                        AgentScopeStateConstants.SUPPORT_AGENT,AgentScopeStateConstants.SUPPORT_AGENT));

        // 增加sales Agent节点到support Agent和end两个节点的条件边
        // 1）如果参数active_agent=support_agent，就从开始节点跳转到support Agent
        // 2）否则，则从sales Agent节点跳转到end结束节点
        graph.addConditionalEdges(
                AgentScopeStateConstants.SALES_AGENT,
                new RouteAfterSalesAction(),
                Map.of(AgentScopeStateConstants.SUPPORT_AGENT, AgentScopeStateConstants.SUPPORT_AGENT,
                        "__end__", END));

        // 增加support Agent节点到sales Agent和end两个节点的条件边
        // 1）如果参数active_agent=sales_agent，就从开始节点跳转到sales Agent
        // 2）否则，则从support Agent节点跳转到end结束节点
        graph.addConditionalEdges(
                AgentScopeStateConstants.SUPPORT_AGENT,
                new RouteAfterSupportAction(),
                Map.of(AgentScopeStateConstants.SALES_AGENT, AgentScopeStateConstants.SALES_AGENT,
                        "__end__", END));
        CompiledGraph compiledGraph = graph.compile();

        // 打印流程图
//        GraphRepresentation representation = compiledGraph.getGraph(GraphRepresentation.Type.PLANTUML,
//                "expander flow");
//        System.out.println(representation.content());
//        System.out.println();
//        System.out.println();

        // 测试
        String query = "嗨，我的账户登录出现了问题，能帮帮我吗？";
        System.out.println("Query: {"+ query + "}");
        Map<String, Object> inputs = Map.of("input", query);
        Optional<OverAllState> resultOpt = compiledGraph.invoke(inputs);

        if (resultOpt.isEmpty()) {
            System.out.println("结果为空!");;
        }

        OverAllState state = resultOpt.get();

        List<Message> messages = (List<Message>) state.value("messages").orElse(List.of());

        System.out.println("回复的信息:");
        messages.forEach(msg -> {
            if(msg instanceof ToolResponseMessage toolResponseMessage){
                System.out.println("  [{" + msg.getMessageType() + "}] {"+ toolResponseMessage.getResponses().getLast().name() +"}");
            }else{
                System.out.println("  [{" + msg.getMessageType() + "}] {"+ msg.getText() +"}");
            }

        });
    }
}
