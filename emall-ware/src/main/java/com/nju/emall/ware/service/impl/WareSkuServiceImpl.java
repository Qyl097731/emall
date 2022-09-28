package com.nju.emall.ware.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nju.common.to.SkuHasStockTo;
import com.nju.common.utils.R;
import com.nju.emall.ware.feign.ProductFeignService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nju.common.utils.PageUtils;
import com.nju.common.utils.Query;

import com.nju.emall.ware.dao.WareSkuDao;
import com.nju.emall.ware.entity.WareSkuEntity;
import com.nju.emall.ware.service.WareSkuService;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            wrapper.eq("sku_id", Long.parseLong(skuId));
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            wrapper.eq("ware_id", Long.parseLong(wareId));
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        List<WareSkuEntity> wareSkuEntities = baseMapper.selectList(new QueryWrapper<WareSkuEntity>()
                .eq("ware_id", wareId).eq("sku_id", skuId));


        if (!CollectionUtils.isEmpty(wareSkuEntities)) {
            baseMapper.addStock(skuId, wareId, skuNum);
        }else{
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            try {
                R r = productFeignService.info(skuId);
                Map<String,Object> skuInfo = (Map<String,Object>) r.get("skuInfo");
                if (r.getCode() == 0){
                    wareSkuEntity.setSkuName((String)skuInfo.get("skuName"));
                }
            }catch (Exception e){
                log.warn(e.getMessage());
            }
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setWareId(wareId);
            baseMapper.insert(wareSkuEntity);
        }
    }

    @Override
    public List<SkuHasStockTo> listSkuHasStock(List<Long> skuIds) {
        List<SkuHasStockTo> hasStockTos = skuIds.stream().map(skuId -> {
            SkuHasStockTo skuHasStockTo = new SkuHasStockTo();
            skuHasStockTo.setSkuId(skuId);
            Long count = baseMapper.getSkuStock(skuId);
            skuHasStockTo.setHasStock(count > 0);
            return skuHasStockTo;
        }).collect(Collectors.toList());

        return hasStockTos;
    }
}
