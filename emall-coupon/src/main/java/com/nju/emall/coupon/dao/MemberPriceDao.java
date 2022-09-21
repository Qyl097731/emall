package com.nju.emall.coupon.dao;

import com.nju.emall.coupon.entity.MemberPriceEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品会员价格
 * 
 * @author qyl
 * @email 553579048@qq.com
 * @date 2022-09-15 21:08:18
 */
@Mapper
public interface MemberPriceDao extends BaseMapper<MemberPriceEntity> {
	
}
