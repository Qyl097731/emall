package com.nju.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @description
 * @date:2022/9/21 17:08
 * @author: qyl
 */
@Data
public class SkuReductionTo {
    private Long skuId;
    private int fullCount;
    private BigDecimal discount;
    private int countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private int priceStatus;
    private List<MemberPrice> memberPrice;
}
