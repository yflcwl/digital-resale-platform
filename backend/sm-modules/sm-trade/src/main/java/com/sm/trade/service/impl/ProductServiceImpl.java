package com.sm.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sm.common.core.exception.ServiceException;
import com.sm.common.core.utils.MapstructUtils;
import com.sm.common.core.utils.StringUtils;
import com.sm.common.mybatis.core.page.PageQuery;
import com.sm.common.mybatis.core.page.TableDataInfo;
import com.sm.common.satoken.utils.LoginHelper;
import com.sm.trade.constant.ProductStatus;
import com.sm.trade.domain.Category;
import com.sm.trade.domain.Product;
import com.sm.trade.domain.ProductBrowse;
import com.sm.trade.domain.ProductFavorite;
import com.sm.trade.domain.ProductImage;
import com.sm.trade.domain.bo.ProductBo;
import com.sm.trade.domain.vo.ProductVo;
import com.sm.trade.domain.vo.SellerInfoVo;
import com.sm.trade.mapper.ProductBrowseMapper;
import com.sm.trade.mapper.ProductFavoriteMapper;
import com.sm.trade.mapper.ProductImageMapper;
import com.sm.trade.mapper.ProductMapper;
import com.sm.trade.mapper.UserProfileMapper;
import com.sm.trade.service.ICategoryService;
import com.sm.trade.service.IBrandService;
import com.sm.trade.service.IProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 商品Service业务层处理
 *
 * @author sm
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements IProductService {

    private final ProductMapper baseMapper;
    private final ProductImageMapper imageMapper;
    private final ProductFavoriteMapper favoriteMapper;
    private final ProductBrowseMapper browseMapper;
    private final UserProfileMapper userProfileMapper;
    private final ICategoryService categoryService;
    private final IBrandService brandService;

    @Override
    public TableDataInfo<ProductVo> queryOnSalePage(ProductBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Product> lqw = buildOnSaleWrapper(bo);
        // 默认最新发布在前;price 排序由前端传 orderByColumn=price&isAsc 控制(PageQuery 自动处理)
        lqw.orderByDesc(Product::getCreateTime);
        Page<ProductVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        fillCategoryBrandName(result.getRecords());
        return TableDataInfo.build(result);
    }

    private LambdaQueryWrapper<Product> buildOnSaleWrapper(ProductBo bo) {
        LambdaQueryWrapper<Product> lqw = Wrappers.lambdaQuery();
        lqw.eq(Product::getStatus, ProductStatus.ON_SALE);
        lqw.like(StringUtils.isNotBlank(bo.getTitle()), Product::getTitle, bo.getTitle());
        lqw.eq(bo.getCategoryId() != null, Product::getCategoryId, bo.getCategoryId());
        lqw.eq(bo.getBrandId() != null, Product::getBrandId, bo.getBrandId());
        lqw.eq(StringUtils.isNotBlank(bo.getConditionSelf()), Product::getConditionSelf, bo.getConditionSelf());
        lqw.eq(StringUtils.isNotBlank(bo.getQualityGrade()), Product::getQualityGrade, bo.getQualityGrade());
        lqw.ge(bo.getPriceMin() != null, Product::getPrice, bo.getPriceMin());
        lqw.le(bo.getPriceMax() != null, Product::getPrice, bo.getPriceMax());
        return lqw;
    }

    @Override
    public TableDataInfo<ProductVo> queryMyPage(ProductBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Product> lqw = Wrappers.lambdaQuery();
        lqw.eq(Product::getSellerId, LoginHelper.getUserId());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), Product::getStatus, bo.getStatus());
        lqw.like(StringUtils.isNotBlank(bo.getTitle()), Product::getTitle, bo.getTitle());
        lqw.orderByDesc(Product::getCreateTime);
        Page<ProductVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        fillCategoryBrandName(result.getRecords());
        return TableDataInfo.build(result);
    }

    @Override
    public ProductVo queryDetail(Long id) {
        Product product = baseMapper.selectById(id);
        if (product == null || ProductStatus.OFF_SHELF.equals(product.getStatus()) && !isOwner(product)) {
            throw new ServiceException("商品不存在");
        }
        ProductVo vo = MapstructUtils.convert(product, ProductVo.class);
        vo.setImages(imageMapper.selectList(Wrappers.<ProductImage>lambdaQuery()
            .eq(ProductImage::getProductId, id)
            .orderByAsc(ProductImage::getSort)));
        Category category = categoryService.selectById(product.getCategoryId());
        vo.setCategoryName(category == null ? null : category.getName());
        if (product.getBrandId() != null) {
            vo.setBrandName(brandService.selectById(product.getBrandId()) == null
                ? null : brandService.selectById(product.getBrandId()).getName());
        }
        SellerInfoVo seller = userProfileMapper.selectSellerInfo(product.getSellerId());
        if (seller != null) {
            vo.setSellerNickname(seller.getNickname());
            vo.setSellerAvatar(seller.getAvatar());
            vo.setSellerRating(seller.getSellerRating());
        }
        if (LoginHelper.isLogin()) {
            vo.setFavorite(favoriteMapper.exists(Wrappers.<ProductFavorite>lambdaQuery()
                .eq(ProductFavorite::getUserId, LoginHelper.getUserId())
                .eq(ProductFavorite::getProductId, id)));
            recordBrowse(id);
        }
        return vo;
    }

    private boolean isOwner(Product product) {
        return LoginHelper.isLogin() && LoginHelper.getUserId().equals(product.getSellerId());
    }

    private void recordBrowse(Long productId) {
        ProductBrowse browse = new ProductBrowse();
        browse.setUserId(LoginHelper.getUserId());
        browse.setProductId(productId);
        browse.setBrowseTime(new Date());
        browseMapper.insert(browse);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publish(ProductBo bo) {
        Product product = MapstructUtils.convert(bo, Product.class);
        product.setId(null);
        product.setSellerId(LoginHelper.getUserId());
        product.setStatus(ProductStatus.DRAFT);
        product.setViewCount(0);
        product.setFavoriteCount(0);
        if (baseMapper.insert(product) <= 0) {
            throw new ServiceException("发布商品失败");
        }
        saveImages(product.getId(), bo.getImages());
        return product.getId();
    }

    private void saveImages(Long productId, List<String> images) {
        if (images == null || images.isEmpty()) {
            return;
        }
        for (int i = 0; i < images.size(); i++) {
            ProductImage image = new ProductImage();
            image.setProductId(productId);
            image.setUrl(images.get(i));
            image.setSort(i);
            image.setIsMain(i == 0 ? "1" : "0");
            image.setCreateTime(new Date());
            imageMapper.insert(image);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void edit(ProductBo bo) {
        Product product = baseMapper.selectById(bo.getId());
        checkOwner(product);
        if (!ProductStatus.DRAFT.equals(product.getStatus()) && !ProductStatus.OFF_SHELF.equals(product.getStatus())) {
            throw new ServiceException("仅草稿或已下架商品可编辑");
        }
        Product update = MapstructUtils.convert(bo, Product.class);
        update.setStatus(null);
        update.setSellerId(null);
        update.setViewCount(null);
        update.setFavoriteCount(null);
        update.setLatestReportId(null);
        update.setQualityGrade(null);
        baseMapper.updateById(update);
        if (bo.getImages() != null) {
            imageMapper.delete(Wrappers.<ProductImage>lambdaQuery().eq(ProductImage::getProductId, product.getId()));
            saveImages(product.getId(), bo.getImages());
        }
    }

    private void checkOwner(Product product) {
        if (product == null) {
            throw new ServiceException("商品不存在");
        }
        if (!LoginHelper.getUserId().equals(product.getSellerId())) {
            throw new ServiceException("无权操作他人商品");
        }
    }

    @Override
    public void offShelf(Long id) {
        Product product = baseMapper.selectById(id);
        checkOwner(product);
        if (!ProductStatus.ON_SALE.equals(product.getStatus())) {
            throw new ServiceException("当前状态不允许下架");
        }
        updateStatusCas(id, ProductStatus.ON_SALE, ProductStatus.OFF_SHELF);
    }

    @Override
    public void onShelf(Long id) {
        Product product = baseMapper.selectById(id);
        checkOwner(product);
        if (!ProductStatus.OFF_SHELF.equals(product.getStatus())) {
            throw new ServiceException("当前状态不允许上架");
        }
        // TODO 质检模块上线后:校验 latestReportId 非空且报告 VALID
        updateStatusCas(id, ProductStatus.OFF_SHELF, ProductStatus.ON_SALE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        Product product = baseMapper.selectById(id);
        checkOwner(product);
        if (!ProductStatus.DRAFT.equals(product.getStatus()) && !ProductStatus.OFF_SHELF.equals(product.getStatus())) {
            throw new ServiceException("仅草稿或已下架商品可删除");
        }
        baseMapper.deleteById(id);
        imageMapper.delete(Wrappers.<ProductImage>lambdaQuery().eq(ProductImage::getProductId, id));
    }

    /**
     * 乐观CAS状态更新:WHERE id=? AND status=期望原状态
     */
    private void updateStatusCas(Long id, String from, String to) {
        LambdaUpdateWrapper<Product> uw = Wrappers.lambdaUpdate();
        uw.set(Product::getStatus, to);
        uw.eq(Product::getId, id);
        uw.eq(Product::getStatus, from);
        if (baseMapper.update(null, uw) <= 0) {
            throw new ServiceException("商品状态已变化,请刷新后重试");
        }
    }

    @Override
    public void favorite(Long productId) {
        Long userId = LoginHelper.getUserId();
        Product product = baseMapper.selectById(productId);
        if (product == null || !ProductStatus.ON_SALE.equals(product.getStatus())) {
            throw new ServiceException("商品不存在或已下架");
        }
        boolean exists = favoriteMapper.exists(Wrappers.<ProductFavorite>lambdaQuery()
            .eq(ProductFavorite::getUserId, userId)
            .eq(ProductFavorite::getProductId, productId));
        if (exists) {
            return;
        }
        ProductFavorite favorite = new ProductFavorite();
        favorite.setUserId(userId);
        favorite.setProductId(productId);
        favorite.setCreateTime(new Date());
        favoriteMapper.insert(favorite);
        incrementFavoriteCount(productId, 1);
    }

    @Override
    public void favoriteCancel(Long productId) {
        Long userId = LoginHelper.getUserId();
        int deleted = favoriteMapper.delete(Wrappers.<ProductFavorite>lambdaQuery()
            .eq(ProductFavorite::getUserId, userId)
            .eq(ProductFavorite::getProductId, productId));
        if (deleted > 0) {
            incrementFavoriteCount(productId, -1);
        }
    }

    private void incrementFavoriteCount(Long productId, int delta) {
        LambdaUpdateWrapper<Product> uw = Wrappers.lambdaUpdate();
        uw.setSql("favorite_count = favorite_count + " + delta);
        uw.eq(Product::getId, productId);
        baseMapper.update(null, uw);
    }

    @Override
    public TableDataInfo<ProductVo> queryFavoritePage(PageQuery pageQuery) {
        Long userId = LoginHelper.getUserId();
        Page<ProductFavorite> favoritePage = favoriteMapper.selectPage(pageQuery.build(),
            Wrappers.<ProductFavorite>lambdaQuery()
                .eq(ProductFavorite::getUserId, userId)
                .orderByDesc(ProductFavorite::getCreateTime));
        List<Long> productIds = favoritePage.getRecords().stream().map(ProductFavorite::getProductId).toList();
        List<ProductVo> records = List.of();
        if (!productIds.isEmpty()) {
            records = baseMapper.selectVoList(Wrappers.<Product>lambdaQuery().in(Product::getId, productIds));
            fillCategoryBrandName(records);
        }
        return new TableDataInfo<>(records, favoritePage.getTotal());
    }

    @Override
    public List<ProductVo> queryBrowseHistory() {
        Long userId = LoginHelper.getUserId();
        List<ProductBrowse> browseList = browseMapper.selectList(Wrappers.<ProductBrowse>lambdaQuery()
            .eq(ProductBrowse::getUserId, userId)
            .orderByDesc(ProductBrowse::getBrowseTime)
            .last("limit 20"));
        List<Long> productIds = browseList.stream().map(ProductBrowse::getProductId).distinct().toList();
        if (productIds.isEmpty()) {
            return List.of();
        }
        List<ProductVo> records = baseMapper.selectVoList(Wrappers.<Product>lambdaQuery().in(Product::getId, productIds));
        fillCategoryBrandName(records);
        return records;
    }

    private void fillCategoryBrandName(List<ProductVo> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        records.forEach(vo -> {
            Category category = categoryService.selectById(vo.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        });
    }
}
