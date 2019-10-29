package com.js.sas.Interceptor;

import com.js.sas.controller.LoginController;
import com.js.sas.entity.SystemUser;
import org.aopalliance.intercept.Interceptor;
import org.apache.poi.ss.formula.functions.Intercept;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author daniel
 * @description:
 * @create: 2019-10-18 09:08
 */
public class LoginHandlerInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {

        SystemUser user = (SystemUser)request.getSession().getAttribute(LoginController.SYSTEM_USER);
        if (user == null || user.equals(""))  {
            response.sendRedirect("/login_err");
            return false;
        }else {
            request.getSession().setMaxInactiveInterval(6*60*60*60);
            Cookie cookie = new Cookie("jsUserName", user.getNickName());
            cookie.setMaxAge(-1);
            response.addCookie(cookie);
        }
        return true;
    }
}
