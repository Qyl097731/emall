package com.nju.emall.authserver.feign;

import com.nju.common.utils.R;
import com.nju.emall.authserver.vo.SocialUser;
import com.nju.emall.authserver.vo.UserLoginVo;
import com.nju.emall.authserver.vo.UserRegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @description
 * @date:2022/9/29 9:32
 * @author: qyl
 */
@FeignClient("emall-member")
public interface MemberFeignSerivce {

    @PostMapping(value = "member/member/register")
    R register(@RequestBody UserRegisterVo vo);

    @PostMapping("member/member/oauth/login")
    R oauthLogin(@RequestBody SocialUser vo);

    @PostMapping("member/member/login")
    R login(@RequestBody UserLoginVo vo);
}
