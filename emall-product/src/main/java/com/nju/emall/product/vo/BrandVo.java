package com.nju.emall.product.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @description
 * @date:2022/9/21 10:34
 * @author: qyl
 */
@Data
@Accessors(chain = true)
public class BrandVo {
    private Long brandId;
    private String brandName;
}
