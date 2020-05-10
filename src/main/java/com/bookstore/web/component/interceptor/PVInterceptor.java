package com.bookstore.web.component.interceptor;

import com.bookstore.utils.DateUtils;
import com.bookstore.web.config.PrefixConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@EnableConfigurationProperties(PrefixConfig.class)
public class PVInterceptor implements HandlerInterceptor {


    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private PrefixConfig prefixConfig;



    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        //总访问量
        if ( operations.get(prefixConfig.getPVTotal()) == null){

            operations.set(prefixConfig.getPVTotal(),"1");

        }else {
            operations.set(prefixConfig.getPVTotal(),Integer.toString(Integer.parseInt(operations.get(prefixConfig.getPVTotal())) + 1));
        }

        //每日访问量
        String format = DateUtils.getToday();
        if (operations.get(prefixConfig.getPVDaily() + format) == null){
            operations.set(prefixConfig.getPVDaily() + format,"1");
        }else {
            operations.set(prefixConfig.getPVDaily() + format,String.valueOf(Integer.parseInt(operations.get(prefixConfig.getPVDaily() + format)) + 1));
        }

        return true;

    }
}
