package com.bookstore.service;

import com.bookstore.pojo.Category;

import java.util.List;

public interface CategoryService {

    List<Category> findByStatus(Boolean status);

    List<Category> findAll();

    Category findById(Integer id);

    List<Category> queryCategoryByPage(int page, int pageSize, String keyword, Boolean status);

    void addCategory(Category category);

    Boolean verifyCategory(String data);

    void deleteCategory(List<Integer> ids);

    void updateCategory(Category category);



    List<Category> queryCategoryByKeyword(String keyword);
}
