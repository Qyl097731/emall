package com.nju.emall.thirdparty.constant;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @description
 * @date:2022/9/28 18:34
 * @author: qyl
 */
@Component
public class EmailConst implements InitializingBean {
    //读取配置文件内容
    //读取配置文件内容
    @Value("${spring.mail.username}")
    private String from;
    //定义邮件
    public static String FROM;
    public static String SUBJECT;
    public static String CONTENT;

    @Override
    public void afterPropertiesSet()  {
        FROM = from;
        SUBJECT = "电商平台";
        CONTENT = "【"+SUBJECT+"】您好，您正在使用邮箱认证，邮箱验证码为：{0},有效期1分钟。";
    }
}
