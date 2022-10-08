package com.nju.emall.order.web;

import com.alipay.api.AlipayApiException;
import com.nju.emall.order.config.AlipayConfig;
import com.nju.emall.order.entity.OrderEntity;
import com.nju.emall.order.service.OrderService;
import com.nju.emall.order.vo.PayVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@Controller
public class PayWebController {

    @Autowired
    private AlipayConfig alipayConfig;

    @Autowired
    private OrderService orderService;

    /**
     * 用户下单:支付宝支付
     * 1、让支付页让浏览器展示
     * 2、支付成功以后，跳转到用户的订单列表页
     * @param orderSn
     * @return
     * @throws AlipayApiException
     */
    @ResponseBody
    @GetMapping(value = "payOrder",produces = "text/html")
    public String payOrder(@RequestParam("orderSn")String orderSn){
        PayVo payVo = orderService.getOrderPay(orderSn);
        return alipayConfig.pay(payVo);
    }
}
