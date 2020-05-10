package com.bookstore.service.impl;

import com.bookstore.enums.ReturnStatusEnums;
import com.bookstore.enums.StatusEnums;
import com.bookstore.enums.StockStatementType;
import com.bookstore.mapper.*;
import com.bookstore.pojo.*;
import com.bookstore.service.ActivityService;
import com.bookstore.service.BookService;
import com.bookstore.service.CartService;
import com.bookstore.service.OrderService;
import com.bookstore.utils.DateUtils;
import com.bookstore.utils.HttpClient;
import com.bookstore.utils.IdWorker;
import com.bookstore.web.config.PayConfig;
import com.bookstore.web.config.PrefixConfig;
import com.github.pagehelper.PageHelper;
import com.github.wxpay.sdk.WXPayUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@EnableConfigurationProperties(PayConfig.class)
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private StockStatementMapper statementMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper detailMapper;

    @Autowired
    private OrderStatusMapper statusMapper;

    @Autowired
    private CartService cartService;

    @Autowired
    private BookService bookService;

    @Autowired
    private PayConfig payConfig;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private OrderReturnMapper orderReturnMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private PrefixConfig prefixConfig;


    @Override
    public int getFavorable(List<String> bookIds, Integer userId){
        Map<String, Integer> map = new HashMap<>();
        Set<Integer> set = new HashSet<>();
        List<Integer> categoryIds = new ArrayList<>();
        List<Book> bookList = bookService.findByIds(bookIds);
        Map<Integer, Map<String, Integer>> categoryMap = new HashMap<>();
        List<Cart> cartList = cartService.queryCartByIds(userId, bookIds);
        for (Cart cart : cartList) {
            //将详情价格存放到详情中，以便后面计算分类活动和商品活动的优惠金额
            map.put(cart.getBookId(), cart.getPrice() * cart.getNumber());
        }
        for (Book book : bookList) {
            set.add(book.getCategoryId());
            if (categoryMap.get(book.getCategoryId()) == null){
                Map<String, Integer> temp = new HashMap<>();
                temp.put(book.getId(), map.get(book.getId()));
                categoryMap.put(book.getCategoryId(), temp);
            }else {
                //如果存在这个key，覆盖即可
                Map<String, Integer> temp = categoryMap.get(book.getCategoryId());
                temp.put(book.getId(), map.get(book.getId()));
                categoryMap.put(book.getCategoryId(), temp);
            }
        }
        for (Integer integer : set) {
            categoryIds.add(integer);
        }
        //活动抵扣金额
        int activityAmount;
        int bookActivityAmount = 0;
        int categoryActivityAmount = 0;
        // 1.全场满减活动
        activityAmount = getActivityAmount(map);
        // 2.指定分类活动
        categoryActivityAmount = getCategoryActivityAmount(categoryIds, categoryMap, categoryActivityAmount);
        // 3.指定商品活动
        bookActivityAmount = getBookActivityAmount(bookIds, map, bookActivityAmount);
        return Math.max(Math.max(bookActivityAmount, categoryActivityAmount), activityAmount);
    }

    private int getBookActivityAmount(List<String> bookIds, Map<String, Integer> map, int bookActivityAmount) {
        List<BookActivity> bookActivityList = activityService.queryBookActivityByBookIds(bookIds);
        if (CollectionUtils.isEmpty(bookActivityList)) {
            bookActivityAmount = 0;
        } else {
            for (BookActivity bookActivity : bookActivityList) {
                bookActivityAmount += map.get(bookActivity.getBookId()) * (100-  bookActivity.getActivity().getDiscount());
            }
            if (bookActivityAmount % 100 > 0) {
                bookActivityAmount /= 100;
                bookActivityAmount++;
            } else {
                bookActivityAmount /= 100;
            }
        }
        return bookActivityAmount;
    }

    private int getCategoryActivityAmount(List<Integer> categoryIds, Map<Integer, Map<String, Integer>> categoryMap, int categoryActivityAmount) {
        List<CategoryActivity> categoryActivityList = activityService.queryCategoryActivityByCategoryIds(categoryIds);
        if (CollectionUtils.isEmpty(categoryActivityList)) {
            categoryActivityAmount = 0;
        } else {
            for (CategoryActivity categoryActivity : categoryActivityList) {
                Map<String, Integer> temp = categoryMap.get(categoryActivity.getCategoryId());
                for (Integer value : temp.values()) {
                    categoryActivityAmount += value * (100 - categoryActivity.getActivity().getDiscount());
                }
            }
            if (categoryActivityAmount % 100 > 0){
                categoryActivityAmount /= 100;
                categoryActivityAmount++;
            }else {
                categoryActivityAmount /= 100;
            }
        }
        return categoryActivityAmount;
    }

    private int getActivityAmount(Map<String, Integer> map) {
        int activityAmount;
        Activity activity = activityService.queryOpenActivity();
        //判断是否为空
        if (activity == null) {
            activityAmount = 0;
        }else {
            //判断是否满足满减活动
            int sum = 0;
            for (Integer value : map.values()) {
                sum += value;
            }
            if (sum >= activity.getFull()) {
                activityAmount = activity.getSubtract();
            }else {
                activityAmount = 0;
            }
        }
        return activityAmount;
    }

    /**
     *
     * @param order
     * @param ids    bookIds
     * @param userId
     * @return
     *
     */
    @Override
    @Transactional
    public Order addOrder(Order order, List<String> ids, int userId) {
        //封装订单信息
        //页面传递过来的数据已经有：支付方式、买家留言、收货地址、收货人手机、收货人邮编
        order.setOrderId(idWorker.nextId());
        List<Cart> cartList = cartService.queryCartByIds(userId, ids);
        //封装订单状态
        OrderStatus orderStatus = new OrderStatus();orderStatus.setCreateTime(new Date());
        orderStatus.setStatus(StatusEnums.UN_PAY.getStatus());orderStatus.setOrderId(order.getOrderId());
        //封装订单条目
        int totalPrice = 0;
        List<OrderDetail> orderDetailList = new ArrayList<>();
        List<StockStatement> stockStatementList = new ArrayList<>();
        for (Cart cart : cartList) {
            OrderDetail orderDetail = new OrderDetail();orderDetail.setBookId(cart.getBookId());
            orderDetail.setBookName(cart.getBookName());orderDetail.setImg(cart.getImg());
            orderDetail.setNumber(cart.getNumber());orderDetail.setOrderId(order.getOrderId());
            orderDetail.setPrice(cart.getPrice());totalPrice += cart.getNumber() * cart.getPrice();
            orderDetailList.add(orderDetail);StockStatement stockStatement = new StockStatement();
            stockStatement.setBookId(cart.getBookId());stockStatement.setCount(cart.getNumber());
            stockStatement.setOperationTime(new Date());stockStatement.setType(1);stockStatementList.add(stockStatement);
        }
        int favorable = this.getFavorable(ids, userId);
        int actualPay = totalPrice - favorable + 1000;
        //填充order信息
        order.setUserId(userId);order.setPostage(1000);order.setTotalPay(totalPrice);order.setActualPay(actualPay);
        order.setStatus(orderStatus);order.setDetailList(orderDetailList);
        order.setFavorable(favorable);
        //提交订单后，还要将购物车清除，并且库存要减少
        bookService.decreaseStock(orderDetailList);
        orderMapper.insert(order);
        statusMapper.insert(orderStatus);
        detailMapper.insertList(orderDetailList);
        cartService.deleteCarts(userId, ids);
        //记录总订单数
        ValueOperations<String, String> operations = recordTotalOrderCount();
        //记录今日订单数
        recordTodayOrderCount(operations);
        //出库信息记录
        statementMapper.insertList(stockStatementList);
        return order;
    }

    private void recordTodayOrderCount(ValueOperations<String, String> operations) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String format = dateFormat.format(new Date());
        if (operations.get(prefixConfig.getOrderQuantityDaily() + format) == null) {
            operations.set(prefixConfig.getOrderQuantityDaily() + format, "1");
        } else {
            operations.set(prefixConfig.getOrderQuantityDaily() + format, String.valueOf(Integer.parseInt(operations.get(prefixConfig.getOrderQuantityDaily() + format)) + 1));
        }
    }

    private ValueOperations<String, String> recordTotalOrderCount() {
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        if (operations.get(prefixConfig.getOrderQuantityTotal()) == null) {
            operations.set(prefixConfig.getOrderQuantityTotal(), "1");
        } else {
            operations.set(prefixConfig.getOrderQuantityTotal(), String.valueOf(Integer.parseInt(operations.get(prefixConfig.getOrderQuantityTotal())) + 1));
        }
        return operations;
    }

    @Override
    public String createUrl(String orderId, Integer actualPay) {

        Map map = new HashMap();

        map.put("appid", payConfig.getAppID());
        map.put("mch_id", payConfig.getMchID());
        map.put("nonce_str", WXPayUtil.generateNonceStr());
        map.put("body", "网上书城");
        map.put("out_trade_no", orderId);
        map.put("total_fee", "1");
        map.put("spbill_create_ip", "127.0.0.1");
        map.put("notify_url", payConfig.getNotifyUrl());
        map.put("trade_type", "NATIVE");
        String url = null;
        try {
            String param = WXPayUtil.generateSignedXml(map, payConfig.getKey());
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(param);
            httpClient.post();

            Map<String, String> result = WXPayUtil.xmlToMap(httpClient.getContent());
            url = result.get("code_url");

        } catch (Exception e) {
            log.info("【支付服务】支付失败");
            throw new RuntimeException();
        }
        return url;

    }


    /**
     * 查询支付状态
     * @param orderId
     * @return
     */
    @Override
    @Transactional
    public Map pay(Long orderId) {

        //查询订单状态，如果订单状态为success则跳转到成功页面
        Map<String, String> map = new HashMap<>();
        map.put("appid", payConfig.getAppID());
        map.put("mch_id", payConfig.getMchID());
        map.put("out_trade_no", orderId.toString());
        map.put("nonce_str", WXPayUtil.generateNonceStr());
        String result = null;
        try {
            Map<String, String> data = getPayMap(map, "https://api.mch.weixin.qq.com/pay/orderquery");
            if (data.get("trade_state").equals("NOTPAY")) {
                return null;
            }
            //返回map之前要将订单的状态改变
            OrderStatus orderStatus = new OrderStatus();
            synchronized (OrderServiceImpl.class) {
                if (statusMapper.selectByPrimaryKey(orderId).getStatus().equals(StatusEnums.UN_PAY.getStatus())) {
                    orderStatus.setStatus(StatusEnums.PAYED.getStatus());
                    orderStatus.setPaymentTime(new Date());
                    orderStatus.setOrderId(orderId);
                    statusMapper.updateByPrimaryKeySelective(orderStatus);
                    //记录总成交额到redis中
                    ValueOperations<String, String> operations = redisTemplate.opsForValue();
                    Integer actualPay = recordTotalTurnover(orderId, operations);
                    //记录当日成交额
                    recordTodayTurnover(operations, actualPay);
                }
            }
            return data;
        } catch (Exception e) {
            log.info("【支付服务】支付状态查询失败");
        }
        return null;

    }

    private Map<String, String> getPayMap(Map<String, String> map, String s) throws Exception {
        String xml = WXPayUtil.generateSignedXml(map, payConfig.getKey());
        HttpClient httpClient = new HttpClient(s);
        httpClient.setHttps(true);
        httpClient.setXmlParam(xml);
        httpClient.post();
        return WXPayUtil.xmlToMap(httpClient.getContent());
    }

    private Integer recordTotalTurnover(Long orderId, ValueOperations<String, String> operations) {
        Integer actualPay = orderMapper.selectByPrimaryKey(orderId).getActualPay();
        if (operations.get(prefixConfig.getTurnoverTotal()) == null) {

            operations.set(prefixConfig.getTurnoverTotal(), String.valueOf(actualPay));
        } else {
            operations.set(prefixConfig.getTurnoverTotal(), String.valueOf(Integer.valueOf(operations.get("TurnoverTotal")) + actualPay));
        }
        return actualPay;
    }

    private void recordTodayTurnover(ValueOperations<String, String> operations, Integer actualPay) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String format = dateFormat.format(new Date());

        if (operations.get(prefixConfig.getTurnoverDaily() + format) == null) {
            operations.set(prefixConfig.getTurnoverDaily() + format, String.valueOf(actualPay));
        } else {
            operations.set(prefixConfig.getTurnoverDaily() + format, String.valueOf(Integer.valueOf(operations.get(prefixConfig.getTurnoverDaily() + format)) + actualPay));
        }
    }

    @Override
    public List<Order> queryOrderByStatus(Integer status, Integer userId) {

        //查询出订单
        List<Order> orderList = null;
        if (status == null) {
            Order order = new Order();
            order.setUserId(userId);
            orderList = orderMapper.select(order);
        } else {
            orderList = orderMapper.queryOrderByStatus(status, userId);
        }

        //填充订单详情和订单状态
        for (Order order : orderList) {

            OrderStatus orderStatus = statusMapper.selectByPrimaryKey(order.getOrderId());
            order.setStatus(orderStatus);

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(order.getOrderId());

            order.setDetailList(detailMapper.select(orderDetail));

        }

        Collections.sort(orderList);


        return orderList;

    }

    @Override
    public Order queryOrderById(Long orderId) {


        //查询订单
        Order order = orderMapper.selectByPrimaryKey(orderId);

        order.setStatus(statusMapper.selectByPrimaryKey(orderId));

        OrderDetail detail = new OrderDetail();
        detail.setOrderId(orderId);
        order.setDetailList(detailMapper.select(detail));

        return order;

    }

    @Override
    public void updateStatus(Long orderId, int i) {

        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setStatus(i);
        orderStatus.setEndTime(new Date());
        statusMapper.updateByPrimaryKeySelective(orderStatus);

    }

    @Override
    public List<Order> queryOrderByStatus(Integer page, Integer pagesize, Integer status) {

        //根据状态查询订单
        List<Order> orderList = null;
        if (status == null) {
            //没给出状态就查询全部
            PageHelper.startPage(page, pagesize);
            orderList = orderMapper.selectAll();
        } else {
            //给出状态的话就按状态查询
            PageHelper.startPage(page, pagesize);
            orderList = orderMapper.findOrderByStatus(status);
        }

        //填充信息
        for (Order order : orderList) {

            //填充状态
            order.setStatus(statusMapper.selectByPrimaryKey(order.getOrderId()));


        }


        return orderList;


    }

    @Override
    public void updateStatus(OrderStatus status, Integer num) {

        OrderStatus condition = new OrderStatus();

        condition.setOrderId(status.getOrderId());
        OrderStatus orderStatus = statusMapper.selectOne(condition);

        orderStatus.setShippingName(status.getShippingName());
        orderStatus.setShippingCode(status.getShippingCode());
        orderStatus.setStatus(num);
        orderStatus.setConsignTime(new Date());

        statusMapper.updateByPrimaryKeySelective(orderStatus);

    }


    /**
     * 0 -> 已付款未发货退款；1 -> 已发货退款
     *
     * @return
     */
    @Override
    @Transactional
    public OrderReturn refund(OrderReturn orderReturn) {
        //查询订单
        Order order = orderMapper.selectByPrimaryKey(orderReturn.getOrderId());
        OrderStatus orderStatus = statusMapper.selectByPrimaryKey(orderReturn.getOrderId());
        //如果是已付款未发货退款，那么直接退款
        //如果是已发货退款，那么需要按流程走
        switch (orderReturn.getType()) {
            case 0:
                orderReturn.setStatus(ReturnStatusEnums.ACCEPTED.getStatus());
                break;
            case 1:
                orderReturn.setStatus(ReturnStatusEnums.PENDING.getStatus());
                //查询出对应的所有订单详情
                OrderDetail orderDetail = new OrderDetail();
                break;

        }
        //封装对象，剩下快递公司和快递单号没有填
        orderReturn.setApplyTime(new Date());
        orderReturn.setReturnAmount(order.getActualPay());
        orderReturn.setReturnName(order.getReceiver());
        orderReturn.setReturnPhone(order.getReceiverPhone());
        orderReturn.setUserId(order.getUserId());
        //修改订单状态为退款
        orderStatus.setStatus(StatusEnums.REFUND.getStatus());
        orderReturnMapper.insert(orderReturn);
        statusMapper.updateByPrimaryKey(orderStatus);
        return orderReturn;
    }

    /**
     * 通过orderId查询对应的退款信息
     *
     * @param orderId
     * @return
     */
    @Override
    public OrderReturn findRefundDetailByOrderId(Long orderId) {

        OrderReturn orderReturn = new OrderReturn();
        orderReturn.setOrderId(orderId);
        return orderReturn = orderReturnMapper.selectOne(orderReturn);


    }

    /**
     * 查询退款信息
     *
     * @param page
     * @param pagesize
     * @param status
     * @return
     */
    @Override
    public List<OrderReturn> findRefundByPage(Integer page, Integer pagesize, Integer status) {

        List<OrderReturn> orderReturnList;
        OrderReturn orderReturn = new OrderReturn();
        orderReturn.setStatus(status);
        PageHelper.startPage(page, pagesize);
        orderReturnList = orderReturnMapper.select(orderReturn);

        return orderReturnList;

    }

    @Override
    public OrderReturn queryRefundById(Integer id) {

        return orderReturnMapper.selectByPrimaryKey(id);
    }

    /**
     * 修改退款状态，并且将商品出库
     *
     * @param status
     * @param id
     * @return
     */
    @Override
    @Transactional
    public OrderReturn updateRefundStatus(Integer status, Integer id) {

        OrderReturn orderReturn = orderReturnMapper.selectByPrimaryKey(id);

        //如果退款成功，库存增加，记录入库信息
        if (status == 2) {
            //查询出对应的所有订单详情
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderReturn.getOrderId());
            List<OrderDetail> detailList = detailMapper.select(orderDetail);
            for (OrderDetail detail : detailList) {
                bookService.updateStockAndSales(StockStatementType.STORAGE.getType(), detail.getNumber(), detail.getBookId());
            }

        }

        orderReturn.setStatus(status);
        orderReturnMapper.updateByPrimaryKey(orderReturn);

        return orderReturn;
    }

    /**
     * 总成交额
     *
     * @return
     */
    @Override
    public int findTurnoverTotal() {
        if (redisTemplate.opsForValue().get(prefixConfig.getTurnoverTotal()) != null) {
            return Integer.parseInt(redisTemplate.opsForValue().get(prefixConfig.getTurnoverTotal()));
        } else {
            return 0;
        }
    }

    @Override
    public int findTurnoverToday() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        if (redisTemplate.opsForValue().get(prefixConfig.getTurnoverDaily() + dateFormat.format(new Date())) != null) {
            return Integer.parseInt(redisTemplate.opsForValue().get(prefixConfig.getTurnoverDaily() + dateFormat.format(new Date())));
        } else {
            return 0;
        }
    }

    @Override
    public int findOrderQuantityTotal() {
        if (redisTemplate.opsForValue().get(prefixConfig.getOrderQuantityTotal()) != null) {
            return Integer.parseInt(redisTemplate.opsForValue().get(prefixConfig.getOrderQuantityTotal()));
        } else {
            return 0;
        }
    }

    @Override
    public int findOrderQuantityToday() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        if (redisTemplate.opsForValue().get(prefixConfig.getOrderQuantityDaily() + dateFormat.format(new Date())) != null) {
            return Integer.parseInt(redisTemplate.opsForValue().get(prefixConfig.getOrderQuantityDaily() + dateFormat.format(new Date())));
        } else {
            return 0;
        }
    }

    @Override
    public List<Integer> queryTodayOrderTypeAll() {

        String today = DateUtils.getToday();
        List<Integer> list = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Example example = new Example(OrderStatus.class);
            example.createCriteria().andBetween("createTime", today + " 00:00:00", today + " 23:59:59")
                    .andEqualTo("status", i);
            List<OrderStatus> orderStatuses = statusMapper.selectByExample(example);
            list.add(orderStatuses.size());
        }

        return list;

    }

    @Override
    public List<Integer> findTurnoverDailyList() {

        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            String date = DateUtils.getDateAgo(i);
            if (redisTemplate.opsForValue().get(prefixConfig.getTurnoverDaily() + date) == null) {
                list.add(0);
            } else {
                list.add(Integer.parseInt(redisTemplate.opsForValue().get(prefixConfig.getTurnoverDaily() + date)));
            }
        }
        return list;

    }





}
