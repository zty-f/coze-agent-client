package com.zty.coze.client;

import com.agentsflex.core.message.Message;
import com.agentsflex.core.message.MessageStatus;
import com.agentsflex.core.parser.AiMessageParser;
import com.agentsflex.core.parser.impl.DefaultAiMessageParser;
import com.agentsflex.core.prompt.DefaultPromptFormat;
import com.agentsflex.core.prompt.Prompt;
import com.agentsflex.core.prompt.PromptFormat;
import com.agentsflex.core.util.Maps;
import com.alibaba.fastjson.JSONPath;

import java.util.List;
import java.util.Map;

/**
 * Coze智能体工具类
 * 
 * @author agents-flex
 */
public class CozeAgentUtil {

    private static final PromptFormat promptFormat = new DefaultPromptFormat() {
        @Override
        protected void buildMessageContent(Message message, Map<String, Object> map) {
            map.put("content_type", "text");
            super.buildMessageContent(message, map);
        }
    };

    /**
     * 获取AI消息解析器
     */
    public static AiMessageParser getAiMessageParser() {
        DefaultAiMessageParser aiMessageParser = new DefaultAiMessageParser();
        aiMessageParser.setContentPath("$.content");
        aiMessageParser.setTotalTokensPath("$.usage.token_count");
        aiMessageParser.setCompletionTokensPath("$.usage.output_count");
        aiMessageParser.setPromptTokensPath("$.usage.input_count");

        aiMessageParser.setStatusParser(content -> {
            Boolean done = (Boolean) JSONPath.eval(content, "$.done");
            if (done != null && done) {
                return MessageStatus.END;
            }
            return MessageStatus.MIDDLE;
        });
        return aiMessageParser;
    }

    /**
     * 将Prompt转换为请求载荷
     */
    public static String promptToPayload(Prompt prompt, String botId, String userId, Map<String, String> customVariables, boolean stream) {
        List<Message> messages = prompt.toMessages();
        return Maps.of()
            .set("bot_id", botId)
            .set("user_id", userId)
            .set("auto_save_history", true)
            .set("additional_messages", promptFormat.toMessagesJsonObject(messages))
            .set("stream", stream)
            .setIf(customVariables != null, "custom_variables", customVariables)
            .toJSON();
    }
} 