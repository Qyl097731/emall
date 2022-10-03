package com.nju.emall.cart.interceptor;

import com.nju.common.constant.AuthServerConstant;
import com.nju.common.vo.MemberResponseVo;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @description
 * @date:2022/9/30 9:28
 * @author: qyl
 */
@Component
public class CartInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        MemberResponseVo member = (MemberResponseVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        if(member == null){
            response.sendRedirect("http://auth.emall.com/login.html");
            return false;
        }
        return true;
    }



}
