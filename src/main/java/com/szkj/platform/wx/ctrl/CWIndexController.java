package com.szkj.platform.wx.ctrl;


import com.szkj.platform.busiz.service.WechatService;
import com.szkj.platform.busiz.utils.AccessTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class CWIndexController {

    @Autowired
    private WechatService wechatService;

    /**
     * h5默认页面
     *
     * @return
     */
    @RequestMapping("/")
    public ModelAndView mobileIndex(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView();
        AccessTokenUtil.authorization(request, response, wechatService);
        mv.setViewName("mobile/index");
        return mv;
    }

    /**
     * 书架
     *
     * @return
     */
    @RequestMapping("/bookshelf")
    public ModelAndView bookshelf(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView();
        AccessTokenUtil.authorization(request, response, wechatService);
        mv.setViewName("mobile/bookshelf");
        return mv;
    }



    /**
     * 我的
     *
     * @return
     */
    @RequestMapping("/setting")
    public ModelAndView setting(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView();
        AccessTokenUtil.authorization(request, response, wechatService);
        mv.setViewName("mobile/setting");
        return mv;
    }

    /**
     * 我的订单
     *
     * @return
     */
    @RequestMapping("/myOrder")
    public ModelAndView myOrder(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView();
        AccessTokenUtil.authorization(request, response, wechatService);
        mv.setViewName("mobile/myOrder");
        return mv;
    }

    /**
     * 收货地址
     *
     * @return
     */
    @RequestMapping("/address")
    public ModelAndView address(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView();
        AccessTokenUtil.authorization(request, response, wechatService);
        mv.setViewName("mobile/address");
        return mv;
    }


    /**
     * 购物车
     *
     * @return
     */
    @RequestMapping("/shoppingCar")
    public ModelAndView shoppingCar(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView();
        AccessTokenUtil.authorization(request, response, wechatService);
        mv.setViewName("mobile/shoppingCar");
        return mv;
    }



    /**
     * 问答详情
     *
     * @return
     */
    @RequestMapping("/myQuestionsList")
    public ModelAndView myQuestionsList(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView();
        AccessTokenUtil.authorization(request, response, wechatService);
        mv.setViewName("mobile/myQuestionsList");
        return mv;
    }

    /**
     * 提问页面
     *
     * @return
     */
    @RequestMapping("/myQuestionsStart")
    public ModelAndView myQuestionsStart(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView();
        AccessTokenUtil.authorization(request, response, wechatService);
        mv.setViewName("mobile/myQuestionsStart");
        return mv;
    }


}
