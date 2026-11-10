package com.sm.admin;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
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
 * 管理后台集成测试
 *
 * 前置条件同其余集成测试(测试环境需关闭验证码与接口加密)。
 * 覆盖:仪表盘统计、商品管理(强制下架/恢复)、分类增删、品牌字典、订单查询、质检报告查阅、普通用户越权拦截。
 */
@Tag("dev")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdminFlowIntegrationTest {

    private static final String CLIENT_ID = "e5cd7e4891bf95d1d19206ce24a7b32e";

    private static String adminToken;
    private static String userToken;
    private static Long tempBrandId;
    private static Long offShelfProductId;

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
    private Map<String, Object> call(String token, HttpMethod method, String url, Object body) {
        ResponseEntity<Map> resp = rest.exchange(url, method, auth(token, body), Map.class);
        return resp.getBody();
    }

    @Test
    @Order(1)
    void login() {
        adminToken = login("admin", "admin123");
        userToken = login("buyer01", "admin123");
        assertNotNull(adminToken);
        assertNotNull(userToken);
    }

    @Test
    @Order(2)
    @SuppressWarnings("unchecked")
    void dashboardHasData() {
        Map<String, Object> resp = call(adminToken, HttpMethod.GET, "/admin/dashboard", null);
        assertEquals(200, ((Number) resp.get("code")).intValue(), "仪表盘应可访问: " + resp.get("msg"));
        Map<String, Object> data = (Map<String, Object>) resp.get("data");
        Map<String, Object> overview = (Map<String, Object>) data.get("overview");
        assertTrue(((Number) overview.get("userCount")).intValue() > 0);
        assertTrue(((Number) overview.get("productCount")).intValue() > 0);
        assertTrue(((Number) overview.get("orderCount")).intValue() > 0);
        assertTrue(((Number) overview.get("reportCount")).intValue() > 0);
        assertTrue(((List<?>) data.get("orderTrend")).size() == 7, "趋势应返回 7 天");
        assertTrue(((List<?>) data.get("orderStatusDist")).size() > 0);
    }

    @Test
    @Order(3)
    @SuppressWarnings("unchecked")
    void productManageOffShelfAndRestore() {
        Map<String, Object> page = call(adminToken, HttpMethod.GET, "/admin/product/page?pageNum=1&pageSize=20&status=ON_SALE", null);
        List<Map<String, Object>> rows = (List<Map<String, Object>>) page.get("rows");
        assertNotNull(rows);
        assertTrue(rows.size() > 0, "应有在售商品");
        // 选择 admin 自己的在售商品(下架后 owner 仍可查看详情并恢复上架)
        Map<String, Object> pick = rows.stream()
            .filter(r -> "1".equals(String.valueOf(r.get("sellerId"))))
            .reduce((first, second) -> second)
            .orElse(rows.get(rows.size() - 1));
        offShelfProductId = toLong(pick.get("id"));

        Map<String, Object> resp = call(adminToken, HttpMethod.POST,
            "/admin/product/" + offShelfProductId + "/off-shelf", null);
        assertEquals(200, ((Number) resp.get("code")).intValue(), "强制下架应成功: " + resp.get("msg"));

        // 卖家侧商品状态应为下架
        Map<String, Object> detail = call(adminToken, HttpMethod.GET, "/trade/product/" + offShelfProductId, null);
        assertEquals("OFF_SHELF", ((Map<String, Object>) detail.get("data")).get("status"));

        // 恢复上架(admin 为该商品 owner)
        Map<String, Object> back = call(adminToken, HttpMethod.POST,
            "/trade/product/" + offShelfProductId + "/on-shelf", null);
        assertEquals(200, ((Number) back.get("code")).intValue(), "重新上架应成功: " + back.get("msg"));
    }

    @Test
    @Order(4)
    void categoryAndBrandDict() {
        // 分类树
        Map<String, Object> tree = call(adminToken, HttpMethod.GET, "/admin/category/tree", null);
        assertTrue(((List<?>) tree.get("data")).size() > 0, "分类树应有数据");

        // 品牌已迁移至字典管理(系统管理 -> 字典管理 -> product_brand)
        Map<String, Object> brandDict = call(adminToken, HttpMethod.GET, "/system/dict/data/type/product_brand", null);
        assertEquals(200, ((Number) brandDict.get("code")).intValue(), "品牌字典应可访问: " + brandDict.get("msg"));
        assertTrue(((List<?>) brandDict.get("data")).size() > 0, "品牌字典应有数据");
    }

    @Test
    @Order(5)
    void orderAndReportQuery() {
        Map<String, Object> orders = call(adminToken, HttpMethod.GET, "/admin/order/page?pageNum=1&pageSize=5", null);
        assertTrue(((Number) orders.get("total")).intValue() > 0, "订单管理应有数据");

        Map<String, Object> reports = call(adminToken, HttpMethod.GET, "/admin/inspection/report/page?pageNum=1&pageSize=5", null);
        assertTrue(((Number) reports.get("total")).intValue() > 0, "质检报告应有数据");

        List<?> rows = (List<?>) reports.get("rows");
        Long reportId = toLong(((Map<?, ?>) rows.get(0)).get("id"));
        Map<String, Object> detail = call(adminToken, HttpMethod.GET, "/admin/inspection/report/" + reportId, null);
        assertEquals(200, ((Number) detail.get("code")).intValue(), "报告详情应可访问");
        assertNotNull(((Map<?, ?>) detail.get("data")).get("defects"), "报告详情应包含缺陷列表");
    }

    @Test
    @Order(6)
    void normalUserForbidden() {
        Map<String, Object> resp = call(userToken, HttpMethod.GET, "/admin/dashboard", null);
        int code = ((Number) resp.get("code")).intValue();
        assertTrue(code == 403 || code == 500, "普通用户访问管理端应被拒绝,实际 code=" + code);
    }
}