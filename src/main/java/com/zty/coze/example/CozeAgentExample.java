package com.zty.coze.example;

import com.zty.coze.client.CozeAgentChatOptions;
import com.zty.coze.client.CozeAgentClient;
import com.zty.coze.client.CozeAgentConfig;
import com.zty.coze.client.ResultData;
import com.zty.coze.manager.CozeAgentManager;
import com.agentsflex.core.llm.StreamResponseListener;
import com.agentsflex.core.llm.ChatContext;
import com.agentsflex.core.llm.response.AiMessageResponse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Coze智能体使用示例
 * 展示新的设计模式：客户端创建不需要用户ID，每次对话时传入用户ID和会话ID
 * 
 * @author agents-flex
 */
public class CozeAgentExample {

    public static void main(String[] args) {
        // 示例1：基本使用
        basicUsageExample();
        
        // 示例2：批量创建客户端
        // batchCreateExample();
        
        // 示例3：流式对话
        // streamChatExample();
        
        // 示例4：自定义配置
        // customConfigExample();
    }

    /**
     * 基本使用示例
     */
    public static void basicUsageExample() {
        System.out.println("=== 基本使用示例 ===");
        
        // 创建管理器（不需要指定用户ID）
        CozeAgentManager manager = CozeAgentManager.create("openKey");
        
        // 获取智能体客户端（一个机器人对应一个客户端）
        String robotId = "1";
        CozeAgentClient client = manager.getClient(robotId);
        
        // 第一次对话（不指定会话ID，系统会自动创建）
        String userId = "user123";
        CozeAgentChatOptions options = new CozeAgentChatOptions(userId);
        String response = client.chat("你好，请介绍一下你自己", options);
        System.out.println("机器人回复: " + response);

        ResultData res = client.chatWithConversationId("你好，请介绍一下你自己", options);
        System.out.println("会话id："+ res.getConversationId()+",机器人回复: "  + res.getContent());
        
        // 继续对话（使用会话ID，会话ID由调用方管理）
        String conversationId = res.getConversationId(); // 这个会话ID由调用方管理
        CozeAgentChatOptions continueOptions = new CozeAgentChatOptions(userId, conversationId);
        String response2 = client.chat("今天天气怎么样？", continueOptions);
        System.out.println("机器人回复: " + response2);
    }

    /**
     * 批量创建客户端示例
     */
    public static void batchCreateExample() {
        System.out.println("\n=== 批量创建客户端示例 ===");
        
        // 创建管理器
        CozeAgentManager manager = CozeAgentManager.create("your-api-key");
        
        // 批量创建客户端（不需要指定用户ID）
        List<String> robotIds = Arrays.asList("bot_001", "bot_002", "bot_003");
        manager.batchCreateClients(robotIds);
        
        // 使用不同的用户ID批量创建（这里只是创建客户端，用户ID在对话时使用）
        Map<String, String> robotIdToUserId = new HashMap<>();
        robotIdToUserId.put("bot_004", "user_001");
        robotIdToUserId.put("bot_005", "user_002");
        manager.batchCreateClients(robotIdToUserId);
        
        // 获取所有机器人ID
        List<String> allRobotIds = manager.getAllRobotIds();
        System.out.println("所有机器人ID: " + allRobotIds);
        System.out.println("客户端数量: " + manager.getClientCount());
        
        // 使用不同用户与不同机器人对话
        String[] userIds = {"user_001", "user_002", "user_003", "user_004", "user_005"};
        for (int i = 0; i < allRobotIds.size() && i < userIds.length; i++) {
            String robotId = allRobotIds.get(i);
            String userId = userIds[i];
            CozeAgentClient client = manager.getClient(robotId);
            
            CozeAgentChatOptions options = new CozeAgentChatOptions(userId);
            String response = client.chat("你好，我是用户" + userId, options);
            System.out.println("机器人 " + robotId + " 回复用户 " + userId + ": " + response);
        }
    }

    /**
     * 流式对话示例
     */
    public static void streamChatExample() {
        System.out.println("\n=== 流式对话示例 ===");
        
        // 创建管理器
        CozeAgentManager manager = CozeAgentManager.create("your-api-key");
        
        // 获取智能体客户端
        CozeAgentClient client = manager.getClient("bot_stream");
        
        // 流式对话
        String userId = "user_stream";
        CozeAgentChatOptions options = new CozeAgentChatOptions(userId);
        
        client.chatStream("请写一首关于春天的诗", new StreamResponseListener() {
            @Override
            public void onStart(ChatContext context) {
                System.out.println("开始流式对话...");
            }

            @Override
            public void onMessage(ChatContext context, AiMessageResponse response) {
                if (response != null && response.getMessage() != null) {
                    System.out.print(response.getMessage().getContent());
                }
            }

            @Override
            public void onStop(ChatContext context) {
                System.out.println("\n流式对话结束");
            }

            @Override
            public void onFailure(ChatContext context, Throwable throwable) {
                System.err.println("流式对话失败: " + throwable.getMessage());
            }
        }, options);
    }

    /**
     * 自定义配置示例
     */
    public static void customConfigExample() {
        System.out.println("\n=== 自定义配置示例 ===");
        
        // 创建自定义配置
        CozeAgentConfig config = new CozeAgentConfig();
        config.setApiKey("your-api-key");
        config.setEndpoint("https://api.coze.cn");
        config.setDebug(true); // 开启调试模式
        config.setStream(true); // 开启流式模式
        
        // 创建管理器
        CozeAgentManager manager = CozeAgentManager.create(config);
        
        // 获取智能体客户端
        CozeAgentClient client = manager.getClient("bot_custom");
        
        // 使用自定义变量
        String userId = "user_custom";
        CozeAgentChatOptions options = new CozeAgentChatOptions(userId);
        Map<String, String> customVariables = new HashMap<>();
        customVariables.put("location", "北京");
        customVariables.put("weather", "晴天");
        options.setCustomVariables(customVariables);
        
        String response = client.chat("告诉我今天的天气情况", options);
        System.out.println("机器人回复: " + response);
    }

    /**
     * 错误处理示例
     */
    public static void errorHandlingExample() {
        System.out.println("\n=== 错误处理示例 ===");
        
        try {
            // 创建管理器
            CozeAgentManager manager = CozeAgentManager.create("invalid-api-key");
            
            // 获取智能体客户端
            CozeAgentClient client = manager.getClient("bot_error");
            
            // 尝试对话（会失败）
            String userId = "user_error";
            CozeAgentChatOptions options = new CozeAgentChatOptions(userId);
            String response = client.chat("你好", options);
            System.out.println("机器人回复: " + response);
            
        } catch (Exception e) {
            System.err.println("对话失败: " + e.getMessage());
        }
        
        // 检查客户端是否存在
        CozeAgentManager manager = CozeAgentManager.create("your-api-key");
        String robotId = "bot_check";
        
        if (!manager.hasClient(robotId)) {
            System.out.println("客户端不存在，正在创建...");
            manager.getClient(robotId);
        }
        
        System.out.println("客户端存在: " + manager.hasClient(robotId));
        
        // 测试用户ID为空的情况
        try {
            CozeAgentClient client = manager.getClient(robotId);
            CozeAgentChatOptions options = new CozeAgentChatOptions(null);
            client.chat("测试", options);
        } catch (IllegalArgumentException e) {
            System.out.println("正确捕获错误: " + e.getMessage());
        }
    }
} 