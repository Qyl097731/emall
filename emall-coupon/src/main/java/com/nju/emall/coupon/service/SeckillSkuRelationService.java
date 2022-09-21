package com.nju.emall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nju.common.utils.PageUtils;
import com.nju.emall.coupon.entity.SeckillSkuRelationEntity;

import java.util.Map;

/**
 * 秒杀活动商品关联
 *
 * @author qyl
 * @email 553579048@qq.com
 * @date 2022-09-15 21:08:18
 */
public interface SeckillSkuRelationService extends IService<SeckillSkuRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

