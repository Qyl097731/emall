package com.nju.emall.product.service.impl;

import com.nju.common.to.SkuReductionTo;
import com.nju.common.to.SpuBoundTo;
import com.nju.emall.product.entity.*;
import com.nju.emall.product.service.*;
import com.nju.emall.product.vo.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nju.common.utils.PageUtils;
import com.nju.common.utils.Query;

import com.nju.emall.product.dao.SkuInfoDao;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    SkuImagesService imagesService;

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((w) -> {
                w.eq("sku_id", key).or().like("sku_name", key);
            });
        }
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equals(catelogId)) {
            wrapper.eq("catalog_id", catelogId);
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equals(catelogId)) {
            wrapper.eq("brand_id", brandId);
        }
        long min = Long.parseLong((String) params.get("min")), max = Long.parseLong((String) params.get("max"));
        if (min != 0) {
            wrapper.gt("price", min);
        }
        if (max != 0) {
            wrapper.lt("price", max);
        }

        IPage<SkuInfoEntity> page = this.page(new Query<SkuInfoEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> listSkuBySpuId(Long spuId) {
        return baseMapper.selectList(new QueryWrapper<SkuInfoEntity>().eq("spu_id",spuId));
    }

    @Override
    public SkuItemVo item(Long skuId) {
        SkuItemVo skuItemVo = new SkuItemVo();

        CompletableFuture<SkuInfoEntity> infoFeature = CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity infoEntity = baseMapper.selectById(skuId);
            skuItemVo.setInfo(infoEntity);
            return infoEntity;
        }, executor);

        CompletableFuture<Void> saleAttrFeature = infoFeature.thenAcceptAsync((res) -> {
            List<SkuItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.listSaleAttrBySpuId(res.getSpuId());
            skuItemVo.setSaleAttr(saleAttrVos);
        }, executor);

        CompletableFuture<Void> descFeature = infoFeature.thenAcceptAsync((res) -> {
            SpuInfoDescEntity infoDescEntity = spuInfoDescService.getById(res.getSpuId());
            skuItemVo.setDesc(infoDescEntity);
        }, executor);

        CompletableFuture<Void> baseAttrFeature = infoFeature.thenAcceptAsync((res) -> {
            List<SpuItemAttrGroupVo> groupWithAttrs = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(),
                    res.getCatalogId());
            skuItemVo.setGroupAttrs(groupWithAttrs);
        }, executor);

        CompletableFuture<Void> imageFeature = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> images = imagesService.getImagesBySkuId(skuId);
            skuItemVo.setImages(images);
        }, executor);

        CompletableFuture.allOf(saleAttrFeature,descFeature,baseAttrFeature,imageFeature).join();

        return skuItemVo;
    }

}
