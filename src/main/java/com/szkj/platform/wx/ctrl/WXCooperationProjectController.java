package com.szkj.platform.wx.ctrl;

import com.szkj.platform.busiz.domain.CooperationProject;
import com.szkj.platform.busiz.domain.Member;
import com.szkj.platform.busiz.domain.ProjectCat;
import com.szkj.platform.busiz.service.CooperationProjectService;
import com.szkj.platform.busiz.service.MemberService;
import com.szkj.platform.busiz.service.ProjectCatService;
import com.szkj.platform.system.domain.Adv;
import com.szkj.platform.system.domain.AdvCat;
import com.szkj.platform.system.service.AdvCatService;
import com.szkj.platform.system.service.AdvService;
import com.szkj.platform.utils.JsonResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 项目合作微信端
 * Created by Administrator on 2018/4/8 0008.
 */
@Controller
public class WXCooperationProjectController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private CooperationProjectService cooperationProjectService;

    @Autowired
    private ProjectCatService projectCatService;

    @Autowired
    private AdvCatService advCatService;

    @Autowired
    private AdvService advService;

    /**
     * 合作项目详情
     * @param request
     * @return
     */
    @RequestMapping("/projectDetails")
    public ModelAndView cooperationProjectInfo(HttpServletRequest request){
        ModelAndView mv = new ModelAndView();
        String project_id = request.getParameter("project_id");
        if (StringUtils.isNotEmpty(project_id)){
            //合作项目详情
            CooperationProject cooperationProject = cooperationProjectService.selectById(Long.parseLong(project_id));
            mv.addObject("cooperationProject",cooperationProject);
            mv.setViewName("mobile/projectDetails");
        }else {
            mv.setViewName("mobile/index");
        }
        return mv;
    }

    /**
     * 合作项目分类列表
     * @param request
     * @return
     */
    @RequestMapping("/coorperProject")
    public ModelAndView cooperationProjectCat(HttpServletRequest request){
        ModelAndView mv = new ModelAndView();
        List<ProjectCat> cats = projectCatService.getCatLists(null);
        List<ProjectCat> catTree = projectCatService.getTree(cats);
        mv.addObject("catList",catTree);
        AdvCat advCat = advCatService.selectByCatCode("00001");
        if (advCat != null) {
            List<Adv> advList = advService.selectAdvsByCatIdAndCount(advCat.getAdv_cat_id(),5);
            mv.addObject("advList",advList);
            mv.setViewName("mobile/coorperProject");
        }
        return mv;
    }

    /**
     * 合作项目列表
     * @param request
     * @return
     */
    @RequestMapping("/projectList")
    public Object cooperationProjectList(HttpServletRequest request){
        ModelAndView mv  = new ModelAndView();
        String project_cat_id = request.getParameter("id");
        if (StringUtils.isNotEmpty(project_cat_id)){
            Long cat_id =Long.parseLong(project_cat_id);
            ProjectCat cat = projectCatService.selectById(cat_id);
            if (cat != null) {
                mv.addObject("cat_name", cat.getCat_name());
                List<CooperationProject> cooperationProjectList = cooperationProjectService.selectProjectList(cat_id);
                mv.addObject("cooperationProjectList", cooperationProjectList);
                mv.setViewName("mobile/projectList");
                return mv;
            }
        }
        mv.setViewName("mobile/index");
        return mv;
    }
}
