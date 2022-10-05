package com.nju.emall.order.feign;

import com.nju.emall.order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @description
 * @date:2022/10/4 19:18
 * @author: qyl
 */
@FeignClient("emall-member")
public interface MemberFeignService {
    @GetMapping(value = "/member/memberreceiveaddress/{memberId}/address")
    List<MemberAddressVo> getAddress(@PathVariable("memberId") Long memberId);
}
