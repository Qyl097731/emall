package com.nju.emall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.nju.common.utils.R;
import com.nju.emall.seckill.feign.CouponFeignService;
import com.nju.emall.seckill.feign.ProductFeignService;
import com.nju.emall.seckill.service.SeckillService;
import com.nju.emall.seckill.to.SeckillSkuRedisTo;
import com.nju.emall.seckill.vo.SeckillSessionWithSkusVo;
import com.nju.emall.seckill.vo.SeckillSkuVo;
import com.nju.emall.seckill.vo.SkuInfoVo;
import org.apache.commons.collections.CollectionUtils;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @description
 * @date:2022/10/8 21:00
 * @author: qyl
 */
@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    RedissonClient redissonClient;


    private final String SESSION__CACHE_PREFIX = "seckill:sessions:";

    private final String SECKILL_CHARE_PREFIX = "seckill:skus";

    private final String SKU_STOCK_SEMAPHORE = "seckill:stock:";    //+商品随机码

    @Override
    public void uploadSeckillSkuLatest3Days() {
        //1.扫描需要参与秒杀的活动
        R r = couponFeignService.getLatest3DaySession();
        if (r.getCode() == 0) {
            List<SeckillSessionWithSkusVo> data = r.getData(new TypeReference<List<SeckillSessionWithSkusVo>>() {
            });
            // 缓存redis
            // 1. 缓存活动信息
            if (!CollectionUtils.isEmpty(data)) {
                saveSessionInfos(data);
                // 2. 换从活动的关联商品信息
                saveSessionSkuInfos(data);
            }
        }
    }

    private void saveSessionInfos(List<SeckillSessionWithSkusVo> data) {
        if (!CollectionUtils.isEmpty(data)) {
            data.forEach(item -> {
                Long startTime = item.getStartTime().getTime();
                Long endTime = item.getEndTime().getTime();
                //存入到Redis中的key
                String key = SESSION__CACHE_PREFIX + startTime + "_" + endTime;
                //判断Redis中是否有该信息，如果没有才进行添加 保证多定时任务启动之后的幂等性，多任务只进行一次
                Boolean hasKey = redisTemplate.hasKey(key);
                //缓存活动信息
                if (hasKey != null && !hasKey) {
                    //获取到活动中所有商品的skuId
                    List<String> skuIds = item.getRelationSkus().stream()
                            .map(sku -> sku.getPromotionSessionId() + "-" + sku.getSkuId().toString()).collect(Collectors.toList());
                    redisTemplate.opsForList().leftPushAll(key, skuIds);
                }
            });
        }
    }

    private void saveSessionSkuInfos(List<SeckillSessionWithSkusVo> sessions) {
        if (!CollectionUtils.isEmpty(sessions)) {
            sessions.forEach(session -> {
                // 绑定某个键，方便后续操作
                BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SECKILL_CHARE_PREFIX);
                //缓存商品
                session.getRelationSkus().forEach(seckillSkuVo -> {
                    String key = seckillSkuVo.getPromotionSessionId().toString() + "-" + seckillSkuVo.getSkuId().toString();
                    Boolean hasKey = redisTemplate.hasKey(key);
                    if (hasKey != null && !hasKey) {

                        SeckillSkuRedisTo redisTo = new SeckillSkuRedisTo();
                        // sku商品详细信息
                        R r = productFeignService.info(seckillSkuVo.getSkuId());
                        if (r.getCode() == 0) {
                            SkuInfoVo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                            });
                            redisTo.setSkuInfo(skuInfo);
                        }

                        // 复制秒杀的基本属性
                        BeanUtils.copyProperties(seckillSkuVo, redisTo);

                        // 设置当前商品的秒杀时间信息
                        redisTo.setStartTime(session.getStartTime().getTime());
                        redisTo.setEndTime(session.getEndTime().getTime());
                        // 随机码 只有在开始秒杀的时候才能暴露 ，防止抢单脚本
                        redisTo.setRandomCode(UUID.randomUUID().toString().replace("-", ""));

                        String seckillValue = JSON.toJSONString(redisTo);
                        ops.put(key, seckillValue);

                        // 分入分布式信号量 控制流量
                        RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + redisTo.getRandomCode());
                        semaphore.trySetPermits(seckillSkuVo.getSeckillCount());
                    }

                });
            });
        }

    }

}
