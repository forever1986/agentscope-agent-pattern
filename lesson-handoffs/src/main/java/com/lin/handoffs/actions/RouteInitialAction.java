package com.lin.handoffs.actions;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.AsyncCommandAction;
import com.alibaba.cloud.ai.graph.action.Command;
import com.lin.handoffs.state.AgentScopeStateConstants;

import java.util.concurrent.CompletableFuture;

/**
 * 从“START”节点到活跃代理的路径
 * 1）如果参数active_agent=sales_agent，就从开始节点跳转到sales Agent
 * 2）否则，则从开始节点跳转到support Agent
 */
public class RouteInitialAction implements AsyncCommandAction {

    @Override
    public CompletableFuture<Command> apply(OverAllState state, RunnableConfig config) {
        String target =
                state.value(AgentScopeStateConstants.ACTIVE_AGENT)
                        .map(Object::toString)
                        .filter(AgentScopeStateConstants.SUPPORT_AGENT::equals)
                        .orElse(AgentScopeStateConstants.SALES_AGENT);
        System.out.println("======RouteInitialAction===========");
        return CompletableFuture.completedFuture(new Command(target));
    }
}
