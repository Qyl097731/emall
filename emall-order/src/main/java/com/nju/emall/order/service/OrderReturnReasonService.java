package com.nju.emall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nju.common.utils.PageUtils;
import com.nju.emall.order.entity.OrderReturnReasonEntity;

import java.util.Map;

/**
 * 退货原因
 *
 * @author qyl
 * @email 553579048@qq.com
 * @date 2022-09-15 21:45:13
 */
public interface OrderReturnReasonService extends IService<OrderReturnReasonEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

