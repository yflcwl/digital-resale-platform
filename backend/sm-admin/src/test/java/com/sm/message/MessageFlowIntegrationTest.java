package com.sm.message;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 站内聊天 + 通知 集成测试
 *
 * 前置条件(与 OrderFlowIntegrationTest 相同):
 * 1. 本地 PostgreSQL / Redis 已启动(WebSocket 推送依赖 Redis,离线可降级为仅落库)
 * 2. application.yml 中 captcha.enable=false、api-decrypt.enabled=false(测试环境)
 * 3. 存在账号 buyer01(buyer01/admin123)与卖家 admin(admin/admin123)
 *
 * 链路:买家发起会话 → 发消息 → 卖家未读 → 卖家查看并回复 → 买家未读 → 已读 → 通知中心
 */
@Tag("dev")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MessageFlowIntegrationTest {

    private static final String CLIENT_ID = "e5cd7e4891bf95d1d19206ce24a7b32e";

    private static String buyerToken;
    private static String sellerToken;
    private static Long conversationId;
    private static Long productId;

    @Autowired
    private TestRestTemplate rest;

    private static Long toLong(Object v) {
        return v == null ? null : Long.parseLong(String.valueOf(v));
    }

    private String login(String username, String password) {
        Map<String, String> body = Map.of(
            "clientId", CLIENT_ID,
            "grantType", "password",
            "username", username,
            "password", password);
        ResponseEntity<Map> resp = rest.postForEntity("/auth/login", json(body), Map.class);
        Map<?, ?> data = (Map<?, ?>) resp.getBody().get("data");
        assertNotNull(data, "登录响应 data 为空");
        return (String) data.get("access_token");
    }

    private HttpEntity<Object> json(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<Object> auth(String token, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        headers.set("clientid", CLIENT_ID);
        return new HttpEntity<>(body, headers);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> doPost(String token, String url, Object body) {
        ResponseEntity<Map> resp = rest.exchange(url, HttpMethod.POST, auth(token, body), Map.class);
        return resp.getBody();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> doGet(String token, String url) {
        ResponseEntity<Map> resp = rest.exchange(url, HttpMethod.GET, auth(token, null), Map.class);
        return resp.getBody();
    }

    @Test
    @Order(1)
    void loginBoth() {
        buyerToken = login("buyer01", "admin123");
        sellerToken = login("admin", "admin123");
        assertNotNull(buyerToken);
        assertNotNull(sellerToken);
    }

    @Test
    @Order(2)
    @SuppressWarnings("unchecked")
    void openConversationFromProduct() {
        // 找一个 admin 的在售商品
        ResponseEntity<Map> page = rest.getForEntity("/trade/product/page?pageNum=1&pageSize=20", Map.class);
        List<Map<String, Object>> rows = (List<Map<String, Object>>) page.getBody().get("rows");
        assertNotNull(rows);
        assertTrue(rows.size() > 0, "演示数据中应有在售商品");
        Map<String, Object> adminProduct = rows.stream()
            .filter(r -> "1".equals(String.valueOf(r.get("sellerId"))))
            .findFirst()
            .orElse(rows.get(0));
        productId = toLong(adminProduct.get("id"));

        Map<String, Object> resp = doPost(buyerToken, "/message/chat/conversation", Map.of("productId", productId));
        assertEquals(200, ((Number) resp.get("code")).intValue(), "发起会话应成功: " + resp.get("msg"));
        conversationId = toLong(resp.get("data"));
        assertTrue(conversationId > 0);

        // 重复发起返回同一会话
        Map<String, Object> again = doPost(buyerToken, "/message/chat/conversation", Map.of("productId", productId));
        assertEquals(conversationId, toLong(again.get("data")), "重复发起应复用同一会话");
    }

    @Test
    @Order(3)
    void buyerSendMessage() {
        Map<String, Object> resp = doPost(buyerToken,
            "/message/chat/conversation/" + conversationId + "/send",
            Map.of("msgType", "TEXT", "content", "你好,这个商品还在吗?"));
        assertEquals(200, ((Number) resp.get("code")).intValue(), "发送消息应成功: " + resp.get("msg"));
    }

    @Test
    @Order(4)
    @SuppressWarnings("unchecked")
    void sellerSeesUnreadAndReplies() {
        Map<String, Object> listResp = doGet(sellerToken, "/message/chat/conversation/list");
        List<Map<String, Object>> conversations = (List<Map<String, Object>>) listResp.get("data");
        Map<String, Object> target = conversations.stream()
            .filter(c -> toLong(c.get("id")).equals(conversationId))
            .findFirst().orElse(null);
        assertNotNull(target, "卖家应能看到该会话");
        assertTrue(((Number) target.get("unread")).intValue() >= 1, "卖家应有未读消息");
        assertNotNull(target.get("otherNickname"), "会话应带对方昵称");
        assertNotNull(target.get("productTitle"), "会话应带商品标题");

        // 卖家回复
        Map<String, Object> resp = doPost(sellerToken,
            "/message/chat/conversation/" + conversationId + "/send",
            Map.of("msgType", "TEXT", "content", "在的,成色很好,可以聊"));
        assertEquals(200, ((Number) resp.get("code")).intValue(), "卖家回复应成功");

        // 卖家已读
        doPost(sellerToken, "/message/chat/conversation/" + conversationId + "/read", null);
        int sellerUnread = ((Number) doGet(sellerToken, "/message/chat/unread-count").get("data")).intValue();
        assertEquals(0, sellerUnread, "卖家已读后未读应为 0");

        // 买家现在有 1 条未读
        int buyerUnread = ((Number) doGet(buyerToken, "/message/chat/unread-count").get("data")).intValue();
        assertTrue(buyerUnread >= 1, "买家应收到卖家的消息未读");
    }

    @Test
    @Order(5)
    @SuppressWarnings("unchecked")
    void messageHistory() {
        Map<String, Object> resp = doGet(buyerToken, "/message/chat/conversation/" + conversationId + "/messages?pageNum=1&pageSize=20");
        assertEquals(200, ((Number) resp.get("code")).intValue());
        List<Map<String, Object>> messages = (List<Map<String, Object>>) resp.get("data");
        assertTrue(messages.size() >= 2, "应至少有两轮消息");

        // 已读
        doPost(buyerToken, "/message/chat/conversation/" + conversationId + "/read", null);
        int buyerUnread = ((Number) doGet(buyerToken, "/message/chat/unread-count").get("data")).intValue();
        assertEquals(0, buyerUnread, "买家已读后未读应为 0");
    }

    @Test
    @Order(6)
    @SuppressWarnings("unchecked")
    void notificationCenter() {
        Map<String, Object> page = doGet(sellerToken, "/message/notification/page?pageNum=1&pageSize=10");
        Number total = (Number) page.get("total");
        assertTrue(total.intValue() >= 1, "卖家应有通知数据");

        int unread = ((Number) doGet(sellerToken, "/message/notification/unread-count").get("data")).intValue();
        assertTrue(unread >= 0);

        doPost(sellerToken, "/message/notification/read-all", null);
        int after = ((Number) doGet(sellerToken, "/message/notification/unread-count").get("data")).intValue();
        assertEquals(0, after, "全部已读后未读应为 0");
    }
}
