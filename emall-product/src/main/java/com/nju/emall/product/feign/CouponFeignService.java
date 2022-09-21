package com.nju.emall.product.feign;

import com.nju.common.to.SkuReductionTo;
import com.nju.common.to.SpuBoundTo;
import com.nju.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @description
 * @date:2022/9/21 15:47
 * @author: qyl
 */
@FeignClient("emall-coupon")
public interface CouponFeignService {
    @PostMapping("/coupon/spubounds/save")
    R saveBounds(@RequestBody SpuBoundTo spuBoundTo);

    @PostMapping("/coupon/skufullreduction/saveinfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
