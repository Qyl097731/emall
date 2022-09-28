package com.nju.emall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @description
 * @date:2022/9/22 11:17
 * @author: qyl
 */
@Data
public class PurchaseDoneVo {
    private Long id;
    List<PurchaseItemDoneVo> items;
}
