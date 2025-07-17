# 项目结构说明

```
coze-agent-client/
├── pom.xml                                    # Maven配置文件
├── README.md                                  # 项目说明文档
├── PROJECT_STRUCTURE.md                       # 项目结构说明（本文件）
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── zty/
│   │               └── coze/
│   │                   ├── client/           # 客户端相关类
│   │                   │   ├── CozeAgentClient.java          # 智能体客户端主类
│   │                   │   ├── CozeAgentConfig.java          # 配置类
│   │                   │   ├── CozeAgentChatContext.java     # 聊天上下文类
│   │                   │   ├── CozeAgentChatOptions.java     # 聊天选项类
│   │                   │   ├── CozeAgentRequestListener.java # 请求监听器接口
│   │                   │   ├── CozeAgentUtil.java            # 工具类
│   │                   │   └── ResultData.java               # 结果数据类
│   │                   ├── manager/          # 管理器相关类
│   │                   │   └── CozeAgentManager.java         # 智能体管理器
│   │                   └── example/          # 示例代码
│   │                       └── CozeAgentExample.java         # 使用示例
│   └── test/
│       └── java/
│           └── com/
│               └── zty/
│                   └── coze/
│                       └── CozeAgentManagerTest.java         # 测试类
```

## 核心类说明

### 1. CozeAgentManager（智能体管理器）
- **位置**: `src/main/java/com/zty/coze/manager/CozeAgentManager.java`
- **功能**: 批量创建和管理智能体客户端，提供缓存机制
- **主要方法**:
  - `create(String apiKey)`: 使用API Key创建管理器
  - `create(CozeAgentConfig config)`: 使用自定义配置创建管理器
  - `getClient(String robotId)`: 获取或创建智能体客户端
  - `batchCreateClients(List<String> robotIds)`: 批量创建客户端
  - `batchCreateClients(Map<String, String> robotIdToUserId)`: 批量创建客户端（指定用户ID）
  - `removeClient(String robotId)`: 移除客户端
  - `clearClients()`: 清空所有客户端
  - `getAllRobotIds()`: 获取所有机器人ID
  - `hasClient(String robotId)`: 检查客户端是否存在
  - `getClientCount()`: 获取客户端数量
  - `getBaseConfig()`: 获取基础配置

### 2. CozeAgentClient（智能体客户端）
- **位置**: `src/main/java/com/zty/coze/client/CozeAgentClient.java`
- **功能**: 与Coze智能体进行交互的核心客户端
- **主要方法**:
  - `chat(String message)`: 简单文本对话
  - `chat(String message, ChatOptions options)`: 带选项的文本对话
  - `chatWithConversationId(String message, ChatOptions options)`: 带会话ID的对话
  - `chatStream(String message, StreamResponseListener listener)`: 简单流式对话
  - `chatStream(String message, StreamResponseListener listener, ChatOptions options)`: 带选项的流式对话
  - `chat(Prompt prompt, ChatOptions options)`: 高级对话方法
  - `chatStream(Prompt prompt, StreamResponseListener listener, ChatOptions options)`: 高级流式对话方法
  - `getRobotId()`: 获取机器人ID

### 3. CozeAgentConfig（配置类）
- **位置**: `src/main/java/com/zty/coze/client/CozeAgentConfig.java`
- **功能**: 配置智能体客户端的行为
- **主要属性**:
  - `apiKey`: API密钥
  - `endpoint`: API端点（默认：https://api.coze.cn）
  - `chatApi`: 聊天API路径（默认：/v3/chat）
  - `debug`: 调试模式
  - `stream`: 流式模式
  - `model`: 模型名称
  - `apiSecret`: API密钥
  - `headersConfig`: 自定义请求头配置

### 4. CozeAgentChatOptions（聊天选项）
- **位置**: `src/main/java/com/zty/coze/client/CozeAgentChatOptions.java`
- **功能**: 自定义对话参数
- **主要属性**:
  - `userId`: 用户ID（必需）
  - `conversationId`: 会话ID（可选）
  - `customVariables`: 自定义变量
  - `temperature`: 温度参数
  - `maxTokens`: 最大令牌数
  - 其他继承自ChatOptions的属性

### 5. CozeAgentChatContext（聊天上下文）
- **位置**: `src/main/java/com/zty/coze/client/CozeAgentChatContext.java`
- **功能**: 管理聊天会话的上下文信息
- **主要属性**:
  - `conversationId`: 会话ID
  - `status`: 会话状态
  - `response`: 响应数据
  - `llm`: LLM实例

### 6. ResultData（结果数据）
- **位置**: `src/main/java/com/zty/coze/client/ResultData.java`
- **功能**: 封装对话结果和会话信息
- **主要属性**:
  - `content`: 对话内容
  - `conversationId`: 会话ID
  - `status`: 状态信息

### 7. CozeAgentUtil（工具类）
- **位置**: `src/main/java/com/zty/coze/client/CozeAgentUtil.java`
- **功能**: 提供工具方法
- **主要方法**:
  - `getAiMessageParser()`: 获取AI消息解析器
  - `promptToPayload()`: 将Prompt转换为请求载荷

### 8. CozeAgentRequestListener（请求监听器）
- **位置**: `src/main/java/com/zty/coze/client/CozeAgentRequestListener.java`
- **功能**: 监听请求状态的回调接口
- **主要方法**:
  - `onStart()`: 请求开始
  - `onMessage()`: 收到消息
  - `onStop()`: 请求结束
  - `onFailure()`: 请求失败

## 设计模式

### 1. 单例模式（Manager）
- `CozeAgentManager` 使用静态工厂方法创建实例
- 内部使用 `ConcurrentHashMap` 缓存客户端实例

### 2. 工厂模式
- `CozeAgentManager` 作为工厂类，负责创建 `CozeAgentClient` 实例
- 支持批量创建和按需创建

### 3. 策略模式
- 支持不同的配置策略（API Key、用户ID等）
- 支持不同的对话选项

### 4. 观察者模式
- `CozeAgentRequestListener` 接口用于监听请求状态
- `StreamResponseListener` 接口用于监听流式响应

### 5. 建造者模式
- 配置类支持链式调用设置参数
- 选项类支持灵活的参数配置

## 线程安全

- `CozeAgentManager` 使用 `ConcurrentHashMap` 保证线程安全
- 客户端实例的创建和获取是线程安全的
- 但单个客户端实例本身不是线程安全的
- 每个客户端都有独立的配置副本，避免配置冲突

## 扩展性

### 1. 配置扩展
- 可以通过继承 `CozeAgentConfig` 添加新的配置项
- 支持自定义端点、API路径等
- 支持自定义请求头配置

### 2. 功能扩展
- 可以通过继承 `CozeAgentClient` 添加新的功能
- 支持自定义请求处理逻辑
- 支持自定义响应处理

### 3. 监听器扩展
- 可以通过实现 `CozeAgentRequestListener` 添加自定义监听逻辑
- 支持自定义响应处理
- 支持流式响应处理

### 4. 选项扩展
- 可以通过继承 `CozeAgentChatOptions` 添加新的选项
- 支持自定义变量传递
- 支持会话管理扩展

## 依赖关系

```
CozeAgentManager
    ↓ 依赖
CozeAgentClient
    ↓ 依赖
CozeAgentConfig, CozeAgentUtil, CozeAgentRequestListener, CozeAgentChatContext
    ↓ 依赖
Agents-Flex Core (外部依赖)
    ↓ 依赖
FastJSON, OkHttp, SLF4J (外部依赖)
```

## 使用流程

1. **初始化**: 创建 `CozeAgentManager` 实例
2. **配置**: 设置API Key等配置信息
3. **创建客户端**: 通过 `getClient()` 或 `batchCreateClients()` 创建客户端
4. **使用**: 调用客户端的对话方法
5. **管理**: 根据需要移除或清空客户端

## 测试覆盖

- `CozeAgentManagerTest` 覆盖了管理器的所有主要功能
- 包括正常流程、异常情况、边界条件等
- 使用JUnit 4进行单元测试

## 版本信息

- **当前版本**: 1.0.0
- **Java版本**: 8+
- **主要依赖**:
  - Agents-Flex Core: 1.0.0
  - FastJSON: 1.2.83
  - OkHttp: 4.12.0
  - SLF4J: 1.7.36 