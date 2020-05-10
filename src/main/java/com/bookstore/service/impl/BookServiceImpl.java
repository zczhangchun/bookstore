package com.bookstore.service.impl;

import com.bookstore.mapper.BookMapper;
import com.bookstore.mapper.StockMapper;
import com.bookstore.mapper.StockStatementMapper;
import com.bookstore.pojo.*;
import com.bookstore.service.BookService;
import com.bookstore.service.CategoryService;
import com.bookstore.utils.DateUtils;
import com.bookstore.utils.JsonUtils;
import com.bookstore.utils.NumberUtils;
import com.bookstore.web.config.PrefixConfig;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
@EnableConfigurationProperties(PrefixConfig.class)
public class BookServiceImpl implements BookService {

    @Value("${img.path}")
    private String path;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private StockStatementMapper statementMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private PrefixConfig prefixConfig;


    /**
     * 通过分页查找图书
     *
     * @param page
     * @param pagesize
     * @return
     */
    @Override

    public List<Book> findByPage(Integer page, Integer pagesize, String keyword, Boolean status) {


        Example example = new Example(Book.class);
        Example.Criteria criteria = example.createCriteria();

        //添加名称模糊条件
        if (!StringUtils.isEmpty(keyword)) {
            criteria.andLike("name", "%" + keyword + "%");
        }

        //添加推荐状态条件
        if (status != null) {
            criteria.andEqualTo("status", status);
        }

        PageHelper.startPage(page, pagesize);
        List<Book> bookList = bookMapper.selectByExample(example);
        for (Book book : bookList) {
            Category category = categoryService.findById(book.getCategoryId());
            if (category != null) {
                book.setCategoryName(category.getName());
            }else {
                book.setCategoryName("无");
            }
        }
        return bookList;
    }

    @Override
    public List<Book> findByCategoryId(Integer categoryId, Integer page, Integer pageSize) {
        /**
         * 通过categoryId查询相应图书
         */
        if (categoryId == null) {
            return null;
        }


        //通过分类id找到对应图书
        Book condition = new Book();
        condition.setCategoryId(categoryId);

        PageHelper.startPage(page, pageSize, true);
        List<Book> bookList = bookMapper.select(condition);

        return bookList;

    }

    @Override
    public List<Book> findByAuthor(String author) {
        if (author == null) {
            return null;
        }

        Book condition = new Book();
        condition.setAuthor(author);
        PageHelper.startPage(1, 0, true);
        return bookMapper.select(condition);

    }

    @Override
    public Book findById(String id) {
        if (StringUtils.isEmpty(id)) {
            return null;
        }


        Book book = JsonUtils.toBean(redisTemplate.opsForValue().get(prefixConfig.getBookPrefix() + id), Book.class);
        book.setCategoryName(categoryService.findById(book.getCategoryId()).getName());
        Category category = categoryService.findById(book.getCategoryId());
        book.setCategoryName(category.getName());
        //查找库存
        book.setStock(stockMapper.selectByPrimaryKey(book.getId()).getStock());

        return book;

    }


    /**
     * 添加图书
     *
     * @param book
     * @param img
     */
    @Override
    @Transactional
    public void addBook(Book book, MultipartFile img) {
        if (!img.isEmpty()) {
            //获取图片的后缀
            String imgName = img.getOriginalFilename();
            String suffix = imgName.substring(imgName.lastIndexOf("."));
            //给文件生成名称
            imgName = NumberUtils.generateUUID() + suffix;
            //上传文件
            try {
                img.transferTo(new File(path, imgName));
                book.setImg(imgName);
            } catch (IOException e) {
                throw new RuntimeException("服务器异常");
            }
        }
        String id = NumberUtils.generateUUID();
        book.setId(id);
        book.setStatus(false);
        Stock stock = new Stock();
        stock.setBookId(book.getId());
        stock.setBookName(book.getName());
        stock.setSales(0);
        stock.setStock(0);
        //存储book
        bookMapper.insert(book);
        stockMapper.insert(stock);
        //存储到redis中
        redisTemplate.opsForValue().set(prefixConfig.getBookPrefix() + book.getId(), JsonUtils.toString(book));


    }

    /**
     * 通过分类删除图书
     */
    @Override
    @Transactional
    public void deleteBookByCategoryId(Integer categoryId) {
        if (categoryId != null) {

            Book condition = new Book();
            condition.setCategoryId(categoryId);
            List<Book> select = bookMapper.select(condition);
            List<String> idList = new ArrayList<>();
            for (Book book : select) {
                idList.add(book.getId());
            }
            //相关图书下架
            this.deleteBook(idList,1);


        }
    }


    /**
     * 修改图书信息
     *
     * @param book
     */
    @Override
    @Transactional
    public void updateBook(Book book) {


        //修改图书信息
        bookMapper.updateByPrimaryKey(book);

        //更新redis
        redisTemplate.opsForValue().set(prefixConfig.getBookPrefix() + book.getId(), JsonUtils.toString(book));

    }


    /**
     * 删除多本图书，其实是修改图书的状态，并不是真的删除，库存也要变成0
     *
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBook(List<String> ids) {

        if (!CollectionUtils.isEmpty(ids)) {
            //删除图书
            Example example = new Example(Book.class);
            example.createCriteria().andIn("id", ids);
            Book book = new Book();
            book.setStatus(false);
            bookMapper.updateByExampleSelective(book, example);

            //redis也要改变数据
            for (String id : ids) {
                //先查询出来
                Book result = JsonUtils.toBean(redisTemplate.opsForValue().get(prefixConfig.getBookPrefix() + id),
                        Book.class);
                //改变其状态
                result.setStatus(false);
                //存入回去
                redisTemplate.opsForValue().set(prefixConfig.getBookPrefix() + id, JsonUtils.toString(result));
            }

        }

    }

    @Override
    @Transactional
    public void deleteBook(List<String> ids, Integer type) {

        if (!CollectionUtils.isEmpty(ids)) {
            //删除图书
            Example example = new Example(Book.class);
            example.createCriteria().andIn("id", ids);
            Book book = new Book();
            book.setStatus(false);
            book.setCategoryId(28);
            bookMapper.updateByExampleSelective(book, example);

            //redis也要改变数据
            for (String id : ids) {
                //先查询出来
                Book result = JsonUtils.toBean(redisTemplate.opsForValue().get(prefixConfig.getBookPrefix() + id),
                        Book.class);
                //改变其状态
                result.setStatus(false);
                result.setCategoryId(28);
                result.setCategoryName("无");
                //存入回去
                redisTemplate.opsForValue().set(prefixConfig.getBookPrefix() + id, JsonUtils.toString(result));
            }

        }

    }

    /**
     * 支付成功后，库存减少
     *
     * @param orderDetailList
     */
    @Override
    @Transactional
    public synchronized void decreaseStock(List<OrderDetail> orderDetailList) {

        //遍历每个订单条目，进行库存的减少
        for (OrderDetail detail : orderDetailList) {

            stockMapper.decreaseStock(detail.getBookId(), detail.getNumber());

            Stock stock = stockMapper.selectByPrimaryKey(detail.getBookId());
            if (stock.getStock() <= 0) {
                //如果库存小于等于0了，那么要将书本下架，直接调用delete方法即可
                List list = new ArrayList();
                list.add(detail.getBookId());
                this.deleteBook(list);
            }

        }

    }

    @Override
    public List<Stock> findStockByPage(Integer page, Integer pagesize, String keyword) {

        Example example = new Example(Stock.class);
        if (keyword != null) {

            example.createCriteria().andLike("bookName", "%" + keyword + "%");

        }
        //查询图书数据库
        PageHelper.startPage(page, pagesize);
        List<Stock> books = stockMapper.selectByExample(example);

        return books;

    }

    /**
     * 修改库存
     *
     * @param type
     * @param count
     * @param bookId
     */
    @Override
    public void updateStock(Integer type, Integer count, String bookId) {

        Stock stock = stockMapper.selectByPrimaryKey(bookId);

        //判断是出库还是入库， 0 -> 入库
        switch (type) {
            //入库
            case 0:
                stock.setStock(stock.getStock() + count);
                break;
            case 1:
                stock.setStock(stock.getStock() - count);
                //如果库存小于等于0了，要下架图书
                if (stock.getStock() == 0) {
                    List<String> list = new ArrayList<>();
                    list.add(stock.getBookId());
                    this.deleteBook(list);
                }
                break;
        }

        StockStatement stockStatement = new StockStatement();
        stockStatement.setBookId(bookId);
        stockStatement.setCount(count);
        stockStatement.setOperationTime(new Date());
        stockStatement.setType(type);
        //将更改记录填回数据表
        statementMapper.insert(stockStatement);
        //存回数据库
        stockMapper.updateByPrimaryKey(stock);


    }


    /**
     * 查询当天的入库与出库的量
     *
     * @return
     */
    @Override
    public int findStatementCountByType(Integer type) {
        Example example = new Example(StockStatement.class);
        String format = DateUtils.getToday();
        example.createCriteria().andBetween("operationTime", format + " 00:00:00", format + " 23:59:59")
                .andEqualTo("type", type);
        List<StockStatement> stockStatementList = statementMapper.selectByExample(example);
        int sum = 0;
        for (StockStatement stockStatement : stockStatementList) {

            sum += stockStatement.getCount();
        }

        return sum;
    }

    /**
     * 获得7天内的入库和出库量
     *
     * @param type
     * @return
     */
    @Override
    public List<Integer> findSevenDayStatementByType(Integer type) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            Example example = new Example(StockStatement.class);
            String date = DateUtils.getDateAgo(i);
            example.createCriteria().andBetween("operationTime", date + " 00:00:00", date + " 23:59:59")
                    .andEqualTo("type", type);
            List<StockStatement> stockStatementList = statementMapper.selectByExample(example);
            int sum = 0;
            for (StockStatement stockStatement : stockStatementList) {
                sum += stockStatement.getCount();
            }
            list.add(sum);
        }

        return list;


    }

    /**
     * 报表
     *
     * @param page
     * @param pagesize
     * @param type
     * @return
     */
    @Override
    public List<StockStatement> findStatementTodayByTypyAndPage(Integer page, Integer pagesize, Integer type) {

        StockStatement stockStatement = new StockStatement();
        String today = DateUtils.getToday();
        Example example = new Example(StockStatement.class);
        example.createCriteria().andBetween("operationTime", today + " 00:00:00", today + " 23:59:59")
                .andEqualTo("type", type);
        PageHelper.startPage(page, pagesize);
        return statementMapper.selectByExample(example);

    }


    @Override
    public List<Book> queryBookByKeyword(String keyword) {
        if (StringUtils.isEmpty(keyword)) {

            return null;
        } else {
            Example example = new Example(Book.class);
            example.createCriteria().andLike("name", "%" + keyword + "%");

            return bookMapper.selectByExample(example);
        }

    }

    @Override
    public List<Book> findByIds(List<String> ids) {

        return this.bookMapper.selectByIdList(ids);

    }

    /**
     * 修改库存，这个方法只是用来处理退款时增加库存用的
     *
     * @param type
     * @param count
     * @param bookId
     */
    @Override
    public void updateStockAndSales(Integer type, Integer count, String bookId) {

        Stock stock = stockMapper.selectByPrimaryKey(bookId);
        //判断是出库还是入库， 0 -> 入库
        if (type == 1) {
            return;
        }
        stock.setStock(stock.getStock() + count);
        stock.setSales(stock.getSales() - count);

        StockStatement stockStatement = new StockStatement();
        stockStatement.setBookId(bookId);
        stockStatement.setCount(count);
        stockStatement.setOperationTime(new Date());
        stockStatement.setType(type);

        //存回数据库
        stockMapper.updateByPrimaryKey(stock);
        //将更改记录填回数据表
        statementMapper.insert(stockStatement);

    }


}
