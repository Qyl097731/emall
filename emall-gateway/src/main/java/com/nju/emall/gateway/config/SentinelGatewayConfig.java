package com.nju.emall.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.fastjson.JSON;
import com.nju.common.exception.BizCodeEnum;
import com.nju.common.utils.R;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Configuration
public class SentinelGatewayConfig {

    public SentinelGatewayConfig() {
        //网关限流了请求，就会调用此回调
        GatewayCallbackManager.setBlockHandler((exchange, t) -> {

            R error = R.error(BizCodeEnum.TO_MANY_REQUEST.getCode(), BizCodeEnum.TO_MANY_REQUEST.getMessage());
            String errorJson = JSON.toJSONString(error);

            return ServerResponse.ok().body(Mono.just(errorJson), String.class);
        });
    }

}
