package com.nju.emall.thirdparty.service.impl;

import com.nju.emall.thirdparty.constant.EmailConst;
import com.nju.emall.thirdparty.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * @description
 * @date:2022/9/28 18:41
 * @author: qyl
 */
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    /**
     * 最简单的邮件发送
     *
     * @return
     */
    @Override
    public boolean sendSimpleMail(String to,String code) {
        String content = EmailConst.CONTENT.replace("{0}", code);
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(EmailConst.FROM);
        simpleMailMessage.setTo(to);
        simpleMailMessage.setSubject(EmailConst.SUBJECT);
        simpleMailMessage.setText(content);
        javaMailSender.send(simpleMailMessage);
        return true;
    }
}
