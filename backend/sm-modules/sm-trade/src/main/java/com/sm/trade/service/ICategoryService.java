package com.sm.trade.service;

import com.sm.trade.domain.Category;
import com.sm.trade.domain.bo.CategoryBo;
import com.sm.trade.domain.vo.CategoryVo;

import java.util.List;

/**
 * 商品分类Service接口
 *
 * @author sm
 */
public interface ICategoryService {

    /**
     * 查询分类树(仅启用)
     */
    List<CategoryVo> selectTree();

    /**
     * 根据ID查询分类
     */
    Category selectById(Long id);
}
