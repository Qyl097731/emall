package com.nju.emall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nju.common.utils.PageUtils;
import com.nju.emall.ware.entity.WareOrderTaskEntity;

import java.util.Map;

/**
 * 库存工作单
 *
 * @author qyl
 * @email 553579048@qq.com
 * @date 2022-09-15 21:46:19
 */
public interface WareOrderTaskService extends IService<WareOrderTaskEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

