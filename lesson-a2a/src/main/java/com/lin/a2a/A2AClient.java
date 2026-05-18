package com.lin.a2a;

import io.agentscope.core.a2a.agent.A2aAgent;
import io.agentscope.core.a2a.agent.card.WellKnownAgentCardResolver;
import io.agentscope.core.message.Msg;

import java.util.Map;

public class A2AClient {

    public static void main(String[] args) {
        // 创建 A2A Agent
        A2aAgent agent = A2aAgent.builder()
                .name("remote-agent")
                .agentCardResolver(WellKnownAgentCardResolver.builder()
                        .baseUrl("http://127.0.0.1:8080") // A2A服务器地址
                        .relativeCardPath("/.well-known/agent-card.json") // A2A服务器中Agent的信息地址
                        .authHeaders(Map.of())
                        .build())
                .build();
        // 调用call方法返回数据
        Msg response = agent.call(Msg.builder()
                .textContent("广州的天气？")
                .build()).block();
        // 打印输出结果
        System.out.println(response==null?null:response.getTextContent());
    }
}
