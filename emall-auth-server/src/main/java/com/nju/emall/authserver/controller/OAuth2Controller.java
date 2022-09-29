package com.nju.emall.authserver.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.nju.common.constant.AuthServerConstant;
import com.nju.common.utils.HttpUtils;
import com.nju.common.utils.R;
import com.nju.common.vo.MemberResponseVo;
import com.nju.emall.authserver.constant.GiteeConst;
import com.nju.emall.authserver.feign.MemberFeignSerivce;
import com.nju.emall.authserver.vo.SocialUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Controller
public class OAuth2Controller {

    @Autowired
    private MemberFeignSerivce memberFeignService;

    @Autowired
    GiteeConst giteeConst;

    @GetMapping(value = "/oauth2.0/gitee/success")
    public String gitee(@RequestParam("code") String code, HttpSession session) throws Exception {

        Map<String, String> map = new HashMap<>();
        map.put("client_id", giteeConst.getClientId());
        map.put("client_secret", giteeConst.getClientSecret());
        map.put("grant_type", giteeConst.getGrantType());
        map.put("redirect_uri", giteeConst.getRedirectUri());
        map.put("code", code);
        //1、根据用户授权返回的code换取access_token
        HttpResponse response = HttpUtils.doPost("https://gitee.com", "/oauth/token", "post", new HashMap<>(), map, new HashMap<>());
        //2、处理
        if (response.getStatusLine().getStatusCode() == 200) {
            //获取到了access_token,转为通用社交登录对象
            String json = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);

            HttpResponse infoResp = HttpUtils.doGet("https://gitee.com", "/api/v5/user", "get", new HashMap<>(), new HashMap<String, String>() {{
                put("access_token", socialUser.getAccess_token());
            }});
            if (infoResp.getStatusLine().getStatusCode() == 200) {
                String userInfo = EntityUtils.toString(infoResp.getEntity());
                SocialUser.OauthUserInfoVo oauthUserInfoVo = JSON.parseObject(userInfo, SocialUser.OauthUserInfoVo.class);
                socialUser.setInfo(oauthUserInfoVo);

                R oauthLogin = memberFeignService.oauthLogin(socialUser);
                if (oauthLogin.getCode() == 0) {
                    MemberResponseVo data = oauthLogin.getData("data", new TypeReference<MemberResponseVo>() {
                    });
                    log.info("登录成功：用户信息：{}", data.toString());

                    //1、第一次使用session，命令浏览器保存卡号，JSESSIONID这个cookie
                    //以后浏览器访问哪个网站就会带上这个网站的cookie
                    //TODO 1、默认发的令牌。当前域（解决子域session共享问题）
                    //TODO 2、使用JSON的序列化方式来序列化对象到Redis中
                    session.setAttribute(AuthServerConstant.LOGIN_USER, data);
                    //2、登录成功跳回首页
                    return "redirect:http://emall.com";
                }
            }
        }
        return "redirect:http://auth.emall.com/login.html";
    }
}
