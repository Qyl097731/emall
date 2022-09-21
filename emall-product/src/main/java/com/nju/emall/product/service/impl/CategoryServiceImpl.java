package com.nju.emall.product.service.impl;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nju.common.utils.PageUtils;
import com.nju.common.utils.Query;

import com.nju.emall.product.dao.CategoryDao;
import com.nju.emall.product.entity.CategoryEntity;
import com.nju.emall.product.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1 查
        List<CategoryEntity> entities = baseMapper.selectList(null);
        //2 组装数据
        return entities.stream()
                .filter((categoryEntity -> categoryEntity.getParentCid() == 0))
                .peek(categoryEntity -> categoryEntity.setChildren(getChildren(categoryEntity, entities)))
                .sorted(Comparator.comparing(CategoryEntity::getSort, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList());
    }

    @Override
    public void removeMenuByIds(List<Long> ids) {

        baseMapper.deleteBatchIds(ids);
    }

    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        return all.stream()
                .filter(category -> root.getCatId().equals(category.getParentCid()))
                .peek(category -> category.setChildren(getChildren(category, all)))
                .sorted(Comparator.comparing(CategoryEntity::getSort, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList());
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> path = new ArrayList<>();
        getParentCatelogPath(catelogId, path);
        return path.toArray(new Long[0]);
    }

    private void getParentCatelogPath(Long catelogId, List<Long> path) {
        CategoryEntity node = this.getById(catelogId);
        if (node != null && node.getParentCid() != 0) {
            getParentCatelogPath(node.getParentCid(), path);
        }
        path.add(catelogId);
    }


}
