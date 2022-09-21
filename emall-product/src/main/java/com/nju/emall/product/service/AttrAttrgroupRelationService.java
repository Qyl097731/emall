package com.nju.emall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nju.common.utils.PageUtils;
import com.nju.emall.product.entity.AttrAttrgroupRelationEntity;
import com.nju.emall.product.vo.AttrGroupRelationVo;

import java.util.List;
import java.util.Map;

/**
 * 属性&属性分组关联
 *
 * @author qyl
 * @email 553579048@qq.com
 * @date 2022-09-16 23:50:22
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveRelations(List<AttrGroupRelationVo> vos);
}

