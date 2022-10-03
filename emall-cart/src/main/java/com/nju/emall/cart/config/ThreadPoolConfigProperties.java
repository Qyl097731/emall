package com.nju.emall.cart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @description
 * @date:2022/9/28 16:32
 * @author: qyl
 */
@Configuration
@ConfigurationProperties(prefix = "emall.thread")
@Data
public class ThreadPoolConfigProperties {
    private Integer coreSize;
    private Integer maxSize;
    private Integer keepAliveTime;
}
