package com.bookstore.web.component.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {





        Object user = request.getSession().getAttribute("user");

        if (user != null){
            return true;
        }

        String url = "/user/skipLogin";
        response.sendRedirect(url);


        String referer = request.getHeader("Referer");
        request.getSession().setAttribute("url",referer);

        return false;

    }

}
