package com.zty.coze.client;

import com.agentsflex.core.llm.ChatOptions;

import java.util.Map;

/**
 * Coze智能体聊天选项
 * 
 * @author agents-flex
 */
public class CozeAgentChatOptions extends ChatOptions {

    private String userId;
    private String conversationId;
    private Map<String, String> customVariables;

    public CozeAgentChatOptions() {
    }

    public CozeAgentChatOptions(String userId) {
        this.userId = userId;
    }

    public CozeAgentChatOptions(String userId, String conversationId) {
        this.userId = userId;
        this.conversationId = conversationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public Map<String, String> getCustomVariables() {
        return customVariables;
    }

    public void setCustomVariables(Map<String, String> customVariables) {
        this.customVariables = customVariables;
    }
} 