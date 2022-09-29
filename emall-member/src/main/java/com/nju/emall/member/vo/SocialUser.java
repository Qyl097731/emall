package com.nju.emall.member.vo;

import lombok.Data;


@Data
public class SocialUser {

    private String access_token;

    private String remind_in;

    private long expires_in;

    private String isRealName;

    private OauthUserInfoVo info;

    @Data
    public static class OauthUserInfoVo {
        private Long id;
        private String login;
        private String name;
        private String avatarUrl;
        private String remark;
        private String email;
    }

}
