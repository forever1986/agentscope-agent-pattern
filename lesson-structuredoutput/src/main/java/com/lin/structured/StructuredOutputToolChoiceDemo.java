package com.lin.structured;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.model.StructuredOutputReminder;

public class StructuredOutputToolChoiceDemo {

    public static void main(String[] args) {

        // 创建智能体并内联配置模型
        ReActAgent agent = ReActAgent.builder()
                .name("Assistant")
                .sysPrompt("你是一个有帮助的 AI 助手。")
                .model(DashScopeChatModel.builder()
                        .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                        .modelName(System.getenv("QWEN_MODEL"))
                        .stream(false)
                        .build())
                .structuredOutputReminder(StructuredOutputReminder.TOOL_CHOICE)  // TOOL_CHOICE是默认的，也可以改为PROMPT
                .build();
        // 调用call方法返回数据
        Msg response = agent.call(Msg.builder()
                .textContent("推荐一本书！")
//                .metadata(Map.of(MessageMetadataKeys.STRUCTURED_OUTPUT_REMINDER_TYPE
//                , StructuredOutputReminder.TOOL_CHOICE.toString())) //某些模型支持强制调用工具，则可以设置这个参数
                .build(), Book.class).block();
        // 打印输出结果
        Book data = response.getStructuredData(Book.class);
        System.out.println(data==null?null:data.toString());


    }
}
