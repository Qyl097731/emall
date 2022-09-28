package com.nju.emall.ware.service.impl;

import com.nju.common.constant.WareConstant;
import com.nju.emall.ware.entity.PurchaseDetailEntity;
import com.nju.emall.ware.service.PurchaseDetailService;
import com.nju.emall.ware.service.WareSkuService;
import com.nju.emall.ware.vo.MergeVo;
import com.nju.emall.ware.vo.PurchaseDoneVo;
import com.nju.emall.ware.vo.PurchaseItemDoneVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nju.common.utils.PageUtils;
import com.nju.common.utils.Query;

import com.nju.emall.ware.dao.PurchaseDao;
import com.nju.emall.ware.entity.PurchaseEntity;
import com.nju.emall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    PurchaseDetailService purchaseDetailService;

    @Autowired
    WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceivePurchase(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status", 0)
                        .or().eq("status", 1)
        );

        return new PageUtils(page);

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        List<Long> items = mergeVo.getItems();
        List<PurchaseDetailEntity> detailEntities = purchaseDetailService.listByIds(items);
        if (!CollectionUtils.isEmpty(detailEntities)) {
            boolean anyMatch = detailEntities.stream().anyMatch(purchaseDetailEntity -> purchaseDetailEntity.getStatus() > 1);
            if (anyMatch) {
                throw new IllegalArgumentException();
            }
        }

        Long id = mergeVo.getPurchaseId();
        if (id == null) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            this.save(purchaseEntity);
            id = purchaseEntity.getId();
        }

        Long finalId = id;
        detailEntities = detailEntities.stream().peek(entity -> {
            entity.setPurchaseId(finalId);
            entity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode()
            );
        }).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(detailEntities);

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        this.updateById(purchaseEntity);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void received(List<Long> ids) {
        if (!CollectionUtils.isEmpty(ids)) {
            List<PurchaseEntity> purchaseEntities = this.listByIds(ids);
            if (!CollectionUtils.isEmpty(purchaseEntities)) {
                List<PurchaseEntity> entityList = purchaseEntities.stream().filter(purchaseEntity ->
                         purchaseEntity.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode())
                        .peek(item -> {
                            item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVED.getCode());
                            purchaseDetailService.updateDetailByPurchaseId(item.getId());
                        })
                        .collect(Collectors.toList());
                this.updateBatchById(entityList);
            }
        }

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void done(PurchaseDoneVo vo) {
        Long id = vo.getId();
        List<PurchaseItemDoneVo> items = vo.getItems();
        if (!CollectionUtils.isEmpty(items)) {
            List<PurchaseDetailEntity> purchaseDetailEntities = new ArrayList<>();
            boolean match = false;
            for (PurchaseItemDoneVo item : items) {
                if ( item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()){
                    match = true;
                }else{
                    PurchaseDetailEntity entity = purchaseDetailService.getById(item.getItemId());
                    wareSkuService.addStock(entity.getSkuId(),entity.getWareId(),entity.getSkuNum());
                }

                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setId(item.getItemId());
                purchaseDetailEntity.setStatus(item.getStatus());
                purchaseDetailEntities.add(purchaseDetailEntity);
            }
            purchaseDetailService.updateBatchById(purchaseDetailEntities);



            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setId(id);
            purchaseEntity.setStatus(match ?
                    WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()
                    : WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());
            updateById(purchaseEntity);


        }
    }

}
