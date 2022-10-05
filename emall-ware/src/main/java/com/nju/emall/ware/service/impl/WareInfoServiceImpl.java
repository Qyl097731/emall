package com.nju.emall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.nju.common.utils.R;
import com.nju.emall.ware.feign.MemberFeignService;
import com.nju.emall.ware.vo.FareVo;
import com.nju.emall.ware.vo.MemberAddressVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nju.common.utils.PageUtils;
import com.nju.common.utils.Query;

import com.nju.emall.ware.dao.WareInfoDao;
import com.nju.emall.ware.entity.WareInfoEntity;
import com.nju.emall.ware.service.WareInfoService;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.eq("id", key)
                    .or().like("name", key)
                    .or().like("address", key)
                    .or().like("areacode", key);
        }
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public FareVo getFare(Long addrId) {
        FareVo fareVo = new FareVo();
        //收获地址的详细信息
        R info = memberFeignService.info(addrId);
        MemberAddressVo memberAddressVo = info.getData("memberReceiveAddress", new TypeReference<MemberAddressVo>() {
        });
        if (memberAddressVo != null) {
            String phone = memberAddressVo.getPhone();
            String fare = phone.substring(phone.length() - 10, phone.length() - 8);
            BigDecimal bigDecimal = new BigDecimal(fare);
            fareVo.setFare(bigDecimal);
            fareVo.setAddress(memberAddressVo);
        }
        return fareVo;
    }

}
