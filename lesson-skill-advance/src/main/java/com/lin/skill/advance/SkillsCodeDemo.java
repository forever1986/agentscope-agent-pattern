package com.lin.skill.advance;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.hook.Hook;
import io.agentscope.core.hook.HookEvent;
import io.agentscope.core.hook.PreActingEvent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.skill.AgentSkill;
import io.agentscope.core.skill.SkillBox;
import io.agentscope.core.skill.repository.ClasspathSkillRepository;
import io.agentscope.core.tool.Toolkit;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;

public class SkillsCodeDemo {

    public static void main(String[] args) throws IOException {

        // 创建工具Toolkit
        Toolkit toolkit = new Toolkit();
        // 创建从Classpath目录获取Skill的读取器
        ClasspathSkillRepository skillRepository = new ClasspathSkillRepository("skills/code");
        // 创建skillBoox
        SkillBox skillBox = new SkillBox(toolkit);
        // 启用所有代码执行工具(Shell、读文件、写文件)
        skillBox.codeExecution()
                .withShell()
                .withRead()
                .withWrite()
                .enable();
        // 将Classpath目录下的skill读取并注册到skillBoox
        List<AgentSkill> skills = skillRepository.getAllSkills();
        for (AgentSkill skill : skills) {
            System.out.println("add skill:" + skill.getName());//打印加载到的skill
            skillBox.registration().skill(skill).apply();
        }

        // 使用一个hook来监听工具调用结果
        Hook toolHook = new Hook() {
            @Override
            public <T extends HookEvent> Mono<T> onEvent(T event) {
                if(event instanceof PreActingEvent preActingEvent){
                    System.out.println(
                            "\n[HOOK PreActingEvent] - Tool: "
                                    + preActingEvent.getToolUse().getName()
                                    + ", Input: "
                                    + preActingEvent.getToolUse().getInput());
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
        String query = """
                帮我使用100个字总结一下 E:\\workspace\\agentscope-agent-pattern\\lesson-skill-advance\\src\\main\\resources\\AgentScopeJava.pdf。
                """;
        Msg response = agent.call(Msg.builder()
                .textContent(query)
                .build())
                .block();
        // 打印输出结果
        System.out.println(response==null?null:response.getTextContent());
    }
}
