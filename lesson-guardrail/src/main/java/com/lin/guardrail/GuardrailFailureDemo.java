package com.lin.guardrail;

import com.lin.guardrail.base.GuardrailException;
import com.lin.guardrail.hook.FifthInputGuardrail;
import com.lin.guardrail.hook.FirstInputGuardrail;
import com.lin.guardrail.hook.SecondInputGuardrail;
import com.lin.guardrail.hook.ThirdInputGuardrail;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.formatter.dashscope.DashScopeChatFormatter;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;

import java.util.List;

public class GuardrailFailureDemo {

    public static void main(String[] args) {

        FirstInputGuardrail firstInputGuardrail = new FirstInputGuardrail("firstInputGuardrail");
        SecondInputGuardrail secondInputGuardrail = new SecondInputGuardrail("secondInputGuardrail");
        ThirdInputGuardrail thirdInputGuardrail = new ThirdInputGuardrail("thirdInputGuardrail");
        FifthInputGuardrail fifthInputGuardrail = new FifthInputGuardrail("fifthInputGuardrail");
        // Create Agent with hook
        ReActAgent agent =
                ReActAgent.builder()
                        .name("HookAgent")
                        .sysPrompt("你是一名乐于助人的助手。在处理数据时，请使用“process_data”这个工具。")
                        .model(DashScopeChatModel.builder()
                                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                                .modelName(System.getenv("QWEN_MODEL"))
                                .stream(false)
                                .formatter(new DashScopeChatFormatter())
                                .build())
                        .hooks(List.of(firstInputGuardrail, secondInputGuardrail, thirdInputGuardrail , fifthInputGuardrail))
                        .build();

        // 调用call方法返回数据
        try {
            Msg response = agent.call(Msg.builder()
                    .textContent("你好！")
                    .build()).block();
            // 打印输出结果
            System.out.println("最终回复："+(response==null?null:response.getTextContent()));
        }catch (GuardrailException e){
            System.out.println();
            System.out.println("不通过原因：");
            e.getErrorsMsg().forEach(System.out::println);
        }
    }

}
