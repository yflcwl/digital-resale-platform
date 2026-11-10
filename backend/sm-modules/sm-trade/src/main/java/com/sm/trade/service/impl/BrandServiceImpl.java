package com.sm.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sm.common.core.exception.ServiceException;
import com.sm.common.core.utils.MapstructUtils;
import com.sm.trade.domain.Product;
import com.sm.trade.domain.bo.BrandBo;
import com.sm.trade.mapper.ProductMapper;
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
    private final ProductMapper productMapper;

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
    public Long add(BrandBo bo) {
        Brand brand = MapstructUtils.convert(bo, Brand.class);
        brand.setId(null);
        baseMapper.insert(brand);
        return brand.getId();
    }

    @Override
    public void edit(BrandBo bo) {
        Brand brand = baseMapper.selectById(bo.getId());
        if (brand == null) {
            throw new ServiceException("品牌不存在");
        }
        Brand update = new Brand();
        update.setId(bo.getId());
        update.setName(bo.getName());
        update.setLogo(bo.getLogo());
        update.setSort(bo.getSort());
        update.setStatus(bo.getStatus());
        baseMapper.updateById(update);
    }

    @Override
    public void remove(Long id) {
        long products = productMapper.selectCount(Wrappers.<Product>lambdaQuery().eq(Product::getBrandId, id));
        if (products > 0) {
            throw new ServiceException("该品牌下存在商品,不可删除");
        }
        baseMapper.deleteById(id);
    }
    @Override
    public Brand selectById(Long id) {
        return baseMapper.selectById(id);
    }
}
