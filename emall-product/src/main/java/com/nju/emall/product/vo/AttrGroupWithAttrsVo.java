package com.nju.emall.product.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.nju.emall.product.entity.AttrEntity;
import lombok.Data;

import java.util.List;

/**
 * @description
 * @date:2022/9/21 11:29
 * @author: qyl
 */
@Data
public class AttrGroupWithAttrsVo {
    private Long attrGroupId;
    /**
     * 组名
     */
    private String attrGroupName;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 描述
     */
    private String descript;
    /**
     * 组图标
     */
    private String icon;
    /**
     * 所属分类id
     */
    private Long catelogId;

    private List<AttrEntity> attrs;

}
