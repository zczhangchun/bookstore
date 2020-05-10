package com.bookstore.service;

import com.bookstore.pojo.Book;
import com.bookstore.pojo.OrderDetail;
import com.bookstore.pojo.Stock;
import com.bookstore.pojo.StockStatement;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BookService {
    List<Book> findByCategoryId(Integer categoryId, Integer page, Integer pageSize);

    Book findById(String id);

    List<Book> findByPage(Integer page, Integer pagesize,String keyword, Boolean status);

    void addBook(Book book, MultipartFile img);

    void deleteBook(List<String> ids);

    void deleteBookByCategoryId(Integer categoryId);

    void updateBook(Book book);

    List<Book> findByAuthor(String author);

    void decreaseStock(List<OrderDetail> orderDetailList);

    List<Stock> findStockByPage(Integer page, Integer pagesize, String keyword);

    void updateStock(Integer type, Integer count, String bookId);

    int findStatementCountByType(Integer type);

    List<Integer> findSevenDayStatementByType(Integer type);

    List<StockStatement> findStatementTodayByTypyAndPage(Integer page, Integer pagesize, Integer type);

    List<Book> queryBookByKeyword(String keyword);

    List<Book> findByIds(List<String> ids);

    void updateStockAndSales(Integer type, Integer count, String bookId);

    void deleteBook(List<String> ids, Integer type);

}
