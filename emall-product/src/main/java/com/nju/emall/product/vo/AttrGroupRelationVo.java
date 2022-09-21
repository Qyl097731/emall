package com.nju.emall.product.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @description
 * @date:2022/9/20 20:00
 * @author: qyl
 */
@Data
@Accessors(chain = true)
public class AttrGroupRelationVo {
    private Long attrId;
    private Long attrGroupId;
}
