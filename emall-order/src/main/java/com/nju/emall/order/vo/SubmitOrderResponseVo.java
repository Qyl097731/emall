package com.nju.emall.order.vo;

import com.nju.emall.order.entity.OrderEntity;
import lombok.Data;


/**
 * @author asus
 */
@Data
public class SubmitOrderResponseVo {

    private OrderEntity order;

    /** 错误状态码 **/
    private Integer code;


}
