package com.bookstore.web.config;

import com.bookstore.web.component.interceptor.AdminInterceptor;
import com.bookstore.web.component.interceptor.PVInterceptor;
import com.bookstore.web.component.interceptor.UserInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SecurityConfig implements WebMvcConfigurer {


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration userInterceptor = registry.addInterceptor(new UserInterceptor());

        InterceptorRegistration adminInterceptor = registry.addInterceptor(new AdminInterceptor());

        InterceptorRegistration PVInterceptor = registry.addInterceptor(localInterceptor());

        //前台拦截配置
        userInterceptor.addPathPatterns("/order/**");
        userInterceptor.addPathPatterns("/cart/**");

        //后台拦截配置
        adminInterceptor.addPathPatterns("/admin/**");
        adminInterceptor.excludePathPatterns("/admin/login");
        adminInterceptor.excludePathPatterns("/admin/skipLogin");

        //访问量拦截
        PVInterceptor.addPathPatterns("/order/**");
        PVInterceptor.addPathPatterns("/cart/**");
        PVInterceptor.addPathPatterns("/book/**");
        PVInterceptor.addPathPatterns("/category/**");
        PVInterceptor.addPathPatterns("/user/**");


    }

    @Bean
    PVInterceptor localInterceptor(){
        return new PVInterceptor();
    }
}
