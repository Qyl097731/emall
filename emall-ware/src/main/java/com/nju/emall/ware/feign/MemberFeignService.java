package com.nju.emall.ware.feign;

import com.nju.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @description
 * @date:2022/10/4 23:18
 * @author: qyl
 */
@FeignClient("emall-member")
public interface MemberFeignService {
    @RequestMapping("member/memberreceiveaddress/info/{id}")
    R info(@PathVariable("id") Long id);
}
