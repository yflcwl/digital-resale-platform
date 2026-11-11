package com.sm.trade.mapper;

import com.sm.common.mybatis.core.mapper.BaseMapperPlus;
import com.sm.trade.domain.OrderReview;
import com.sm.trade.domain.vo.ReviewVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 交易评价Mapper接口
 *
 * @author sm
 */
public interface OrderReviewMapper extends BaseMapperPlus<OrderReview, OrderReview> {

    /**
     * 卖家收到的评价分页(含评价人昵称/头像)
     */
    List<ReviewVo> selectSellerReviews(@Param("sellerId") Long sellerId,
                                       @Param("offset") long offset, @Param("limit") long limit);

    /**
     * 卖家评价总数
     */
    long selectSellerReviewCount(@Param("sellerId") Long sellerId);
}
