package com.sm.trade.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.sm.common.core.domain.R;
import com.sm.common.mybatis.core.page.PageQuery;
import com.sm.common.mybatis.core.page.TableDataInfo;
import com.sm.trade.domain.bo.OrderBo;
import com.sm.trade.domain.vo.OrderVo;
import com.sm.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理后台:订单管理(只读)
 *
 * @author sm
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/order")
@SaCheckRole("superadmin")
public class AdminOrderController {

    private final IOrderService orderService;

    /**
     * 订单分页(全部)
     */
    @GetMapping("/page")
    public TableDataInfo<OrderVo> page(OrderBo bo, PageQuery pageQuery) {
        return orderService.adminPage(bo, pageQuery);
    }

    /**
     * 订单详情(含状态流转)
     */
    @GetMapping("/{id}")
    public R<OrderVo> detail(@PathVariable Long id) {
        return R.ok(orderService.queryDetail(id));
    }
}
