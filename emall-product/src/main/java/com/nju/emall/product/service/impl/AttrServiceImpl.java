package com.nju.emall.product.service.impl;

import com.nju.common.constant.ProductConst;
import com.nju.emall.product.entity.AttrAttrgroupRelationEntity;
import com.nju.emall.product.entity.AttrGroupEntity;
import com.nju.emall.product.entity.CategoryEntity;
import com.nju.emall.product.service.AttrAttrgroupRelationService;
import com.nju.emall.product.service.AttrGroupService;
import com.nju.emall.product.service.CategoryService;
import com.nju.emall.product.vo.AttrRespVo;
import com.nju.emall.product.vo.AttrVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nju.common.utils.PageUtils;
import com.nju.common.utils.Query;

import com.nju.emall.product.dao.AttrDao;
import com.nju.emall.product.entity.AttrEntity;
import com.nju.emall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity, "attrGroupId");
        save(attrEntity);
        if (attr.getAttrGroupId() != null && attr.getAttrType().equals(ProductConst.AttrType.BASE.getCode())) {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationService.save(relationEntity);
        }
    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String attrType) {
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("attr_type",
                ProductConst.AttrType.SALE.getMsg().equalsIgnoreCase(attrType) ?
                        ProductConst.AttrType.SALE.getCode() :
                        ProductConst.AttrType.BASE.getCode());
        if (catelogId != 0) {
            wrapper.eq("catelog_id", catelogId);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(item -> wrapper.like("attr_id", key)).or().like("attr_name", key);
        }

        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);

        List<AttrEntity> records = page.getRecords();

        List<AttrRespVo> respVos = records
                .stream()
                .map(record -> getAttrInfoWithoutPath(record.getAttrId()))
                .collect(Collectors.toList());

        PageUtils pageUtils = new PageUtils(page);
        pageUtils.setList(respVos);
        return pageUtils;
    }

    @Override
    public AttrRespVo getAttrInfo(Long attrId) {
        AttrRespVo attrRespVo = getAttrInfoWithoutPath(attrId);
        AttrEntity attrEntity = baseMapper.selectById(attrId);
        if (attrEntity != null) {
            attrRespVo.setCatelogPath(categoryService.findCatelogPath(attrEntity.getCatelogId()));
        }
        return attrRespVo;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        baseMapper.updateById(attrEntity);

        if (ProductConst.AttrType.BASE.getCode().equals(attrEntity.getAttrType())) {

            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrId(attr.getAttrId());
            relationEntity.setAttrGroupId(attr.getAttrGroupId());

            QueryWrapper<AttrAttrgroupRelationEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("attr_id", attr.getAttrId());
            if (attrAttrgroupRelationService.count(wrapper) > 0) {
                attrAttrgroupRelationService.update(relationEntity, wrapper);
            } else {
                attrAttrgroupRelationService.save(relationEntity);
            }
        }
    }

    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> relationEntities = attrAttrgroupRelationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id",
                attrgroupId));
        if (!CollectionUtils.isEmpty(relationEntities)) {
            List<Long> ids = relationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
            return baseMapper.selectBatchIds(ids);
        }
        return null;
    }

    @Override
    public PageUtils queryNoAttrRelationsPage(Map<String, Object> params, Long attrGroupId) {
        QueryWrapper<AttrAttrgroupRelationEntity> wrapper = new QueryWrapper<>();

        wrapper.eq("attr_group_id", attrGroupId).select("attr_id");

        IPage<AttrAttrgroupRelationEntity> page = attrAttrgroupRelationService.page(new Query<AttrAttrgroupRelationEntity>().getPage(params),
                wrapper);

        List<AttrEntity> entities = new ArrayList<>();
        List<AttrAttrgroupRelationEntity> others = page.getRecords();
        if (!CollectionUtils.isEmpty(others)) {
            String key = (String) params.get("key");
            QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<>();
            if (!StringUtils.isEmpty(key)) {
                queryWrapper.like("attr_name", key);
            }
            List<Long> existIds = others
                    .stream()
                    .map(AttrAttrgroupRelationEntity::getAttrId)
                    .collect(Collectors.toList());

            List<AttrEntity> all = baseMapper.selectList(queryWrapper);
            List<Long> ids = all.stream()
                    .filter(item -> !existIds.contains(item.getAttrId()))
                    .map(AttrEntity::getAttrId)
                    .collect(Collectors.toList());
            entities = baseMapper.selectBatchIds(ids);
        }
        PageUtils pageUtils = new PageUtils(page);
        pageUtils.setList(entities);
        return pageUtils;
    }

    private AttrRespVo getAttrInfoWithoutPath(Long attrId) {
        AttrRespVo attrRespVo = new AttrRespVo();
        AttrEntity attrEntity = baseMapper.selectById(attrId);
        BeanUtils.copyProperties(attrEntity, attrRespVo);
        // 插入类别
        CategoryEntity categoryEntity = categoryService.getById(attrEntity.getCatelogId());
        if (categoryEntity != null) {
            attrRespVo.setCatelogName(categoryEntity.getName());
        }
        if (ProductConst.AttrType.BASE.getCode().equals(attrEntity.getAttrType())) {
            // 插入组名
            AttrAttrgroupRelationEntity relationEntity =
                    attrAttrgroupRelationService.getOne(new QueryWrapper<AttrAttrgroupRelationEntity>()
                            .eq("attr_id", attrId));
            if (relationEntity != null) {
                AttrGroupEntity groupEntity = attrGroupService.getById(relationEntity.getAttrGroupId());
                attrRespVo.setAttrGroupId(relationEntity.getAttrGroupId());
                if (groupEntity != null) {
                    attrRespVo.setAttrGroupName(groupEntity.getAttrGroupName());
                }
            }
        }

        return attrRespVo;
    }

}
