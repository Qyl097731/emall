package com.nju.emall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.nju.common.exception.NoStockException;
import com.nju.common.to.OrderTo;
import com.nju.common.to.SkuHasStockTo;
import com.nju.common.to.mq.StockDetailTo;
import com.nju.common.to.mq.StockLockedTo;
import com.nju.common.utils.R;
import com.nju.emall.ware.entity.WareOrderTaskDetailEntity;
import com.nju.emall.ware.entity.WareOrderTaskEntity;
import com.nju.emall.ware.feign.OrderFeignService;
import com.nju.emall.ware.feign.ProductFeignService;
import com.nju.emall.ware.service.WareOrderTaskDetailService;
import com.nju.emall.ware.service.WareOrderTaskService;
import com.nju.emall.ware.vo.LockStockResultVo;
import com.nju.emall.ware.vo.OrderItemVo;
import com.nju.emall.ware.vo.OrderVo;
import com.nju.emall.ware.vo.WareSkuLockVo;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
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
import org.springframework.transaction.annotation.Transactional;

@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    WareOrderTaskService orderTaskService;

    @Autowired
    WareOrderTaskDetailService orderTaskDetailService;

    @Autowired
    OrderFeignService orderFeignService;

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


    private void unlockStock(Long skuId, Long wareId, Integer num, Long taskDetailId) {
        // 解锁库存
        baseMapper.unlockStock(skuId, wareId, num);
        // 更新库存工作单的状态
        WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
        entity.setId(taskDetailId);
        entity.setLockStatus(2);
        orderTaskDetailService.updateById(entity);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        List<WareSkuEntity> wareSkuEntities = baseMapper.selectList(new QueryWrapper<WareSkuEntity>()
                .eq("ware_id", wareId).eq("sku_id", skuId));


        if (!CollectionUtils.isEmpty(wareSkuEntities)) {
            baseMapper.addStock(skuId, wareId, skuNum);
        } else {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            try {
                R r = productFeignService.info(skuId);
                Map<String, Object> skuInfo = (Map<String, Object>) r.get("skuInfo");
                if (r.getCode() == 0) {
                    wareSkuEntity.setSkuName((String) skuInfo.get("skuName"));
                }
            } catch (Exception e) {
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


    // 库存解锁的场景
    // 1. 下订单成功，订单过期没有支付被系统自动取消、被用户手动取消
    // 2. 下订单成功，库存锁定成功，接下来的业务调用失败，导致订单回滚，之前锁定的库存要回滚
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {
        /**
         * 保存库存工作单详情
         * 追溯
         */
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskEntity.setTaskStatus(1);
        orderTaskService.save(wareOrderTaskEntity);

        List<LockStockResultVo> resultVos = new ArrayList<>();
        // 找到每个商品在哪个仓库有库存
        List<OrderItemVo> locks = vo.getLocks();
        if (!CollectionUtils.isEmpty(locks)) {
            List<SkuWareHasStock> collect = locks.stream().map(item -> {
                SkuWareHasStock stock = new SkuWareHasStock();
                Long skuId = item.getSkuId();
                stock.setSkuId(skuId);
                List<Long> wareids = baseMapper.listWareIdHasSkuStock(skuId);
                stock.setNum(item.getCount());
                stock.setWareIds(wareids);
                return stock;
            }).collect(Collectors.toList());

            Boolean allLock = false;
            // 锁定库存
            for (SkuWareHasStock hasStock : collect) {
                Boolean skuStocked = false;
                Long skuId = hasStock.getSkuId();
                List<Long> wareIds = hasStock.getWareIds();
                // 没有库存了
                if (CollectionUtils.isEmpty(wareIds)) {
                    throw new NoStockException(skuId);
                }
                //1.如果每一个商品都锁定成功，将当前商品锁定了几件的工作单记录发给MQ
                //2.锁定失败。前面保存的工作单信息就回滚，发送出去的消息，即使要解锁记录，由于数据库查不到，所以就不用解锁
                for (Long wareId : wareIds) {
                    Long count = baseMapper.lockSkuStock(skuId, wareId, hasStock.getNum());
                    if (count == 1) {
                        skuStocked = true;
                        // 告诉MQ库存锁定成功
                        WareOrderTaskDetailEntity wareOrderTaskDetailEntity =
                                new WareOrderTaskDetailEntity(null, skuId, "", hasStock.getNum(), wareOrderTaskEntity.getId(), wareId, 1);
                        orderTaskDetailService.save(wareOrderTaskDetailEntity);
                        StockLockedTo lockedTo = new StockLockedTo();
                        lockedTo.setId(wareOrderTaskEntity.getId());
                        StockDetailTo detailTo = new StockDetailTo();
                        BeanUtils.copyProperties(wareOrderTaskDetailEntity, detailTo);
                        lockedTo.setDetailTo(detailTo);
                        // 如果只发id，可能消息队列获取到的关联的id已经被回滚了，无法获取之前操作数据
                        rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", lockedTo);
                        break;
                    } else {
                        // 锁失败，创建下一个仓库
                    }
                }
                if (!skuStocked) {
                    // 当前仓库都没被锁住
                    throw new NoStockException(skuId);
                }

            }
        }
        return true;
    }

    @Override
    public void unlockStock(StockLockedTo to) {
        StockDetailTo detail = to.getDetailTo();
        Long skuId = detail.getSkuId();
        Long detailId = detail.getId();
        // 解锁 查询数据库关于这个订单的锁定库存信息
        // 有：证明库存锁定成功了
        //  解锁：订单情况
        //      没有这个订单，必须解锁
        //      有这个订单，不是解锁库存
        //          订单状态：已经取消 解锁库存
        //                   没取消： 不能解锁
        // 没有 库存锁定失败，库存回滚

        WareOrderTaskDetailEntity detailEntity = orderTaskDetailService.getById(detailId);
        if (detailEntity != null) {
            // 解锁
            Long id = to.getId();
            WareOrderTaskEntity wareOrderTaskEntity = orderTaskService.getById(id);
            String orderSn = wareOrderTaskEntity.getOrderSn();// 根据订单号查询订单状态
            R r = orderFeignService.getOrderStatus(orderSn);
            if (r.getCode() == 0) {
                // 订单数据返回成功
                OrderVo data = r.getData(new TypeReference<OrderVo>() {
                });
                if (data == null || data.getStatus() == 4) {
                    // 订单已经被取消
                    if (wareOrderTaskEntity.getTaskStatus() == 1) {
                        unlockStock(skuId, detailEntity.getWareId(), detail.getSkuNum(), detailId);
                    }
                }
            } else {
                //消息拒绝以后重新放在队列里面，让别人继续消费解锁
                //远程调用服务失败
                throw new RuntimeException("远程调用服务失败");
            }
        } else {
            //无需解锁
        }
    }

    // 防止订单卡顿 ， 库存消息优先到期， 查出订单的状态一直滞后，什么都不做就走了
    // 导致库存永远无法解锁
    @Transactional
    @Override
    public void unlockStock(OrderTo orderTo) {
        String orderSn = orderTo.getOrderSn();
        // 查询最新库存状态，防止重复解锁
        WareOrderTaskEntity task = orderTaskService.getOrderTaskByOrderSn(orderSn);
        Long id = task.getId();
        List<WareOrderTaskDetailEntity> list = orderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>().eq("task_id", id).eq("lock_status", 1));
        for (WareOrderTaskDetailEntity entity : list) {
            unlockStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum(), id);
        }
        task.setTaskStatus(2);
        orderTaskService.updateById(task);
    }

    @Data
    class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareIds;
    }
}
