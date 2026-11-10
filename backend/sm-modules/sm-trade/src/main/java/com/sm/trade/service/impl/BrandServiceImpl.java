package com.sm.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sm.trade.domain.Brand;
import com.sm.trade.domain.CategoryBrand;
import com.sm.trade.domain.vo.BrandVo;
import com.sm.trade.mapper.BrandMapper;
import com.sm.trade.mapper.CategoryBrandMapper;
import com.sm.trade.service.IBrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 品牌Service业务层处理
 *
 * @author sm
 */
@RequiredArgsConstructor
@Service
public class BrandServiceImpl implements IBrandService {

    private final BrandMapper baseMapper;
    private final CategoryBrandMapper categoryBrandMapper;

    @Override
    public List<BrandVo> selectList(Long categoryId) {
        LambdaQueryWrapper<Brand> lqw = Wrappers.lambdaQuery();
        lqw.eq(Brand::getStatus, "0");
        if (categoryId != null) {
            List<Long> brandIds = categoryBrandMapper.selectList(
                    Wrappers.<CategoryBrand>lambdaQuery()
                        .eq(CategoryBrand::getCategoryId, categoryId))
                .stream().map(CategoryBrand::getBrandId).toList();
            if (brandIds.isEmpty()) {
                return List.of();
            }
            lqw.in(Brand::getId, brandIds);
        }
        lqw.orderByAsc(Brand::getSort);
        return baseMapper.selectVoList(lqw);
    }

    @Override
    public Brand selectById(Long id) {
        return baseMapper.selectById(id);
    }
}
