package com.bookstore.web.controller;

import com.bookstore.pojo.Order;
import com.bookstore.pojo.OrderReturn;
import com.bookstore.service.OrderService;
import com.bookstore.utils.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 跳转到退款详情
     * @param orderId
     * @return
     */
    @RequestMapping("skipRefundDetail")
    public String skipRefundDetail(Long orderId, Model model){

        OrderReturn orderReturn = orderService.findRefundDetailByOrderId(orderId);
        model.addAttribute("orderReturn", orderReturn);
        return "refundDetail";
    }

    /**
     * 退款
     *
     * @return
     */
    @RequestMapping("refund")
    public String refund(OrderReturn orderReturn, Model model){
        OrderReturn result = orderService.refund(orderReturn);

        //判断状态，如果是1，那么需要继续填写物流信息
        model.addAttribute("orderReturn", result);
        return "refundDetail";

    }

    @RequestMapping("skipRefund")
    public String skipRefund(Long orderId, Integer type, Model model){

        model.addAttribute("orderId", orderId);
        model.addAttribute("type", type);

        return "refund";
    }

    /**
     * 生成订单，跳转到支付页面
     * @param order
     * @param bindingResult
     * @param ids
     * @param session
     * @param model
     * @return
     */
    @RequestMapping("addOrder")
    public String addOrder(@Valid Order order, BindingResult bindingResult, @RequestParam("ids") List<String> ids, HttpSession session, Model model){
        if (bindingResult.hasErrors()){
            return "error";
        }
        int userId = NumberUtils.getUserId(session);
        Order result = orderService.addOrder(order,ids,userId);
        model.addAttribute("order",result);
        return "pay";
    }

    /**
     * 创建支付二维码的url
     * @param orderId
     * @param actualPay
     * @return
     */
    @RequestMapping("createUrl")
    @ResponseBody
    public String createUrl(@RequestParam("orderId") String orderId,
                      @RequestParam("actualPay")Integer actualPay){

        String url = orderService.createUrl(orderId,actualPay);
        return url;

    }

    /**
     * 查询订单状态，如果已支付返回true，没支付返回false
     * @param orderId
     * @return
     */
    @RequestMapping("pay")
    @ResponseBody
    public Boolean pay(@RequestParam("orderId") Long orderId){

        Map map = orderService.pay(orderId);


        if (map == null){
            return false;
        }
        if (map.get("trade_state").equals("SUCCESS")){
            return true;
        }
        return false;
    }

    /**
     * 跳转到支付成功的页面
     * @param orderId
     * @param actualPay
     * @param model
     * @return
     */
    @RequestMapping("skipSuccess")
    public String skipSuccess(@RequestParam("orderId") String orderId,
                              @RequestParam("actualPay") Integer actualPay,
                              Model model){
        model.addAttribute("orderId",orderId);
        model.addAttribute("actualPay",actualPay);
        return "success";
    }

    /**
     * 通过订单状态查询订单
     * @param status
     * @param session
     * @param model
     * @return
     */
    @RequestMapping("queryOrderByStatus")
    public String queryOrderByStatus(@RequestParam(name = "status",defaultValue = "") Integer status,
                                     HttpSession session,Model model){

        Integer userId = NumberUtils.getUserId(session);
        List<Order> orderList = orderService.queryOrderByStatus(status,userId);
        model.addAttribute("orderList",orderList);
        return "orderManage";

    }

    /**
     * 跳转到支付页面
     * @param orderId
     * @param model
     * @return
     */
    @RequestMapping("skipPay")
    public String skipPay(@RequestParam("orderId") Long orderId, Model model){

        Order order = orderService.queryOrderById(orderId);
        model.addAttribute("order",order);
        return "pay";

    }

    /**
     * 用户确认收货
     * @param orderId
     * @return
     */
    @RequestMapping("confirm")
    public String confirm(@RequestParam("orderId") Long orderId){

        orderService.updateStatus(orderId, 4);

        return "redirect:queryOrderByStatus";
    }



}
