package com.nju.emall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @description
 * @date:2022/9/22 9:48
 * @author: qyl
 */
@Data
public class MergeVo {
    private Long purchaseId;
    private List<Long> items;
}
