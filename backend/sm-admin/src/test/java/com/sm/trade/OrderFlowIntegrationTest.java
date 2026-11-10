package com.sm.trade;

import com.sm.common.core.domain.R;
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
 * 订单全链路集成测试
 *
 * 前置条件:
 * 1. 本地 PostgreSQL / Redis / RocketMQ 已启动
 * 2. 数据库已执行 postgres_sm.sql、postgres_sm_data.sql、postgres_sm_menu.sql
 * 3. application.yml 中 captcha.enable=false、api-decrypt.enabled=false(测试环境)
 * 4. 存在测试账号 buyer01(buyer01/admin123)与卖家 admin(admin/admin123)
 *
 * 测试链路:登录 → 浏览商品 → 建地址 → 下单(锁定商品)→ 重复下单拦截 → 支付
 * → 支付幂等 → 发货 → 收货 → 评价 → 订单完成,覆盖订单状态机全部迁移。
 *
 * 说明:测试会真实写入订单/评价/通知数据(用于毕业设计演示),重复执行时自动挑选新的在售商品。
 */
@Tag("dev")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderFlowIntegrationTest {

    private static final String CLIENT_ID = "e5cd7e4891bf95d1d19206ce24a7b32e";

    private static String buyerToken;
    private static String sellerToken;
    private static Long addressId;
    private static Long orderId;
    private static Long productId;

    @Autowired
    private TestRestTemplate rest;

    /* ---------- 辅助方法 ---------- */

    /** 若依 Jackson 将 Long 序列化为字符串,统一安全转换 */
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
        Map data = (Map) resp.getBody().get("data");
        assertNotNull(data, "登录响应 data 为空");
        return (String) data.get("access_token");
    }

    private HttpEntity<Object> json(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<Object> authJson(String token, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        headers.set("clientid", CLIENT_ID);
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<Object> authGet(String token) {
        HttpHeaders headers = new HttpHeaders();
        if (token != null) {
            headers.setBearerAuth(token);
        }
        headers.set("clientid", CLIENT_ID);
        return new HttpEntity<>(headers);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getData(String token, String url) {
        ResponseEntity<Map> resp = rest.exchange(url, HttpMethod.GET, authGet(token), Map.class);
        return (Map<String, Object>) resp.getBody().get("data");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> doPost(String token, String url, Object body) {
        ResponseEntity<Map> resp = rest.exchange(url, HttpMethod.POST, authJson(token, body), Map.class);
        return resp.getBody();
    }

    /* ---------- 测试步骤 ---------- */

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
    void browseAnonymously() {
        ResponseEntity<Map> page = rest.getForEntity("/trade/product/page?pageNum=1&pageSize=20", Map.class);
        Number total = (Number) page.getBody().get("total");
        assertTrue(total.intValue() > 0, "在售商品数量应大于 0");

        List<Map<String, Object>> rows = (List<Map<String, Object>>) page.getBody().get("rows");
        Map<String, Object> candidate = rows.stream()
            .filter(r -> !"1".equals(String.valueOf(((Map<?, ?>) r).get("sellerId"))))
            .findFirst().orElse(null);
        // 演示数据卖家均为 admin(userId=1),需选择任意在售商品下单;买家账号与卖家不同即可
        Map<String, Object> pick = candidate != null ? candidate : rows.get(0);
        productId = toLong(pick.get("id"));
        assertNotNull(pick.get("images"), "列表商品应带图片数据");

        Map<String, Object> detail = getData(null, "/trade/product/" + productId);
        assertNotNull(detail, "商品详情不应为空");
        assertEquals("ON_SALE", detail.get("status"));
    }

    @Test
    @Order(3)
    void createAddress() {
        Map<String, Object> body = Map.of(
            "receiver", "测试买家",
            "phone", "13800000001",
            "province", "四川省",
            "city", "成都市",
            "district", "高新区",
            "detail", "天府大道 100 号(集成测试)");
        Map<String, Object> resp = doPost(buyerToken, "/trade/address", body);
        assertEquals(200, ((Number) resp.get("code")).intValue());
        addressId = toLong(resp.get("data"));
        assertTrue(addressId > 0);
    }

    @Test
    @Order(4)
    void createOrderLocksProduct() {
        Map<String, Object> body = Map.of("productId", productId, "addressId", addressId);
        Map<String, Object> resp = doPost(buyerToken, "/trade/order", body);
        assertEquals(200, ((Number) resp.get("code")).intValue(), "下单应成功: " + resp.get("msg"));
        orderId = toLong(resp.get("data"));

        Map<String, Object> product = getData(buyerToken, "/trade/product/" + productId);
        assertEquals("LOCKED", product.get("status"), "下单后商品应锁定");
    }

    @Test
    @Order(5)
    void duplicateOrderRejected() {
        Map<String, Object> body = Map.of("productId", productId, "addressId", addressId);
        Map<String, Object> resp = doPost(buyerToken, "/trade/order", body);
        assertTrue(((Number) resp.get("code")).intValue() != 200, "已锁定商品重复下单应被拒绝");
    }

    @Test
    @Order(6)
    void payAndIdempotent() {
        Map<String, Object> resp = doPost(buyerToken, "/trade/order/" + orderId + "/pay?payType=ALIPAY", null);
        assertEquals(200, ((Number) resp.get("code")).intValue(), "支付应成功: " + resp.get("msg"));
        assertEquals("PAID", getData(buyerToken, "/trade/order/" + orderId).get("status"));
        assertEquals("SOLD", getData(buyerToken, "/trade/product/" + productId).get("status"), "支付后商品应为已售出");

        // 幂等:重复支付不报错、状态不变
        Map<String, Object> again = doPost(buyerToken, "/trade/order/" + orderId + "/pay?payType=ALIPAY", null);
        assertEquals(200, ((Number) again.get("code")).intValue(), "重复支付应幂等成功");
        assertEquals("PAID", getData(buyerToken, "/trade/order/" + orderId).get("status"));
    }

    @Test
    @Order(7)
    void sellerShip() {
        Map<String, Object> body = Map.of("expressCompany", "顺丰速运", "expressNo", "SF-TEST-0001");
        Map<String, Object> resp = doPost(sellerToken, "/trade/order/" + orderId + "/ship", body);
        assertEquals(200, ((Number) resp.get("code")).intValue(), "发货应成功: " + resp.get("msg"));

        Map<String, Object> order = getData(sellerToken, "/trade/order/" + orderId);
        assertEquals("SHIPPED", order.get("status"));
        assertEquals("SF-TEST-0001", order.get("expressNo"));
    }

    @Test
    @Order(8)
    void buyerReceive() {
        Map<String, Object> resp = doPost(buyerToken, "/trade/order/" + orderId + "/receive", null);
        assertEquals(200, ((Number) resp.get("code")).intValue(), "确认收货应成功: " + resp.get("msg"));
        assertEquals("RECEIVED", getData(buyerToken, "/trade/order/" + orderId).get("status"));
    }

    @Test
    @Order(9)
    @SuppressWarnings("unchecked")
    void buyerReviewFinishesOrder() {
        Map<String, Object> body = Map.of("rating", 5, "content", "集成测试:成色与描述一致,发货快");
        Map<String, Object> resp = doPost(buyerToken, "/trade/order/" + orderId + "/review", body);
        assertEquals(200, ((Number) resp.get("code")).intValue(), "评价应成功: " + resp.get("msg"));

        Map<String, Object> order = getData(buyerToken, "/trade/order/" + orderId);
        assertEquals("FINISHED", order.get("status"), "评价后订单应自动完成");
        List<Map<String, Object>> logs = (List<Map<String, Object>>) order.get("statusLogs");
        assertTrue(logs.size() >= 6, "状态流转日志应覆盖 下单→支付→发货→收货→评价→完成");
    }

    @Test
    @Order(10)
    @SuppressWarnings("unchecked")
    void orderListsForBothSides() {
        ResponseEntity<Map> buyerOrders = rest.exchange(
            "/trade/order/page?pageNum=1&pageSize=10&role=buyer", HttpMethod.GET, authGet(buyerToken), Map.class);
        assertTrue(((Number) buyerOrders.getBody().get("total")).intValue() >= 1, "买家订单列表应有数据");

        ResponseEntity<Map> sellerOrders = rest.exchange(
            "/trade/order/page?pageNum=1&pageSize=10&role=seller", HttpMethod.GET, authGet(sellerToken), Map.class);
        assertTrue(((Number) sellerOrders.getBody().get("total")).intValue() >= 1, "卖家订单列表应有数据");

        List<Map<String, Object>> rows = (List<Map<String, Object>>) sellerOrders.getBody().get("rows");
        assertNotNull(rows.get(0).get("productTitle"), "订单列表应解析出商品标题快照");
    }
}
