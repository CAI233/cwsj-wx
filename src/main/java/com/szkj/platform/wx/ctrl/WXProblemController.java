package com.szkj.platform.wx.ctrl;

import com.szkj.platform.busiz.domain.Member;
import com.szkj.platform.busiz.domain.Problem;
import com.szkj.platform.busiz.domain.Works;
import com.szkj.platform.busiz.mapper.MemberPayRecordMapper;
import com.szkj.platform.busiz.service.MemberService;
import com.szkj.platform.busiz.service.ProblemService;
import com.szkj.platform.busiz.service.WorksService;
import com.szkj.platform.busiz.utils.AccessTokenUtil;
import com.szkj.platform.utils.JsonResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@Controller
public class WXProblemController {

    @Autowired
    private ProblemService problemService;
    @Autowired
    private WorksService worksService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberPayRecordMapper memberPayRecordMapper;

    /**
     * 问答作品提问
     *
     * @param request
     * @param problem
     * @return
     */
    @RequestMapping("/api/wx/problem/submit")
    @ResponseBody
    public Object submitProblem(HttpServletRequest request, HttpServletResponse response, Problem problem) {
        Object member1 = request.getSession().getAttribute("member");
        if (member1 == null) {
            return JsonResult.getExpire("登录超时！");
        }
        Member member = (Member) member1;
        try {
            if (StringUtils.isEmpty(problem.getProblem_content())) {
                return JsonResult.getError("请输入提问内容！");
            }
            if (problem.getWorks_id() == null) {
                return JsonResult.getError("请提供作品id！");
            }
            Double realPrice = worksService.getRealPrice(problem.getWorks_id());
            problem.setMember_id(member.getMember_id());
            Long problem_id;
            //查询上一个该用户在改作品下的提问
            Problem lastProblem = problemService.selectWaitProblemByWorksAndMember(problem.getWorks_id(), member.getMember_id());
            String url;
            //如果存在
            if (lastProblem != null) {
                problem_id = lastProblem.getId();
                problemService.update(lastProblem, problem);
            } else {
                problem_id = problemService.saveProblem(problem);
               }
            //支付地址
            url = "http://" + request.getServerName() + "/wechatPay?id=" + problem.getWorks_id() + "&money=" + (int)(realPrice * 100)+"&child_id=" + problem_id;
//            System.out.println(" url = "+url);
            JsonResult success = JsonResult.getSuccess("提交成功！");
            success.setData(url);
            return success;
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException("数据异常！");
        }
    }

    /**
     * 作品提问列表
     *
     * @param request
     * @return
     */
    @RequestMapping("/myQuestions")
    public ModelAndView myQuestion(HttpServletRequest request) {
        ModelAndView mv = new ModelAndView();
        Object member1 = request.getSession().getAttribute("member");
        if (member1 != null) {
            Member member = (Member) member1;
            List<Map<String, Object>> list = problemService.getMyProblem(member.getMember_id());
            mv.addObject("problemList", list);
            mv.setViewName("mobile/myQuestions");
        } else {
            mv.setViewName("mobile/index");
        }
        return mv;
    }

    /**
     * 提问详情
     *
     * @param request
     * @return
     */
    @RequestMapping("/problemInfo")
    public ModelAndView problemInfo(HttpServletRequest request) {
        ModelAndView mv = new ModelAndView();
        Object member1 = request.getSession().getAttribute("member");
        if (member1 != null) {
            Member member = (Member) member1;
            String workid = request.getParameter("works_id");
            if (StringUtils.isNotEmpty(workid)) {
                Long works_id = Long.parseLong(workid);
                List<Problem> list = problemService.getMyProblemList(member.getMember_id(), works_id);
                mv.addObject("problemInfo", list);
                mv.setViewName("mobile/myQuestionsList");
            } else {
                mv.setViewName("mobile/index");
            }
        } else {
            mv.setViewName("mobile/index");
        }
        return mv;
    }
}
