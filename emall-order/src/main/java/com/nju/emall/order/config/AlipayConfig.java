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
    public static String APP_PRIVATE_KEY = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCRXgDP" +
            "+PF9gwZcr0Q6WUDBAChoGnfLvKZ31RO5DzSdSmDcqr52Zd1ww13eBR8bOD7BkaYfQoMDjME4XP+s1TZoLjy8NU5wbClmo9022yTuiRKORpH574zW+goEJdm0RxTn05kiHHQdUZtcgP1OMud7RABKY+N2L/Xaine7kO6nWJdaiQJ+7Hh69zaP8Az0BdO45CQZuEqkLLfl/+Cft8IzaYMu9gw7rZNQPR6CT++kXTicOCxMVm7R5xD9dKD/7/UnBts97tse2EKEkXEYr5fZjTeNovVPmNDy2yFqIdmeeGpLhntgU14gJY+dlTKijWnKz+RGGPDCQiMBzkqf3+8ZAgMBAAECggEAPGhV6hbHM6MZJ7bloWGpmQpZwDkPgyiX/MAc/BAnix13dvu1PjpCdTOadEKolIUg/wtpfyAClZ0ORj/PN6E6hLyCHKwCNpGb+r0pljwm8sEfZuRIkRZM7qDlPKgF+fZ354Z04kn0rqJU73Hi0XYx33V4sXEk+t4fSnu0Gcu2gO+cLGS3sxbylEwh3qJ9/ZcLgF0cXpjbWE0s5DMoA9W20Rb85TNH9gH928VMsyQyEWoVnfS5Ak6cE6A1mhs//zrz61JwOcBmbmMsOQl/UG3jjStFwkzf/LEzIq7bIyMlBYyjIwQ9G00Tag30uaCcoMLVN4n7hA5phtxmf7f4YZJ5yQKBgQDZYCon8i39Ko7DN9cRkFhT6FIw+v9tu88aLHXr9vnZaPMrsc4QKMP4HLWoV/dQ5tf3Dh/QWPo+qxb2dh7dqj1qrxVtyXBNYIcLdgqJU2X9xYLfdrGwoe+VxJfVL0mT3Gph+7QYj6i2bE01PzGpGn99N4lbvtHIqcqKaxDwxXFuuwKBgQCrMl4QMCXY7TGNi04y8VODF2vyq+rovaWqWG+wpm6V/OxjJKTcwnyHnSfXGB7a4qFfCHrrLamfLq/7TIBwKK5k4nq/SjA7JcI8L+sgAJJNQFz2JT5KDypoZVjYI8ZHhMnp2+geH8aCj9SHfiEm5MMs587XqEqep8fNkPHgmOyeOwKBgA4EV3OP+Es07VPFTAcD6c4vUu15ofLvg2FKsKfwmxfb3qJYqtL77fcX0tbyJS6AVDjIHmEO+8QCwE5QO5lLmv5AH/+eV7GFoe+pQvCsAg2lKNVEEB01s/9SWAAVNodkVS/QTUKwMM7imO/wDUG8RbBLXn4eBeUMndTjJoqAENp1AoGAdgfD0mVfsq/V+kCRKY44hakgQ69DxjL6hPd0Cda1Z7N5RuF8yLdOJDG1xhZbL44Qs/SkyoAw8g6+RgP4iVuC4QtElZ4Qorbls5lWAjpio+A8N3h1+Zl7I368Qkhn6+chTprO7fX6ZwnC/ad4M9iEt/EaKpVc3QXQO2ozWSYV/QECgYBgPEl5AyGOt3yt1fdNGN/lCMzNnDtCdJEetKH/rt4RyrgvBkU7yC5DIG8RVsDpmwLPgXBa/P6inUGql+QA8Dg2/si5lV4XHhMAHDrWRwIPqwjPAjk8gAwe7D0A9qaQ5uc8Cp4S0Jn+KnKMqIGRpbgMYr0reUn1haaCF4WaFIP2BA==";
    /**
     * 支付宝公钥,查看地址：https://openhome.alipay.com/platform/appDaily.htm 对应APPID下的支付宝公钥。
     */

    public static String ALIPAY_PUBLIC_KEY =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3sWVTzdsgBbIcrk092fJY73aLVPwKm5//GzPDkBJaAqgdvQ7QbNqaLC5YWmp69WIq80fEn0GdoWexFrLagnXBBOk4X7DigOgeRK+BTKVwzzChCaasf0CLjm4ck2M2BA1g+lnNb7qecPrNrOVx+kS0/FQ4MxbOm/YGVHJCV2qKQH4tuw5CmB8m8D098c7uMW2c5MRH+QJDKbNfxI6SZ2VdDN4l6dx5MtdmEgkOjMeGnoBOxCJIDjtTygj9L9wlE1iJakJoumt6DLW0AUPC+M484P6+V1FrvKh5c0glsGfy2Oimfv8TPPiXREaBmQ+XKuKiZzIYdL+DtPbEanoRoxyUQIDAQAB";
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
        //订单超时时间
        String timeout = "1m";

        request.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"timeout_express\":\""+timeout+"\","
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
