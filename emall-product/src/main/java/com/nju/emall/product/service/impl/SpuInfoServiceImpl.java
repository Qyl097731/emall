package com.nju.emall.product.service.impl;

import com.nju.common.constant.ProductConst;
import com.nju.common.to.SkuHasStockTo;
import com.nju.common.to.SkuReductionTo;
import com.nju.common.to.SpuBoundTo;
import com.nju.common.to.es.SkuEsModel;
import com.nju.common.utils.R;
import com.nju.emall.product.entity.*;
import com.nju.emall.product.feign.CouponFeignService;
import com.nju.emall.product.feign.SearchFeignService;
import com.nju.emall.product.feign.WareFeignService;
import com.nju.emall.product.service.*;
import com.nju.emall.product.vo.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nju.common.utils.PageUtils;
import com.nju.common.utils.Query;

import com.nju.emall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    SpuImagesService spuImagesService;

    @Autowired
    ProductAttrValueService productAttrValueService;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    AttrService attrService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfoEntity);
        this.saveBaseSpuInfo(spuInfoEntity);

        List<String> decript = vo.getDecript();
        if (!CollectionUtils.isEmpty(decript)) {
            SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
            spuInfoDescEntity.setDecript(String.join(",", decript));
            spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
            spuInfoDescService.saveSpuInfoDesc(spuInfoDescEntity);
        }

        List<String> images = vo.getImages();
        if (!CollectionUtils.isEmpty(images)) {
            spuImagesService.saveImages(spuInfoEntity.getId(), images);
        }

        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)) {
            productAttrValueService.saveProductAttr(spuInfoEntity.getId(), baseAttrs);
        }

        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        R result = couponFeignService.saveBounds(spuBoundTo);
        if (result.getCode() != 0) {
            log.error("远程保存spu积分信息失败");
        }


        List<Skus> skus = vo.getSkus();
        if (!CollectionUtils.isEmpty(skus)) {
            skus.forEach(sku -> {
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku, skuInfoEntity);

                List<Images> skuImages = sku.getImages();
                if (!CollectionUtils.isEmpty(skuImages)) {
                    skuImages.stream()
                            .filter(image -> image.getDefaultImg() == 1)
                            .findFirst()
                            .ifPresent(defaultImage -> skuInfoEntity.setSkuDefaultImg(defaultImage.getImgUrl()));
                }

                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId())
                        .setCatalogId(spuInfoEntity.getCatalogId())
                        .setSaleCount(0L)
                        .setSpuId(spuInfoEntity.getId());
                skuInfoService.save(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();
                List<SkuImagesEntity> imagesEntities = sku.getImages()
                        .stream()
                        .filter(img -> !StringUtils.isEmpty(img.getImgUrl())).map(img -> {
                            SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                            skuImagesEntity.setSkuId(skuId);
                            skuImagesEntity.setImgUrl(img.getImgUrl());
                            skuImagesEntity.setDefaultImg(img.getDefaultImg());
                            return skuImagesEntity;
                        }).collect(Collectors.toList());
                skuImagesService.saveBatch(imagesEntities);

                List<Attr> attrs = sku.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attrs.stream().map(attr -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuId);
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(vo, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                R reductionResult = couponFeignService.saveSkuReduction(skuReductionTo);
                if (reductionResult.getCode() != 0) {
                    log.error("远程保存Sku优惠信息失败");
                }

            });
        }
    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.save(spuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((w) -> {
                w.eq("id", key).or().like("spu_name", key);
            });
        }
        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            wrapper.eq("publish_status", status);
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equals(brandId)) {
            wrapper.eq("brand_id", brandId);
        }
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equals(catelogId)) {
            wrapper.eq("catalog_id", catelogId);
        }
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void up(Long spuId) {
        if (spuId != null) {
            Map<Long, Boolean> map = null;
            List<ProductAttrValueEntity> attrValueEntities = productAttrValueService.baseAttrListForSpu(spuId);
            if (!CollectionUtils.isEmpty(attrValueEntities)) {
                List<Long> ids = attrValueEntities.stream()
                        .map(ProductAttrValueEntity::getAttrId)
                        .collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(ids)) {
                    List<Long> searchIds = attrService.listSearchAttrIds(ids);
                    if (!CollectionUtils.isEmpty(searchIds)) {

                        HashSet<Long> idSet = new HashSet<>(searchIds);

                        List<SkuEsModel.Attrs> attrs = attrValueEntities.stream()
                                .filter(item -> idSet.contains(item.getAttrId()))
                                .map(item -> {
                                    SkuEsModel.Attrs attr = new SkuEsModel.Attrs();
                                    BeanUtils.copyProperties(item, attr);
                                    return attr;
                                }).collect(Collectors.toList());

                        List<SkuInfoEntity> skuInfoEntities = skuInfoService.listSkuBySpuId(spuId);
                        if (!CollectionUtils.isEmpty(skuInfoEntities)) {

                            List<Long> skuIds = skuInfoEntities.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
                            if (!CollectionUtils.isEmpty(skuIds)) {
                                try {
                                    R hasstock = wareFeignService.hasstock(skuIds);
                                    Map<String, Object> objectMap = (Map<String, Object>) hasstock.get("data");
                                    List<SkuHasStockTo> skuHasStockTos = (List<SkuHasStockTo>) objectMap.get("data");
                                    if (!CollectionUtils.isEmpty(skuHasStockTos)) {
                                        map = skuHasStockTos.stream()
                                                .collect(Collectors.toMap(SkuHasStockTo::getSkuId,
                                                        SkuHasStockTo::getHasStock));
                                    }
                                } catch (Exception e) {
                                    log.error("库存服务查询异常，原因{}", e);
                                }
                                Map<Long, Boolean> finalMap = map;
                                List<SkuEsModel> skuEsModels = skuInfoEntities.stream()
                                        .map(skuInfoEntity -> {
                                            SkuEsModel skuEsModel = new SkuEsModel();
                                            BeanUtils.copyProperties(skuInfoEntity, skuEsModel);
                                            skuEsModel.setSkuPrice(skuInfoEntity.getPrice());
                                            skuEsModel.setSkuImg(skuInfoEntity.getSkuDefaultImg());

                                            skuEsModel.setHotScore(0L);
                                            if (finalMap != null) {
                                                skuEsModel.setHasStock(finalMap.getOrDefault(skuInfoEntity.getSkuId()
                                                        , false));
                                            }
                                            BrandEntity brandEntity = brandService.getById(skuEsModel.getBrandId());
                                            skuEsModel.setBrandName(brandEntity.getName());
                                            skuEsModel.setBrandImg(brandEntity.getLogo());

                                            CategoryEntity categoryEntity = categoryService.getById(skuEsModel.getSkuId());
                                            skuEsModel.setCatalogName(categoryEntity.getName());

                                            skuEsModel.setAttrs(attrs);

                                            return skuEsModel;
                                        }).collect(Collectors.toList());

                                R statusUp = searchFeignService.productStatusUp(skuEsModels);
                                if (statusUp.getCode() == 0) {
                                    baseMapper.updateSpuStatus(spuId, ProductConst.ProductStatusEnum.SPU_UP.getCode());
                                } else {
                                    //远程调用失败
                                    //TODO 7、重复调用？接口幂等性:重试机制
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
