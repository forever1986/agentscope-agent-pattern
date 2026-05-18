package com.lin.rag;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.embedding.EmbeddingModel;
import io.agentscope.core.embedding.dashscope.DashScopeMultiModalEmbedding;
import io.agentscope.core.hook.Hook;
import io.agentscope.core.hook.HookEvent;
import io.agentscope.core.hook.PreActingEvent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.rag.Knowledge;
import io.agentscope.core.rag.RAGMode;
import io.agentscope.core.rag.knowledge.SimpleKnowledge;
import io.agentscope.core.rag.model.Document;
import io.agentscope.core.rag.model.RetrieveConfig;
import io.agentscope.core.rag.reader.ReaderInput;
import io.agentscope.core.rag.reader.SplitStrategy;
import io.agentscope.core.rag.reader.TextReader;
import io.agentscope.core.rag.store.InMemoryStore;
import reactor.core.publisher.Mono;

import java.util.List;

public class RAGAgenticDemo {

    public static void main(String[] args) {
        // 创建知识库
        EmbeddingModel embeddingModel = DashScopeMultiModalEmbedding.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName("qwen3-vl-embedding")
                .dimensions(1024)
                .build();

        Knowledge knowledge = SimpleKnowledge.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(InMemoryStore.builder().dimensions(1024).build())
                .build();

        // 添加文档
        TextReader reader = new TextReader(512, SplitStrategy.PARAGRAPH, 50);
        List<Document> docs = reader.read(ReaderInput.fromString("""
        呱呱不存在公司是一家聚焦新媒体与营销服务，核心为 “号 + 店一体” 解决方案，覆盖抖音、小红书、视频号、B 站等平台，提供新媒体运营、品牌及效果营销全链路服务。
        """)).block();
        knowledge.addDocuments(docs).block();

        // 构建hook-为了显示其使用Agentic模式调用了工具
        Hook hook = new Hook() {
            @Override
            public <T extends HookEvent> Mono<T> onEvent(T event) {
                if( event instanceof PreActingEvent preActingEvent){
                    if(preActingEvent.getToolkit()!=null && preActingEvent.getToolkit().getToolNames()!=null){
                        System.out.println("================使用工具==================");
                        preActingEvent.getToolkit().getToolNames().forEach(tool -> {
                            System.out.println("工具:" + tool);
                        });
                        System.out.println("================使用工具==================");
                    }
                }
                return Mono.just(event);
            }
        };

        // 构建智能体
        ReActAgent agent = ReActAgent.builder()
                .name("助手")
                .sysPrompt("你是一个可以访问知识库的有用助手。")
                .model(DashScopeChatModel.builder()
                        .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                        .modelName(System.getenv("QWEN_MODEL"))
                        .build())
                .hook(hook)
                // 启用 Agentic RAG 模式
                .knowledge(knowledge)
                .ragMode(RAGMode.AGENTIC)
                .retrieveConfig(
                        RetrieveConfig.builder()
                                .limit(3)
                                .scoreThreshold(0.3)
                                .build())
                .build();
        // 调用call方法返回数据
        Msg response = agent.call(Msg.builder()
                .textContent("给我\"呱呱不存在公司\"这个家公司的信息")
                .build()).block();
        // 打印输出结果
        System.out.println(response==null?null:response.getTextContent());
    }
}
