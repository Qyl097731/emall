package com.nju.emall.member.feign;

import com.nju.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @description
 * @date:2022/10/8 11:38
 * @author: qyl
 */
@FeignClient("emall-order")
public interface OrderFeignService {
    @PostMapping("order/order/listWithItem")
    R listWithItem(@RequestBody Map<String, Object> params);
}
