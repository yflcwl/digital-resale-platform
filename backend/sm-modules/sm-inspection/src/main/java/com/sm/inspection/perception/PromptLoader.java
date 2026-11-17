package com.sm.inspection.perception;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 提示词加载器
 *
 * <p>从 classpath:prompts/ 目录读取 UTF-8 提示词文件,并支持 {变量名} 占位符替换。
 * 文件首次读取后缓存,修改提示词无需改代码。</p>
 *
 * @author sm
 */
public final class PromptLoader {

    private static final String BASE_PATH = "/prompts/";

    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();

    private PromptLoader() {
    }

    /**
     * 读取提示词文件(不含扩展名,如 inspection-system)
     */
    public static String load(String name) {
        return CACHE.computeIfAbsent(name, key -> {
            String path = BASE_PATH + key + ".md";
            try (InputStream in = PromptLoader.class.getResourceAsStream(path)) {
                if (in == null) {
                    throw new IllegalStateException("提示词文件不存在: classpath:" + path);
                }
                return new String(in.readAllBytes(), StandardCharsets.UTF_8).strip();
            } catch (IOException e) {
                throw new UncheckedIOException("提示词文件读取失败: classpath:" + path, e);
            }
        });
    }

    /**
     * 读取提示词并替换 {变量名} 占位符
     */
    public static String format(String name, Map<String, String> variables) {
        String template = load(name);
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            template = template.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return template;
    }
}
