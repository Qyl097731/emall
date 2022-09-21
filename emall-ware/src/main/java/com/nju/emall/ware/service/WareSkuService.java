package com.nju.emall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nju.common.utils.PageUtils;
import com.nju.emall.ware.entity.WareSkuEntity;

import java.util.Map;

/**
 * 商品库存
 *
 * @author qyl
 * @email 553579048@qq.com
 * @date 2022-09-15 21:46:18
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

