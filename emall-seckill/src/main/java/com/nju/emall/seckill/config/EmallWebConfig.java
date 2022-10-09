package com.nju.emall.seckill.config;

import com.nju.emall.seckill.interceptor.SeckillInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @description
 * @date:2022/9/28 18:09
 * @author: qyl
 */
@Configuration
public class EmallWebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SeckillInterceptor()).addPathPatterns("/**");
    }
}
