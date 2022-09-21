package com.nju.emall.product.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @description
 * @date:2022/9/20 14:43
 * @author: qyl
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AttrRespVo extends AttrVo {
    private String catelogName;
    private String attrGroupName;
    private Long[] catelogPath;
    private Long catelogId;
}
