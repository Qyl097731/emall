package com.nju.emall.thirdparty.controller;

import com.nju.common.utils.R;
import com.nju.emall.thirdparty.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @description
 * @date:2022/9/28 19:37
 * @author: qyl
 */
@RestController
@RequestMapping("email")
public class EmailController {
    @Autowired
    private EmailService emailService;

    @PostMapping("/sendCode")
    public R sendCode(@RequestParam("to") String to,@RequestParam("code") String code){
        emailService.sendSimpleMail(to,code);
        return R.ok();
    }
}
