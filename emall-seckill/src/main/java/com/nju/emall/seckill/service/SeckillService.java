package com.nju.emall.seckill.service;

import com.nju.emall.seckill.to.SeckillSkuRedisTo;

import java.util.List;

/**
 * @description
 * @date:2022/10/8 21:00
 * @author: qyl
 */
public interface SeckillService {
    void uploadSeckillSkuLatest3Days();

    List<SeckillSkuRedisTo> getCurrentSeckillSkus();

    SeckillSkuRedisTo getSkuSeckilInfo(Long skuId);

    String kill(String killId, String key, Integer num) throws InterruptedException;
}
