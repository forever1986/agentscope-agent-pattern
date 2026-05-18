package com.lin.openclaw.config;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.Agent;
import io.agentscope.core.agui.registry.AguiAgentRegistry;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.plan.PlanNotebook;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.coding.ShellCommandTool;
import io.agentscope.core.tool.file.ReadFileTool;
import io.agentscope.core.tool.file.WriteFileTool;
import io.agentscope.spring.boot.agui.common.AguiAgentRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.net.URL;
import java.util.Set;
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

        // 定义三大基本工具（读、写和执行）
        URL resource = ClassLoader.getSystemResource("workspaces");
        File directory = new File(resource.getPath());
        // 创建工具
        Toolkit toolkit = new Toolkit();

        ShellCommandTool shellTool = new ShellCommandTool(directory.getPath(),
                        Set.of("python", "python3", "node", "nodejs"),
                        null);
        ReadFileTool readTool = new ReadFileTool(directory.getPath());
        WriteFileTool writeTool = new WriteFileTool(directory.getPath());
        toolkit.registerTool(shellTool);
        toolkit.registerTool(readTool);
        toolkit.registerTool(writeTool);

        // 定义计划PlanNotebook
        PlanNotebook planNotebook = PlanNotebook.builder()
                .needUserConfirm(false) // 设置不需要询问用户就可以直接执行计划
                .build();

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
                .planNotebook(planNotebook)
                .memory(new InMemoryMemory())
                .maxIters(100)
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
