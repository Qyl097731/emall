package com.nju.emall.coupon.dao;

import com.nju.emall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author qyl
 * @email 553579048@qq.com
 * @date 2022-09-15 21:08:18
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
