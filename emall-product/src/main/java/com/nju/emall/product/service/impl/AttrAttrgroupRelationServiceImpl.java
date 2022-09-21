package com.nju.emall.product.service.impl;

import com.nju.emall.product.vo.AttrGroupRelationVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nju.common.utils.PageUtils;
import com.nju.common.utils.Query;

import com.nju.emall.product.dao.AttrAttrgroupRelationDao;
import com.nju.emall.product.entity.AttrAttrgroupRelationEntity;
import com.nju.emall.product.service.AttrAttrgroupRelationService;


@Service("attrAttrgroupRelationService")
public class AttrAttrgroupRelationServiceImpl extends ServiceImpl<AttrAttrgroupRelationDao, AttrAttrgroupRelationEntity> implements AttrAttrgroupRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrAttrgroupRelationEntity> page = this.page(
                new Query<AttrAttrgroupRelationEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveRelations(List<AttrGroupRelationVo> vos) {
        if (!CollectionUtils.isEmpty(vos)) {
            List<AttrAttrgroupRelationEntity> relationEntities = vos.stream().map(vo -> {
                AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
                BeanUtils.copyProperties(vo, relationEntity);
                return relationEntity;
            }).collect(Collectors.toList());
            this.saveBatch(relationEntities);
        }

    }


}
