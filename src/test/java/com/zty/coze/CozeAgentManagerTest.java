package com.zty.coze;

import com.zty.coze.client.CozeAgentClient;
import com.zty.coze.client.CozeAgentConfig;
import com.zty.coze.manager.CozeAgentManager;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * CozeAgentManager测试类
 * 
 * @author agents-flex
 */
public class CozeAgentManagerTest {

    // @Test
    // public void testCreateManagerWithApiKey() {
    //     CozeAgentManager manager = CozeAgentManager.create("test-api-key");
    //     assertNotNull(manager);
    //     assertEquals("test-api-key", manager.getBaseConfig().getApiKey());
    // }
    //
    // @Test
    // public void testCreateManagerWithConfig() {
    //     CozeAgentConfig config = new CozeAgentConfig();
    //     config.setApiKey("test-api-key");
    //     config.setDebug(true);
    //
    //     CozeAgentManager manager = CozeAgentManager.create(config);
    //     assertNotNull(manager);
    //     assertEquals("test-api-key", manager.getBaseConfig().getApiKey());
    //     assertTrue(manager.getBaseConfig().isDebug());
    // }
    //
    // @Test
    // public void testGetClient() {
    //     CozeAgentManager manager = CozeAgentManager.create("test-api-key");
    //
    //     String robotId = "test-bot-001";
    //     CozeAgentClient client = manager.getClient(robotId);
    //
    //     assertNotNull(client);
    //     assertEquals(robotId, client.getRobotId());
    //     assertEquals(1, manager.getClientCount());
    // }
    //
    // @Test
    // public void testGetClientTwice() {
    //     CozeAgentManager manager = CozeAgentManager.create("test-api-key");
    //
    //     String robotId = "test-bot-002";
    //     CozeAgentClient client1 = manager.getClient(robotId);
    //     CozeAgentClient client2 = manager.getClient(robotId);
    //
    //     assertSame(client1, client2); // 应该返回同一个实例
    //     assertEquals(1, manager.getClientCount());
    // }
    //
    // @Test
    // public void testBatchCreateClients() {
    //     CozeAgentManager manager = CozeAgentManager.create("test-api-key");
    //
    //     List<String> robotIds = Arrays.asList("bot-001", "bot-002", "bot-003");
    //     manager.batchCreateClients(robotIds);
    //
    //     assertEquals(3, manager.getClientCount());
    //     assertTrue(manager.hasClient("bot-001"));
    //     assertTrue(manager.hasClient("bot-002"));
    //     assertTrue(manager.hasClient("bot-003"));
    //
    //     List<String> allRobotIds = manager.getAllRobotIds();
    //     assertEquals(3, allRobotIds.size());
    //     assertTrue(allRobotIds.containsAll(robotIds));
    // }
    //
    // @Test
    // public void testBatchCreateClientsWithUserId() {
    //     CozeAgentManager manager = CozeAgentManager.create("test-api-key");
    //
    //     Map<String, String> robotIdToUserId = new HashMap<>();
    //     robotIdToUserId.put("bot-004", "user-001");
    //     robotIdToUserId.put("bot-005", "user-002");
    //
    //     manager.batchCreateClients(robotIdToUserId);
    //
    //     assertEquals(2, manager.getClientCount());
    //     assertTrue(manager.hasClient("bot-004"));
    //     assertTrue(manager.hasClient("bot-005"));
    // }
    //
    // @Test
    // public void testRemoveClient() {
    //     CozeAgentManager manager = CozeAgentManager.create("test-api-key");
    //
    //     String robotId = "test-bot-003";
    //     manager.getClient(robotId);
    //     assertEquals(1, manager.getClientCount());
    //
    //     manager.removeClient(robotId);
    //     assertEquals(0, manager.getClientCount());
    //     assertFalse(manager.hasClient(robotId));
    // }
    //
    // @Test
    // public void testClearClients() {
    //     CozeAgentManager manager = CozeAgentManager.create("test-api-key");
    //
    //     List<String> robotIds = Arrays.asList("bot-001", "bot-002", "bot-003");
    //     manager.batchCreateClients(robotIds);
    //     assertEquals(3, manager.getClientCount());
    //
    //     manager.clearClients();
    //     assertEquals(0, manager.getClientCount());
    //     assertTrue(manager.getAllRobotIds().isEmpty());
    // }
    //
    // @Test(expected = IllegalArgumentException.class)
    // public void testGetClientWithNullRobotId() {
    //     CozeAgentManager manager = CozeAgentManager.create("test-api-key");
    //     manager.getClient(null);
    // }
    //
    // @Test(expected = IllegalArgumentException.class)
    // public void testGetClientWithEmptyRobotId() {
    //     CozeAgentManager manager = CozeAgentManager.create("test-api-key");
    //     manager.getClient("");
    // }
    //
    // @Test
    // public void testBatchCreateClientsWithEmptyList() {
    //     CozeAgentManager manager = CozeAgentManager.create("test-api-key");
    //     manager.batchCreateClients(Arrays.asList());
    //     assertEquals(0, manager.getClientCount());
    // }
    //
    // @Test
    // public void testBatchCreateClientsWithNullList() {
    //     CozeAgentManager manager = CozeAgentManager.create("test-api-key");
    //     manager.batchCreateClients((List<String>) null);
    //     assertEquals(0, manager.getClientCount());
    // }
    //
    // @Test
    // public void testBatchCreateClientsWithEmptyMap() {
    //     CozeAgentManager manager = CozeAgentManager.create("test-api-key");
    //     manager.batchCreateClients(new HashMap<>());
    //     assertEquals(0, manager.getClientCount());
    // }
    //
    // @Test
    // public void testBatchCreateClientsWithNullMap() {
    //     CozeAgentManager manager = CozeAgentManager.create("test-api-key");
    //     manager.batchCreateClients((Map<String, String>) null);
    //     assertEquals(0, manager.getClientCount());
    // }
    //
    // @Test
    // public void testRemoveClientWithNullRobotId() {
    //     CozeAgentManager manager = CozeAgentManager.create("test-api-key");
    //     manager.removeClient(null); // 不应该抛出异常
    //     assertEquals(0, manager.getClientCount());
    // }
    //
    // @Test
    // public void testRemoveClientWithEmptyRobotId() {
    //     CozeAgentManager manager = CozeAgentManager.create("test-api-key");
    //     manager.removeClient(""); // 不应该抛出异常
    //     assertEquals(0, manager.getClientCount());
    // }
    //
    // @Test
    // public void testHasClientWithNullRobotId() {
    //     CozeAgentManager manager = CozeAgentManager.create("test-api-key");
    //     assertFalse(manager.hasClient(null));
    // }
    //
    // @Test
    // public void testHasClientWithEmptyRobotId() {
    //     CozeAgentManager manager = CozeAgentManager.create("test-api-key");
    //     assertFalse(manager.hasClient(""));
    // }
} 