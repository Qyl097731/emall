package com.nju.emall.product.feign;

import com.nju.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @description
 * @date:2022/9/24 15:08
 * @author: qyl
 */
@FeignClient("emall-ware")
public interface WareFeignService {

    @PostMapping("ware/waresku/hasstock")
    R hasstock(@RequestBody List<Long> skuIds) ;
}
