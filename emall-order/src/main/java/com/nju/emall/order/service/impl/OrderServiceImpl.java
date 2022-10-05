package com.nju.emall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.nju.common.constant.AuthServerConstant;
import com.nju.common.to.SkuHasStockTo;
import com.nju.common.utils.R;
import com.nju.common.vo.MemberResponseVo;
import com.nju.emall.order.config.MyThreadConfig;
import com.nju.emall.order.feign.CartFeignSerivce;
import com.nju.emall.order.feign.MemberFeignService;
import com.nju.emall.order.feign.WmsFeignService;
import com.nju.emall.order.vo.MemberAddressVo;
import com.nju.emall.order.vo.OrderConfirmVo;
import com.nju.emall.order.vo.OrderItemVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nju.common.utils.PageUtils;
import com.nju.common.utils.Query;

import com.nju.emall.order.dao.OrderDao;
import com.nju.emall.order.entity.OrderEntity;
import com.nju.emall.order.service.OrderService;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignSerivce cartFeignSerivce;

    @Autowired
    WmsFeignService wmsFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder(HttpServletRequest request) {

        OrderConfirmVo confirmVo = new OrderConfirmVo();

        // 获取所有可选地址
        MemberResponseVo memberResponseVo = (MemberResponseVo) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> addressFeature = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(attributes);
            List<MemberAddressVo> addresses = memberFeignService.getAddress(memberResponseVo.getId());
            confirmVo.setMemberAddressVos(addresses);
        }, executor);


        CompletableFuture<Void> cartItemFeature = CompletableFuture.runAsync(() -> {
            // 获取购物车中的所有商品
            RequestContextHolder.setRequestAttributes(attributes);
            List<OrderItemVo> cartItems = cartFeignSerivce.getCurrentCartItems();
            confirmVo.setItems(cartItems);
        }, executor).thenRunAsync(() -> {
            Map<Long, Boolean> hasstock = new HashMap<>();
            List<OrderItemVo> items = confirmVo.getItems();
            if (!CollectionUtils.isEmpty(items)) {
                List<Long> ids = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
                R r = wmsFeignService.hasstock(ids);
                List<SkuHasStockTo> data = r.getData("data", new TypeReference<List<SkuHasStockTo>>() {
                });
                if (!CollectionUtils.isEmpty(data)) {
                    hasstock = data.stream().collect(Collectors.toMap(SkuHasStockTo::getSkuId, SkuHasStockTo::getHasStock));
                }
            }
            confirmVo.setStocks(hasstock);
        });

        // 用户积分
        Integer integration = memberResponseVo.getIntegration();
        confirmVo.setIntegration(integration);

        CompletableFuture.allOf(addressFeature, cartItemFeature).join();

        return confirmVo;
    }

}
