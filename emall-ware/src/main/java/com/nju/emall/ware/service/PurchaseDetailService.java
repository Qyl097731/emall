package com.nju.emall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nju.common.utils.PageUtils;
import com.nju.emall.ware.entity.PurchaseDetailEntity;

import java.util.Map;

/**
 * 
 *
 * @author qyl
 * @email 553579048@qq.com
 * @date 2022-09-15 21:46:18
 */
public interface PurchaseDetailService extends IService<PurchaseDetailEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

