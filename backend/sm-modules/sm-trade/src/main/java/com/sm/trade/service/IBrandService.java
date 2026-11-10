package com.sm.trade.service;

import com.sm.trade.domain.Brand;
import com.sm.trade.domain.bo.BrandBo;
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

    /**
     * 管理端:新增品牌
     */
    Long add(BrandBo bo);

    /**
     * 管理端:修改品牌
     */
    void edit(BrandBo bo);

    /**
     * 管理端:删除品牌(存在商品引用时禁止)
     */
    void remove(Long id);
}
