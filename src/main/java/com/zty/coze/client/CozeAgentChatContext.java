package com.zty.coze.client;

import com.agentsflex.core.llm.ChatContext;
import com.agentsflex.core.message.AiMessage;

/**
 * Coze智能体聊天上下文
 * 
 * @author agents-flex
 */
public class CozeAgentChatContext extends ChatContext {

    private String id;
    private String status;
    private String response;
    private AiMessage message;
    private String conversationId;

    public CozeAgentChatContext() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public AiMessage getMessage() {
        return message;
    }

    public void setMessage(AiMessage message) {
        this.message = message;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    /**
     * 设置LLM实例
     */
    public void setLlm(CozeAgentClient llm) {
        super.setLlm(llm);
    }
} 