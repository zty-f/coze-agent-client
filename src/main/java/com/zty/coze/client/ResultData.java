package com.zty.coze.client;

public class ResultData {
    private String conversationId;
    private String content;

    public ResultData(String conversationId, String content) {
        this.conversationId = conversationId;
        this.content = content;
    }

    public String getConversationId() {
        return conversationId;
    }

    public String getContent() {
        return content;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
