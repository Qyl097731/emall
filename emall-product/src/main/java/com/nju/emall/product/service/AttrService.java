package com.nju.emall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nju.common.utils.PageUtils;
import com.nju.emall.product.entity.AttrEntity;
import com.nju.emall.product.vo.AttrRespVo;
import com.nju.emall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author qyl
 * @email 553579048@qq.com
 * @date 2022-09-16 23:50:22
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attr);

    PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String attrType);

    AttrRespVo getAttrInfo(Long attrId);

    void updateAttr(AttrVo attr);

    List<AttrEntity> getRelationAttr(Long attrgroupId);

    PageUtils queryNoAttrRelationsPage(Map<String, Object> params, Long attrGroupId);

}

