package com.sm.trade.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.sm.common.core.domain.R;
import com.sm.common.mybatis.core.page.PageQuery;
import com.sm.common.mybatis.core.page.TableDataInfo;
import com.sm.trade.domain.bo.ProductBo;
import com.sm.trade.domain.vo.ProductVo;
import com.sm.trade.domain.vo.ReviewVo;
import com.sm.trade.domain.vo.SellerHomeVo;
import com.sm.trade.service.IProductService;
import com.sm.trade.service.ISellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 卖家主页Controller(公开)
 *
 * @author sm
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/trade/seller")
public class SellerController {

    private final ISellerService sellerService;
    private final IProductService productService;

    /**
     * 卖家主页信息
     */
    @SaIgnore
    @GetMapping("/{sellerId}")
    public R<SellerHomeVo> home(@PathVariable Long sellerId) {
        return R.ok(sellerService.getHome(sellerId));
    }

    /**
     * 卖家的在售商品
     */
    @SaIgnore
    @GetMapping("/{sellerId}/products")
    public TableDataInfo<ProductVo> products(@PathVariable Long sellerId, PageQuery pageQuery) {
        ProductBo bo = new ProductBo();
        bo.setSellerId(sellerId);
        return productService.queryOnSalePage(bo, pageQuery);
    }

    /**
     * 卖家收到的评价
     */
    @SaIgnore
    @GetMapping("/{sellerId}/reviews")
    public TableDataInfo<ReviewVo> reviews(@PathVariable Long sellerId, PageQuery pageQuery) {
        return sellerService.queryReviews(sellerId, pageQuery);
    }
}
