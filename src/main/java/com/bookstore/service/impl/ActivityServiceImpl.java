package com.bookstore.service.impl;

import com.bookstore.mapper.*;
import com.bookstore.pojo.*;
import com.bookstore.service.ActivityService;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Iterator;
import java.util.List;

@Service
public class ActivityServiceImpl implements ActivityService {

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private BookActivityMapper bookActivityMapper;

    @Autowired
    private CategoryActivityMapper categoryActivityMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private BookMapper bookMapper;


    @Override
    public Activity find() {

        return activityMapper.selectByPrimaryKey(1);

    }

    @Override
    public List<Activity> queryActivityByPage(Integer page, Integer pageSize, String keyword, Integer useType) {

        Example example = new Example(Activity.class);
        Example.Criteria criteria = example.createCriteria();
        if (!StringUtils.isEmpty(keyword)) {
            criteria.andLike("activityName", "%" + keyword + "%");
        }

        //添加推荐状态条件
        if (useType != null) {
            criteria.andEqualTo("useType", useType);
        }

        criteria.andEqualTo("status", true);

        PageHelper.startPage(page, pageSize);
        return activityMapper.selectByExample(example);



    }

    /**
     * 删除活动，其实就是状态变成关闭，不是真正的删除
     * @param ids
     */
    @Override
    @Transactional
    public void deleteActivity(List<Integer> ids) {

        Example example = new Example(Activity.class);
        example.createCriteria().andIn("id",  ids);
        Activity activity = new Activity();
        activity.setStatus(false);
        activityMapper.updateByExampleSelective(activity, example);


    }

    /**
     * 修改活动，也有可能是增加活动，先判断
     * @param activity
     * @param id
     */
    @Override
    @Transactional
    public void updateActivity(Activity activity, String id) {
        //先判断类型
        int useType = activity.getUseType();
        if (useType == 0){
            //type = 0 ，全程促销，直接就改即可
            Example example = new Example(Activity.class);
            example.createCriteria().andEqualTo("id", 1);
            activityMapper.updateByExample(activity, example);
        }else if (useType == 1){
            //type = 1 ，指定分类打折，先查看数据库有没有该分类的活动，没有的话在插入，有的话就更新
            CategoryActivity categoryCondition = new CategoryActivity();
            categoryCondition.setCategoryId(Integer.parseInt(id));
            CategoryActivity categoryActivity = categoryActivityMapper.selectOne(categoryCondition);
            if (categoryActivity != null){
                //不等于空，直接修改
                activity.setId(categoryActivity.getActivityId());
                activityMapper.updateByPrimaryKey(activity);
            }else {
                //为空，两个表添加之
                updateCategoryActivity(activity, id);
            }
        }else {
            BookActivity bookCondition = new BookActivity();
            bookCondition.setBookId(id);
            BookActivity bookActivity = bookActivityMapper.selectOne(bookCondition);
            if (bookActivity != null){
                //不等于空，说明存在，获取对应的活动ID，去修改
                activity.setId(bookActivity.getActivityId());
                activityMapper.updateByPrimaryKey(activity);
            }else {
                updateBookActivity(activity, id);
            }

        }

    }

    private void updateBookActivity(Activity activity, String id) {
        //如果为空，就要新添加一个活动，主活动表和图书活动表
        activityMapper.insert(activity);
        BookActivity bookActivityInsert = new BookActivity();
        bookActivityInsert.setBookId(id);
        bookActivityInsert.setActivityId(activity.getId());
        //查询对应图书信息
        Book book = bookMapper.selectByPrimaryKey(id);
        bookActivityInsert.setBookName(book.getName());
        bookActivityMapper.insert(bookActivityInsert);
    }

    private void updateCategoryActivity(Activity activity, String id) {
        activityMapper.insert(activity);
        CategoryActivity categoryActivityInsert = new CategoryActivity();
        categoryActivityInsert.setCategoryId(Integer.parseInt(id));
        categoryActivityInsert.setActivityId(activity.getId());
        //需要填充分类名称，因此需要查询出对应的分类信息
        Category category = categoryMapper.selectByPrimaryKey(Integer.parseInt(id));
        categoryActivityInsert.setCategoryName(category.getName());
        categoryActivityMapper.insert(categoryActivityInsert);
    }

    @Override
    public Activity queryActivityById(Integer id) {

        return activityMapper.selectByPrimaryKey(id);

    }

    @Override
    public CategoryActivity queryCategoryActivityByActivityId(Integer activityId) {

        CategoryActivity condition = new CategoryActivity();
        condition.setActivityId(activityId);
        return categoryActivityMapper.selectOne(condition);

    }

    @Override
    public BookActivity queryBookActivityByActivityId(Integer activityId) {
        BookActivity condition = new BookActivity();
        condition.setActivityId(activityId);
        return bookActivityMapper.selectOne(condition);
    }

    /**
     * 查看全场活动是否开启，没开启返回空对象
     * @return
     */
    @Override
    public Activity queryOpenActivity() {

        Activity activity = new Activity();
        activity.setId(1);
        activity.setStatus(true);
        return this.activityMapper.selectOne(activity);

    }

    /**
     * 查询已开启的商品活动
     * @param ids
     * @return
     */
    @Override
    public List<BookActivity> queryBookActivityByBookIds(List<String> ids) {

        Example example = new Example(BookActivity.class);
        example.createCriteria().andIn("bookId", ids);
        List<BookActivity> bookActivities = bookActivityMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(bookActivities)) {
            return null;
        }

        //不为空，就为每个bookActivityies填充Activity对象
        Iterator<BookActivity> iterator = bookActivities.iterator();
        while (iterator.hasNext()){
            BookActivity next = iterator.next();
            Activity activity = activityMapper.selectByPrimaryKey(next.getActivityId());
            if (activity.getStatus()){
                //如果活动开启，填充
                next.setActivity(activity);
            }else {
                //活动没开启，把这个从集合中删除
                iterator.remove();
            }
        }

        return bookActivities;


    }

    @Override
    public List<CategoryActivity> queryCategoryActivityByCategoryIds(List<Integer> categoryIds) {

        Example example = new Example(CategoryActivity.class);
        example.createCriteria().andIn("categoryId", categoryIds);
        List<CategoryActivity> categoryActivityList = categoryActivityMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(categoryActivityList)) {
            return null;
        }

        //不为空，填充activity对象
        Iterator<CategoryActivity> iterator = categoryActivityList.iterator();
        while (iterator.hasNext()){
            CategoryActivity next = iterator.next();
            Activity activity = activityMapper.selectByPrimaryKey(next.getActivityId());
            if (activity.getStatus()){
                next.setActivity(activity);
            }else {
                iterator.remove();
            }
        }

        return categoryActivityList;


    }
}
