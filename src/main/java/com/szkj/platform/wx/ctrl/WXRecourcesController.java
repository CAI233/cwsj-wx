package com.szkj.platform.wx.ctrl;

import com.szkj.platform.busiz.domain.Member;
import com.szkj.platform.busiz.domain.Resources;
import com.szkj.platform.busiz.service.ResourcesService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class WXRecourcesController {

    @Autowired
    private ResourcesService resourcesServicer;

    /**
     * 资源详情
     * @param request
     * @return
     */
    @RequestMapping("/resourceInfo")
    public ModelAndView resourceInfo(HttpServletRequest request){
        ModelAndView mv = new ModelAndView();
//        Object member = request.getSession().getAttribute("member");
//        if (member != null){
            String id = request.getParameter("id");
            if (StringUtils.isNotEmpty(id)){
                Long res_id = Long.parseLong(id);
                Resources res = resourcesServicer.selectById(res_id);
                mv.addObject("resource",res);
                mv.setViewName("mobile/resourceInfo");
            }else {
                mv.setViewName("mobile/index");
            }
//        }else {
//            mv.setViewName("mobile/index");
//        }
        return mv;
    }

}
