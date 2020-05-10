package com.bookstore.web.controller;

import com.bookstore.pojo.Activity;
import com.bookstore.pojo.Address;
import com.bookstore.pojo.Cart;
import com.bookstore.service.ActivityService;
import com.bookstore.service.CartService;
import com.bookstore.service.OrderService;
import com.bookstore.service.UserService;
import com.bookstore.utils.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @author zhangchun
 */
@Controller
@RequestMapping("cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private OrderService orderService;


    /**
     * 跳转到创建订单页面
     *
     * @param ids
     * @param model
     * @param session
     * @return
     */
    @RequestMapping("skipCreateOrder")
    public String skipOrderDetail(@RequestParam("ids") List<String> ids, Model model, HttpSession session) {
        Integer userId = NumberUtils.getUserId(session);
        List<Cart> cartList = cartService.queryCartByIds(userId, ids);
        List<Address> addressList = userService.queryAddressByUserId(NumberUtils.getUserId(session));
        Activity activity = activityService.find();


        model.addAttribute("cartList", cartList);
        model.addAttribute("addressList", addressList);

        model.addAttribute("favorable", orderService.getFavorable(ids, userId));
        return "createOrder";

    }

    /**
     * 对购物车详情的数量进行修改
     *
     * @param bookId
     * @param number
     * @param session
     * @return
     */
    @RequestMapping("updateCart")
    @ResponseBody
    public String updateCart(@RequestParam("bookId") String bookId,
                             @RequestParam("number") Integer number,
                             HttpSession session) {

        int userId = NumberUtils.getUserId(session);
        int totalPrice = cartService.updateCart(userId, bookId, number);

        return "" + totalPrice;

    }

    /**
     * 添加购物车条目
     *
     * @return
     */
    @RequestMapping("addCart")
    @ResponseBody
    public void addCart(HttpSession session, Cart cart) {


        Integer userId = NumberUtils.getUserId(session);
        cartService.addCart(userId, cart);

    }

    /**
     * 跳转到购物车界面
     *
     * @param session
     * @param model
     * @return
     */
    @RequestMapping("skipCart")
    public String skipCart(HttpSession session, Model model) {
        int userId = NumberUtils.getUserId(session);
        List<Cart> cartList = cartService.queryCartList(userId);
        model.addAttribute("cartList", cartList);
        return "cart";
    }

    /**
     * 删除购物车详情
     *
     * @param session
     * @param id
     */
    @RequestMapping("deleteCart")
    @ResponseBody
    public void deleteCart(HttpSession session, @RequestParam("bookId") String id) {
        int userId = NumberUtils.getUserId(session);
        cartService.deleteCart(userId, id);


    }


}
