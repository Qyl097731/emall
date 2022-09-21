package com.nju.emall.product.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nju.emall.product.entity.AttrAttrgroupRelationEntity;
import com.nju.emall.product.entity.BrandEntity;
import com.nju.emall.product.vo.BrandVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nju.emall.product.entity.CategoryBrandRelationEntity;
import com.nju.emall.product.service.CategoryBrandRelationService;
import com.nju.common.utils.PageUtils;
import com.nju.common.utils.R;



/**
 * 品牌分类关联
 *
 * @author qyl
 * @email 553579048@qq.com
 * @date 2022-09-16 23:50:22
 */
@RestController
@RequestMapping("product/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = categoryBrandRelationService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 品牌列表
     */
    @GetMapping("brands/list")
    public R brandList(@RequestParam(value = "catId",required = true)Long catId){
        List<BrandEntity> relationEntities  = categoryBrandRelationService.brandListByCatId(catId);
        List<BrandVo> vos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(relationEntities)) {
            vos = relationEntities.stream().map(item ->
                    new BrandVo().setBrandId(item.getBrandId()).setBrandName(item.getName())).collect(Collectors.toList());
        }
        return R.ok().put("data", vos);
    }

    /**
     * 关联信息
     */
    @GetMapping("catelog/list")
    public R cateloglist(@RequestParam Long brandId){
        List<CategoryBrandRelationEntity> data = categoryBrandRelationService.list(new QueryWrapper<CategoryBrandRelationEntity>().eq(
                "brand_id", brandId));
        return R.ok().put("data", data);
    }



    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存
     */
    @PutMapping("/save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.saveDetail(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
