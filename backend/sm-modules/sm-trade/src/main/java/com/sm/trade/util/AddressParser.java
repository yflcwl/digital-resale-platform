package com.sm.trade.util;

import com.sm.common.core.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 收货地址智能解析器(规则版)
 *
 * <p>支持把"姓名 + 电话 + 省市区 + 详细地址"的一段文字拆分为结构化字段,
 * 例如:张三 13800138000 四川省成都市高新区天府大道100号 / 上海市浦东新区世纪大道1号 李四 13900001111</p>
 *
 * @author sm
 */
public final class AddressParser {

    /** 手机号(11 位,1 开头) */
    private static final Pattern PHONE = Pattern.compile("1[3-9]\\d{9}");
    /** 固话(区号-号码) */
    private static final Pattern LANDLINE = Pattern.compile("0\\d{2,3}-?\\d{7,8}");
    /** 直辖市 */
    private static final Pattern MUNICIPALITY = Pattern.compile("(北京市|上海市|天津市|重庆市)");
    /** 省级行政区(省/自治区/特别行政区) */
    private static final Pattern PROVINCE = Pattern.compile("([\\u4e00-\\u9fa5]{2,10}?(?:省|自治区|特别行政区))");
    /** 地级市(市/自治州/地区/盟) */
    private static final Pattern CITY = Pattern.compile("([\\u4e00-\\u9fa5]{2,10}?(?:市|自治州|地区|盟))");
    /** 区县(区/县/旗/县级市) */
    private static final Pattern DISTRICT = Pattern.compile("([\\u4e00-\\u9fa5]{2,10}?(?:区|县|旗|市))");
    /** 姓名中不应出现的地址关键字 */
    private static final Pattern ADDRESS_WORD = Pattern.compile("(省|市|区|县|旗|镇|乡|村|路|街|道|号|栋|幢|楼|室|园|苑|巷|弄|自治区|特别行政区)");

    private AddressParser() {
    }

    /**
     * 解析结果
     */
    public record Result(String receiver, String phone, String province, String city,
                         String district, String detail) {
    }

    public static Result parse(String raw) {
        if (StringUtils.isBlank(raw)) {
            return new Result(null, null, null, null, null, null);
        }
        String text = raw.trim().replaceAll("[\\r\\n\\t]+", " ").replaceAll(" {2,}", " ");
        text = text.replaceAll("(收货人|收件人|联系人|电话|手机|地址|收货地址)[:：]", " ").trim();

        // 电话:优先手机号,其次固话
        Matcher phoneMatcher = PHONE.matcher(text);
        String phone = phoneMatcher.find() ? phoneMatcher.group() : null;
        if (phone == null) {
            Matcher landlineMatcher = LANDLINE.matcher(text);
            phone = landlineMatcher.find() ? landlineMatcher.group() : null;
        }
        String rest = phone == null ? text : text.replace(phone, " ").trim();

        // 姓名:优先取空白分隔的 2~4 位纯中文 token;否则取电话前后紧邻的 2~4 位中文
        String receiver = null;
        List<String> tokens = new ArrayList<>(List.of(rest.split("[\\s,，;；]+")));
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (token.matches("[\\u4e00-\\u9fa5]{2,4}") && !ADDRESS_WORD.matcher(token).find()) {
                receiver = token;
                tokens.remove(i);
                break;
            }
        }
        if (receiver == null && phone != null) {
            receiver = adjacentChineseName(raw, phone);
        }
        String addressText = String.join(" ", tokens).trim();
        if (receiver != null) {
            addressText = addressText.replace(receiver, " ").trim();
        }

        // 省 / 市 / 区
        String province = null;
        String city = null;
        String district = null;
        String remaining = addressText;

        Matcher municipality = MUNICIPALITY.matcher(remaining);
        if (municipality.find()) {
            province = municipality.group(1);
            city = province;
            remaining = remaining.replace(province, " ").trim();
        } else {
            Matcher provinceMatcher = PROVINCE.matcher(remaining);
            if (provinceMatcher.find()) {
                province = provinceMatcher.group(1);
                remaining = remaining.replaceFirst(Pattern.quote(province), " ").trim();
            }
            Matcher cityMatcher = CITY.matcher(remaining);
            if (cityMatcher.find()) {
                city = cityMatcher.group(1);
                remaining = remaining.replaceFirst(Pattern.quote(city), " ").trim();
            }
        }
        Matcher districtMatcher = DISTRICT.matcher(remaining);
        if (districtMatcher.find()) {
            district = districtMatcher.group(1);
            remaining = remaining.replaceFirst(Pattern.quote(district), " ").trim();
        }

        String detail = remaining.replaceAll(" {2,}", " ").trim();
        return new Result(receiver, phone, province, city, district,
            detail.isEmpty() ? null : detail);
    }

    /**
     * 无空格连写场景:在原文中取电话前/后紧邻的 2~4 位中文作为姓名
     */
    private static String adjacentChineseName(String raw, String phone) {
        int idx = raw.indexOf(phone);
        if (idx < 0) {
            return null;
        }
        String before = raw.substring(0, idx).trim();
        String after = raw.substring(idx + phone.length()).trim();
        String fromBefore = trailingChinese(before);
        if (fromBefore != null && !ADDRESS_WORD.matcher(fromBefore).find()) {
            return fromBefore;
        }
        String fromAfter = leadingChinese(after);
        if (fromAfter != null && !ADDRESS_WORD.matcher(fromAfter).find()) {
            return fromAfter;
        }
        return null;
    }

    private static String trailingChinese(String text) {
        Matcher m = Pattern.compile("([\\u4e00-\\u9fa5]{2,4})$").matcher(text);
        return m.find() ? m.group(1) : null;
    }

    private static String leadingChinese(String text) {
        Matcher m = Pattern.compile("^([\\u4e00-\\u9fa5]{2,4})").matcher(text);
        return m.find() ? m.group(1) : null;
    }
}
