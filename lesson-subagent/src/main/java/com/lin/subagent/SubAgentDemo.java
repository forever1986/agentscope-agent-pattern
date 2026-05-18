package com.lin.subagent;

import com.lin.subagent.core.task.TaskRepository;
import com.lin.subagent.core.tool.TaskToolsBuilder;
import com.lin.subagent.tools.ScenicSpotInfoTool;
import com.lin.subagent.tools.WeatherTool;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.tool.Toolkit;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class SubAgentDemo {

    public static void main(String[] args) throws IOException {

        DashScopeChatModel model = DashScopeChatModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName(System.getenv("QWEN_MODEL"))
                .stream(false)
                .build();

        // dependency-analyzer (only programmatic sub-agent)
        Toolkit depToolkit = new Toolkit();
        depToolkit.registerTool(new WeatherTool());
        ReActAgent weatherAnalyzerReAct =
                ReActAgent.builder()
                        .name("出门穿搭推荐助手")
                        .description("你是一个出门搭配分析助手，可以调用 getWeather 工具获取当前的天气情况，然后给出一个出门穿搭建议。")
                        .model(model)
                        .sysPrompt("你是一个出门搭配分析助手，可以调用 getWeather 工具获取当前的天气情况，然后给出一个出门穿搭建议。")
                        .toolkit(depToolkit)
                        .memory(new InMemoryMemory())
                        .build();

        // 将weatherAnalyzerReAct放入TaskToolsBuilder

        Map<String, Object> defaultToolsByName =
                Map.of(
                        "scenic_spot_info", new ScenicSpotInfoTool());
        TaskToolsBuilder builder =
                TaskToolsBuilder.builder()
                        .model(model)
                        .subAgent("出门穿搭推荐助手", weatherAnalyzerReAct)
                        .defaultToolsByName(defaultToolsByName)
                        .taskRepository(
                                new TaskRepository());
        // 将md文件注册为Agent，放到TaskToolsBuilder
        URL resource = ClassLoader.getSystemResource("agents");
        File directory = new File(resource.getPath());
        List<File> fileList = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(directory.toPath())) {
            stream.filter(Files::isRegularFile) // 只保留普通文件
                    .filter(path -> path.toString().toLowerCase().endsWith(".md")) // 过滤 .md 后缀
                    .forEach(path -> {
                        fileList.add(path.toFile());
                    });
        }
        for (File file : fileList) {
            builder.addAgentResource(file);
        }
        TaskToolsBuilder.TaskToolsResult taskToolsResult = builder.build();

        // 创建主Agent的Toolkit
        Toolkit orchestratorToolkit = new Toolkit();
        orchestratorToolkit.registerTool(taskToolsResult.taskTool());
        orchestratorToolkit.registerTool(taskToolsResult.taskOutputTool());

        ReActAgent orchestratorReActAgent =
                ReActAgent.builder()
                        .name("聊天助手")
                        .description("你是一个聊天助手，如果遇到用户穿搭和景点规划问题，可以委托给“出门穿搭推荐助手”、“旅游规划助手”的子Agent进行处理，最后你归纳总结。")
                        .model(model)
                        .sysPrompt("你是一个聊天助手，如果遇到用户穿搭和景点规划问题，可以委托给“出门穿搭推荐助手”、“旅游规划助手”的子Agent进行处理，最后你归纳总结。")
                        .toolkit(orchestratorToolkit)
                        .memory(new InMemoryMemory())
                        .build();

        // 主智能体会在需要时自动调用专家智能体
        Msg response = orchestratorReActAgent.call(Msg.builder().textContent("今天去广州越秀公园参观，应该穿什么？有什么景点可以逛？").build())
                .block();


        // 打印输出结果
        System.out.println("==================回复的信息===========================");
        System.out.println(response==null?null:response.getTextContent());

    }
}
