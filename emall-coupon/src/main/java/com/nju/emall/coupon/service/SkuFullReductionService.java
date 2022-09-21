package com.nju.emall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nju.common.to.SkuReductionTo;
import com.nju.common.utils.PageUtils;
import com.nju.emall.coupon.entity.SkuFullReductionEntity;

import java.util.Map;

/**
 * 商品满减信息
 *
 * @author qyl
 * @email 553579048@qq.com
 * @date 2022-09-15 21:08:18
 */
public interface SkuFullReductionService extends IService<SkuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuReduction(SkuReductionTo skuReductionTo);
}

