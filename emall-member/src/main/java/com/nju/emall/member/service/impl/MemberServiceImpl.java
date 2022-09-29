package com.nju.emall.member.service.impl;

import com.nju.common.utils.HttpUtils;
import com.nju.emall.member.dao.MemberLevelDao;
import com.nju.emall.member.entity.MemberLevelEntity;
import com.nju.emall.member.exception.PhoneExistException;
import com.nju.emall.member.exception.UsernameExistException;
import com.nju.emall.member.service.MemberLevelService;
import com.nju.emall.member.vo.MemberUserLoginVo;
import com.nju.emall.member.vo.MemberUserRegisterVo;
import com.nju.emall.member.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nju.common.utils.PageUtils;
import com.nju.common.utils.Query;

import com.nju.emall.member.dao.MemberDao;
import com.nju.emall.member.entity.MemberEntity;
import com.nju.emall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(MemberUserRegisterVo vo) {
        MemberEntity memberEntity = new MemberEntity();

        MemberLevelEntity levelEntity = memberLevelDao.getDefaultLevel();
        memberEntity.setLevelId(levelEntity.getId());

        checkPhoneExist(vo.getPhone());
        checkUsernameExist(vo.getUserName());

        memberEntity.setUsername(vo.getUserName());
        memberEntity.setNickname(vo.getUserName());
        memberEntity.setMobile(vo.getPhone());
        memberEntity.setEmail(vo.getPhone());

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = encoder.encode(vo.getPassword());
        memberEntity.setPassword(password);

        baseMapper.insert(memberEntity);
    }

    @Override
    public MemberEntity login(MemberUserLoginVo vo) {
        String loginacct = vo.getLoginacct();
        String password = vo.getPassword();
        MemberEntity memberEntity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginacct).or().eq("mobile", loginacct));
        if (memberEntity == null) {
            return null;
        } else {
            String entityPassword = memberEntity.getPassword();
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            if (!encoder.matches(password, entityPassword)) {
                return null;
            }
        }
        return memberEntity;

    }

    @Override
    public MemberEntity login(SocialUser vo) {
        SocialUser.OauthUserInfoVo info = vo.getInfo();
        MemberEntity memberEntity = new MemberEntity();
        if (info != null) {
            String uid = String.valueOf(info.getId());
            MemberEntity existMember = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid",
                    uid));
            if (existMember != null) {
                memberEntity.setId(existMember.getId());
                memberEntity.setAccessToken(vo.getAccess_token());
                memberEntity.setExpiresIn(vo.getExpires_in());
                memberEntity.setNickname(info.getName());
                baseMapper.updateById(memberEntity);
                existMember.setAccessToken(vo.getAccess_token());
                existMember.setExpiresIn(vo.getExpires_in());
                existMember.setNickname(info.getName());
                return existMember;
            } else {
                memberEntity.setSocialUid(uid);
                memberEntity.setExpiresIn(vo.getExpires_in());
                memberEntity.setAccessToken(vo.getAccess_token());
                memberEntity.setNickname(info.getName());
                baseMapper.insert(memberEntity);
                return memberEntity;
            }
        }
        return memberEntity;
    }

    private void checkPhoneExist(String phone) {
        Long count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (count > 0) {
            throw new PhoneExistException();
        }
    }

    private void checkUsernameExist(String userName) {
        Long count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", userName));
        if (count > 0) {
            throw new UsernameExistException();
        }
    }

}
