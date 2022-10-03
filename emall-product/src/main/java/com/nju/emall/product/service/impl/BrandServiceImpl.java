package com.nju.emall.product.service.impl;

import com.nju.common.utils.PinyinUtil;
import com.nju.emall.product.dao.CategoryBrandRelationDao;
import com.nju.emall.product.service.CategoryBrandRelationService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nju.common.utils.PageUtils;
import com.nju.common.utils.Query;

import com.nju.emall.product.dao.BrandDao;
import com.nju.emall.product.entity.BrandEntity;
import com.nju.emall.product.service.BrandService;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = (String) params.get("key");
        QueryWrapper<BrandEntity> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(key)) {
            wrapper.eq("brand_id", key).or().like("name", key);
        }
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void saveBrand(BrandEntity brand) {
        String name = brand.getName();
        brand.setFirstLetter(PinyinUtil.toFirstUpChar(name));
        baseMapper.insert(brand);
    }

    @Override
    public void updateBrand(BrandEntity brand) {
        String name = brand.getName();
        brand.setFirstLetter(PinyinUtil.toFirstUpChar(name));
        baseMapper.updateById(brand);
        categoryBrandRelationService.updateBrand(brand.getBrandId(),brand.getName());
    }

}
