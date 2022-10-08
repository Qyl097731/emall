package com.nju.common.exception;

import lombok.NoArgsConstructor;

/**
 * @description
 * @date:2022/10/5 19:53
 * @author: qyl
 */
@NoArgsConstructor
public class NoStockException extends RuntimeException {


    public NoStockException(Long skuId){
        super("商品id " + skuId + ", 没有足够的库存了");
    }
}
