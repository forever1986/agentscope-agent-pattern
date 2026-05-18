package com.lin.agui.config;

import com.lin.agui.tools.WeatherTool;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.Agent;
import io.agentscope.core.agui.registry.AguiAgentRegistry;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.spring.boot.agui.common.AguiAgentRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Supplier;

@Configuration
public class AgentConfiguration {

    @Bean
    public AguiAgentRegistryCustomizer aguiAgentRegistryCustomizer() {
        AguiAgentRegistryCustomizer aguiAgentRegistryCustomizer = new AguiAgentRegistryCustomizer() {
            @Override
            public void customize(AguiAgentRegistry registry) {
                // 为默认代理注册一个工厂
                // 使用工厂可确保每个请求都获得一个新的代理实例
                registry.registerFactory("default", createDefaultAgent());

                // 注册具有不同 ID 的其他代理
                // 示例：一个没有工具的简单聊天代理
                registry.registerFactory("chat", createChatAgent());
            }
        };

        return aguiAgentRegistryCustomizer;
    }

    private Supplier<Agent> createDefaultAgent() {

        // 创建工具
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new WeatherTool());

        // 创建Agent
        Supplier<Agent> supplier = () -> ReActAgent.builder()
                .name("AG-UI Assistant")
                .sysPrompt(""" 
                        您是一个通过 AG-UI 协议接入的有用的人工智能助手。
                        您可以帮助用户完成各种任务，包括天气查询。
                        请在回复时做到简洁明了、有用
                        """)
                .model(DashScopeChatModel.builder()
                        .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                        .modelName(System.getenv("QWEN_MODEL"))
                        .enableThinking(false)
                        .build())
                .toolkit(toolkit)
                .memory(new InMemoryMemory())
                .maxIters(10)
                .build();
        return supplier;
    }

    private Supplier<Agent> createChatAgent() {

        Supplier<Agent> supplier = () ->  ReActAgent.builder()
                .name("Chat Assistant")
                .sysPrompt("""
                           你是一位友善的对话助手。
                           进行自然的对话，并为用户提供
                           一般性问题的解答以及交流方面的帮助。
                           """)
                .model(DashScopeChatModel.builder()
                        .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                        .modelName(System.getenv("QWEN_MODEL"))
                        .build())
                .memory(new InMemoryMemory())
                .maxIters(1)
                .build();
        return supplier;
    }

}
