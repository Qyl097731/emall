package com.nju.emall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.nju.emall.order.vo.PayVo;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.OrderUtils;

import java.math.BigDecimal;

/**
 * projectName:  covidSystem
 * packageName: com.njxzc.servicebase
 * date: 2022-04-15 02:40
 * copyright(c) 2020 南晓18卓工 邱依良
 *
 * @author 邱依良
 */
@Configuration
public class AlipayConfig {
    // 应用ID,支付宝提供的APPID，上面截图中有提到
    /**
     * 商户app_id
     */
    public static String APP_ID = "2021000116674144";

    /**
     * 商户私钥，您的PKCS8格式RSA2私钥
     */
    public static String APP_PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCAMcjKE+7AFutUzDGlWUk1pmYo1s1DIKuXbFJilapOtmzHKD5relCA+ZfTNpUgG1b51GghVKdVCkWRGEu3oQ9wDdo2+r4NHuyBtZyjW7+iOLHMdcfDy0mnS7VJgMHEbsVln247FriPOKAudSVMx0huwznpf6bbriBl4T/u52BwIB4ans95KDxVZfc6T+G5Cxf80llKhaOQu6CWzysU1FAdxECYv2nQQx35R4DxpE/FARDCQT+al5n2D4vGuMezIE2cyG84WJPZmHp8oYzOCDMitx8SK3E4pXkSt5EhdqzNjt6KVmh5zREniaNIVMZO8xvNi9bivxp1AR1MR4yuuUefAgMBAAECggEAUCVP4C7YDFYH6OsEeti4cYdHxW6Nw9MQFEpPs3zYjf91QIoDhFRjMk9ZQV6Vmw4csXK/a18ugnc6iP0EkZhrO2YdsFPRMAjOWkW92U/KHlSNrENSkjFtKd+mRn7uJa4PUXp8gwqwdjIzyvfISxkoSr9ZXIdHPX6+mJNb0jPr0g0OWARHox85yylcefINX/r12AlrQStDjUBDw1Wng2uuIl9YfaZmJxZDmS7z3cD3g0Wxuh/68aaIVP+eBfaIACKn085Pap2ePmOt28x6C3wVyigiJNnAfchenEb+hvpO1CBi5XAn98m0jw0xPrSsrbbTtMSy7QOCA6NCmwFYotadGQKBgQDLfyaaDNaBRgEj7JdKL7/KVqlrGNmDuxdXl9nyEskKNNb+5WWdiG5xPnBuUajhFCyUalWqq+X8B3mYS3Kud84ibCpmYbTGH9zr8ZJELbdzF2kCsgsADLea3qwclOa/4SoJV36F4hDx1JleWRvyjnK9kCI9DyPURlZ9mKFItj977QKBgQChRPY9Vju+lONUzeiH7bXO4Hb4/KeaP6v2v+GuCgPqa3nhsY7reusOnV78TXc0NTL/3YX+rRfI29knlhyhdZX8I7Op7N699jORMhRgdnC+mwj0tpMvTTnJAYgxIHwro2c4KxU7W8/p9hA2qiT381ag9XBmkDtBI6SuMgwSrC+YOwKBgA8A8PequqYNY0giyYTOwHVKrnMXAfKUeMQcjhUrDWeTPcbNYxCaXoo68E8x9iGiOyc2L9rFkc9jPbPc3DfA3ZHsJ7QkwA9big5bYU1lkYEK2NM6DVWTmnXwtP/mepzrHQ5TRSDZ6iXqZuRt7YGUvyD634tg8mNUTEw2VK3nwgaBAoGACPUDmDSJ7PPoDHXDTIlvZ6Yf9YRS6sZOjs3pgvkbbpG5m0d5YKrbE1y6QdkrncK2qm9rWdHTWFydmw1DWdRmGDMDb5SwamyaQJ4/c3wglvWVccISOrZXV0q2fcQIIYRZ30zFSR64VY5NKDwbAYqKbTY0n/2yzNNl+ibddMwLeLECgYEAm9eaj1RbE5clHPrLtlHwyP8HybJzORiEZJhJFgTPCVosC0fGHmuqTE7YX4h/juuN14rZSok8lMBqFKGQRggF/F+qZB2VNrtDboS+vXraQJqJIF+vz+5vEtgZZLR15dtRKbmcHoWqirqtqWxkJ4uWZeKX12Neaa7ECt6FqjGJVNE=";

    /**
     * 支付宝公钥,查看地址：https://openhome.alipay.com/platform/appDaily.htm 对应APPID下的支付宝公钥。
     */

    public static String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3sWVTzdsgBbIcrk092fJY73aLVPwKm5GzPDkBJaAqgdvQ7QbNqaLC5YWmp69WIq80fEn0GdoWexFrLagnXBBOk4X7DigOgeRK+BTKVwzzChCaasf0CLjm4ck2M2BA1g+lnNb7qecPrNrOVx+kS0FQ4MxbOmYGVHJCV2qKQH4tuw5CmB8m8D098c7uMW2c5MRH+QJDKbNfxI6SZ2VdDN4l6dx5MtdmEgkOjMeGnoBOxCJIDjtTygj9L9wlE1iJakJoumt6DLW0AUPC+M484P6+V1FrvKh5c0glsGfy2Oimfv8TPPiXREaBmQ+XKuKiZzIYdL+DtPbEanoRoxyUQIDAQAB";
    /**
     *  服务器异步通知页面路径  必须外网可以正常访问.如果只是测试使用,那么设置成自己项目启动后可以访问到的一个路径,作为支付宝发送通知的路径
     */
    public static String NOTIFY_URL = "http://15050595765.gnway.cc/payed/notify";
    /**
     *   页面跳转同步通知页面路径 支付正常完成后,会访问的路径.
     */

    public static String RETURN_URL = "http://member.emall.com/memberOrder.html";


    /**
     *   返回数据的格式
     */
    public static String FORMAT = "JSON";

    /**
     *   签名方式，注意这里，如果步骤设置的是RSA则用RSA,如果按照扇面步骤做的话,选择RSA2
     */
    public static String SIGN_TYPE = "RSA2";
    /**
     *   字符编码格式
     */
    public static String CHARSET = "utf-8";
    /**
     *   支付宝网关
     */
    public static String GATEWAY_URL = "https://openapi.alipaydev.com/gateway.do";

    public String pay(PayVo vo) {
        //实例化客户端,填入所需参数
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.GATEWAY_URL, AlipayConfig.APP_ID, AlipayConfig.APP_PRIVATE_KEY, AlipayConfig.FORMAT, AlipayConfig.CHARSET, AlipayConfig.ALIPAY_PUBLIC_KEY, AlipayConfig.SIGN_TYPE);
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        //在公共参数中设置回跳和通知地址
        request.setReturnUrl(AlipayConfig.RETURN_URL);
        request.setNotifyUrl(AlipayConfig.NOTIFY_URL);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        //生成随机Id
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount =  vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        // 商品描述 可空
        String body = vo.getBody();
        request.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");
        String form = "";
        try {
            // 调用SDK生成表单
            form = alipayClient.pageExecute(request).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return form;
    }

}
