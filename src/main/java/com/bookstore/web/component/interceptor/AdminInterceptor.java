package com.bookstore.web.component.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AdminInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        Object admin = request.getSession().getAttribute("admin");
        if (admin != null){
            return true;
        }



        response.sendRedirect("/admin/skipLogin");

        return false;

    }
}
