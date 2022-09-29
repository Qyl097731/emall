package com.nju.emall.member.exception;

/**
 * @description
 * @date:2022/9/29 9:07
 * @author: qyl
 */
public class UsernameExistException extends RuntimeException {
    public UsernameExistException() {
        super("用户名已经存在");
    }
}
