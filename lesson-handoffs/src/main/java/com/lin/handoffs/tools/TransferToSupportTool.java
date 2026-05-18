/*
 * Copyright 2024-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lin.handoffs.tools;

import com.alibaba.cloud.ai.graph.agent.tools.ToolContextHelper;
import com.lin.handoffs.state.AgentScopeStateConstants;
import io.agentscope.core.tool.Tool;
import org.springframework.ai.chat.model.ToolContext;

/**
 * 用于销售代理的“AgentScope转接工具”：将对话转接给支持代理。
 */
public final class TransferToSupportTool {

    public static final String TOOL_NAME = "transfer_to_support";

    private TransferToSupportTool() {}

    @Tool(
            name = TOOL_NAME,
            description =
                    "将对话转接给客服人员。此选项适用于客户询问技术问题、故障排除方法或账户相关问题的情况。")
    public String transferToSupport(ToolContext toolContext) {
        ToolContextHelper.getStateForUpdate(toolContext)
                .ifPresent(
                        update ->
                                update.put(
                                        AgentScopeStateConstants.ACTIVE_AGENT,
                                        AgentScopeStateConstants.SUPPORT_AGENT));
        System.out.println("======call transferToSupport===========");
        return "从销售代理转为支持代理。";
    }

    public static TransferToSupportTool create() {
        return new TransferToSupportTool();
    }
}
