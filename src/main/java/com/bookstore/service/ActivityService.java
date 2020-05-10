package com.bookstore.service;

import com.bookstore.pojo.Activity;
import com.bookstore.pojo.BookActivity;
import com.bookstore.pojo.CategoryActivity;

import java.util.List;

public interface ActivityService {


    Activity find();

    List<Activity> queryActivityByPage(Integer page, Integer pageSize, String keyword, Integer useType);

    void deleteActivity(List<Integer> ids);

    void updateActivity(Activity activity, String id);

    Activity queryActivityById(Integer id);

    CategoryActivity queryCategoryActivityByActivityId(Integer activityId);

    BookActivity queryBookActivityByActivityId(Integer activityId);

    Activity queryOpenActivity();

    List<BookActivity> queryBookActivityByBookIds(List<String> ids);

    List<CategoryActivity> queryCategoryActivityByCategoryIds(List<Integer> categoryIds);
}
