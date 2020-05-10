package com.bookstore.web.controller;

import com.bookstore.enums.StatusEnums;
import com.bookstore.enums.StockStatementType;
import com.bookstore.pojo.*;
import com.bookstore.service.*;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("admin")
public class AdminController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BookService bookService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private LogService logService;

    @Autowired
    private ActivityService activityService;


    @RequestMapping("skipAddAdmin")
    public String skipAddAdmin(){
        return "admin-authority-add";
    }

    @RequestMapping("addAdmin")
    public String addAdmin(Admin admin){
        adminService.addAdmin(admin);
        return "redirect:skipAuthority";
    }

    @RequestMapping("queryCategoryByKeyword")
    @ResponseBody
    public List<Category> queryCategoryByKeyword(@RequestParam("keyword") String keyword) {
        List<Category> categoryList = categoryService.queryCategoryByKeyword(keyword);
        return categoryList;
    }

    /**
     * 通过书名模糊查询
     *
     * @param keyword
     * @return
     */
    @RequestMapping("queryBookByKeyword")
    @ResponseBody
    public List<Book> queryBookByKeyword(@RequestParam("keyword") String keyword) {
        List<Book> bookList = bookService.queryBookByKeyword(keyword);
        return bookList;

    }

    @RequestMapping("skipActivityEdit")
    public String skipActivityEdit(Integer id, Model model){

        Activity activity = activityService.queryActivityById(id);
        Integer useType = activity.getUseType();
        if (useType == 1){
            CategoryActivity categoryActivity = activityService.queryCategoryActivityByActivityId(id);
            model.addAttribute("categoryActivity", categoryActivity);
        }

        if (useType == 2){
            BookActivity bookActivity = activityService.queryBookActivityByActivityId(id);
            model.addAttribute("bookActivity", bookActivity);
        }

        model.addAttribute("activity", activity);

        return "admin-activity-edit";
    }


    /**
     * 修改促销活动
     *
     * @param activity
     * @return
     */
    @RequestMapping("updateActivity")
    public String updateActivity(Activity activity,
                                 @RequestParam(name = "bookId", defaultValue = "") String bookId,
                                 @RequestParam(name = "categoryId", defaultValue = "") String categoryId) {

        if (!StringUtils.isEmpty(bookId)){
            activityService.updateActivity(activity, bookId);
        }else if (!StringUtils.isEmpty(categoryId)){
            activityService.updateActivity(activity, categoryId);
        }else {
            activityService.updateActivity(activity, null);
        }

        return "redirect:skipActivityList";

    }



    /**
     * 跳转到促销活动添加页面
     *
     * @param model
     * @return
     */
    @RequestMapping("skipActivity")
    public String skipActivity(Model model) {
        Activity activity = activityService.find();

        model.addAttribute("activity", activity);

        return "admin-activity-add";
    }

    /**
     * 关闭活动
     *
     * @param ids
     */
    @RequestMapping("deleteActivity")
    @ResponseBody
    public void deleteActivity(@RequestParam("list") List<Integer> ids) {

        activityService.deleteActivity(ids);

    }

    /**
     * 跳转到活动列表
     *
     * @param page
     * @param pageSize
     * @param keyword
     * @param useType
     * @param model
     * @return
     */
    @RequestMapping("skipActivityList")
    public String skipActivityList(@RequestParam(name = "page", defaultValue = "1") Integer page,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   @RequestParam(name = "keyword", defaultValue = "") String keyword,
                                   @RequestParam(name = "useType", defaultValue = "") Integer useType,
                                   Model model) {

        List<Activity> activityList = activityService.queryActivityByPage(page, pageSize, keyword, useType);
        PageInfo<Activity> pageInfo = new PageInfo<>(activityList);

        model.addAttribute("keyword", keyword);
        model.addAttribute("useType", useType);
        model.addAttribute("pageInfo", pageInfo);

        return "admin-activity-list";

    }

    /**
     * 跳转到出库入库详情页面
     *
     * @param page
     * @param pagesize
     * @param type
     * @param model
     * @return
     */
    @RequestMapping("skipStatement")
    public String skipStatement(@RequestParam(name = "page", defaultValue = "1") Integer page,
                                @RequestParam(name = "pageSize", defaultValue = "10") Integer pagesize,
                                @RequestParam(name = "type", defaultValue = "") Integer type,
                                Model model) {

        List<StockStatement> stockStatementList = bookService.findStatementTodayByTypyAndPage(page, pagesize, type);
        PageInfo<StockStatement> pageInfo = new PageInfo<>(stockStatementList);

        model.addAttribute("pageInfo", pageInfo);
        model.addAttribute("type", type);

        switch (type) {
            case 1:
                model.addAttribute("msg", "出库");
                break;
            case 0:
                model.addAttribute("msg", "入库");
                break;
        }

        return "admin-statement-list";

    }

    /**
     * 查询各种数据，跳转到报表页面
     *
     * @return
     */
    @RequestMapping("skipReport")
    public String skipReport(HttpServletRequest request, Model model) {

        //总访问量
        request.setAttribute("PVTotal", adminService.findPVTotal());

        //今日访问量
        request.setAttribute("PVToday", adminService.findPVToday());

        //总成交额
        request.setAttribute("turnoverTotal", orderService.findTurnoverTotal());

        //今日成交额
        request.setAttribute("turnoverToday", orderService.findTurnoverToday());

        // 总订单数
        request.setAttribute("orderQuantityTotal", orderService.findOrderQuantityTotal());

        // 今日订单数
        request.setAttribute("orderQuantityToday", orderService.findOrderQuantityToday());

        //今日入库数量
        request.setAttribute("storage", bookService.findStatementCountByType(StockStatementType.STORAGE.getType()));

        //今日出库数量
        request.setAttribute("putsInStorage", bookService.findStatementCountByType(StockStatementType.PUTS_IN_STORAGE.getType()));

        //近7日的入库数量
        model.addAttribute("storageList", bookService.findSevenDayStatementByType(StockStatementType.STORAGE.getType()));

        //近7日的出库数量
        model.addAttribute("putsInStorageList", bookService.findSevenDayStatementByType(StockStatementType.PUTS_IN_STORAGE.getType()));

        //今日订单各种状态占比
        model.addAttribute("orderTypeList", orderService.queryTodayOrderTypeAll());

        //7日成交额
        model.addAttribute("TurnoverDailyList", orderService.findTurnoverDailyList());

        //7日浏览量
        model.addAttribute("PVDailyList", adminService.queryPVDailyList());

        return "admin-report";
    }

    /**
     * 修改退款状态
     *
     * @param status
     * @param id
     * @param model
     * @return
     */
    @RequestMapping("updateRefundStatus")
    public String updateRefundStatus(Integer status, Integer id, Model model) {

        OrderReturn orderReturn = orderService.updateRefundStatus(status, id);
        model.addAttribute("orderReturn", orderReturn);
        return "admin-refund-detail";

    }

    /**
     * 跳转到退款详情页面
     *
     * @param id
     * @param model
     * @return
     */
    @RequestMapping("skipRefundDetail")
    public String skipRefundDetail(Integer id, Model model) {

        OrderReturn orderReturn = orderService.queryRefundById(id);
        model.addAttribute("orderReturn", orderReturn);
        return "admin-refund-detail";
    }

    /**
     * 跳转到退款列表页面
     *
     * @param page
     * @param pagesize
     * @param status
     * @param model
     * @return
     */
    @RequestMapping("skipRefund")
    public String skipRefund(@RequestParam(name = "page", defaultValue = "1") Integer page,
                             @RequestParam(name = "pageSize", defaultValue = "10") Integer pagesize,
                             @RequestParam(name = "status", defaultValue = "") Integer status,
                             Model model) {

        List<OrderReturn> orderReturnList = orderService.findRefundByPage(page, pagesize, status);
        PageInfo<OrderReturn> pageInfo = new PageInfo<>(orderReturnList);
        model.addAttribute("pageInfo", pageInfo);

        return "admin-refund-list";

    }


    /**
     * 修改库存
     *
     * @param type   操作类型，出库or入库
     * @param count
     * @param bookId
     */
    @RequestMapping("updateStock")
    @ResponseBody
    public void updateStock(@RequestParam Integer type,
                            @RequestParam Integer count,
                            @RequestParam String bookId) {


        bookService.updateStock(type, count, bookId);

    }

    /**
     * 跳转到库存管理页面
     *
     * @param page
     * @param pagesize
     * @param keyword
     * @param model
     * @return
     */
    @RequestMapping("skipStockList")
    public String skipStockList(@RequestParam(name = "page", defaultValue = "1") Integer page,
                                @RequestParam(name = "pageSize", defaultValue = "10") Integer pagesize,
                                @RequestParam(name = "keyword", defaultValue = "") String keyword,
                                Model model) {

        List<Stock> bookList = bookService.findStockByPage(page, pagesize, keyword);
        PageInfo<Stock> pageInfo = new PageInfo(bookList);
        model.addAttribute("pageInfo", pageInfo);
        model.addAttribute("keyword", keyword);

        return "admin-stock-list";


    }

    /**
     * 修改管理员等级
     *
     * @param id
     * @return
     */
    @RequestMapping("updateLevel")
    public String updateLevel(Integer id) {
        adminService.updateLevel(id);
        return "redirect:skipAuthority";
    }

    /**
     * 跳转到权限管理页面
     *
     * @param request
     * @return
     */
    @RequestMapping("skipAuthority")
    public String skipAuthority(HttpServletRequest request) {

        Admin admin = (Admin) request.getSession().getAttribute("admin");
        if (admin.getLevel() != 1) {
            throw new RuntimeException();
        }

        List<Admin> adminList = adminService.findAll();
        request.setAttribute("adminList", adminList);

        return "admin-authority";

    }

    /**
     * 跳转到日志列表页面
     *
     * @param page
     * @param pagesize
     * @param model
     * @return
     */
    @RequestMapping("skipLogList")
    public String skipLogList(@RequestParam(name = "page", defaultValue = "1") Integer page,
                              @RequestParam(name = "pagesize", defaultValue = "10") Integer pagesize,
                              Model model) {

        List<Log> logList = logService.findAll(page, pagesize);
        PageInfo<Log> pageInfo = new PageInfo<>(logList);
        model.addAttribute("pageInfo", pageInfo);
        return "admin-log-list";
    }

    /**
     * 注销功能
     *
     * @param session
     * @return
     */
    @RequestMapping("logout")
    public String logout(HttpSession session) {

        session.removeAttribute("admin");
        return "redirect:skipLogin";
    }

    /**
     * 登陆功能
     *
     * @param admin
     * @param request
     * @return
     */
    @RequestMapping("login")
    public String login(Admin admin, HttpServletRequest request) {

        try {
            Admin result = adminService.login(admin);
            request.getSession().setAttribute("admin", result);
            //如果有异常抛出，说明校验失败
        } catch (Exception e) {
            request.setAttribute("msg", "用户名或密码错误");
            return "admin-login";
        }


        return "redirect:index";
    }

    /**
     * 跳转到登陆页面
     *
     * @return
     */
    @RequestMapping("skipLogin")
    public String skipLogin() {
        return "admin-login";
    }

    /**
     * 对订单发货
     *
     * @param status
     * @return
     */
    @RequestMapping("shipping")
    public String shipping(OrderStatus status) {

        orderService.updateStatus(status, StatusEnums.UN_CONFIRM.getStatus());

        return "redirect:skipOrderList";
    }

    /**
     * 跳转到订单发货页面
     *
     * @param orderId
     * @param model
     * @return
     */
    @RequestMapping("skipOrderShipping")
    public String skipOrderShipping(@RequestParam("orderId") String orderId, Model model) {

        model.addAttribute("orderId", orderId);

        return "admin-order-shipping";
    }

    /**
     * 跳转到订单详情页面
     *
     * @param orderId
     * @param model
     * @return
     */
    @RequestMapping("skipOrderQuery")
    public String skipOrderQuery(@RequestParam("orderId") Long orderId, Model model) {

        Order order = orderService.queryOrderById(orderId);

        model.addAttribute("order", order);

        return "admin-order-query";


    }

    /**
     * 跳转到订单列表页面
     *
     * @param status
     * @param page
     * @param pagesize
     * @param model
     * @return
     */
    @RequestMapping("skipOrderList")
    public String skipOrderList(@RequestParam(name = "status", defaultValue = "") Integer status,
                                @RequestParam(name = "page", defaultValue = "1") Integer page,
                                @RequestParam(name = "pagesize", defaultValue = "10") Integer pagesize,
                                Model model) {

        List<Order> orderList = orderService.queryOrderByStatus(page, pagesize, status);
        PageInfo<Order> pageInfo = new PageInfo(orderList);
        model.addAttribute("pageInfo", pageInfo);
        model.addAttribute("status", status);
        return "admin-order-list";
    }

    /**
     * 修改图书信息
     *
     * @param book
     * @param result
     * @return
     */
    @RequestMapping("updateBook")
    public String updateBook(@Valid Book book, BindingResult result) {

        if (result.hasErrors()) {
            return "error";
        }

        bookService.updateBook(book);

        return "redirect:skipBookList";
    }

    /**
     * 删除图书
     *
     * @param ids
     */
    @RequestMapping("deleteBook")
    @ResponseBody
    public void deleteBook(@RequestParam("list") List<String> ids) {
        bookService.deleteBook(ids);
    }

    /**
     * 添加图书
     *
     * @param book
     * @param result
     * @param img
     * @return
     */
    @RequestMapping("addBook")
    public String addBook(@Valid Book book, BindingResult result, MultipartFile img) {

        if (result.hasErrors()) {
            if (!result.getFieldError().getField().equals("img")
            ) {
                return "error";
            }
        }

        bookService.addBook(book, img);
        return "redirect:skipBookList";
    }

    /**
     * 跳转到图书管理页面
     *
     * @return
     */
    @RequestMapping("skipBookList")
    public String skipBookList(@RequestParam(name = "page", defaultValue = "1") Integer page,
                               @RequestParam(name = "pageSize", defaultValue = "10") Integer pagesize,
                               @RequestParam(name = "keyword", defaultValue = "") String keyword,
                               @RequestParam(name = "status", defaultValue = "") Boolean status,
                               Model model) {

        List<Book> bookList = bookService.findByPage(page, pagesize, keyword, status);
        PageInfo pageInfo = new PageInfo(bookList);
        model.addAttribute("pageInfo", pageInfo);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        return "admin-book-list";
    }

    /**
     * 修改分类
     *
     * @param category
     * @return
     */
    @RequestMapping("editCategory")
    public String editCategory(Category category, Model model) {

        categoryService.updateCategory(category);

        return "redirect:loadCategory";
    }

    /**
     * 批量删除分类
     *
     * @param ids
     */
    @RequestMapping("deleteCategory")
    @ResponseBody
    public void deleteCategory(@RequestParam("list") List<Integer> ids) {

        categoryService.deleteCategory(ids);

    }

    /**
     * 添加分类
     *
     * @param category
     * @param result
     * @return
     */
    @RequestMapping("addCategory")
    public String addCategory(@Valid Category category, BindingResult result) {

        if (result.hasErrors()) {
            //如果参数有错误，那么直接返回错误页面
            return "error";
        }
        if (!categoryService.verifyCategory(category.getName())) {
            //如果校验失败返回错误页面
            return "error";
        }

        categoryService.addCategory(category);

        return "redirect:loadCategory";
    }

    /**
     * 校验分类名称是否已经存在
     *
     * @param data
     * @return
     */
    @RequestMapping("verifyCategory")
    @ResponseBody
    public String verifyCategory(@RequestParam(name = "data") String data, Model model) {


        return categoryService.verifyCategory(data).toString();
    }

    /**
     * 根据条件查询分类
     *
     * @param page     查询的页数
     * @param pageSize 每页的数量
     * @param keyword  搜索的关键词
     * @param model
     * @return
     */
    @RequestMapping("loadCategory")
    public String loadCategory(@RequestParam(name = "page", defaultValue = "1") Integer page,
                               @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                               @RequestParam(name = "keyword", defaultValue = "") String keyword,
                               @RequestParam(name = "status", defaultValue = "") Boolean status,
                               Model model) {

        List<Category> categoryList = categoryService.queryCategoryByPage(page, pageSize, keyword, status);
        PageInfo<Category> pageInfo = new PageInfo(categoryList);
        model.addAttribute("pageInfo", pageInfo);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        return "admin-category-list";

    }

    /**
     * 跳转到分类列表页面
     *
     * @return
     */
    @RequestMapping("skipCategoryList")
    public String skipCategoryList() {

        return "admin-category-list";
    }

    /**
     * 跳转到编辑分类页面
     *
     * @param id
     * @param model
     * @return
     */
    @RequestMapping("skipCategoryEdit")
    public String skipCategoryEdit(@RequestParam("id") Integer id, Model model) {

        Category category = categoryService.findById(id);

        model.addAttribute("category", category);

        return "admin-category-edit";
    }

    /**
     * 跳转到添加分类页面
     *
     * @return
     */
    @RequestMapping("skipCategoryAdd")
    public String skipCategoryAdd() {

        return "admin-category-add";
    }


    /**
     * 跳转到图书编辑页面
     *
     * @param id
     * @param model
     * @return
     */
    @RequestMapping("skipBookEdit")
    public String skipBookEdit(@RequestParam("id") String id, Model model) {

        Book book = bookService.findById(id);
        model.addAttribute("book", book);
        List<Category> categoryList = categoryService.findAll();
        model.addAttribute("categoryList", categoryList);

        return "admin-book-edit";
    }


    /**
     * 跳转到添加图书页面
     *
     * @param model
     * @return
     */
    @RequestMapping("skipBookAdd")
    public String skipBookAdd(Model model) {

        List<Category> categoryList = categoryService.findAll();
        model.addAttribute("categoryList", categoryList);
        return "admin-book-add";
    }

    /**
     * 跳转到后台主页
     *
     * @param request
     * @return
     */
    @RequestMapping("index")
    public String skipIndex(HttpServletRequest request) {
        return "admin-index";
    }

}
