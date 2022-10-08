package com.nju.emall.ware.feign;

import com.nju.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @description
 * @date:2022/10/7 18:22
 * @author: qyl
 */
@FeignClient("emall-order")
public interface OrderFeignService {
    @GetMapping("order/order/status/{orderSn}")
    R getOrderStatus(@PathVariable("orderSn") String orderSn);
}
