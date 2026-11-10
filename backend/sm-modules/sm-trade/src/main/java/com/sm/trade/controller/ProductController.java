package com.sm.trade.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.sm.common.core.domain.R;
import com.sm.common.core.validate.AddGroup;
import com.sm.common.core.validate.EditGroup;
import com.sm.common.core.validate.QueryGroup;
import com.sm.common.idempotent.annotation.RepeatSubmit;
import com.sm.common.mybatis.core.page.PageQuery;
import com.sm.common.mybatis.core.page.TableDataInfo;
import com.sm.trade.domain.bo.ProductBo;
import com.sm.trade.domain.vo.ProductVo;
import com.sm.trade.service.IProductService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商品Controller(用户端)
 *
 * @author sm
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/trade/product")
public class ProductController {

    private final IProductService productService;

    /**
     * 在售商品分页(匿名可浏览)
     */
    @GetMapping("/page")
    public TableDataInfo<ProductVo> page(@Validated(QueryGroup.class) ProductBo bo, PageQuery pageQuery) {
        return productService.queryOnSalePage(bo, pageQuery);
    }

    /**
     * 商品详情(匿名可浏览)
     */
    @GetMapping("/{id}")
    public R<ProductVo> detail(@PathVariable @NotNull(message = "商品ID不能为空") Long id) {
        return R.ok(productService.queryDetail(id));
    }

    /**
     * 我的商品(卖家视角)
     */
    @SaCheckLogin
    @GetMapping("/my")
    public TableDataInfo<ProductVo> my(ProductBo bo, PageQuery pageQuery) {
        return productService.queryMyPage(bo, pageQuery);
    }

    /**
     * 发布商品(草稿)
     */
    @SaCheckLogin
    @RepeatSubmit
    @PostMapping
    public R<Long> publish(@Validated(AddGroup.class) @RequestBody ProductBo bo) {
        return R.ok(productService.publish(bo));
    }

    /**
     * 编辑商品
     */
    @SaCheckLogin
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ProductBo bo) {
        productService.edit(bo);
        return R.ok();
    }

    /**
     * 下架商品
     */
    @SaCheckLogin
    @PostMapping("/{id}/off-shelf")
    public R<Void> offShelf(@PathVariable @NotNull(message = "商品ID不能为空") Long id) {
        productService.offShelf(id);
        return R.ok();
    }

    /**
     * 重新上架
     */
    @SaCheckLogin
    @PostMapping("/{id}/on-shelf")
    public R<Void> onShelf(@PathVariable @NotNull(message = "商品ID不能为空") Long id) {
        productService.onShelf(id);
        return R.ok();
    }

    /**
     * 删除商品
     */
    @SaCheckLogin
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable @NotNull(message = "商品ID不能为空") Long id) {
        productService.remove(id);
        return R.ok();
    }

    /**
     * 收藏商品
     */
    @SaCheckLogin
    @PostMapping("/{id}/favorite")
    public R<Void> favorite(@PathVariable @NotNull(message = "商品ID不能为空") Long id) {
        productService.favorite(id);
        return R.ok();
    }

    /**
     * 取消收藏
     */
    @SaCheckLogin
    @DeleteMapping("/{id}/favorite")
    public R<Void> favoriteCancel(@PathVariable @NotNull(message = "商品ID不能为空") Long id) {
        productService.favoriteCancel(id);
        return R.ok();
    }

    /**
     * 我的收藏
     */
    @SaCheckLogin
    @GetMapping("/favorite/page")
    public TableDataInfo<ProductVo> favoritePage(PageQuery pageQuery) {
        return productService.queryFavoritePage(pageQuery);
    }

    /**
     * 我的浏览历史
     */
    @SaCheckLogin
    @GetMapping("/browse")
    public R<List<ProductVo>> browseHistory() {
        return R.ok(productService.queryBrowseHistory());
    }
}
