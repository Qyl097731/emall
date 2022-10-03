package com.nju.emall.authserver.constant;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author asus
 */
@ConfigurationProperties(prefix = "oauth.gitee")
@Component
@Data
public class GiteeConst {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String grantType;
}
