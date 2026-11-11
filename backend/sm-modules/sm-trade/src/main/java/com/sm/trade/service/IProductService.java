package com.sm.trade.service;

import com.sm.common.mybatis.core.page.PageQuery;
import com.sm.common.mybatis.core.page.TableDataInfo;
import com.sm.trade.domain.bo.ProductBo;
import com.sm.trade.domain.vo.ProductVo;

import java.util.List;

/**
 * 商品Service接口
 *
 * @author sm
 */
public interface IProductService {

    /**
     * 在售商品分页列表(支持关键词/分类/品牌/价格/成色筛选与排序)
     */
    TableDataInfo<ProductVo> queryOnSalePage(ProductBo bo, PageQuery pageQuery);

    /**
     * 我的商品分页列表(卖家视角,全部状态)
     */
    TableDataInfo<ProductVo> queryMyPage(ProductBo bo, PageQuery pageQuery);

    /**
     * 商品详情(含图片/卖家信息,浏览+1)
     */
    ProductVo queryDetail(Long id);

    /**
     * 发布商品(草稿)
     */
    Long publish(ProductBo bo);

    /**
     * 编辑商品(仅草稿/已下架)
     */
    void edit(ProductBo bo);

    /**
     * 下架商品(在售状态)
     */
    void offShelf(Long id);

    /**
     * 重新上架(已下架状态)
     */
    void onShelf(Long id);

    /**
     * 删除商品(仅草稿/已下架)
     */
    void remove(Long id);

    /**
     * 收藏商品
     */
    void favorite(Long productId);

    /**
     * 取消收藏
     */
    void favoriteCancel(Long productId);

    /**
     * 我的收藏分页
     */
    TableDataInfo<ProductVo> queryFavoritePage(PageQuery pageQuery);

    /**
     * 我的浏览历史(最近20条)
     */
    List<ProductVo> queryBrowseHistory();

    /**
     * 管理端:商品分页(全部状态)
     */
    TableDataInfo<ProductVo> adminPage(ProductBo bo, PageQuery pageQuery);

    /**
     * 管理端:强制下架(仅在售商品)
     */
    void adminOffShelf(Long id);

    /**
     * 管理端:删除商品(仅草稿/已下架)
     */
    void adminRemove(Long id);

    /**
     * 首页精选商品(在售 + is_featured=1)
     */
    List<ProductVo> selectFeatured(int limit);

    /**
     * 管理端:批量设置/取消首页精选
     *
     * @return 实际更新行数
     */
    int adminSetFeatured(List<Long> ids, boolean featured);
}
