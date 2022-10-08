package com.nju.emall.seckill.feign;

import com.nju.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @description
 * @date:2022/10/8 21:02
 * @author: qyl
 */
@FeignClient("emall-coupon")
public interface CouponFeignService {
    @GetMapping(value = "coupon/seckillsession/latest3DaySession")
    R getLatest3DaySession();
}
