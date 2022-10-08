package com.nju.emall.order.listener;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.nju.emall.order.config.AlipayConfig;
import com.nju.emall.order.service.OrderService;
import com.nju.emall.order.vo.PayAsyncVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author asus
 * @Description: 订单支付成功监听器
 **/
@RestController
public class OrderPayedListener {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AlipayConfig alipayConfig;

    @PostMapping(value = "/payed/notify")
    public String handleAlipayed(PayAsyncVo vo, HttpServletRequest request) throws AlipayApiException,
            UnsupportedEncodingException {
        // 只要收到支付宝的异步通知，返回 success 支付宝便不再通知
        // 获取支付宝POST过来反馈信息
        // 验签
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParas = request.getParameterMap();
        Set<Map.Entry<String, String[]>> entries = requestParas.entrySet();
        for (Map.Entry<String, String[]> entry : entries) {
            String name = entry.getKey();
            String[] values = entry.getValue();
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
            // valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }

        boolean signVerified = AlipaySignature.rsaCheckV1(params, AlipayConfig.ALIPAY_PUBLIC_KEY,
                AlipayConfig.CHARSET, AlipayConfig.SIGN_TYPE); //调用SDK验证签名
        if (signVerified) {
            System.out.println("签名验证成功...");
            //去修改订单状态
            return orderService.handlePayResult(vo);
        } else {
            System.out.println("签名验证失败...");
            return "error";
        }
    }

//    @PostMapping(value = "/pay/notify")
//    public String asyncNotify(@RequestBody String notifyData) {
//        //异步通知结果
//        return orderService.asyncNotify(notifyData);
//    }

}
