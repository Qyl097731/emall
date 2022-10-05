package com.nju.emall.cart.service;


import com.nju.emall.cart.vo.CartItemVo;
import com.nju.emall.cart.vo.CartVo;

import javax.servlet.http.HttpSession;
import java.util.List;

public interface CartService {

    /**
     * 将商品添加至购物车
     * @param skuId
     * @param num
     * @return
     */
    CartItemVo addToCart(Long skuId, Integer num, HttpSession session);

    /**
     * 获取购物车某个购物项
     * @param skuId
     * @return
     */
    CartItemVo getCartItem(Long skuId,HttpSession session);
    /**
     * 获取购物车里面的信息
     * @return
     */
    CartVo getCart(HttpSession session);

    /**
     * 清空购物车的数据
     */
    void clearCartInfo(String cartKey);

    /**
     * 勾选购物项
     */
    void checkItem(Long skuId, Integer check,HttpSession session);

    /**
     * 改变商品数量
     * @param skuId
     * @param num
     */
    void changeItemCount(Long skuId, Integer num,HttpSession session);


    /**
     * 删除购物项
     * @param skuId
     */
    void deleteIdCartInfo(Integer skuId,HttpSession session);

    List<CartItemVo> getUserCartItems();

}
