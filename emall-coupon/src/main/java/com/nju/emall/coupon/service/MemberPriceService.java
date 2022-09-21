package com.nju.emall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nju.common.utils.PageUtils;
import com.nju.emall.coupon.entity.MemberPriceEntity;

import java.util.Map;

/**
 * 商品会员价格
 *
 * @author qyl
 * @email 553579048@qq.com
 * @date 2022-09-15 21:08:18
 */
public interface MemberPriceService extends IService<MemberPriceEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

