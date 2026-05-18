//package com.lin.router;
//
//import com.alibaba.cloud.ai.graph.StateGraph;
//import com.alibaba.cloud.ai.graph.agent.flow.agent.FlowAgent;
//import com.alibaba.cloud.ai.graph.agent.flow.builder.FlowAgentBuilder;
//import com.alibaba.cloud.ai.graph.agent.flow.builder.FlowGraphBuilder;
//import com.alibaba.cloud.ai.graph.agent.flow.enums.FlowAgentEnum;
//import com.alibaba.cloud.ai.graph.exception.GraphStateException;
//import io.agentscope.core.model.Model;
//import org.springframework.ai.chat.model.ChatModel;
//
//public class ExAgentScopeRoutingAgent extends FlowAgent {
//
//    private final Model model;
//    private final ChatModel chatModel;
//    private final String fallbackAgent;
//    private final String systemPrompt;
//    private final String instruction;
//
//    protected ExAgentScopeRoutingAgent(ExAgentScopeRoutingAgentBuilder builder) {
//        super(builder.name, builder.description, builder.compileConfig, builder.subAgents,
//                builder.stateSerializer, builder.executor, builder.hooks);
//        this.model = builder.model;
//        this.chatModel = builder.chatModel;
//        this.fallbackAgent = builder.fallbackAgent;
//        this.systemPrompt = builder.systemPrompt;
//        this.instruction = builder.instruction;
//    }
//
//    public static ExAgentScopeRoutingAgentBuilder builder() {
//        return new ExAgentScopeRoutingAgentBuilder();
//    }
//
//    public Model getModel() {
//        return model;
//    }
//
//    public String getFallbackAgent() {
//        return fallbackAgent;
//    }
//
//    public String getSystemPrompt() {
//        return systemPrompt;
//    }
//
//    public String getInstruction() {
//        return instruction;
//    }
//
//
//    @Override
//    protected StateGraph buildSpecificGraph(FlowGraphBuilder.FlowGraphConfig config) throws GraphStateException {
//        config.customProperty("agentScopeModel", this.model);
//        config.setChatModel(this.chatModel);
//        return FlowGraphBuilder.buildGraph(FlowAgentEnum.ROUTING.getType(), config);
//    }
//
//    public static final class ExAgentScopeRoutingAgentBuilder extends FlowAgentBuilder<ExAgentScopeRoutingAgent, ExAgentScopeRoutingAgentBuilder> {
//
//        private Model model;
//        private ChatModel chatModel;
//        private String fallbackAgent;
//        private String systemPrompt;
//        private String instruction;
//
//        public ExAgentScopeRoutingAgent.ExAgentScopeRoutingAgentBuilder model(Model model) {
//            this.model = model;
//            return this;
//        }
//
//        public ExAgentScopeRoutingAgent.ExAgentScopeRoutingAgentBuilder chatModel(ChatModel chatModel) {
//            this.chatModel = chatModel;
//            return this;
//        }
//
//        public ExAgentScopeRoutingAgent.ExAgentScopeRoutingAgentBuilder fallbackAgent(String fallbackAgent) {
//            this.fallbackAgent = fallbackAgent;
//            return this;
//        }
//
//        public ExAgentScopeRoutingAgent.ExAgentScopeRoutingAgentBuilder systemPrompt(String systemPrompt) {
//            this.systemPrompt = systemPrompt;
//            return this;
//        }
//
//        public ExAgentScopeRoutingAgent.ExAgentScopeRoutingAgentBuilder instruction(String instruction) {
//            this.instruction = instruction;
//            return this;
//        }
//
//        @Override
//        protected ExAgentScopeRoutingAgent.ExAgentScopeRoutingAgentBuilder self() {
//            return this;
//        }
//
//        @Override
//        protected void validate() {
//            super.validate();
//            if (model == null) {
//                throw new IllegalArgumentException("AgentScope Model must be provided for AgentScope routing agent");
//            }
//
//            if (chatModel == null) {
//                throw new IllegalArgumentException("ChatModel must be provided for AgentScope routing agent");
//            }
//        }
//
//        @Override
//        public ExAgentScopeRoutingAgent doBuild() {
//            validate();
//            return new ExAgentScopeRoutingAgent(this);
//        }
//    }
//}
