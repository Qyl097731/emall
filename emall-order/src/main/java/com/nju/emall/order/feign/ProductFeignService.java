package com.nju.emall.order.feign;

import com.nju.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @description
 * @date:2022/10/5 15:22
 * @author: qyl
 */
@FeignClient("emall-product")
public interface ProductFeignService {
    @GetMapping("product/spuinfo/spuInfo/{skuId}")
    R getSpuInfoBySkuId(@PathVariable("skuId")Long skuId);
}
