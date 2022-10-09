package com.nju.emall.seckill.config;

import com.alibaba.csp.sentinel.adapter.servlet.callback.WebCallbackManager;
import com.alibaba.fastjson.JSON;
import com.nju.common.exception.BizCodeEnum;
import com.nju.common.utils.R;
import org.springframework.context.annotation.Configuration;

/**
 * @author asus
 */
@Configuration
public class EmallSeckillSentinelConfig {

    public EmallSeckillSentinelConfig() {

        WebCallbackManager.setUrlBlockHandler((request, response, ex) -> {
            R error = R.error(BizCodeEnum.TO_MANY_REQUEST.getCode(), BizCodeEnum.TO_MANY_REQUEST.getMessage());
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.getWriter().write(JSON.toJSONString(error));
        });

    }

}
