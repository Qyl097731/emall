package com.nju.emall.authserver.feign;

import com.nju.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @description
 * @date:2022/9/28 19:42
 * @author: qyl
 */
@FeignClient("emall-third-party")
public interface ThirdPartFeignService {

    @PostMapping("/email/sendCode")
    R sendCode(@RequestParam("to") String to, @RequestParam("code") String code);
}
