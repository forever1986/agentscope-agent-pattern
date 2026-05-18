package com.lin.structured;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.OpenAIChatModel;
import io.agentscope.core.model.StructuredOutputReminder;

public class StructuredOutputPromptDemo {

    public static void main(String[] args) {
        // 创建智能体并内联配置模型
        ReActAgent agent = ReActAgent.builder()
                .name("Assistant")
                .sysPrompt("你是一个有帮助的 AI 助手。")
                .model(OpenAIChatModel.builder()
                        .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                        .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                        .modelName("qwen1.5-14b-chat")
                        .stream(false)
                        .build())
                .structuredOutputReminder(StructuredOutputReminder.PROMPT)  // TOOL_CHOICE是默认的，也可以改为PROMPT
                .build();
        // 调用call方法返回数据
        Msg response = agent.call(Msg.builder()
                .textContent("推荐一本书！")

                .build(), Book.class).block();
        // 打印输出结果
        Book data = response.getStructuredData(Book.class);
        System.out.println(data==null?null:data.toString());

        System.out.println("=============过程message===================");
        agent.getMemory().getMessages().forEach(msg ->{
            System.out.println(msg.getTextContent());
        });
    }
}
