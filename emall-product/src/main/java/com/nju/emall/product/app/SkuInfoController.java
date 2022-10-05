package com.nju.emall.product.app;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nju.emall.product.entity.SkuInfoEntity;
import com.nju.emall.product.service.SkuInfoService;
import com.nju.common.utils.PageUtils;
import com.nju.common.utils.R;


/**
 * sku信息
 *
 * @author qyl
 * @email 553579048@qq.com
 * @date 2022-09-16 23:50:22
 */
@RestController
@RequestMapping("product/skuinfo")
public class SkuInfoController {
    @Autowired
    private SkuInfoService skuInfoService;

    @GetMapping("{skuId}/price")
    public BigDecimal getPrice(@PathVariable("skuId") Long skuId) {
        SkuInfoEntity infoEntity = skuInfoService.getById(skuId);
        return infoEntity != null ? infoEntity.getPrice() : BigDecimal.ZERO;
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = skuInfoService.queryPageByCondition(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId) {
        SkuInfoEntity skuInfo = skuInfoService.getById(skuId);

        return Objects.requireNonNull(R.ok().put("skuInfo", skuInfo)).put("skuName", skuInfo.getSkuName());
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody SkuInfoEntity skuInfo) {
        skuInfoService.save(skuInfo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody SkuInfoEntity skuInfo) {
        skuInfoService.updateById(skuInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] skuIds) {
        skuInfoService.removeByIds(Arrays.asList(skuIds));

        return R.ok();
    }

}
