package com.nju.emall.product.fallback;

import com.nju.common.exception.BizCodeEnum;
import com.nju.common.utils.R;
import com.nju.emall.product.feign.SeckillFeignService;
import org.springframework.stereotype.Component;

@Component
public class SeckillFeignServiceFallBack implements SeckillFeignService {
    @Override
    public R getSkuSeckilInfo(Long skuId) {
        return R.error(BizCodeEnum.TO_MANY_REQUEST.getCode(),BizCodeEnum.TO_MANY_REQUEST.getMessage());
    }
}
