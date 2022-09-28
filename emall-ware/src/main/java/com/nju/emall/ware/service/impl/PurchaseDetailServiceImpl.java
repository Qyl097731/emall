package com.nju.emall.ware.service.impl;

import com.nju.common.constant.WareConstant;
import com.nju.emall.ware.entity.PurchaseEntity;
import com.nju.emall.ware.entity.WareSkuEntity;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nju.common.utils.PageUtils;
import com.nju.common.utils.Query;

import com.nju.emall.ware.dao.PurchaseDetailDao;
import com.nju.emall.ware.entity.PurchaseDetailEntity;
import com.nju.emall.ware.service.PurchaseDetailService;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<PurchaseDetailEntity> wrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(w -> {
                w.eq("purchase_id", key).or().eq("sku_id", key);
            });
        }
        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            wrapper.eq("status", Integer.parseInt(status));
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            wrapper.eq("ware_id", Long.parseLong(wareId));
        }
        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void updateDetailByPurchaseId(Long id) {
        List<PurchaseDetailEntity> detailEntities = this.list(new QueryWrapper<PurchaseDetailEntity>().eq("purchase_id", id));
        if (!CollectionUtils.isEmpty(detailEntities)) {
            List<PurchaseDetailEntity> purchaseDetailEntities = detailEntities.stream()
                    .peek(item -> item.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode()))
                    .collect(Collectors.toList());
            updateBatchById(purchaseDetailEntities);

        }
    }

}
