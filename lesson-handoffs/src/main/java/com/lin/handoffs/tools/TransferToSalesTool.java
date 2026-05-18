package com.lin.handoffs.tools;

import com.alibaba.cloud.ai.graph.agent.tools.ToolContextHelper;
import com.lin.handoffs.state.AgentScopeStateConstants;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import org.springframework.ai.chat.model.ToolContext;

/**
 * 用于support Agent的“AgentScope转接工具”：将对话转接给销售人员。
 */
public final class TransferToSalesTool {

    public static final String TOOL_NAME = "transfer_to_sales";

    private TransferToSalesTool() {}

    @Tool(
            name = TOOL_NAME,
            description =
                    "将对话转接给销售代表。此选项适用于客户询问价格、购买方式或产品供应情况时使用。")
    public String transferToSales(
            @ToolParam(
                            name = "reason",
                            description =
                                    "将对话转接给销售代表。简要说明转接原因（例如：客户询问价格）；当客户询问价格、购买方式或产品供应情况时使用。")
                    String reason,
            ToolContext toolContext) {
        ToolContextHelper.getStateForUpdate(toolContext)
                .ifPresent(
                        update ->
                                update.put(
                                        AgentScopeStateConstants.ACTIVE_AGENT,
                                        AgentScopeStateConstants.SALES_AGENT));
        System.out.println("======call transferToSales===========");
        return "从技术支持人员转为销售代理人员。原因: "
                + (reason != null ? reason : "客户需求销售服务");
    }

    public static TransferToSalesTool create() {
        return new TransferToSalesTool();
    }
}
