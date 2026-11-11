package com.sm.trade.service;

import com.sm.common.mybatis.core.page.PageQuery;
import com.sm.common.mybatis.core.page.TableDataInfo;
import com.sm.trade.domain.vo.ReviewVo;
import com.sm.trade.domain.vo.SellerHomeVo;

/**
 * 卖家主页Service接口
 *
 * @author sm
 */
public interface ISellerService {

    /**
     * 卖家主页信息(资料 + 评分 + 在售数量)
     */
    SellerHomeVo getHome(Long sellerId);

    /**
     * 卖家收到的评价分页
     */
    TableDataInfo<ReviewVo> queryReviews(Long sellerId, PageQuery pageQuery);
}
