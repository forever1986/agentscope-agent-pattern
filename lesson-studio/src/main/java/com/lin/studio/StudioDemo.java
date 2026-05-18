package com.lin.studio;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.studio.StudioManager;
import io.agentscope.core.studio.StudioMessageHook;

public class StudioDemo {

    public static void main(String[] args) {
        // 初始化 Studio 连接
        StudioManager.init()
                .studioUrl("http://localhost:3000")
                .project("MyProject")
                .runName("demo_" + System.currentTimeMillis())
                .initialize()
                .block();

        // 创建带 Hook 的 Agent
        ReActAgent agent = ReActAgent.builder()
                .name("Assistant")
                .sysPrompt("你是一个有帮助的 AI 助手。")
                .model(DashScopeChatModel.builder()
                        .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                        .modelName(System.getenv("QWEN_MODEL"))
                        .build())
                .hook(new StudioMessageHook(StudioManager.getClient()))
                .build();
        // 调用call方法返回数据
        Msg response = agent.call(Msg.builder()
                        .textContent("你好！")
                        .build())
                .block();
        // 打印输出结果
        System.out.println(response==null?null:response.getTextContent());
        // 清理资源
        StudioManager.shutdown();
    }
}
