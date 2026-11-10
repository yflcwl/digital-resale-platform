package com.sm.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sm.common.core.utils.MapstructUtils;
import com.sm.trade.domain.Category;
import com.sm.trade.domain.vo.CategoryVo;
import com.sm.trade.mapper.CategoryMapper;
import com.sm.trade.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
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
