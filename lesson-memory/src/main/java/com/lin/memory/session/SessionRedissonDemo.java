package com.lin.memory.session;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.session.Session;
import io.agentscope.core.session.SessionManager;
import io.agentscope.core.session.redis.redisson.RedissonSession;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;

public class SessionRedissonDemo {

    public static void main(String[] args) {

        // 创建 Session 并加载已有会话
        RedissonClient redissonClient = Redisson.create(); // 由于本示例连接是本地无密码的redis，默认配置即可
        Session session = new RedissonSession.Builder()
                .redissonClient(redissonClient)
                .build();

        System.out.println("========询问：推荐5部电影============");
        callAgent(session,"user-0001", "推荐5部电影！直接说出电影名称即可！");
        System.out.println("========第一次：使用不一样的sessionKey============");
        callAgent(session,"user-0002", "从刚才5部电影中，推荐一部你认为最好的！");
        System.out.println("========第二次：使用一样的sessionKey=user-0001=============");
        callAgent(session,"user-0001", "从刚才5部电影中，推荐一部你认为最好的！");

        redissonClient.shutdown();
    }

    private static void callAgent(Session session, String sessionKey, String query){
        // 创建智能体并内联配置模型
        InMemoryMemory inMemoryMemory = new InMemoryMemory();
        ReActAgent agent = ReActAgent.builder()
                .name("Assistant")
                .sysPrompt("你是一个有帮助的 AI 助手。")
                .model(DashScopeChatModel.builder()
                        .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                        .modelName(System.getenv("QWEN_MODEL"))
                        .build())
                .memory(inMemoryMemory)
                .build();
        // 加载会话（会话不存在时静默跳过）
        SessionManager.forSessionId(sessionKey)
                .withSession(session)
                .addComponent(agent)
                .addComponent(inMemoryMemory)
                .loadIfExists();
//        agent.loadIfExists(session, sessionKey);// 如果是单个内容加载，可以直接使用其本身loadIfExists方法
        // 调用call方法返回数据
        Msg response = agent.call(Msg.builder()
                .textContent(query)
                .build()).block();
        // 打印输出结果
        System.out.println(response==null?null:response.getTextContent());
        // 保存到session中
        SessionManager.forSessionId(sessionKey)
                .withSession(session)
                .addComponent(agent)
                .addComponent(inMemoryMemory)
                .saveSession();
//        agent.saveTo(session, sessionKey);// 如果是单个内容保存，可以直接使用其本身saveTo方法
    }
}
