package com.nju.emall.product.dao;

import com.nju.emall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author qyl
 * @email 553579048@qq.com
 * @date 2022-09-16 23:50:21
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
