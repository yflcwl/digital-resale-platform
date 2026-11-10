package com.sm.trade.service;

import com.sm.trade.domain.Brand;
import com.sm.trade.domain.vo.BrandVo;

import java.util.List;

/**
 * 品牌Service接口
 *
 * @author sm
 */
public interface IBrandService {

    /**
     * 查询启用品牌列表
     *
     * @param categoryId 分类ID(可选,按分类-品牌关联过滤)
     */
    List<BrandVo> selectList(Long categoryId);

    /**
     * 根据ID查询品牌
     */
    Brand selectById(Long id);
}
