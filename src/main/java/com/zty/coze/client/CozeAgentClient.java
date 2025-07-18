package com.zty.coze.client;

import com.agentsflex.core.document.Document;
import com.agentsflex.core.llm.BaseLlm;
import com.agentsflex.core.llm.ChatOptions;
import com.agentsflex.core.llm.StreamResponseListener;
import com.agentsflex.core.llm.client.HttpClient;
import com.agentsflex.core.llm.embedding.EmbeddingOptions;
import com.agentsflex.core.llm.response.AiMessageResponse;
import com.agentsflex.core.message.AiMessage;
import com.agentsflex.core.message.Message;
import com.agentsflex.core.parser.AiMessageParser;
import com.agentsflex.core.prompt.Prompt;
import com.agentsflex.core.prompt.TextPrompt;
import com.agentsflex.core.store.VectorData;
import com.agentsflex.core.util.LogUtil;
import com.agentsflex.core.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * Coze智能体客户端
 * 支持通过robotId快速获取对应的客户端，提供完整的调用方法
 * 一个机器人对应一个客户端，客户端可以复用，每次对话时传入用户ID和会话ID
 * 
 * @author agents-flex
 */
public class CozeAgentClient extends BaseLlm<CozeAgentConfig> {

    private final HttpClient httpClient = new HttpClient();
    private final AiMessageParser aiMessageParser = CozeAgentUtil.getAiMessageParser();
    private final String robotId;

    public CozeAgentClient(CozeAgentConfig config, String robotId) {
        super(config);
        this.robotId = robotId;
        if (StringUtil.noText(robotId)) {
            throw new IllegalArgumentException("robotId不能为空");
        }
    }

    /**
     * 获取机器人ID
     */
    public String getRobotId() {
        return robotId;
    }

    /**
     * 构建请求头
     */
    private Map<String, String> buildHeader() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + config.getApiKey());
        return headers;
    }

    /**
     * 机器人对话
     */
    private void botChat(Prompt prompt, CozeAgentRequestListener listener, ChatOptions chatOptions, boolean stream) {
        String userId = null;
        String conversationId = null;
        Map<String, String> customVariables = null;

        if (chatOptions instanceof CozeAgentChatOptions) {
            CozeAgentChatOptions options = (CozeAgentChatOptions) chatOptions;
            userId = options.getUserId();
            conversationId = options.getConversationId();
            customVariables = options.getCustomVariables();
        }

        // 用户ID是必需的
        if (StringUtil.noText(userId)) {
            throw new IllegalArgumentException("用户ID不能为空，请在CozeAgentChatOptions中设置userId");
        }

        String payload = CozeAgentUtil.promptToPayload(prompt, robotId, userId, customVariables, stream);
        String url = config.getEndpoint() + config.getChatApi();
        if (StringUtil.hasText(conversationId)) {
            url += "?conversation_id=" + conversationId;
        }
        String response = httpClient.post(url, buildHeader(), payload);

        if (config.isDebug()) {
            LogUtil.println(">>>>receive payload:" + response);
        }

        // stream mode
        if (stream) {
            handleStreamResponse(response, listener);
            return;
        }

        JSONObject jsonObject = JSON.parseObject(response);
        String code = jsonObject.getString("code");
        if (!Objects.equals(code, "0")){
            throw new RuntimeException("Coze智能体对话失败，code: " + code + ", msg: " + jsonObject.getString("msg"));
        }
        String error = jsonObject.getString("msg");
        String curConversationId = JSON.parseObject(jsonObject.getString("data")).getString("conversation_id");

        CozeAgentChatContext cozeChat = jsonObject.getObject("data", (Type) CozeAgentChatContext.class);
        if (!error.isEmpty() && !Objects.equals(code, "0")) {
            if (cozeChat == null) {
                cozeChat = new CozeAgentChatContext();
                cozeChat.setLlm(this);
                cozeChat.setResponse(response);
            }
            listener.onFailure(cozeChat, new Throwable(error));
            listener.onStop(cozeChat);
            return;
        } else if (cozeChat != null) {
            cozeChat.setLlm(this);
            cozeChat.setResponse(response);
        }
        cozeChat.setConversationId(curConversationId);
        // try to check status
        int attemptCount = 0;
        boolean isCompleted = false;
        int maxAttempts = 20;
        while (attemptCount < maxAttempts && !isCompleted) {
            attemptCount++;
            try {
                cozeChat = checkStatus(cozeChat);
                listener.onMessage(cozeChat);

                isCompleted = Objects.equals(cozeChat.getStatus(), "completed");
                if (isCompleted || attemptCount == maxAttempts) {
                    listener.onStop(cozeChat);
                    break;
                }
                Thread.sleep(10000);
                System.out.println(attemptCount);
            } catch (Exception e) {
                listener.onFailure(cozeChat, e.getCause());
                listener.onStop(cozeChat);
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 处理流式响应
     */
    private void handleStreamResponse(String response, CozeAgentRequestListener listener) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(response.getBytes(Charset.defaultCharset()));
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, Charset.defaultCharset()));
        CozeAgentChatContext context = new CozeAgentChatContext();
        context.setLlm(this);

        // 记录completed消息，在处理完answer消息后再进行处理
        CozeAgentChatContext completedContext = null;

        List<AiMessage> messageList = new ArrayList<>();
        try {
            // 在处理消息前，先进行初始化，保持与其他LLM流式处理流程一致
            listener.onStart(context);

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || !line.startsWith("data:") || line.contains("[DONE]")) {
                    continue;
                }

                //remove "data:"
                line = line.substring(5);
                JSONObject data = JSON.parseObject(line);
                String status = data.getString("status");
                String type = data.getString("type");
                if ("completed".equalsIgnoreCase(status)) {
                    completedContext = JSON.parseObject(line, CozeAgentChatContext.class);
                    completedContext.setResponse(line);
                    continue;
                }
                // N 条answer，最后一条是完整的
                if ("answer".equalsIgnoreCase(type)) {
                    AiMessage message = new AiMessage();
                    message.setContent(data.getString("content"));
                    messageList.add(message);
                }
            }
            if (!messageList.isEmpty()) {
                // 删除最后一条完整的之后输出
                messageList.remove(messageList.size() - 1);
                for (AiMessage m : messageList) {
                    context.setMessage(m);
                    listener.onMessage(context);
                    Thread.sleep(10);
                }
            }

            if (completedContext != null) {
                listener.onStop(completedContext);
            }
        } catch (IOException ex) {
            listener.onFailure(context, ex.getCause());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 检查状态
     */
    private CozeAgentChatContext checkStatus(CozeAgentChatContext cozeChat) {
        String chatId = cozeChat.getId();
        String conversationId = cozeChat.getConversationId();
        String url = String.format("%s/v3/chat/retrieve?chat_id=%s", config.getEndpoint(), chatId);
        if (StringUtil.hasText(conversationId)) {
            url += "&conversation_id=" + conversationId;
        }
        String response = httpClient.get(url, buildHeader());
        JSONObject resObj = JSON.parseObject(response);
        // 需要返回最新的response信息，否则会导致调用方获取不到conversation_id等完整信息
        CozeAgentChatContext cozeChatContext = resObj.getObject("data", (Type) CozeAgentChatContext.class);
        cozeChatContext.setResponse(response);
        return cozeChatContext;
    }

    /**
     * 获取消息列表
     */
    private JSONArray fetchMessageList(CozeAgentChatContext cozeChat) {
        String chatId = cozeChat.getId();
        String conversationId = cozeChat.getConversationId();
        String endpoint = config.getEndpoint();
        String url = String.format("%s/v3/chat/message/list?chat_id=%s&conversation_id=%s", endpoint, chatId, conversationId);
        String response = httpClient.get(url, buildHeader());
        JSONObject jsonObject = JSON.parseObject(response);
        String code = jsonObject.getString("code");
        String error = jsonObject.getString("msg");
        JSONArray messageList = jsonObject.getJSONArray("data");
        if (!error.isEmpty() && !Objects.equals(code, "0")) {
            return null;
        }
        return messageList;
    }


    /**
     * 获取聊天答案
     */
    public AiMessage getChatAnswer(CozeAgentChatContext cozeChat) {
        JSONArray messageList = fetchMessageList(cozeChat);
        if (messageList == null || messageList.isEmpty()) {
            return null;
        }
        List<JSONObject> objects = messageList.stream()
                .map(JSONObject.class::cast)
                .filter(obj -> "answer".equals(obj.getString("type")))
                .collect(Collectors.toList());
        JSONObject answer = !objects.isEmpty() ? objects.get(0) : null;
        if (answer != null) {
            /*
             * coze上的工作流一个请求可以返回多条消息，需要全部返回，用3个换行符进行分隔
             * 使用3个换行符的原因：
             *   若调用方不关心多条消息，不太影响直接展示；
             *   若调用方关心多条消息，可以进行分割处理且3个换行符能减少误分隔的概率；
             */
            StringBuilder sb = new StringBuilder(answer.getString("content"));
            for (int i = 1; i < objects.size(); i++) {
                sb.append("\n\n\n").append(objects.get(i).getString("content"));
            }
            answer.put("content", sb.toString());
            return aiMessageParser.parse(answer);
        }
        return null;
    }

    @Override
    public VectorData embed(Document document, EmbeddingOptions options) {
        throw new UnsupportedOperationException("Coze智能体不支持embedding功能");
    }

    @Override
    public AiMessageResponse chat(Prompt prompt, ChatOptions options) {
        CountDownLatch latch = new CountDownLatch(1);
        Message[] messages = new Message[1];
        String[] responses = new String[1];
        Throwable[] failureThrowable = new Throwable[1];

        this.botChat(prompt, new CozeAgentRequestListener() {
            @Override
            public void onStart(CozeAgentChatContext context) {
            }

            @Override
            public void onMessage(CozeAgentChatContext context) {
                boolean isCompleted = Objects.equals(context.getStatus(), "completed");
                if (isCompleted) {
                    AiMessage answer = getChatAnswer(context);
                    messages[0] = answer;
                    responses[0] = context.getResponse();
                }
            }

            @Override
            public void onFailure(CozeAgentChatContext context, Throwable throwable) {
                failureThrowable[0] = throwable;
                responses[0] = context.getResponse();
                latch.countDown();
            }

            @Override
            public void onStop(CozeAgentChatContext context) {
                latch.countDown();
            }
        }, options, false);

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        AiMessageResponse response = new AiMessageResponse(prompt, responses[0], (AiMessage) messages[0]);

        if (messages[0] == null || failureThrowable[0] != null) {
            response.setError(true);
            if (failureThrowable[0] != null) {
                response.setErrorMessage(failureThrowable[0].getMessage());
            }
        }

        return response;
    }

    @Override
    public void chatStream(Prompt prompt, StreamResponseListener listener, ChatOptions options) {
        this.botChat(prompt, new CozeAgentRequestListener() {
            @Override
            public void onStart(CozeAgentChatContext context) {
                listener.onStart(context);
            }

            @Override
            public void onMessage(CozeAgentChatContext context) {
                AiMessageResponse response = new AiMessageResponse(prompt, context.getResponse(), context.getMessage());
                listener.onMessage(context, response);
            }

            @Override
            public void onFailure(CozeAgentChatContext context, Throwable throwable) {
                listener.onFailure(context, throwable);
            }

            @Override
            public void onStop(CozeAgentChatContext context) {
                listener.onStop(context);
            }
        }, options, true);
    }

    public String chat(String message) {
        return chat(new TextPrompt(message), null).getMessage().getContent();
    }

    public String chat(String message, ChatOptions options) {
        AiMessageResponse response = chat(new TextPrompt(message), options);
        if (response.isError()) {
            throw new RuntimeException(response.getErrorMessage());
        }
        return response.getMessage().getContent();
    }

    // 返回会话id供调用方使用
    public ResultData chatWithConversationId(String message, ChatOptions options) {
        AiMessageResponse response = chat(new TextPrompt(message), options);
        if (response.isError()) {
            throw new RuntimeException(response.getErrorMessage());
        }
        return new ResultData(JSON.parseObject(JSON.parseObject(response.getResponse()).getString("data")).getString("conversation_id"), response.getMessage().getContent());
    }

    public void chatStream(String message, StreamResponseListener listener) {
        chatStream(new TextPrompt(message), listener, null);
    }

    public void chatStream(String message, StreamResponseListener listener, ChatOptions options) {
        chatStream(new TextPrompt(message), listener, options);
    }
} 