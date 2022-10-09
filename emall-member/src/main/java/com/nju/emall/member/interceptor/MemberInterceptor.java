package com.nju.emall.member.interceptor;

import com.nju.common.constant.AuthServerConstant;
import com.nju.common.vo.MemberResponseVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;

/**
 * @description
 * @date:2022/9/30 9:28
 * @author: qyl
 */
@Component
public class MemberInterceptor implements HandlerInterceptor {

    public static ThreadLocal<HttpSession> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        boolean match = new AntPathMatcher().match("/member/**", uri);
        if (match) {
            return true;
        }

        HttpSession session = request.getSession();
        threadLocal.set(session);
        MemberResponseVo member = (MemberResponseVo) session.getAttribute(AuthServerConstant.LOGIN_USER);

        if (member == null) {
            //未登录，返回登录页面
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("<script>alert('请先进行登录，再进行后续操作！');location.href='http://auth.emall.com/login.html'</script>");
            // session.setAttribute("msg", "请先进行登录");
            // response.sendRedirect("http://auth.emall.com/login.html");
            return false;
        }
        return true;
    }
}
