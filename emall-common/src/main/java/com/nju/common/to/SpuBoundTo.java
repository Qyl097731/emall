package com.nju.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @description
 * @date:2022/9/21 16:07
 * @author: qyl
 */
@Data
public class SpuBoundTo {
    private Long spuId;
    private BigDecimal buyBounds;
    private BigDecimal growBounds;
}
