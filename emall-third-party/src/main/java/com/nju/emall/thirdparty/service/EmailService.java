package com.nju.emall.thirdparty.service;

/**
 * @description
 * @date:2022/9/28 18:40
 * @author: qyl
 */
public interface EmailService {
    boolean sendSimpleMail(String to,String code);
}
