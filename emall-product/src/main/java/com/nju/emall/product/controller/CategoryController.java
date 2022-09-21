package com.nju.emall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.nju.emall.product.service.CategoryBrandRelationService;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.nju.emall.product.entity.CategoryEntity;
import com.nju.emall.product.service.CategoryService;
import com.nju.common.utils.PageUtils;
import com.nju.common.utils.R;



/**
 * 商品三级分类
 *
 * @author qyl
 * @email 553579048@qq.com
 * @date 2022-09-16 23:50:21
 */
@RestController
@RequestMapping("product/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    /**
     * 查出所有分类、子分类、以树形结构组装起来
     */
    @GetMapping("/list/tree")
    public R list(){
        List<CategoryEntity> entities =  categoryService.listWithTree();
        return R.ok().put("data", entities);
    }


    /**
     * 信息
     */
    @GetMapping("/info/{catId}")
    public R info(@PathVariable("catId") Long catId){
		CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("category", category);
    }

    /**
     * 保存
     */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/save")
    public R save(@RequestBody CategoryEntity category){
		categoryService.saveOrUpdate(category);
        Long catId = category.getCatId();
        if (catId != null){
            categoryBrandRelationService.updateCategory(catId,category.getName());
        }
        return R.ok();
    }

    /**
     * 批量修改
     */
    @PutMapping("/update/sort")
    public R updateSort(@RequestBody CategoryEntity[] categories){
        categoryService.updateBatchById(Arrays.asList(categories));
        return R.ok();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete")
    public R delete(@RequestBody Long[] catIds){
		categoryService.removeMenuByIds(Arrays.asList(catIds));
        return R.ok();
    }

}
