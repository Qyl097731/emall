package com.nju.emall.authserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRedisHttpSession
@EnableDiscoveryClient
@EnableFeignClients("com.nju.emall.authserver.feign")
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class EmallAuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmallAuthServerApplication.class, args);
    }

}
