package com.lin.tool;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.hook.ActingChunkEvent;
import io.agentscope.core.hook.Hook;
import io.agentscope.core.hook.HookEvent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.ToolResultBlock;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolEmitter;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.core.tool.Toolkit;
import reactor.core.publisher.Mono;

public class ToolCallTypeDemo {

    public static void main(String[] args) {

        // 定义工具
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new ToolCallTypeDemo());

        // 创建智能体并内联配置模型
        ReActAgent agent = ReActAgent.builder()
                .name("Assistant")
                .sysPrompt("你是一个有帮助的 AI 助手。")
                .model(DashScopeChatModel.builder()
                        .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                        .modelName(System.getenv("QWEN_MODEL"))
                        .build())
                // 加入工具
                .toolkit(toolkit)
                // 设置Hook回调进度，关于Hook的内容后续再细讲
                .hook(new Hook(){
                    @Override
                    public <T extends HookEvent> Mono<T> onEvent(T event) {
                        if (event instanceof ActingChunkEvent) {
                            ActingChunkEvent actingChunkEvent = (ActingChunkEvent) event;
                            System.out.println("目前进度："+actingChunkEvent.getChunk().getOutput());
                        }
                        return Mono.just(event);
                    }
                })
                .build();

        // 调用call方法返回数据
        Msg response = agent.call(Msg.builder()
                .textContent("北京今天天气如何？")
                .build()).block();

        // 打印输出结果
        System.out.println(response==null?null:response.getTextContent());

        // 调用call方法返回数据
        response = agent.call(Msg.builder()
                .textContent("给我整理10份财报？")
                .build()).block();

        // 打印输出结果
        System.out.println(response==null?null:response.getTextContent());
    }

    @Tool(description = "异步获取指定城市的天气")
    public Mono<String> getWeather(
            @ToolParam(name = "city", description = "城市名称") String city) {
        System.out.println("=====执行getWeather工具=======");
        return Mono.just(city + " 的天气：晴天，25°C");
    }

    @Tool(description = "生成财报数据")
    public ToolResultBlock generate(
            @ToolParam(name = "count", description = "整理财报的数量" ) int count,
            ToolEmitter emitter) {  // 自动注入，无需 @ToolParam
        System.out.println("=====执行generate工具=======");
        for (int i = 0; i < count; i++) {
            emitter.emit(ToolResultBlock.text("进度 " + i));
        }
        return ToolResultBlock.text("完成");
    }

}
