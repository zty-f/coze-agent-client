package com.zty.coze.client;

/**
 * Coze智能体请求监听器
 * 
 * @author agents-flex
 */
public interface CozeAgentRequestListener {

    /**
     * 开始请求
     */
    void onStart(CozeAgentChatContext context);

    /**
     * 收到消息
     */
    void onMessage(CozeAgentChatContext context);

    /**
     * 请求失败
     */
    void onFailure(CozeAgentChatContext context, Throwable throwable);

    /**
     * 请求结束
     */
    void onStop(CozeAgentChatContext context);
} 