package com.lin.skill;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.skill.AgentSkill;
import io.agentscope.core.skill.SkillBox;
import io.agentscope.core.skill.repository.ClasspathSkillRepository;
import io.agentscope.core.tool.Toolkit;

import java.io.IOException;
import java.util.List;

public class SkillsSimpleDemo {

    public static void main(String[] args) throws IOException {

        // 创建工具Toolkit
        Toolkit toolkit = new Toolkit();
        // 创建从Classpath目录获取Skill的读取器
        ClasspathSkillRepository skillRepository = new ClasspathSkillRepository("skills");
        // 创建skillBoox
        SkillBox skillBox = new SkillBox(toolkit);
        // 将Classpath目录下的skill读取并注册到skillBoox
        List<AgentSkill> skills = skillRepository.getAllSkills();
        for (AgentSkill skill : skills) {
            System.out.println("add skill:" + skill.getName());//打印加载到的skill
            skillBox.registration().skill(skill).apply();
        }

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
