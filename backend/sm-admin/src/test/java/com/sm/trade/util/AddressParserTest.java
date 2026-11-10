package com.sm.trade.util;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 收货地址智能解析单元测试
 */
@Tag("dev")
class AddressParserTest {

    @Test
    void standardWithSpaces() {
        AddressParser.Result r = AddressParser.parse("张三 13800138000 四川省成都市高新区天府大道100号");
        assertEquals("张三", r.receiver());
        assertEquals("13800138000", r.phone());
        assertEquals("四川省", r.province());
        assertEquals("成都市", r.city());
        assertEquals("高新区", r.district());
        assertEquals("天府大道100号", r.detail());
    }

    @Test
    void phoneFirst() {
        AddressParser.Result r = AddressParser.parse("13800138000 李四 浙江省杭州市西湖区文三路90号 3幢201室");
        assertEquals("李四", r.receiver());
        assertEquals("13800138000", r.phone());
        assertEquals("浙江省", r.province());
        assertEquals("杭州市", r.city());
        assertEquals("西湖区", r.district());
        assertEquals("文三路90号 3幢201室", r.detail());
    }

    @Test
    void noSpaces() {
        AddressParser.Result r = AddressParser.parse("王五13900001111广东省深圳市南山区科技园路1号");
        assertEquals("王五", r.receiver());
        assertEquals("13900001111", r.phone());
        assertEquals("广东省", r.province());
        assertEquals("深圳市", r.city());
        assertEquals("南山区", r.district());
        assertEquals("科技园路1号", r.detail());
    }

    @Test
    void municipality() {
        AddressParser.Result r = AddressParser.parse("赵六 13700002222 上海市浦东新区世纪大道1号");
        assertEquals("赵六", r.receiver());
        assertEquals("上海市", r.province());
        assertEquals("上海市", r.city());
        assertEquals("浦东新区", r.district());
        assertEquals("世纪大道1号", r.detail());
    }

    @Test
    void labeledText() {
        AddressParser.Result r = AddressParser.parse("收货人:孙七 电话:13600003333 地址:江苏省南京市鼓楼区中山北路2号");
        assertEquals("孙七", r.receiver());
        assertEquals("13600003333", r.phone());
        assertEquals("江苏省", r.province());
        assertEquals("南京市", r.city());
        assertEquals("鼓楼区", r.district());
        assertEquals("中山北路2号", r.detail());
    }

    @Test
    void blankInput() {
        AddressParser.Result r = AddressParser.parse("   ");
        assertNull(r.receiver());
        assertNull(r.phone());
        assertNull(r.detail());
    }
}
