package com.sm.inspection;

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
 * 智能质检流程集成测试(Mock 感知模式)
 *
 * 前置条件:PostgreSQL / Redis / RocketMQ 已启动;application.yml 关闭验证码与接口加密;
 * inspection.mock=true(Mock 感知,规则裁决真实执行)。
 *
 * 链路:发布商品(草稿)→ 提交质检(MQ 异步)→ Mock 感知 → 规则引擎分级定价
 * → 报告生成 → 商品自动上架;并覆盖补拍(照片不足)、重复提交守卫、报告查询与反馈。
 */
@Tag("dev")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InspectionFlowIntegrationTest {

    private static final String CLIENT_ID = "e5cd7e4891bf95d1d19206ce24a7b32e";
    private static final List<String> THREE_PHOTOS = List.of("/product/1.jpg", "/product/2.jpg", "/product/3.jpg");

    private static String token;
    private static Long productId;
    private static Long taskId;
    private static Long reportId;

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

    private HttpEntity<Object> auth(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        headers.set("clientid", CLIENT_ID);
        return new HttpEntity<>(body, headers);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> post(String url, Object body) {
        return rest.exchange(url, HttpMethod.POST, auth(body), Map.class).getBody();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> get(String url) {
        return rest.exchange(url, HttpMethod.GET, auth(null), Map.class).getBody();
    }

    /**
     * 轮询任务直到终态(SUCCESS / FAILED / NEED_RESHOOT),最多 40 秒
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> waitTask(Long taskId) throws InterruptedException {
        for (int i = 0; i < 40; i++) {
            Map<String, Object> resp = get("/inspection/task/" + taskId);
            Object dataObj = resp.get("data");
            Map<String, Object> task = dataObj instanceof Map
                ? (Map<String, Object>) ((Map<?, ?>) dataObj).get("task") : null;
            if (task != null) {
                String status = String.valueOf(task.get("status"));
                if ("SUCCESS".equals(status) || "FAILED".equals(status) || "NEED_RESHOOT".equals(status)) {
                    return resp;
                }
            }
            Thread.sleep(1000);
        }
        throw new AssertionError("质检任务轮询超时");
    }

    private Long publishProduct(String title) {
        Map<String, Object> body = Map.of(
            "title", title,
            "categoryId", 11,
            "brandName", "苹果",
            "price", 2999,
            "conditionSelf", "GOOD",
            "description", "智能质检集成测试商品",
            "images", List.of("/product/1.jpg"));
        Map<String, Object> resp = post("/trade/product", body);
        assertEquals(200, ((Number) resp.get("code")).intValue(), "发布商品应成功: " + resp.get("msg"));
        return toLong(resp.get("data"));
    }

    @Test
    @Order(1)
    void login() {
        token = login("admin", "admin123");
        assertNotNull(token);
    }

    @Test
    @Order(2)
    void submitAndAutoPass() throws InterruptedException {
        productId = publishProduct("质检流程测试-iPhone 13(自动上架)");

        Map<String, Object> submit = post("/inspection/task", Map.of("productId", productId, "photoUrls", THREE_PHOTOS));
        assertEquals(200, ((Number) submit.get("code")).intValue(), "提交质检应成功: " + submit.get("msg"));
        taskId = toLong(submit.get("data"));

        Map<String, Object> result = waitTask(taskId);
        Map<String, Object> taskData = (Map<String, Object>) result.get("data");
        Map<String, Object> task = (Map<String, Object>) taskData.get("task");
        assertEquals("SUCCESS", task.get("status"), "质检应成功: " + task.get("errorMsg"));
        reportId = toLong(taskData.get("reportId"));
        assertTrue(reportId > 0, "应生成质检报告");

        // 商品自动上架且成色回写
        Map<String, Object> product = get("/trade/product/" + productId);
        Map<String, Object> productData = (Map<String, Object>) product.get("data");
        assertEquals("ON_SALE", productData.get("status"), "质检通过后商品应自动上架");
        assertEquals("A", productData.get("qualityGrade"), "Mock 感知(扣10分)应为 A 级");
        assertEquals(reportId, toLong(productData.get("latestReportId")));
    }

    @Test
    @Order(3)
    @SuppressWarnings("unchecked")
    void reportDetailAndFeedback() {
        Map<String, Object> resp = get("/inspection/report/" + reportId);
        assertEquals(200, ((Number) resp.get("code")).intValue());
        Map<String, Object> data = (Map<String, Object>) resp.get("data");
        Map<String, Object> report = (Map<String, Object>) data.get("report");
        assertEquals("A", report.get("qualityGrade"));
        assertNotNull(report.get("priceMin"), "应给出建议价下限");
        assertNotNull(report.get("priceMax"), "应给出建议价上限");
        assertEquals("AI 辅助质检,仅供参考", report.get("disclaimer"));

        List<Map<String, Object>> defects = (List<Map<String, Object>>) data.get("defects");
        assertEquals(2, defects.size(), "Mock 感知应检出 2 处缺陷");

        // 商品报告公开接口
        Map<String, Object> productReport = get("/inspection/product/" + productId + "/report");
        assertEquals(200, ((Number) productReport.get("code")).intValue());
        assertNotNull(productReport.get("data"), "商品应能查到有效报告");

        // 反馈
        Map<String, Object> feedback = post("/inspection/report/" + reportId + "/feedback",
            Map.of("content", "集成测试:报告描述准确"));
        assertEquals(200, ((Number) feedback.get("code")).intValue(), "反馈应成功");
    }

    @Test
    @Order(4)
    void cannotResubmitAfterOnSale() {
        Map<String, Object> resp = post("/inspection/task", Map.of("productId", productId, "photoUrls", THREE_PHOTOS));
        int code = ((Number) resp.get("code")).intValue();
        assertTrue(code != 200, "已上架商品不可重复提交质检,实际 code=" + code);
    }

    @Test
    @Order(5)
    @SuppressWarnings("unchecked")
    void reshootFlow() throws InterruptedException {
        Long draftProductId = publishProduct("质检流程测试-补拍场景");

        // 仅 2 张照片 → 应转需补拍
        Map<String, Object> submit = post("/inspection/task",
            Map.of("productId", draftProductId, "photoUrls", List.of("/product/1.jpg", "/product/2.jpg")));
        assertEquals(200, ((Number) submit.get("code")).intValue(), "提交应成功: " + submit.get("msg"));
        Long reshootTaskId = toLong(submit.get("data"));

        Map<String, Object> result = waitTask(reshootTaskId);
        Map<String, Object> task = (Map<String, Object>) ((Map<String, Object>) result.get("data")).get("task");
        assertEquals("NEED_RESHOOT", task.get("status"), "照片不足应转需补拍");
        assertNotNull(task.get("errorMsg"), "应提示补拍原因");

        // 商品退回草稿
        Map<String, Object> product = get("/trade/product/" + draftProductId);
        assertEquals("DRAFT", ((Map<String, Object>) product.get("data")).get("status"), "补拍期间商品应退回草稿");

        // 补拍 3 张 → 成功
        Map<String, Object> reshoot = post("/inspection/task/" + reshootTaskId + "/reshoot",
            Map.of("photoUrls", THREE_PHOTOS));
        assertEquals(200, ((Number) reshoot.get("code")).intValue(), "补拍应成功: " + reshoot.get("msg"));
        Long newTaskId = toLong(reshoot.get("data"));

        Map<String, Object> second = waitTask(newTaskId);
        Map<String, Object> secondTask = (Map<String, Object>) ((Map<String, Object>) second.get("data")).get("task");
        assertEquals("SUCCESS", secondTask.get("status"), "补拍后应质检成功: " + secondTask.get("errorMsg"));
        assertEquals(2, toLong(secondTask.get("roundNo")), "补拍应为第 2 轮质检");
    }
}
