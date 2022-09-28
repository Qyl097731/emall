package com.nju.emall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.nju.emall.product.entity.AttrEntity;
import com.nju.emall.product.service.AttrAttrgroupRelationService;
import com.nju.emall.product.service.AttrService;
import com.nju.emall.product.service.CategoryService;
import com.nju.emall.product.vo.AttrGroupRelationVo;
import com.nju.emall.product.vo.AttrGroupWithAttrsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nju.emall.product.entity.AttrGroupEntity;
import com.nju.emall.product.service.AttrGroupService;
import com.nju.common.utils.PageUtils;
import com.nju.common.utils.R;


/**
 * 属性分组
 *
 * @author qyl
 * @email 553579048@qq.com
 * @date 2022-09-16 23:50:22
 */
@Slf4j
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;


    /**
     * 删除
     */
    @PostMapping("attr/relation/delete")
    public R deleteRelation(@RequestBody AttrGroupRelationVo[] vos) {
        attrGroupService.deleteRelation(Arrays.asList(vos));

        return R.ok();
    }

    @GetMapping("{catelogId}/withattr")
    public R getAttrGroupWithAttrs(@PathVariable Long catelogId){
        List<AttrGroupWithAttrsVo> vos = attrGroupService.getAttrGroupWithAttrsByCatelogId(catelogId);
        return R.ok().put("data", vos);

    }

    /**
     * 增加关联
     */
    @PostMapping("attr/relation")
    public R relation(@RequestBody List<AttrGroupRelationVo> vos) {
        attrAttrgroupRelationService.saveRelations(vos);
        return R.ok();
    }

    /**
     * 列表
     */
    @GetMapping("/list/{catelogId}")
    public R list(@RequestParam Map<String, Object> params,
                  @PathVariable(name = "catelogId") Long catelogId) {
        PageUtils page = attrGroupService.queryPage(params, catelogId);

        return R.ok().put("page", page);
    }


    @GetMapping("{attrgroupId}/attr/relation")
    public R attrRelations(@PathVariable(name = "attrgroupId") Long attrgroupId) {
        List<AttrEntity> attrs = attrService.getRelationAttr(attrgroupId);

        return R.ok().put("data", attrs);
    }

    @GetMapping("{attrGroupId}/noattr/relation")
    public R noAttrRelations(@RequestParam Map<String, Object> params, @PathVariable("attrGroupId") Long attrGroupId) {
        PageUtils page = attrService.queryNoAttrRelationsPage(params,attrGroupId);
        return R.ok().put("page", page);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId) {
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long catelogId = attrGroup.getCatelogId();
        Long[] path = categoryService.findCatelogPath(catelogId);
        attrGroup.setCatelogPath(path);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrGroupIds) {
        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
