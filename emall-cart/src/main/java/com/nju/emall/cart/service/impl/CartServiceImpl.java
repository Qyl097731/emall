package com.nju.emall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.mysql.cj.xdevapi.JsonArray;
import com.nju.common.constant.AuthServerConstant;
import com.nju.common.constant.CartConstant;
import com.nju.common.utils.R;
import com.nju.common.vo.MemberResponseVo;
import com.nju.emall.cart.feign.ProductFeignService;
import com.nju.emall.cart.interceptor.CartInterceptor;
import com.nju.emall.cart.service.CartService;
import com.nju.emall.cart.vo.CartItemVo;
import com.nju.emall.cart.vo.CartVo;
import com.nju.emall.cart.vo.SkuInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@Service("cartService")
public class CartServiceImpl implements CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    private ThreadPoolExecutor executor;

    @Override
    public CartItemVo addToCart(Long skuId, Integer num, HttpSession session) {
        BoundHashOperations<String, Object, Object> boundHashOps = getOps(session);

        CartItemVo cartItemVo = new CartItemVo();
        String s = (String) boundHashOps.get(skuId.toString());


        CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
            CartItemVo itemVo = null;
            if (!StringUtils.isEmpty(s)) {
                itemVo = JSON.parseObject(s, CartItemVo.class);
            }

            R r = productFeignService.info(skuId);
            SkuInfoVo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
            });
            cartItemVo.setCheck(true);


            cartItemVo.setCount(num + (itemVo == null ? 0 : itemVo.getCount()));
            cartItemVo.setImage(skuInfo.getSkuDefaultImg());
            cartItemVo.setTitle(skuInfo.getSkuTitle());
            cartItemVo.setSkuId(skuId);
            cartItemVo.setPrice(skuInfo.getPrice());
        }, executor);

        CompletableFuture<Void> getSkuSaleAttrValues = CompletableFuture.runAsync(() -> {
            List<String> skuSaleAttrValues = productFeignService.getSkuSaleAttrValues(skuId);
            cartItemVo.setSkuAttrValues(skuSaleAttrValues);
        }, executor);

        CompletableFuture.allOf(getSkuInfoTask, getSkuSaleAttrValues).thenRun(() -> {
            boundHashOps.put(skuId.toString(), JSON.toJSONString(cartItemVo));
        });

        return cartItemVo;
    }

    @NotNull
    private BoundHashOperations<String, Object, Object> getOps(HttpSession session) {
        MemberResponseVo member = (MemberResponseVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        String cartKey = "";
        if (member != null) {
            cartKey = CartConstant.CART_PREFIX + member.getId();
        }
        return redisTemplate.boundHashOps(cartKey);
    }

    //
    @Override
    public CartItemVo getCartItem(Long skuId, HttpSession session) {
        //?????????????????????????????????
        BoundHashOperations<String, Object, Object> cartOps = getOps(session);

        String redisValue = (String) cartOps.get(skuId.toString());

        return JSON.parseObject(redisValue, CartItemVo.class);
    }

    /**
     * ????????????????????????????????????????????????????????????
     */
    @Override
    public CartVo getCart(HttpSession session) {
        CartVo cartVo = new CartVo();
        BoundHashOperations<String, Object, Object> ops = getOps(session);
        List<Object> values = ops.values();
        if (!CollectionUtils.isEmpty(values)) {
            List<CartItemVo> itemVos = values.stream().map(item -> JSON.parseObject((String) item, CartItemVo.class)).collect(Collectors.toList());
            cartVo.setItems(itemVos);
        }
        return cartVo;
    }


    @Override
    public void clearCartInfo(String cartKey) {
        redisTemplate.delete(cartKey);
    }

    @Override
    public void checkItem(Long skuId, Integer check, HttpSession session) {
        //??????????????????????????????
        CartItemVo cartItem = getCartItem(skuId, session);
        //??????????????????
        cartItem.setCheck(check == 1);

        //???????????????redis???
        String redisValue = JSON.toJSONString(cartItem);

        BoundHashOperations<String, Object, Object> cartOps = getOps(session);
        cartOps.put(skuId.toString(), redisValue);
    }

    /**
     * ?????????????????????
     */
    @Override
    public void changeItemCount(Long skuId, Integer num, HttpSession session) {

        //??????????????????????????????
        CartItemVo cartItem = getCartItem(skuId, session);
        cartItem.setCount(num);

        BoundHashOperations<String, Object, Object> cartOps = getOps(session);
        //???????????????redis???
        String redisValue = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(), redisValue);
    }

    /**
     * ???????????????
     */
    @Override
    public void deleteIdCartInfo(Integer skuId, HttpSession session) {
        BoundHashOperations<String, Object, Object> cartOps = getOps(session);
        cartOps.delete(skuId.toString());
    }

    @Override
    public List<CartItemVo> getUserCartItems() {
        List<CartItemVo> vos = new ArrayList<>();
        CartVo cart = getCart(CartInterceptor.threadLocal.get());
        if (cart != null) {
            List<CartItemVo> items = cart.getItems();
            if (!CollectionUtils.isEmpty(items)) {
                vos = items.stream().filter(CartItemVo::getCheck)
                        .map(item -> {
                            BigDecimal price = productFeignService.getPrice(item.getSkuId());
                            item.setPrice(price);
                            return item;
                        }).collect(Collectors.toList());
            }
        }
        return vos;
    }
//
//    @Override
//    public List<CartItemVo> getUserCartItems() {
//
//        List<CartItemVo> cartItemVoList = new ArrayList<>();
//        //?????????????????????????????????
//        UserInfoTo userInfoTo = CartInterceptor.toThreadLocal.get();
//        //?????????????????????????????????null
//        if (userInfoTo.getUserId() == null) {
//            return null;
//        } else {
//            //??????????????????
//            String cartKey = CART_PREFIX + userInfoTo.getUserId();
//            //???????????????
//            List<CartItemVo> cartItems = getCartItems(cartKey);
//            if (cartItems == null) {
//                throw new CartExceptionHandler();
//            }
//            //??????????????????
//            cartItemVoList = cartItems.stream()
//                    .filter(items -> items.getCheck())
//                    .map(item -> {
//                        //?????????????????????????????????????????????
//                        BigDecimal price = productFeignService.getPrice(item.getSkuId());
//                        item.setPrice(price);
//                        return item;
//                    })
//                    .collect(Collectors.toList());
//        }
//
//        return cartItemVoList;
//    }
}
