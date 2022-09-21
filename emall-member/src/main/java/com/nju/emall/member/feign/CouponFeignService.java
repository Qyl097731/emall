package com.nju.emall.member.feign;

import com.nju.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @description
 * @date:2022/9/15 23:46
 * @author: qyl
 */
@FeignClient("emall-member")
public interface CouponFeignService {
    @RequestMapping("coupon/coupon/member/list")
    R memberCoupons() ;
}
