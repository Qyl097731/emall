package com.nju.emall.order.feign;

import com.nju.common.utils.R;
import com.nju.emall.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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

    @GetMapping("ware/wareinfo/fare")
    R getFare(@RequestParam("addrId") Long addrId);

    @PostMapping("ware/waresku/lock/order")
    R orderLockStock(@RequestBody WareSkuLockVo vo);
}
