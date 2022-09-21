package com.nju.emall.thirdparty.oss;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @description
 * @date:2022/9/19 8:19
 * @author: qyl
 */
@Component
@Data
public class OssConst {
    //读取配置文件
    @Value("${spring.cloud.alicloud.oss.endpoint}")
    private String endpoint;

    @Value("${spring.cloud.alicloud.oss.bucket}")
    private String bucket;

    @Value("${spring.cloud.alicloud.access-key}")
    private String accessId;

    @Value("${spring.cloud.alicloud.secret-key}")
    private String secretKey;
}
