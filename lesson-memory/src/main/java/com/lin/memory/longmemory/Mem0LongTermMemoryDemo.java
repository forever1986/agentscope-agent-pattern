package com.lin.memory.longmemory;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.LongTermMemory;
import io.agentscope.core.memory.LongTermMemoryMode;
import io.agentscope.core.memory.mem0.Mem0ApiType;
import io.agentscope.core.memory.mem0.Mem0LongTermMemory;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.model.DashScopeChatModel;

import java.util.List;

public class Mem0LongTermMemoryDemo {

    public static void main(String[] args) throws InterruptedException {
        // 使用自建 Mem0，需要指定 apiType 为 Mem0ApiType.SELF_HOSTED
        Mem0LongTermMemory mem0LongTermMemory = Mem0LongTermMemory.builder()
//                .agentName("SmartAssistant") // agentId：标识代理（可选）
                .userId("alex") // userId：标识用户/工作区（可选）
//                .runName("session-0001") // runId：标识会话/运行（可选） agentName、userId和runName至少填一个
                .apiBaseUrl("https://api.mem0.ai/")  //Mem0云托管服务地址
                .apiKey(System.getenv("MEM0_API_KEY"))  // 可选，这里是云托管，需要设置API KEY
                .apiType(Mem0ApiType.PLATFORM)  // 指定为云托管的Mem0
                .build();
        // 加入长期记忆
        Msg userMsg = Msg.builder()
                .role(MsgRole.USER)
                .textContent("Hi, I'm Alex. I'm a vegetarian and I'm allergic to nuts.")
                .build();
        Msg assistantMsg = Msg.builder()
                .role(MsgRole.ASSISTANT)
                .textContent("Hello Alex! I see that you're a vegetarian with a nut allergy.")
                .build();
        mem0LongTermMemory.record(List.of(userMsg, assistantMsg)).block();
        // 暂停15秒钟，因为云管平台是异步存储
        Thread.sleep(15000);
        // 第一次不加入长期记忆
        System.out.println("========第一次调用Agent=============");
        callAgent(null, "Is Alex a vegetarian?");
        // 第二次加入长期记忆
        System.out.println("========第二次调用Agent=============");
        callAgent(mem0LongTermMemory, "Is Alex a vegetarian?");


    }

    private static void callAgent(LongTermMemory longTermMemory, String query){
        // 创建智能体并内联配置模型
        ReActAgent agent = ReActAgent.builder()
                .sysPrompt("你是一个有帮助的 AI 助手。")
                .model(DashScopeChatModel.builder()
                        .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                        .modelName(System.getenv("QWEN_MODEL"))
                        .stream(false)
                        .build())
                .longTermMemory(longTermMemory)
                .longTermMemoryMode(LongTermMemoryMode.STATIC_CONTROL) // 不同策略对存储长期基于由不同操作
                .build();
        // 调用call方法返回数据
        Msg response = agent.call(Msg.builder()
                .textContent(query)
                .build()).block();
        // 打印输出结果
        System.out.println(response==null?null:response.getTextContent());
    }
}
