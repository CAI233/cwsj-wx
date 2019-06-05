package com.szkj.platform.system.ctrl;

import com.szkj.platform.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Administrator on 2016/10/24.
 * try {
 * System.out.println(SpringContextUtil.getApplicationContext().getResource("").getURL().getPath());
 * } catch (IOException e) {
 * e.printStackTrace();
 * }
 */
@Controller
public class IndexController {

    @Autowired
    private UserService userService;

    @RequestMapping(path = {"/admin"})
    public ModelAndView admin(HttpServletRequest request) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("admin/index");
        return mv;
    }


}
