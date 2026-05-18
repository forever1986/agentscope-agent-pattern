package com.lin.handoffs.actions;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.AsyncCommandAction;
import com.alibaba.cloud.ai.graph.action.Command;
import com.lin.handoffs.state.AgentScopeStateConstants;

import java.util.concurrent.CompletableFuture;

/**
 * 在“support Agent”之后的路径
 * 1）如果参数active_agent=sales_agent，就从开始节点跳转到sales Agent
 * 2）否则，则从support Agent节点跳转到end结束节点
 */
public class RouteAfterSupportAction implements AsyncCommandAction {

    @Override
    public CompletableFuture<Command> apply(OverAllState state, RunnableConfig config) {
        String active =
                state.value(AgentScopeStateConstants.ACTIVE_AGENT).map(Object::toString).orElse("");
        String target =
                AgentScopeStateConstants.SALES_AGENT.equals(active)
                        ? AgentScopeStateConstants.SALES_AGENT
                        : "__end__";
        System.out.println("======RouteAfterSupportAction===========");
        return CompletableFuture.completedFuture(new Command(target));
    }
}
