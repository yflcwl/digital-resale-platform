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

    /**
     * 批量查询分类名称(id → name)
     */
    java.util.Map<Long, String> selectNameMap(java.util.Collection<Long> ids);

    /**
     * 管理端:分类树(含停用)
     */
    List<CategoryVo> selectAdminTree();

    /**
     * 管理端:新增分类
     */
    Long add(CategoryBo bo);

    /**
     * 管理端:修改分类
     */
    void edit(CategoryBo bo);

    /**
     * 管理端:删除分类(存在子分类或商品引用时禁止)
     */
    void remove(Long id);
}
