package com.bookstore;

import com.bookstore.mapper.BookMapper;
import com.bookstore.mapper.StockMapper;
import com.bookstore.pojo.Book;
import com.bookstore.pojo.Stock;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class Test {

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate redisTemplate;





    @org.junit.Test
    public void m1(){

        List<Stock> stocks = stockMapper.selectAll();

        for (Stock stock : stocks) {
            Book book = bookMapper.selectByPrimaryKey(stock.getBookId());
            stock.setBookName(book.getName());
            stockMapper.updateByPrimaryKey(stock);
        }

    }

    @org.junit.Test
    public void m2(){

//        User user = new User();
//        user.setUsername("zhangsan");
//        user.setPassword("zc123123");
//
//        stringRedisTemplate.opsForValue().set("user1", user.toString());
//
//        redisTemplate.opsForValue().set("user2", user);

        stringRedisTemplate.delete("user1");
        redisTemplate.delete("user2");

        RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();

        RedisConnectionFactory connectionFactory1 = stringRedisTemplate.getConnectionFactory();

        RedisAtomicInteger atomicInteger = new RedisAtomicInteger("count1",redisTemplate.getConnectionFactory());

        System.out.println(atomicInteger.getAndIncrement());

        RedisAtomicInteger atomicInteger1 = new RedisAtomicInteger("count2", stringRedisTemplate.getConnectionFactory());
        System.out.println(atomicInteger1.getAndIncrement());

        RedisAtomicInteger atomicInteger2 = new RedisAtomicInteger("count2", stringRedisTemplate.getConnectionFactory());

        System.out.println(atomicInteger2.getAndIncrement());

        atomicInteger.getAndIncrement();

//        atomicInteger.addAndGet()



    }


}
