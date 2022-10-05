package com.nju.emall.order.feign;

import com.nju.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @description
 * @date:2022/10/4 22:56
 * @author: qyl
 */
@FeignClient("emall-ware")
public interface WmsFeignService {
    @PostMapping("ware/waresku/hasstock")
    R hasstock(@RequestBody List<Long> skuIds);
}
