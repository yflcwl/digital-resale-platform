package com.sm.trade.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.sm.common.core.domain.R;
import com.sm.common.idempotent.annotation.RepeatSubmit;
import com.sm.common.mybatis.core.page.PageQuery;
import com.sm.common.mybatis.core.page.TableDataInfo;
import com.sm.trade.domain.bo.OrderBo;
import com.sm.trade.domain.vo.OrderVo;
import com.sm.trade.service.IOrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单Controller(用户端)
 *
 * @author sm
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/trade/order")
public class OrderController {

    private final IOrderService orderService;

    /**
     * 创建订单(锁定商品)
     */
    @SaCheckLogin
    @RepeatSubmit
    @PostMapping
    public R<Long> create(@Valid @RequestBody OrderBo bo) {
        return R.ok(orderService.create(bo));
    }

    /**
     * 模拟支付
     */
    @SaCheckLogin
    @PostMapping("/{id}/pay")
    public R<Void> pay(@PathVariable @NotNull(message = "订单ID不能为空") Long id,
                       @RequestParam(required = false) String payType) {
        orderService.pay(id, payType);
        return R.ok();
    }

    /**
     * 买家取消订单
     */
    @SaCheckLogin
    @PostMapping("/{id}/cancel")
    public R<Void> cancel(@PathVariable @NotNull(message = "订单ID不能为空") Long id) {
        orderService.cancel(id);
        return R.ok();
    }

    /**
     * 卖家发货
     */
    @SaCheckLogin
    @PostMapping("/{id}/ship")
    public R<Void> ship(@PathVariable @NotNull(message = "订单ID不能为空") Long id,
                        @RequestBody OrderBo bo) {
        orderService.ship(id, bo.getExpressCompany(), bo.getExpressNo());
        return R.ok();
    }

    /**
     * 买家确认收货
     */
    @SaCheckLogin
    @PostMapping("/{id}/receive")
    public R<Void> receive(@PathVariable @NotNull(message = "订单ID不能为空") Long id) {
        orderService.receive(id);
        return R.ok();
    }

    /**
     * 买家评价
     */
    @SaCheckLogin
    @PostMapping("/{id}/review")
    public R<Void> review(@PathVariable @NotNull(message = "订单ID不能为空") Long id,
                          @RequestBody OrderBo bo) {
        orderService.review(id, bo.getRating(), bo.getContent());
        return R.ok();
    }

    /**
     * 我的订单分页(role=buyer 我买到的 / seller 我卖出的)
     */
    @SaCheckLogin
    @GetMapping("/page")
    public TableDataInfo<OrderVo> page(OrderBo bo, PageQuery pageQuery,
                                       @RequestParam(defaultValue = "buyer") String role) {
        return orderService.queryMyPage(bo, pageQuery, role);
    }

    /**
     * 订单详情(含状态流转)
     */
    @SaCheckLogin
    @GetMapping("/{id}")
    public R<OrderVo> detail(@PathVariable @NotNull(message = "订单ID不能为空") Long id) {
        return R.ok(orderService.queryDetail(id));
    }
}
