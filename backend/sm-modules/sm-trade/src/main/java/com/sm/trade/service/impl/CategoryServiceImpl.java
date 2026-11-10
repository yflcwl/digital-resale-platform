package com.sm.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sm.common.core.exception.ServiceException;
import com.sm.common.core.utils.MapstructUtils;
import com.sm.trade.domain.Product;
import com.sm.trade.domain.bo.CategoryBo;
import com.sm.trade.mapper.ProductMapper;
import com.sm.common.core.utils.MapstructUtils;
import com.sm.trade.domain.Category;
import com.sm.trade.domain.vo.CategoryVo;
import com.sm.trade.mapper.CategoryMapper;
import com.sm.trade.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品分类Service业务层处理
 *
 * @author sm
 */
@RequiredArgsConstructor
@Service
public class CategoryServiceImpl implements ICategoryService {

    private final CategoryMapper baseMapper;
    private final ProductMapper productMapper;

    @Override
    public List<CategoryVo> selectTree() {
        LambdaQueryWrapper<Category> lqw = Wrappers.lambdaQuery();
        lqw.eq(Category::getStatus, "0");
        lqw.orderByAsc(Category::getSort);
        List<CategoryVo> all = baseMapper.selectVoList(lqw);
        return buildTree(all);
    }

    @Override
    public Category selectById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public Map<Long, String> selectNameMap(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return baseMapper.selectBatchIds(ids).stream()
            .collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));
    }

    @Override
    public List<CategoryVo> selectAdminTree() {
        LambdaQueryWrapper<Category> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(Category::getSort);
        return buildTree(baseMapper.selectVoList(lqw));
    }

    @Override
    public Long add(CategoryBo bo) {
        Category category = MapstructUtils.convert(bo, Category.class);
        category.setId(null);
        if (category.getParentId() != null && category.getParentId() != 0L) {
            Category parent = baseMapper.selectById(category.getParentId());
            if (parent == null) {
                throw new ServiceException("父分类不存在");
            }
            category.setLevel((parent.getLevel() == null ? 1 : parent.getLevel()) + 1);
        } else {
            category.setParentId(0L);
            category.setLevel(1);
        }
        if (category.getLevel() > 3) {
            throw new ServiceException("分类最多三级");
        }
        baseMapper.insert(category);
        return category.getId();
    }

    @Override
    public void edit(CategoryBo bo) {
        Category category = baseMapper.selectById(bo.getId());
        if (category == null) {
            throw new ServiceException("分类不存在");
        }
        Category update = new Category();
        update.setId(bo.getId());
        update.setName(bo.getName());
        update.setIcon(bo.getIcon());
        update.setSort(bo.getSort());
        update.setStatus(bo.getStatus());
        baseMapper.updateById(update);
    }

    @Override
    public void remove(Long id) {
        long children = baseMapper.selectCount(Wrappers.<Category>lambdaQuery().eq(Category::getParentId, id));
        if (children > 0) {
            throw new ServiceException("请先删除子分类");
        }
        long products = productMapper.selectCount(Wrappers.<Product>lambdaQuery().eq(Product::getCategoryId, id));
        if (products > 0) {
            throw new ServiceException("该分类下存在商品,不可删除");
        }
        baseMapper.deleteById(id);
    }
    private List<CategoryVo> buildTree(List<CategoryVo> list) {
        List<CategoryVo> roots = list.stream()
            .filter(c -> c.getParentId() == null || c.getParentId() == 0L)
            .collect(Collectors.toList());
        roots.forEach(root -> fillChildren(root, list));
        return roots;
    }

    private void fillChildren(CategoryVo parent, List<CategoryVo> all) {
        List<CategoryVo> children = all.stream()
            .filter(c -> parent.getId().equals(c.getParentId()))
            .collect(Collectors.toList());
        parent.setChildren(children);
        children.forEach(child -> fillChildren(child, all));
    }
}
