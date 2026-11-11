package com.sm.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sm.common.mybatis.core.page.PageQuery;
import com.sm.common.mybatis.core.page.TableDataInfo;
import com.sm.common.satoken.utils.LoginHelper;
import com.sm.trade.constant.ProductStatus;
import com.sm.trade.domain.Product;
import com.sm.trade.domain.UserProfile;
import com.sm.trade.domain.UserAddress;
import com.sm.trade.domain.vo.ReviewVo;
import com.sm.trade.domain.vo.SellerInfoVo;
import com.sm.trade.domain.vo.SellerHomeVo;
import com.sm.trade.mapper.OrderReviewMapper;
import com.sm.trade.mapper.ProductMapper;
import com.sm.trade.mapper.UserProfileMapper;
import com.sm.trade.service.ISellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 卖家主页Service业务层处理
 *
 * @author sm
 */
@RequiredArgsConstructor
@Service
public class SellerServiceImpl implements ISellerService {

    private final UserProfileMapper userProfileMapper;
    private final ProductMapper productMapper;
    private final OrderReviewMapper reviewMapper;

    @Override
    public SellerHomeVo getHome(Long sellerId) {
        SellerHomeVo vo = new SellerHomeVo();
        vo.setUserId(sellerId);
        SellerInfoVo seller = userProfileMapper.selectSellerInfo(sellerId);
        if (seller == null) {
            vo.setNickname("卖家" + sellerId);
        } else {
            vo.setNickname(seller.getNickname());
            vo.setAvatar(seller.getAvatar());
            vo.setSellerRating(seller.getSellerRating());
            vo.setDealCount(seller.getDealCount());
        }
        UserProfile profile = userProfileMapper.selectById(sellerId);
        if (profile != null) {
            vo.setBio(profile.getBio());
            vo.setRatingCount(profile.getRatingCount());
        }
        vo.setOnSaleCount(productMapper.selectCount(Wrappers.<Product>lambdaQuery()
            .eq(Product::getSellerId, sellerId)
            .eq(Product::getStatus, ProductStatus.ON_SALE)));
        return vo;
    }

    @Override
    public TableDataInfo<ReviewVo> queryReviews(Long sellerId, PageQuery pageQuery) {
        long pageNum = pageQuery.getPageNum() == null ? 1 : pageQuery.getPageNum();
        long pageSize = pageQuery.getPageSize() == null ? 10 : pageQuery.getPageSize();
        List<ReviewVo> rows = reviewMapper.selectSellerReviews(sellerId, (pageNum - 1) * pageSize, pageSize);
        long total = reviewMapper.selectSellerReviewCount(sellerId);
        return new TableDataInfo<>(rows, total);
    }
}
