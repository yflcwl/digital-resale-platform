package com.sm.inspection.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * 质检域与商品表的跨域访问Mapper(同库 SQL,不跨模块 Java 依赖)
 *
 * @author sm
 */
public interface InspectionProductMapper {

    /**
     * 查询商品关键信息
     */
    Map<String, Object> selectProductInfo(@Param("productId") Long productId);

    /**
     * 商品状态 CAS 更新
     */
    int updateProductStatusCas(@Param("productId") Long productId,
                               @Param("from") String from, @Param("to") String to);

    /**
     * 回写质检结果(成色等级 + 最新报告)
     */
    int updateProductReport(@Param("productId") Long productId,
                            @Param("grade") String grade, @Param("reportId") Long reportId);
}
