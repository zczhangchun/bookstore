package com.bookstore.web.controller;

import com.bookstore.pojo.Book;
import com.bookstore.service.BookService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author zhangchun
 */
@Controller
@RequestMapping("book")
public class BookController {

    @Autowired
    private BookService bookService;

    @RequestMapping("findByAuthor")
    public String findByAuthor(@RequestParam(name = "author") String author,
                               @RequestParam(name = "page", required = true, defaultValue = "1")Integer page,
                               @RequestParam(name = "pageSize", required = true, defaultValue = "12")Integer pageSize,
                               Model model){

        List<Book> bookList = bookService.findByAuthor(author);
        PageInfo pageInfo = new PageInfo(bookList);
        model.addAttribute("pageInfo",pageInfo);
        return "search";
    }


    @RequestMapping("findByKeyword")
    public String findByKeyword(@RequestParam(value = "page",defaultValue = "1") Integer page,
                                @RequestParam(value = "pagesize",defaultValue = "12") Integer pagesize,
                                @RequestParam(value = "keyword",defaultValue = "") String keyword,
                                Model model){

        List<Book> bookList = bookService.findByPage(page,pagesize,keyword,true);
        PageInfo pageInfo = new PageInfo(bookList);
        model.addAttribute("pageInfo",pageInfo);
        model.addAttribute("keyword",keyword);
        return "search";
    }

    @RequestMapping("findByCategoryId")
    public String findByCategoryId(@RequestParam(name = "categoryId") Integer categoryId,
                                   @RequestParam(name = "page", required = true, defaultValue = "1")Integer page,
                                   @RequestParam(name = "pageSize", required = true, defaultValue = "12")Integer pageSize,
                                   Model model){

        List<Book> bookList = bookService.findByCategoryId(categoryId, page, pageSize);
        PageInfo<Book> pageInfo = new PageInfo<>(bookList);
        model.addAttribute("pageInfo",pageInfo);
        return "forward:/category/findAll";

    }

    /**
     * 跳转到图书详情页面，并加载数据
     * @param id
     * @param model
     * @return
     */
    @RequestMapping("skipDetail")
    public String skipDetail(String id, Model model){

        Book book = bookService.findById(id);
        model.addAttribute("book",book);
        return "bookDetail";
    }


}
