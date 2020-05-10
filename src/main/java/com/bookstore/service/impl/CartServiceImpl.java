package com.bookstore.service.impl;

import com.bookstore.pojo.Cart;
import com.bookstore.service.CartService;
import com.bookstore.utils.JsonUtils;
import com.bookstore.web.config.PrefixConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@EnableConfigurationProperties(PrefixConfig.class)
public class CartServiceImpl implements CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private PrefixConfig prefixConfig;


    @Override
    public void addCart(Integer userId,Cart cart) {

        //获取redis中的购物车的id
        String key = getKey(userId);

        //获取购物车明细的id
        String hashKey = cart.getBookId();

        //提前获取number的值
        int num  = cart.getNumber();

        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        //判断有没有该购物车明细
        if (hashOps.hasKey(hashKey)) {
            //如果存在该购物车明细的id，增加数量即可
             cart = JsonUtils.toBean(hashOps.get(hashKey).toString(), Cart.class);
            //增加数量
            //增加数量之前先判断有没有大于100，大于100则按100算
            int number = cart.getNumber() + num;
            if (number >= 100){
                cart.setNumber(100);
            }else {
                cart.setNumber(number);
            }

        }

        //将购物车信息写入redis中
        hashOps.put(cart.getBookId(), JsonUtils.toString(cart));


    }

    /**
     * 通过用户Id查询出对应的所有购物车明细
     * @param userId
     * @return
     */
    @Override
    public List<Cart> queryCartList(Integer userId){
        //获取指定用户的购物车信息
        String key = getKey(userId);

        //查询该用户的购物车信息
        if (!redisTemplate.hasKey(key)){
            //如果该用户信息不存在，直接返回null即可
            return null;
        }

        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        List<Object> values = operations.values();
        List<Cart> cartList = new ArrayList<>();
        for (Object value : values) {
            cartList.add(JsonUtils.toBean(value.toString(), Cart.class));
        }
        return cartList;

    }

    /**
     * 通过用户id与购物车明细id删除购物车明细
     * @param userId
     * @param id
     */

    @Override
    public void deleteCart(int userId, String id) {

        //删除购物车明细，先获取购物车id
        String key = getKey(userId);


        //判断有没有该明细
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        if (hashOps.hasKey(id)){
            hashOps.delete(id);
        }


    }

    /**
     * 删除多个购物车明细
     * @param userId
     * @param ids
     */
    @Override
    public void deleteCarts(int userId, List<String> ids){
        String key = getKey(userId);

        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);

        for (String id : ids) {
            hashOps.delete(id);
        }
    }

    /**
     * 修改购物车数量
     * @param userId
     * @param bookId
     * @param number
     * @return
     */

    @Override
    public int updateCart(int userId, String bookId, Integer number) {

        String key = this.getKey(userId);


        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);

        Cart cart = JsonUtils.toBean(hashOps.get(bookId).toString(),Cart.class);

        //修改数量
        if (!cart.getNumber().equals(number)){
            cart.setNumber(number);
        }

        //存入redis
        hashOps.put(bookId,JsonUtils.toString(cart));

        return cart.getNumber() * cart.getPrice();
    }


    /**
     * 查询多个购物车明细
     * @param userId
     * @param ids
     * @return
     */
    @Override
    public List<Cart> queryCartByIds(Integer userId, List<String> ids) {
        String key = this.getKey(userId);


        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(key);

        List<String> stringList = hashOps.multiGet(ids);
        List<Cart> cartList = new ArrayList<>();
        for (String s : stringList) {
            cartList.add(JsonUtils.toBean(s,Cart.class));
        }
        return cartList;

    }

    private String getKey(Integer userId){

        return prefixConfig.getCartPrefix() + userId;

    }
}
