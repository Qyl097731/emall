package com.nju.emall.order.dao;

import com.nju.emall.order.entity.OrderSettingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单配置信息
 * 
 * @author qyl
 * @email 553579048@qq.com
 * @date 2022-09-15 21:45:13
 */
@Mapper
public interface OrderSettingDao extends BaseMapper<OrderSettingEntity> {
	
}
