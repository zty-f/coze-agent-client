package com.zty.coze.manager;

import com.zty.coze.client.CozeAgentClient;
import com.zty.coze.client.CozeAgentConfig;
import com.agentsflex.core.util.StringUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Coze智能体管理器
 * 支持批量生成智能体客户端，并通过robotId快速获取对应的客户端
 * 一个机器人对应一个客户端，客户端可以复用
 * 
 * @author agents-flex
 */
public class CozeAgentManager {

    private static final Map<String, CozeAgentClient> clientCache = new ConcurrentHashMap<>();
    private final CozeAgentConfig baseConfig;

    public CozeAgentManager(CozeAgentConfig baseConfig) {
        this.baseConfig = baseConfig;
    }

    /**
     * 创建管理器实例
     */
    public static CozeAgentManager create(CozeAgentConfig config) {
        return new CozeAgentManager(config);
    }

    /**
     * 创建管理器实例（使用默认配置）
     */
    public static CozeAgentManager create(String apiKey) {
        CozeAgentConfig config = new CozeAgentConfig();
        config.setApiKey(apiKey);
        return new CozeAgentManager(config);
    }

    /**
     * 获取或创建智能体客户端
     */
    public CozeAgentClient getClient(String robotId) {
        if (StringUtil.noText(robotId)) {
            throw new IllegalArgumentException("robotId不能为空");
        }

        return clientCache.computeIfAbsent(robotId, this::createClient);
    }

    /**
     * 创建智能体客户端
     */
    private CozeAgentClient createClient(String robotId) {
        // 为每个机器人创建独立的配置副本
        CozeAgentConfig config = cloneConfig(baseConfig);
        return new CozeAgentClient(config, robotId);
    }

    /**
     * 批量创建智能体客户端
     */
    public void batchCreateClients(List<String> robotIds) {
        if (robotIds == null || robotIds.isEmpty()) {
            return;
        }

        for (String robotId : robotIds) {
            if (StringUtil.hasText(robotId)) {
                getClient(robotId);
            }
        }
    }

    /**
     * 批量创建智能体客户端（使用不同的用户ID）
     */
    public void batchCreateClients(Map<String, String> robotIdToUserId) {
        if (robotIdToUserId == null || robotIdToUserId.isEmpty()) {
            return;
        }

        for (Map.Entry<String, String> entry : robotIdToUserId.entrySet()) {
            String robotId = entry.getKey();
            String userId = entry.getValue();
            
            if (StringUtil.hasText(robotId)) {
                // 创建客户端（用户ID在对话时传入，这里只是创建客户端）
                getClient(robotId);
            }
        }
    }

    /**
     * 移除智能体客户端
     */
    public void removeClient(String robotId) {
        if (StringUtil.hasText(robotId)) {
            clientCache.remove(robotId);
        }
    }

    /**
     * 清空所有客户端缓存
     */
    public void clearClients() {
        clientCache.clear();
    }

    /**
     * 获取所有已创建的机器人ID
     */
    public List<String> getAllRobotIds() {
        return new java.util.ArrayList<>(clientCache.keySet());
    }

    /**
     * 检查是否已存在指定机器人ID的客户端
     */
    public boolean hasClient(String robotId) {
        return StringUtil.hasText(robotId) && clientCache.containsKey(robotId);
    }

    /**
     * 获取客户端数量
     */
    public int getClientCount() {
        return clientCache.size();
    }

    /**
     * 克隆配置
     */
    private CozeAgentConfig cloneConfig(CozeAgentConfig original) {
        CozeAgentConfig cloned = new CozeAgentConfig();
        cloned.setApiKey(original.getApiKey());
        cloned.setEndpoint(original.getEndpoint());
        cloned.setChatApi(original.getChatApi());
        cloned.setStream(original.isStream());
        cloned.setDebug(original.isDebug());
        cloned.setModel(original.getModel());
        cloned.setApiSecret(original.getApiSecret());
        cloned.setHeadersConfig(original.getHeadersConfig());
        return cloned;
    }

    /**
     * 获取基础配置
     */
    public CozeAgentConfig getBaseConfig() {
        return baseConfig;
    }
} 