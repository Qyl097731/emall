package com.nju.emall.ware.feign;

import com.nju.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @description
 * @date:2022/9/22 13:09
 * @author: qyl
 */
@FeignClient("emall-product")
public interface ProductFeignService {

    @GetMapping("product/skuinfo/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);
}
