package com.nju.emall.coupon.service.impl;

import com.nju.common.to.MemberPrice;
import com.nju.common.to.SkuReductionTo;
import com.nju.emall.coupon.entity.MemberPriceEntity;
import com.nju.emall.coupon.entity.SkuLadderEntity;
import com.nju.emall.coupon.service.MemberPriceService;
import com.nju.emall.coupon.service.SkuLadderService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nju.common.utils.PageUtils;
import com.nju.common.utils.Query;

import com.nju.emall.coupon.dao.SkuFullReductionDao;
import com.nju.emall.coupon.entity.SkuFullReductionEntity;
import com.nju.emall.coupon.service.SkuFullReductionService;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    SkuLadderService skuLadderService;

    @Autowired
    MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {
        if (skuReductionTo != null) {
            if (skuReductionTo.getFullCount() > 0) {
                SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
                skuLadderEntity.setSkuId(skuReductionTo.getSkuId());
                skuLadderEntity.setFullCount(skuReductionTo.getFullCount());
                skuLadderEntity.setDiscount(skuReductionTo.getDiscount());
                skuLadderEntity.setAddOther(skuReductionTo.getCountStatus());
                skuLadderService.save(skuLadderEntity);
            }

            if (skuReductionTo.getFullPrice() != null && skuReductionTo.getFullPrice().compareTo(BigDecimal.ZERO) > 0) {
                SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
                BeanUtils.copyProperties(skuReductionTo, skuFullReductionEntity);
                this.save(skuFullReductionEntity);
            }

            List<MemberPrice> memberPrices = skuReductionTo.getMemberPrice();
            if (!CollectionUtils.isEmpty(memberPrices)) {
                List<MemberPriceEntity> priceEntities = memberPrices
                        .stream()
                        .filter(memberPrice -> memberPrice.getPrice().compareTo(BigDecimal.ZERO) > 0)
                        .map(memberPrice -> {
                            MemberPriceEntity entity = new MemberPriceEntity();
                            entity.setMemberLevelId(memberPrice.getId());
                            entity.setMemberLevelName(memberPrice.getName());
                            entity.setMemberPrice(memberPrice.getPrice());
                            entity.setAddOther(1);
                            return entity;
                        }).collect(Collectors.toList());
                memberPriceService.saveBatch(priceEntities);
            }
        }
    }

}
