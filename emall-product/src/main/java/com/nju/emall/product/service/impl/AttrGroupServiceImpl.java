package com.nju.emall.product.service.impl;

import com.nju.emall.product.entity.AttrAttrgroupRelationEntity;
import com.nju.emall.product.service.AttrService;
import com.nju.emall.product.vo.AttrGroupRelationVo;
import com.nju.emall.product.vo.AttrGroupWithAttrsVo;
import com.nju.emall.product.vo.SpuItemAttrGroupVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nju.common.utils.PageUtils;
import com.nju.common.utils.Query;

import com.nju.emall.product.dao.AttrGroupDao;
import com.nju.emall.product.entity.AttrGroupEntity;
import com.nju.emall.product.service.AttrGroupService;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        String key = (String) params.get("key");
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        if (catelogId != 0) {
            wrapper.eq("catelog_id", catelogId);
        }
        if (StringUtils.isNotEmpty(key)) {
            wrapper.and((obj ->
                    obj.like("attr_group_id", key).or().like("attr_group_name", key)));
        }
        IPage<AttrGroupEntity> iPage = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
        return new PageUtils(iPage);
    }

    @Override
    public void deleteRelation(List<AttrGroupRelationVo> vos) {
        List<AttrAttrgroupRelationEntity> relationEntities = vos.stream().map(item -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item, relationEntity);
            return relationEntity;
        }).collect(Collectors.toList());
        attrGroupDao.deleteRelation(relationEntities);
    }

    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        List<AttrGroupWithAttrsVo> vos = new ArrayList<>();
        List<AttrGroupEntity> groupEntities = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        if (!CollectionUtils.isEmpty(groupEntities)) {
            vos = groupEntities.stream().map(group -> {
                AttrGroupWithAttrsVo vo = new AttrGroupWithAttrsVo();
                BeanUtils.copyProperties(group, vo);
                vo.setAttrs(attrService.getRelationAttr(group.getAttrGroupId()));
                return vo;
            }).collect(Collectors.toList());
        }
        return vos;
    }

    @Override
    public List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId) {
        return baseMapper.getAttrGroupWithAttrsBySpuId(spuId, catalogId);
    }

}
