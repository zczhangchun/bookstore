package com.bookstore.web.component.aop;

import com.bookstore.pojo.Admin;
import com.bookstore.pojo.Log;
import com.bookstore.service.LogService;
import com.bookstore.utils.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Component
@Aspect
public class LogAop {

    ThreadLocal<Date> tl = new ThreadLocal<>();

    @Autowired
    private LogService logService;

    @Autowired
    private HttpServletRequest request;


    @Pointcut("execution(* com.bookstore.web.controller.AdminController.*(..))")
    private void pt1() {
    }

    @Before("pt1()")
    public void beforePrint(){
        tl.set(new Date());

    }


    @AfterReturning("pt1()")
    public void afterReturning(JoinPoint jp) {
        Log log = new Log();
        Object result = request.getSession().getAttribute("admin");
        if (result == null) {
            return;
        }
        String methodName = jp.getSignature().getName();
        if (StringUtils.equals(methodName,"skipLogList")) {
            return;
        }
        Admin admin = (Admin) result;
        log.setId(NumberUtils.generateUUID());
        log.setMethod(methodName);
        log.setAid(admin.getId());
        log.setIp(request.getRemoteAddr().substring(4));
        log.setVisitTime(tl.get());
        log.setExecutionTime( System.currentTimeMillis() - log.getVisitTime().getTime());
        log.setUrl(request.getRequestURI());
        logService.save(log);
    }


}
