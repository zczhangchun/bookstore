package com.bookstore.service.impl;

import com.bookstore.exception.BookStoreException;
import com.bookstore.mapper.AdminMapper;
import com.bookstore.pojo.Admin;
import com.bookstore.service.AdminService;
import com.bookstore.utils.DateUtils;
import com.bookstore.web.config.PrefixConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@EnableConfigurationProperties(PrefixConfig.class)
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private PrefixConfig prefixConfig;

    @Override
    public Admin login(Admin admin){


        //校验用户名是否存在
        Admin condition = new Admin();
        condition.setUsername(admin.getUsername());
        Admin result = adminMapper.selectOne(condition);
        if (result == null){
            log.info("【后台管理服务】账号不存在");
            throw new BookStoreException();
        }

        //校验密码是否正确
        if (!StringUtils.equals(result.getPassword(),admin.getPassword())){
            log.info("【后台管理服务】密码不正确");
            throw new BookStoreException();
        }

        return result;

    }

    @Override
    public List<Admin> findAll() {

        return adminMapper.selectAll();
    }

    @Override
    public void updateLevel(Integer id) {

        Admin admin = adminMapper.selectByPrimaryKey(id);

        if (admin == null){
            throw new BookStoreException();
        }

        Integer level = admin.getLevel();
        if (level == 2){
            admin.setLevel(3);
        }else {
            admin.setLevel(2);
        }

        adminMapper.updateByPrimaryKey(admin);



    }

    /**
     * 总访问量
     * @return
     */
    @Override
    public int findPVTotal() {
        if (redisTemplate.opsForValue().get(prefixConfig.getPVTotal()) != null) {
            return Integer.parseInt(redisTemplate.opsForValue().get(prefixConfig.getPVTotal()));
        }else {
            return 0;
        }
    }


    /**
     * 今日访问量
     * @return
     */
    @Override
    public int findPVToday() {
        String today = DateUtils.getToday();
        if (redisTemplate.opsForValue().get(prefixConfig.getPVDaily() + today) != null) {

            return Integer.parseInt(redisTemplate.opsForValue().get(prefixConfig.getPVDaily() + today));
        }else {
            return 0;
        }
    }

    @Override
    public List<Integer> queryPVDailyList() {

        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 7; i++) {

            String date = DateUtils.getDateAgo(i);
            if (redisTemplate.opsForValue().get(prefixConfig.getPVDaily() + date) == null){
                list.add(0);
            }else {
                list.add(Integer.parseInt(redisTemplate.opsForValue().get(prefixConfig.getPVDaily() + date)));
            }


        }
        return list;

    }

    @Override
    public void addAdmin(Admin admin) {


        adminMapper.insert(admin);

    }


}
