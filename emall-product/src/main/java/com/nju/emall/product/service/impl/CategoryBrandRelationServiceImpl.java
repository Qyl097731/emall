package com.nju.emall.product.service.impl;

import com.nju.emall.product.dao.BrandDao;
import com.nju.emall.product.dao.CategoryDao;
import com.nju.emall.product.entity.BrandEntity;
import com.nju.emall.product.entity.CategoryEntity;
import com.nju.emall.product.service.BrandService;
import com.nju.emall.product.vo.BrandVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nju.common.utils.PageUtils;
import com.nju.common.utils.Query;

import com.nju.emall.product.dao.CategoryBrandRelationDao;
import com.nju.emall.product.entity.CategoryBrandRelationEntity;
import com.nju.emall.product.service.CategoryBrandRelationService;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {


    @Autowired
    CategoryDao categoryDao;

    @Autowired
    BrandService brandService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();
        BrandEntity brandEntity = brandService.getById(brandId);
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
        categoryBrandRelation.setBrandName(brandEntity.getName());
        categoryBrandRelation.setCatelogName(categoryEntity.getName());
        this.save(categoryBrandRelation);
    }

    @Override
    public void updateBrand(Long brandId, String name) {
        CategoryBrandRelationEntity categoryBrandRelationEntity = new CategoryBrandRelationEntity();
        categoryBrandRelationEntity.setBrandId(brandId);
        categoryBrandRelationEntity.setBrandName(name);
        baseMapper.update(categoryBrandRelationEntity, new QueryWrapper<CategoryBrandRelationEntity>().eq("brand_id",
                brandId));
    }

    @Override
    public void updateCategory(Long catId, String name) {
        CategoryBrandRelationEntity categoryBrandRelationEntity = new CategoryBrandRelationEntity();
        categoryBrandRelationEntity.setCatelogId(catId);
        categoryBrandRelationEntity.setCatelogName(name);
        baseMapper.update(categoryBrandRelationEntity,
                new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));
    }

    @Override
    public List<BrandEntity> brandListByCatId(Long catId) {
        List<BrandEntity> brands = new ArrayList<>();
        List<CategoryBrandRelationEntity> relationEntities = baseMapper.selectList(new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));
        if (!CollectionUtils.isEmpty(relationEntities)){
            List<Long> ids = relationEntities.stream().map(CategoryBrandRelationEntity::getBrandId).collect(Collectors.toList());
            brands = brandService.listByIds(ids);
        }
        return brands;
    }


}
