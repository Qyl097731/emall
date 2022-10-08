package com.nju.emall.member;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRedisHttpSession
@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients
@MapperScan("com.nju.emall.member.dao")
public class EmallMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmallMemberApplication.class, args);
    }

}
