package com.lin.handoffs.state;

/**
 * “AgentScope 多代理交接工作流程（sales + support）的状态键和Agent名称
 */
public final class AgentScopeStateConstants {

    private AgentScopeStateConstants() {}

    public static final String ACTIVE_AGENT = "active_agent";

    public static final String SALES_AGENT = "sales_agent";
    public static final String SUPPORT_AGENT = "support_agent";
}
