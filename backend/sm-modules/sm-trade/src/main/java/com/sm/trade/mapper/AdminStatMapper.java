package com.sm.trade.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 管理端统计Mapper(跨表统计 SQL)
 *
 * @author sm
 */
public interface AdminStatMapper {

    /**
     * 概览卡片统计
     */
    Map<String, Object> selectOverview();

    /**
     * 近 7 天订单趋势(含今天)
     */
    List<Map<String, Object>> selectOrderTrend7d();

    /**
     * 订单状态分布
     */
    List<Map<String, Object>> selectOrderStatusDist();

    /**
     * 分类商品分布(Top 8)
     */
    List<Map<String, Object>> selectCategoryProductDist(@Param("limit") int limit);
}
