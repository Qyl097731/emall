package com.nju.common.to;

import lombok.Data;

/**
 * @description
 * @date:2022/9/24 14:56
 * @author: qyl
 */
@Data
public class SkuHasStockTo {
    private Long skuId;
    private Boolean hasStock;
}
