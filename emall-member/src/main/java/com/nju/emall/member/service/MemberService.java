package com.nju.emall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nju.common.utils.PageUtils;
import com.nju.emall.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author qyl
 * @email 553579048@qq.com
 * @date 2022-09-15 21:31:48
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

