# Coze Agent Client

一个独立的Maven模块，用于批量生成Coze智能体客户端，支持通过robotId快速获取对应的客户端，提供完整的调用方法。

## 设计理念

- **一个机器人对应一个客户端**: 每个机器人ID对应一个客户端实例，客户端可以复用
- **用户ID和会话ID在对话时传入**: 客户端创建时不需要指定用户ID，每次对话时传入用户ID和会话ID
- **会话ID由调用方管理**: 客户端不记录用户和机器人的会话ID，由调用方自行处理
- **批量创建支持**: 支持批量创建多个客户端，提高效率

## 功能特性

- ✅ **批量生成客户端**: 支持批量创建多个智能体客户端
- ✅ **快速获取**: 通过robotId快速获取对应的客户端
- ✅ **完整调用方法**: 提供同步、异步、流式等多种调用方式
- ✅ **配置灵活**: 支持自定义配置和选项
- ✅ **线程安全**: 使用ConcurrentHashMap保证线程安全
- ✅ **易于集成**: 独立的Maven模块，易于集成到现有项目
- ✅ **会话管理**: 支持会话ID的获取和管理
- ✅ **自定义变量**: 支持在对话中传入自定义变量

## Maven依赖

```xml
<dependency>
    <groupId>io.github.zty-f</groupId>
    <artifactId>coze-agent-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 快速开始

### 1. 基本使用

```java
// 创建管理器（不需要指定用户ID）
CozeAgentManager manager = CozeAgentManager.create("your-api-key");

// 获取智能体客户端（一个机器人对应一个客户端）
String robotId = "bot_123456";
CozeAgentClient client = manager.getClient(robotId);

// 第一次对话（不指定会话ID，系统会自动创建）
String userId = "user123";
CozeAgentChatOptions options = new CozeAgentChatOptions(userId);
        String response = client.chat("你好，请介绍一下你自己", options);
        System.out.println("机器人回复: " + response);

        // 获取会话ID的对话方式
        ResultData result = client.chatWithConversationId("你好，请介绍一下你自己", options);
        System.out.println("会话ID: " + result.getConversationId() + ", 回复: " + result.getContent());

        // 继续对话（使用会话ID，会话ID由调用方管理）
        String conversationId = result.getConversationId(); // 这个会话ID由调用方管理
        CozeAgentChatOptions continueOptions = new CozeAgentChatOptions(userId, conversationId);
        String response2 = client.chat("今天天气怎么样？", continueOptions);
        System.out.println("机器人回复: " + response2);
```

### 2. 批量创建客户端

```java
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
```

### 3. 流式对话

```java
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
```

### 4. 自定义配置

```java
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
```

## API文档

### CozeAgentManager

管理器类，负责批量创建和管理智能体客户端。

#### 主要方法

- `static CozeAgentManager create(String apiKey)`: 使用API Key创建管理器
- `static CozeAgentManager create(CozeAgentConfig config)`: 使用自定义配置创建管理器
- `CozeAgentClient getClient(String robotId)`: 获取或创建智能体客户端
- `void batchCreateClients(List<String> robotIds)`: 批量创建客户端
- `void batchCreateClients(Map<String, String> robotIdToUserId)`: 批量创建客户端（指定用户ID）
- `void removeClient(String robotId)`: 移除客户端
- `void clearClients()`: 清空所有客户端
- `List<String> getAllRobotIds()`: 获取所有机器人ID
- `boolean hasClient(String robotId)`: 检查客户端是否存在
- `int getClientCount()`: 获取客户端数量
- `CozeAgentConfig getBaseConfig()`: 获取基础配置

### CozeAgentClient

智能体客户端类，提供与Coze智能体交互的方法。

#### 主要方法

- `String chat(String message, ChatOptions options)`: 带选项的文本对话
- `String chat(String message)`: 简单文本对话
- `String chat(String message, ChatOptions options)`: 带选项的文本对话
- `ResultData chatWithConversationId(String message, ChatOptions options)`: 带会话ID的对话
- `void chatStream(String message, StreamResponseListener listener)`: 简单流式对话
- `void chatStream(String message, StreamResponseListener listener, ChatOptions options)`: 带选项的流式对话
- `AiMessageResponse chat(Prompt prompt, ChatOptions options)`: 高级对话方法
- `void chatStream(Prompt prompt, StreamResponseListener listener, ChatOptions options)`: 高级流式对话方法
- `String getRobotId()`: 获取机器人ID

### CozeAgentConfig

配置类，用于配置智能体客户端的行为。

#### 主要属性

- `apiKey`: API密钥
- `endpoint`: API端点（默认：https://api.coze.cn）
- `chatApi`: 聊天API路径（默认：/v3/chat）
- `debug`: 是否开启调试模式
- `stream`: 是否使用流式模式
- `model`: 模型名称
- `apiSecret`: API密钥
- `headersConfig`: 自定义请求头配置

### CozeAgentChatOptions

聊天选项类，用于自定义对话参数。

#### 主要属性

- `userId`: 用户ID（必需）
- `conversationId`: 会话ID（可选，不指定时系统自动创建）
- `customVariables`: 自定义变量
- `temperature`: 温度参数
- `maxTokens`: 最大令牌数
- 其他继承自ChatOptions的属性

### ResultData

结果数据类，包含对话结果和会话信息。

#### 主要属性

- `content`: 对话内容
- `conversationId`: 会话ID
- `status`: 状态信息

## 使用场景

1. **多机器人管理**: 当需要管理多个不同的Coze智能体时
2. **批量处理**: 需要同时与多个智能体进行对话
3. **动态创建**: 根据业务需求动态创建智能体客户端
4. **性能优化**: 通过缓存机制避免重复创建客户端
5. **配置管理**: 统一管理多个智能体的配置
6. **会话管理**: 需要管理用户会话状态的应用（会话ID由调用方管理）
7. **自定义变量**: 需要在对话中传入特定上下文信息的场景

## 设计优势

1. **客户端复用**: 一个机器人对应一个客户端，避免重复创建
2. **灵活的用户管理**: 每次对话时指定用户ID，支持多用户场景
3. **会话ID自主管理**: 调用方可以完全控制会话ID的管理策略
4. **批量创建支持**: 支持批量创建多个客户端，提高效率
5. **线程安全**: 管理器使用ConcurrentHashMap保证线程安全
6. **配置隔离**: 每个客户端都有独立的配置副本，避免配置冲突

## 注意事项

1. **API Key安全**: 请妥善保管您的API Key，不要将其硬编码在代码中
2. **用户ID必需**: 每次对话时必须提供用户ID
3. **会话ID可选**: 不提供会话ID时系统会自动创建新会话
4. **会话ID管理**: 会话ID由调用方自行管理，客户端不记录会话状态
5. **错误处理**: 建议在使用时添加适当的错误处理机制
6. **资源管理**: 长时间不使用的客户端建议及时清理
7. **并发安全**: 管理器是线程安全的，但客户端本身不是线程安全的
8. **网络异常**: 网络异常时会有相应的异常抛出，请做好异常处理
9. **配置克隆**: 每个客户端都会克隆基础配置，避免配置冲突

## 示例代码

完整的示例代码请参考 `src/main/java/com/zty/coze/example/CozeAgentExample.java`

## 许可证

Apache License 2.0

## 贡献

欢迎提交Issue和Pull Request来改进这个项目。 