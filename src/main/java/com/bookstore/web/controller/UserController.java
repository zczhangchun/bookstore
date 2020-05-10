package com.bookstore.web.controller;

import com.bookstore.dto.UpdatePassword;
import com.bookstore.exception.BookStoreException;
import com.bookstore.pojo.Address;
import com.bookstore.pojo.User;
import com.bookstore.service.UserService;
import com.bookstore.utils.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("user")
public class UserController {

    @Autowired
    private UserService userService;


    @RequestMapping("updateAddress")
    public String updateAddress(@Valid Address address, BindingResult result, HttpServletRequest request){
        if (result.hasErrors()){
            return "error";
        }
        address.setUserId(NumberUtils.getUserId(request.getSession()));
        userService.updateAddress(address);
        request.setAttribute("msg","修改成功");

        return "forward:queryAddressByUserId";

    }

    /**
     * 查询用户的所有地址信息
     * @param request
     * @return
     */
    @RequestMapping("queryAddressByUserId")
    public String queryAddressByUserId(HttpServletRequest request){

        Integer userId = NumberUtils.getUserId(request.getSession());
        List<Address> addressList = userService.queryAddressByUserId(userId);
        request.setAttribute("addressList",addressList);
        return "receiver";

    }

    /**
     * 添加收货地址
     * @param address
     * @param request
     * @return
     */
    @RequestMapping("addReceiver")
    public String addReceiver(Address address, HttpServletRequest request){
        Integer userId = NumberUtils.getUserId(request.getSession());
        address.setUserId(userId);

        userService.addReceiver(address);

        request.setAttribute("msg","添加成功");
        return "receiver-add";
    }

    /**
     * 验证数据
     * @param data
     * @param data2
     * @param type
     * @return
     */
    @RequestMapping("verify")
    @ResponseBody
    public Boolean verify(@RequestParam("data") String data,
                          @RequestParam(value = "data2", defaultValue = "") String data2,
                          @RequestParam("type") Integer type) {

        Boolean bool = userService.verify(data, data2, type);

        return bool;

    }

    /**
     * 修改密码
     * @param form
     * @param result
     * @param request
     * @return
     */
    @RequestMapping("updatePassword")
    public String passwordAlter(@Valid UpdatePassword form,BindingResult result, HttpServletRequest request) {

        if (result.hasErrors()){
            return "error";
        }

        form.setUserId(NumberUtils.getUserId(request.getSession()));

        try {
            userService.updatePassword(form);
        } catch (BookStoreException e) {
            //如果抛异常，说明旧密码错误，跳转回center
            request.setAttribute("msg","旧密码错误");
            return "center";
        }

        //没有错，注销用户，让其重新登陆
        request.getSession().removeAttribute("user");
        request.setAttribute("msg", "修改密码成功，请重新登陆");
        return "login";

    }


    /**
     * 用户注销
     *
     * @param session
     * @return
     */
    @RequestMapping("logout")
    public String logout(HttpSession session) {
        session.removeAttribute("user");
        return "index";
    }

    /**
     * 用户注册
     *
     * @return
     */
    @RequestMapping("register")
    public String register(@Valid User form, BindingResult result, Model model, HttpSession session) {
        Map<String, String> error = new HashMap<>();
        if (result.hasErrors()) {

            return "error";

        }

        //如果验证没有错误，那么就验证各种参数
        User user = userService.register(form);

        //将user对象存入session域中
        session.removeAttribute("user");
        session.setAttribute("user", user);


        return "index";
    }

    /**
     * 短信验证
     *
     * @param phone
     */
    @RequestMapping("sms")
    public void sms(HttpSession session, @RequestParam(name = "phone", required = false) String phone) {

        if (StringUtils.isEmpty(phone)) {
            User user = (User) session.getAttribute("user");
            phone = user.getPhone();
        }
        userService.sms(phone);

    }


    /**
     * 用户登陆
     *
     * @param form
     * @return
     */
    @RequestMapping("login")
    public String Login(HttpServletRequest request, User form) {

        //校验验证码是否正确，不正确直接返回错误页面;
        HttpSession session = request.getSession();
        String result = (String) session.getAttribute("captcha");

        if (!StringUtils.equals(result,form.getCaptcha())){return "error";}

        //调用service方法，并且捕捉异常
        try {
            User user = userService.login(form);
            //没有异常，就跳转到主页，并将信息存储到session域中，需要先把原先session域中的user清除
            session.removeAttribute("user");
            session.setAttribute("user", user);
            String url = (String) session.getAttribute("url");
            if (url != null){

                return "redirect:" + url;

            }


            return "index";
        } catch (BookStoreException e) {
            //如果是BookStore异常，那么就跳转回登陆页面，并把错误信息存入
            request.setAttribute("msg", "用户名或密码错误");
            return "login";
        }


    }

    @RequestMapping("skipLogin")
    public String skipLogin() {
        return "login";
    }

    @RequestMapping("skipRegister")
    public String skipRegister() {
        return "register";
    }

    @RequestMapping("skipCenter")
    public String skipCenter() {


        return "center";
    }


    @RequestMapping("skipReceiverAdd")
    public String skipReceiverAdd(){

        return "receiver-add";
    }

}
