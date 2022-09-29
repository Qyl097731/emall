package com.nju.emall.member.exception;

/**
 * @description
 * @date:2022/9/29 9:07
 * @author: qyl
 */
public class PhoneExistException extends RuntimeException {
    public PhoneExistException() {
        super("手机号存在");
    }
}
