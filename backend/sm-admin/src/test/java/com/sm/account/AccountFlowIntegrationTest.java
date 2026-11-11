package com.sm.account;

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
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 账户闭环集成测试
 *
 * 前置条件同其余集成测试(captcha.enable=false、api-decrypt.enabled=false)。
 * 覆盖:用户注册(自动绑定普通用户角色/登录后可见菜单)、找回密码(邮箱验证码重置)、卖家主页公开访问。
 */
@Tag("dev")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AccountFlowIntegrationTest {

    private static final String CLIENT_ID = "e5cd7e4891bf95d1d19206ce24a7b32e";

    @Autowired
    private TestRestTemplate rest;

    private final String randomUserName = "it_user_" + ThreadLocalRandom.current().nextInt(100000, 999999);

    private HttpEntity<Object> json(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("clientid", CLIENT_ID);
        return new HttpEntity<>(body, headers);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> post(String url, Object body) {
        return rest.postForEntity(url, json(body), Map.class).getBody();
    }

    private String login(String username, String password) {
        Map<String, Object> resp = post("/auth/login", Map.of(
            "clientId", CLIENT_ID, "grantType", "password", "username", username, "password", password));
        Map<?, ?> data = (Map<?, ?>) resp.get("data");
        assertNotNull(data, "登录响应 data 为空: " + resp.get("msg"));
        return (String) data.get("access_token");
    }

    @Test
    @Order(1)
    @SuppressWarnings("unchecked")
    void registerAndLoginWithMenus() {
        // 注册(测试环境关闭验证码)
        Map<String, Object> register = post("/auth/register", Map.of(
            "clientId", CLIENT_ID,
            "grantType", "password",
            "username", randomUserName,
            "password", "test123456",
            "userType", "sys_user"));
        assertEquals(200, ((Number) register.get("code")).intValue(), "注册应成功: " + register.get("msg"));

        // 新用户可登录
        String token = login(randomUserName, "test123456");
        assertNotNull(token);

        // 自动绑定普通用户角色 → getRouters 返回用户端菜单
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.set("clientid", CLIENT_ID);
        ResponseEntity<Map> menus = rest.exchange("/system/menu/getRouters", HttpMethod.GET,
            new HttpEntity<>(headers), Map.class);
        List<?> data = (List<?>) menus.getBody().get("data");
        assertNotNull(data, "菜单响应 data 为空");
        assertTrue(data.size() > 0, "注册用户应能看到用户端菜单(角色绑定生效)");
    }

    @Test
    @Order(2)
    @SuppressWarnings("unchecked")
    void forgotPasswordFlow() {
        // 发送验证码(本地无邮件配置时返回验证码)
        Map<String, Object> send = post("/auth/forgot/send-code", Map.of("email", "buyer01@test.com"));
        assertEquals(200, ((Number) send.get("code")).intValue(), "发送验证码应成功: " + send.get("msg"));
        String code = (String) send.get("data");
        assertNotNull(code, "本地演示模式应返回验证码,响应=" + send);

        // 重置密码
        Map<String, Object> reset = post("/auth/forgot/reset", Map.of(
            "email", "buyer01@test.com", "code", code, "password", "newpass123"));
        assertEquals(200, ((Number) reset.get("code")).intValue(), "重置密码应成功: " + reset.get("msg"));

        // 新密码可登录
        String token = login("buyer01", "newpass123");
        assertNotNull(token);

        // 还原原密码,避免影响其他测试
        Map<String, Object> send2 = post("/auth/forgot/send-code", Map.of("email", "buyer01@test.com"));
        String code2 = (String) send2.get("data");
        Map<String, Object> reset2 = post("/auth/forgot/reset", Map.of(
            "email", "buyer01@test.com", "code", code2, "password", "admin123"));
        assertEquals(200, ((Number) reset2.get("code")).intValue(), "还原密码应成功");
    }

    @Test
    @Order(3)
    @SuppressWarnings("unchecked")
    void sellerHomePublic() {
        Map<String, Object> home = rest.getForEntity("/trade/seller/1", Map.class).getBody();
        assertEquals(200, ((Number) home.get("code")).intValue(), "卖家主页应可匿名访问");
        Map<String, Object> data = (Map<String, Object>) home.get("data");
        assertNotNull(data.get("nickname"), "卖家昵称应有值");

        Map<String, Object> products = rest.getForEntity(
            "/trade/seller/1/products?pageNum=1&pageSize=5", Map.class).getBody();
        assertTrue(((Number) products.get("total")).intValue() > 0, "卖家在售商品应大于 0");

        Map<String, Object> reviews = rest.getForEntity(
            "/trade/seller/1/reviews?pageNum=1&pageSize=5", Map.class).getBody();
        assertTrue(((Number) reviews.get("total")).intValue() >= 0, "评价分页应可访问");
    }
}
