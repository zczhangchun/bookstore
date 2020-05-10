package com.bookstore.service.impl;

import com.bookstore.dto.UpdatePassword;
import com.bookstore.exception.BookStoreException;
import com.bookstore.mapper.AddressMapper;
import com.bookstore.mapper.UserMapper;
import com.bookstore.pojo.Address;
import com.bookstore.pojo.User;
import com.bookstore.service.UserService;
import com.bookstore.utils.CodecUtils;
import com.bookstore.utils.Md5Utils;
import com.bookstore.utils.NumberUtils;
import com.bookstore.utils.SmsUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private AddressMapper addressMapper;

    @Autowired
    private SmsUtils smsUtils;

    private final String PREFIX = "sms:phone:";

    @Override
    public User login(User form){
        /**
         * 登陆功能：
         * 需要对用户的用户名进行查询，
         * 查询出来比较密码是否一致，一致的话，返回用户的信息，用来回显
         */
        //根据用户名查询出用户来
        User condition = new User();
        condition.setUsername(form.getUsername());
        User user = userMapper.selectOne(condition);
        if (user == null){
            //用户名找不到，抛异常
            throw new BookStoreException();
        }
        //查询的用户与表单用户密码进行比较
        if (!StringUtils.equals(CodecUtils.md5Hex(form.getPassword(),user.getSalt()),user.getPassword())){
            //不正确，抛异常
            throw new BookStoreException();
        }


        //正确，返回用户信息
        return user;

    }

    /**
     * 注册功能：
     * 校验用户的用户名是否存在
     * 校验手机号是否存在
     * 校验验证码
     */
    @Override

    public User register(User user) {
        //校验用户名是否存在，存在直接抛异常
        if (!this.verify(user.getUsername(),1)) {
            log.info("【用户服务】有非法用户闯入");
            throw new BookStoreException();
        }
        if (!this.verify(user.getPhone(),2)){
            log.info("【用户服务】有非法用户闯入");
            throw new BookStoreException();
        }
        if (!this.verify(user.getCode(),user.getPhone(),3)){
            log.info("【用户服务】验证码错误");
            throw new BookStoreException();
        }
        //都正确，则插入数据
        //则对密码进行加密
        //生成盐
        String salt = Md5Utils.generate();
        user.setSalt(salt);
        //对密码进行 密码+盐的MD5加密
        user.setPassword(CodecUtils.md5Hex(user.getPassword(),salt));
        userMapper.insert(user);
        //并且返回用户信息
        return userMapper.selectOne(user);
    }

    @Override

    public void sms(@RequestParam("phone") String phone) {
        /**
         * 发送短信
         *
         */
        //调用短信工具类，将手机号码与验证码传递过去
        String code = NumberUtils.generateCode(6);
        smsUtils.send(phone,code);
        //将电话号码与验证码存入redis中，保存时长为5分钟
        redisTemplate.opsForValue().set(PREFIX + phone,code,5, TimeUnit.MINUTES);

    }


    @Override

    public void updatePassword(UpdatePassword form) {

        //校验旧密码是否正确
        User result = userMapper.selectByPrimaryKey(form.getUserId());
        if (!StringUtils.equals(result.getPassword(),CodecUtils.md5Hex(form.getOldPassword(),result.getSalt()))){
            //如果不相同，抛异常
            throw new BookStoreException();
        }

        //相同则进行验证码校验
        if (!this.verify(form.getCode(),result.getPhone(),3)){
            throw new RuntimeException();
        }

        //进行密码修改
        result.setPassword(CodecUtils.md5Hex(form.getNewPassword(),result.getSalt()));
        userMapper.updateByPrimaryKey(result);


    }

    /**
     *
     * @param data
     * @param type 1代表校验的类型是用户名，2代表校验手机号，3代表验证码
     * @return
     */
    @Override
    public Boolean verify(String data, String data2, Integer type) {

        User condition = new User();
        Boolean bool = true;


        switch (type){
            case 1:
                condition.setUsername(data);
                bool = CollectionUtils.isEmpty(userMapper.select(condition));
                break;
            case 2:
                condition.setPhone(data);
                bool = CollectionUtils.isEmpty(userMapper.select(condition));
                break;
            case 3:
                String code = redisTemplate.opsForValue().get(PREFIX + data2);
                bool = StringUtils.equals(data,code);
                break;
        }

        //查询
        return bool;

    }

    @Override
    public void addReceiver(Address address) {

        addressMapper.insert(address);


    }

    @Override
    public List<Address> queryAddressByUserId(Integer userId) {

        Address address = new Address();
        address.setUserId(userId);

        return addressMapper.select(address);

    }

    @Override
    public void updateAddress(Address address) {

        addressMapper.updateByPrimaryKey(address);

    }

    public Boolean verify(String data, Integer type) {
        return this.verify(data,null,type);
    }

}
