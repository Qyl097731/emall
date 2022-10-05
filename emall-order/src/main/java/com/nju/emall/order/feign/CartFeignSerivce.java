package com.nju.emall.order.feign;

import com.nju.emall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @description
 * @date:2022/10/4 19:44
 * @author: qyl
 */
@FeignClient("emall-cart")
public interface CartFeignSerivce {
    @GetMapping(value = "/currentUserCartItems")
    List<OrderItemVo> getCurrentCartItems();
}
