package com.nju.emall.order.web;

import com.nju.common.exception.NoStockException;
import com.nju.emall.order.service.OrderService;
import com.nju.emall.order.vo.OrderConfirmVo;
import com.nju.emall.order.vo.OrderSubmitVo;
import com.nju.emall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ExecutionException;

@Controller
public class OrderWebController {

    @Autowired
    private OrderService orderService;

    /**
     * 去结算确认页
     *
     * @param model
     * @param request
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @GetMapping(value = "/toTrade")
    public String toTrade(Model model, HttpServletRequest request) {

        OrderConfirmVo confirmVo = orderService.confirmOrder(request);

        model.addAttribute("confirmOrderData", confirmVo);
        //展示订单确认的数据
        System.out.println(confirmVo);

        return "confirm";
    }


    @PostMapping("submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes attributes) {
        SubmitOrderResponseVo responseVo = orderService.submitOrder(vo);
        try {
            if (responseVo.getCode() == 0) {
                // 下单失败回到订单确认也重新确认订单信息
                model.addAttribute("submitOrderResp", responseVo);
                return "pay";
            } else {
                String msg = "下单失败";
                switch (responseVo.getCode()) {
                    case 1:
                        msg += " 令牌订单信息过期，请刷新再次提交";
                        break;
                    case 2:
                        msg += " 订单商品价格发生变化，请确认后再次提交";
                        break;
                    case 3:
                        msg += " 库存锁定失败，商品库存不足";
                        break;
                }
                attributes.addFlashAttribute("msg", msg);
                return "redirect:http://order.emall.com/toTrade";
            }
        }catch (Exception e) {
            if (e instanceof NoStockException) {
                String message = e.getMessage();
                attributes.addFlashAttribute("msg",message);
            }
            return "redirect:http://order.emall.com/toTrade";
        }
    }
}
