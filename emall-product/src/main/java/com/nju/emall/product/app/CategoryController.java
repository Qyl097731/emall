package com.nju.emall.product.app;

import java.util.Arrays;
import java.util.List;

import com.nju.emall.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.nju.emall.product.entity.CategoryEntity;
import com.nju.emall.product.service.CategoryService;
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
     * 级联更新所有关联的数据
     *
     * @CacheEvict:失效模式
     * @CachePut:双写模式，需要有返回值
     * 1、同时进行多种缓存操作：@Caching
     * 2、指定删除某个分区下的所有数据 @CacheEvict(value = "category",allEntries = true)
     * 3、存储同一类型的数据，都可以指定为同一分区
     * @param category
     */
    // @Caching(evict = {
    //         @CacheEvict(value = "category",key = "'getLevel1Categorys'"),
    //         @CacheEvict(value = "category",key = "'getCatalogJson'")
    // })
    @CacheEvict(value = {"category"},key = "'getLevel1Categorys'",allEntries = true)
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
