package com.nju.emall.member.dao;

import com.nju.emall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author qyl
 * @email 553579048@qq.com
 * @date 2022-09-15 21:31:48
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
