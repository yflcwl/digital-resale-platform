package com.sm.trade.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 管理端质检报告查询Mapper
 *
 * @author sm
 */
public interface AdminInspectionMapper {

    /**
     * 报告分页(含商品信息)
     */
    List<Map<String, Object>> selectReportPage(@Param("offset") long offset, @Param("limit") long limit,
                                                @Param("grade") String grade, @Param("keyword") String keyword);

    /**
     * 报告总数
     */
    long selectReportCount(@Param("grade") String grade, @Param("keyword") String keyword);

    /**
     * 报告详情
     */
    Map<String, Object> selectReportDetail(@Param("reportId") Long reportId);

    /**
     * 报告缺陷列表
     */
    List<Map<String, Object>> selectReportDefects(@Param("reportId") Long reportId);
}
