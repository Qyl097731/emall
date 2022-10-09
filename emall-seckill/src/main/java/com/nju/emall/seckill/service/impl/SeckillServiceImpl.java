package com.nju.emall.seckill.service.impl;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.nju.common.constant.AuthServerConstant;
import com.nju.common.to.mq.SeckillOrderTo;
import com.nju.common.utils.R;
import com.nju.common.vo.MemberResponseVo;
import com.nju.emall.seckill.feign.CouponFeignService;
import com.nju.emall.seckill.feign.ProductFeignService;
import com.nju.emall.seckill.interceptor.SeckillInterceptor;
import com.nju.emall.seckill.service.SeckillService;
import com.nju.emall.seckill.to.SeckillSkuRedisTo;
import com.nju.emall.seckill.vo.SeckillSessionWithSkusVo;
import com.nju.emall.seckill.vo.SkuInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @description
 * @date:2022/10/8 21:00
 * @author: qyl
 */
@Slf4j
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

    @Autowired
    RabbitTemplate rabbitTemplate;

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

    public List<SeckillSkuRedisTo> blockHandler(BlockException e) {

        log.error("getCurrentSeckillSkusResource被限流了,{}",e.getMessage());
        return null;
    }

    @SentinelResource(value = "getCurrentSeckillSkusResource",blockHandler = "blockHandler")
    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {

        //1、确定当前属于哪个秒杀场次
        long currentTime = System.currentTimeMillis();
        try (Entry entry = SphU.entry("seckillSkus")) {
            //从Redis中查询到所有key以seckill:sessions开头的所有数据
            Set<String> keys = redisTemplate.keys(SESSION__CACHE_PREFIX + "*");
            for (String key : keys) {
                //seckill:sessions:1594396764000_1594453242000
                String replace = key.replace(SESSION__CACHE_PREFIX, "");
                String[] s = replace.split("_");
                //获取存入Redis商品的开始时间
                long startTime = Long.parseLong(s[0]);
                //获取存入Redis商品的结束时间
                long endTime = Long.parseLong(s[1]);

                //判断是否是当前秒杀场次
                if (currentTime >= startTime && currentTime <= endTime) {
                    //2、获取这个秒杀场次需要的所有商品信息
                    List<String> range = redisTemplate.opsForList().range(key, 0, -1);
                    BoundHashOperations<String, String, String> hasOps = redisTemplate.boundHashOps(SECKILL_CHARE_PREFIX);
                    assert range != null;
                    List<String> listValue = hasOps.multiGet(range);
                    if (!CollectionUtils.isEmpty(listValue)) {
                        return listValue.stream().map(item -> {
                            // redisTo.setRandomCode(null);当前秒杀开始需要随机码
                            return JSON.parseObject(item, SeckillSkuRedisTo.class);
                        }).collect(Collectors.toList());
                    }
                    break;
                }
            }
        }catch (BlockException e) {
            log.error("资源被限流{}",e.getMessage());
        }

        return null;
    }

    @Override
    public SeckillSkuRedisTo getSkuSeckilInfo(Long skuId) {

        //1、找到所有需要秒杀的商品的key信息---seckill:skus
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SECKILL_CHARE_PREFIX);

        //拿到所有的key
        Set<String> keys = hashOps.keys();
        if (!CollectionUtils.isEmpty(keys)) {
            //4-45 正则表达式进行匹配
            String reg = "\\d-" + skuId;
            for (String key : keys) {
                //如果匹配上了
                if (Pattern.matches(reg, key)) {
                    //从Redis中取出数据来
                    String redisValue = hashOps.get(key);
                    //进行序列化
                    SeckillSkuRedisTo redisTo = JSON.parseObject(redisValue, SeckillSkuRedisTo.class);

                    //随机码
                    Long currentTime = System.currentTimeMillis();
                    Long startTime = redisTo.getStartTime();
                    Long endTime = redisTo.getEndTime();
                    //如果当前时间大于等于秒杀活动开始时间并且要小于活动结束时间
                    if (currentTime >= startTime && currentTime <= endTime) {
                        return redisTo;
                    }
                    redisTo.setRandomCode(null);
                    return redisTo;
                }
            }
        }
        return null;
    }

    /**
     * 当前商品进行秒杀（秒杀开始）
     *
     * @param killId
     * @param key
     * @param num
     * @return
     */
    @Override
    public String kill(String killId, String key, Integer num) throws InterruptedException {

        long s1 = System.currentTimeMillis();
        //获取当前用户的信息
        MemberResponseVo user =
                (MemberResponseVo) SeckillInterceptor.threadLocal.get().getAttribute(AuthServerConstant.LOGIN_USER);


        //1、获取当前秒杀商品的详细信息从Redis中获取
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SECKILL_CHARE_PREFIX);
        String skuInfoValue = hashOps.get(killId);
        if (StringUtils.isEmpty(skuInfoValue)) {
            return null;
        }
        //(合法性效验)
        SeckillSkuRedisTo redisTo = JSON.parseObject(skuInfoValue, SeckillSkuRedisTo.class);
        Long startTime = redisTo.getStartTime();
        Long endTime = redisTo.getEndTime();
        long currentTime = System.currentTimeMillis();
        //判断当前这个秒杀请求是否在活动时间区间内(效验时间的合法性)
        if (currentTime >= startTime && currentTime <= endTime) {

            //2、效验随机码和商品id
            String randomCode = redisTo.getRandomCode();
            String skuId = redisTo.getPromotionSessionId() + "-" + redisTo.getSkuId();
            if (randomCode.equals(key) ) {
                //3、验证购物数量是否合理和库存量是否充足
                Integer seckillLimit = redisTo.getSeckillLimit();

                //获取信号量
                String seckillCount = redisTemplate.opsForValue().get(SKU_STOCK_SEMAPHORE + randomCode);
                int count = Integer.parseInt(seckillCount);
                //判断信号量是否大于0,并且买的数量不能超过库存
                if (count > 0 && num <= seckillLimit && count > num) {
                    //4、验证这个人是否已经买过了（幂等性处理）,如果秒杀成功，就去占位。userId-sessionId-skuId
                    //SETNX 原子性处理
                    String redisKey = user.getId() + "-" + skuId;
                    //设置自动过期(活动结束时间-当前时间)
                    long ttl = endTime - currentTime;
                    Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), ttl, TimeUnit.MILLISECONDS);
                    if (aBoolean != null && aBoolean) {
                        //占位成功说明从来没有买过,分布式锁(获取信号量-1)
                        RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + randomCode);
                        //TODO 秒杀成功，快速下单
                        boolean semaphoreCount = semaphore.tryAcquire(num, 100, TimeUnit.MILLISECONDS);
                        //保证Redis中还有商品库存
                        if (semaphoreCount) {
                            //创建订单号和订单信息发送给MQ
                            // 秒杀成功 快速下单 发送消息到 MQ 整个操作时间在 10ms 左右
                            String timeId = IdWorker.getTimeId();
                            SeckillOrderTo orderTo = new SeckillOrderTo();
                            orderTo.setOrderSn(timeId);
                            orderTo.setMemberId(user.getId());
                            orderTo.setNum(num);
                            orderTo.setPromotionSessionId(redisTo.getPromotionSessionId());
                            orderTo.setSkuId(redisTo.getSkuId());
                            orderTo.setSeckillPrice(redisTo.getSeckillPrice());
                            rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", orderTo);
                            return timeId;
                        }
                    }
                }
            }
        }
        return null;
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
