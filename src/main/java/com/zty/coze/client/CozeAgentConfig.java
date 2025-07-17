package com.zty.coze.client;

import com.agentsflex.core.llm.LlmConfig;

/**
 * Coze智能体配置
 * 
 * @author agents-flex
 */
public class CozeAgentConfig extends LlmConfig {

    private final String DEFAULT_CHAT_API = "/v3/chat";
    private final String DEFAULT_ENDPOINT = "https://api.coze.cn";
    private String chatApi;

    private boolean stream;

    public CozeAgentConfig() {
        this.setChatApi(DEFAULT_CHAT_API);
        this.setEndpoint(DEFAULT_ENDPOINT);
    }

    public void setChatApi(String chatApi) {
        this.chatApi = chatApi;
    }

    public String getChatApi() {
        return chatApi;
    }

    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }

    /**
     * 是否开启调试模式
     */
    public boolean isDebug() {
        return super.isDebug();
    }

    /**
     * 设置调试模式
     */
    public void setDebug(boolean debug) {
        super.setDebug(debug);
    }
} 