package com.bookstore.web.controller;

import com.bookstore.pojo.Book;
import com.bookstore.pojo.Category;
import com.bookstore.service.BookService;
import com.bookstore.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhangchun
 */
@Controller
@RequestMapping("category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BookService bookService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 加载主页的内容，加载被推荐的分类
     * @param status
     * @param model
     * @return
     */
    @RequestMapping("loadIndex")
    public String loadIndex(@RequestParam(name = "status") Boolean status, Model model){

        //查找被推荐的分类
        List<Category> categoryList = categoryService.findByStatus(status);

        Map<Category,List<Book>> map = new HashMap<>();

        //查找被推荐分类下的图书
        for (Category category : categoryList) {
            List<Book> bookList = bookService.findByCategoryId(category.getId(),1,4);
            map.put(category,bookList);
        }

        model.addAttribute("map",map);

        return "index::table_category";

    }

    /**
     * 查找所有分类，并跳转到分类页面
     * @param model
     * @return
     */
    @RequestMapping("findAll")

    public String findAll(Model model){
        List<Category> categoryList = categoryService.findAll();

        model.addAttribute("categoryList",categoryList);

        return "categories";
    }

    @RequestMapping("skipIndex")
    public String skipIndex(){
        return "index";
    }
}
