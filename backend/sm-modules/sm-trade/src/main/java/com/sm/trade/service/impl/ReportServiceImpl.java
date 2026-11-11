package com.sm.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sm.common.core.exception.ServiceException;
import com.sm.common.core.utils.StringUtils;
import com.sm.common.mybatis.core.page.PageQuery;
import com.sm.common.mybatis.core.page.TableDataInfo;
import com.sm.common.satoken.utils.LoginHelper;
import com.sm.trade.constant.ProductStatus;
import com.sm.trade.domain.Product;
import com.sm.trade.domain.Report;
import com.sm.trade.mapper.ProductMapper;
import com.sm.trade.mapper.ReportMapper;
import com.sm.trade.service.IReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 商品举报Service业务层处理
 *
 * @author sm
 */
@RequiredArgsConstructor
@Service
public class ReportServiceImpl implements IReportService {

    private final ReportMapper baseMapper;
    private final ProductMapper productMapper;

    @Override
    public void submit(Long productId, String reason, String content) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new ServiceException("商品不存在");
        }
        if (LoginHelper.getUserId().equals(product.getSellerId())) {
            throw new ServiceException("不能举报自己的商品");
        }
        if (StringUtils.isBlank(reason)) {
            throw new ServiceException("请选择举报原因");
        }
        long exists = baseMapper.selectCount(Wrappers.<Report>lambdaQuery()
            .eq(Report::getReporterId, LoginHelper.getUserId())
            .eq(Report::getProductId, productId)
            .eq(Report::getStatus, "PENDING"));
        if (exists > 0) {
            throw new ServiceException("你已举报过该商品,请等待处理");
        }
        Report report = new Report();
        report.setReporterId(LoginHelper.getUserId());
        report.setProductId(productId);
        report.setReason(reason);
        report.setContent(content);
        report.setStatus("PENDING");
        report.setCreateTime(new Date());
        baseMapper.insert(report);
    }

    @Override
    public TableDataInfo<Report> queryAdminPage(String status, PageQuery pageQuery) {
        return TableDataInfo.build(baseMapper.selectPage(pageQuery.build(),
            Wrappers.<Report>lambdaQuery()
                .eq(StringUtils.isNotBlank(status), Report::getStatus, status)
                .orderByAsc(Report::getStatus)
                .orderByDesc(Report::getCreateTime)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handle(Long id, String status, String remark, boolean offShelf) {
        if (!"HANDLED".equals(status) && !"REJECTED".equals(status)) {
            throw new ServiceException("处理状态不合法");
        }
        Report report = baseMapper.selectById(id);
        if (report == null) {
            throw new ServiceException("举报记录不存在");
        }
        if (!"PENDING".equals(report.getStatus())) {
            throw new ServiceException("该举报已处理");
        }
        baseMapper.update(null, Wrappers.<Report>lambdaUpdate()
            .set(Report::getStatus, status)
            .set(Report::getHandlerId, LoginHelper.getUserId())
            .set(Report::getHandleRemark, remark)
            .set(Report::getHandleTime, new Date())
            .eq(Report::getId, id)
            .eq(Report::getStatus, "PENDING"));
        // 处理为违规且勾选下架时,强制下架商品(仅限在售)
        if (offShelf && "HANDLED".equals(status)) {
            LambdaUpdateWrapper<Product> uw = Wrappers.lambdaUpdate();
            uw.set(Product::getStatus, ProductStatus.OFF_SHELF);
            uw.eq(Product::getId, report.getProductId());
            uw.eq(Product::getStatus, ProductStatus.ON_SALE);
            productMapper.update(null, uw);
        }
    }
}
