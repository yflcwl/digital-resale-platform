package com.sm.trade.service;

import com.sm.common.mybatis.core.page.PageQuery;
import com.sm.common.mybatis.core.page.TableDataInfo;
import com.sm.trade.domain.Report;

/**
 * 商品举报Service接口
 *
 * @author sm
 */
public interface IReportService {

    /**
     * 用户提交举报
     */
    void submit(Long productId, String reason, String content);

    /**
     * 管理端:举报分页
     */
    TableDataInfo<Report> queryAdminPage(String status, PageQuery pageQuery);

    /**
     * 管理端:处理举报
     *
     * @param offShelf 是否同时强制下架商品
     */
    void handle(Long id, String status, String remark, boolean offShelf);
}
