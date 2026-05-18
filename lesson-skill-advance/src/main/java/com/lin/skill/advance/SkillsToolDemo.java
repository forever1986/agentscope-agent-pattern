package com.lin.skill.advance;

import com.lin.skill.advance.tools.InventoryTool;
import com.lin.skill.advance.tools.SalesTool;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.hook.Hook;
import io.agentscope.core.hook.HookEvent;
import io.agentscope.core.hook.PreReasoningEvent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.skill.AgentSkill;
import io.agentscope.core.skill.SkillBox;
import io.agentscope.core.skill.repository.ClasspathSkillRepository;
import io.agentscope.core.tool.Toolkit;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;

public class SkillsToolDemo {

    public static void main(String[] args) throws IOException {

        // 创建工具Toolkit
        Toolkit toolkit = new Toolkit();
        // 创建从Classpath目录获取Skill的读取器
        ClasspathSkillRepository skillRepository = new ClasspathSkillRepository("skills/tools");
        // 创建skillBoox
        SkillBox skillBox = new SkillBox(toolkit);
        // 将Classpath目录下的skill读取并注册到skillBoox
        List<AgentSkill> skills = skillRepository.getAllSkills();
        for (AgentSkill skill : skills) {
            System.out.println("add skill:" + skill.getName());//打印加载到的skill
            if("sales_analytics".equals(skill.getName())){
                // 加入SalesTool
                skillBox.registration().skill(skill).agentTool(new SalesTool()).apply();
            }else{
                // 加入InventoryTool
                skillBox.registration().skill(skill).agentTool(new InventoryTool()).apply();
            }
        }

        // 使用一个hook来监听工具调用结果
        Hook toolHook = new Hook() {
            @Override
            public <T extends HookEvent> Mono<T> onEvent(T event) {
                if(event instanceof PreReasoningEvent preReasoningEvent){
                    System.out.println(
                            "\n[HOOK PreReasoningEvent] - Tool: "
                                    + ((ReActAgent)preReasoningEvent.getAgent()).getToolkit().getActiveGroups());
                }
                return Mono.just(event);
            }
        };

        // 创建智能体并内联配置模型
        ReActAgent agent = ReActAgent.builder()
                .name("Assistant")
                .sysPrompt("你是一个有帮助的 AI 助手。")
                .model(DashScopeChatModel.builder()
                        .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                        .modelName(System.getenv("QWEN_MODEL"))
                        .stream(false)
                        .build())
                .skillBox(skillBox) // 将skillBoox放入agent
                .toolkit(toolkit)
                .hook(toolHook)
                .build();
        // 调用call方法返回数据
        String query =
                "Write a SQL query to find all customers who made orders over $1000 in the last"
                        + " month";
        Msg response = agent.call(Msg.builder()
                .textContent(query)
                .build())
                .block();
        // 打印输出结果
        System.out.println(response==null?null:response.getTextContent());
    }
}
