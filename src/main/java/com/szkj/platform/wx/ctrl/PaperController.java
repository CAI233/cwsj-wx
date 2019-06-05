package com.szkj.platform.wx.ctrl;

import com.szkj.platform.busiz.ctrl.QuestionController;
import com.szkj.platform.busiz.domain.Member;
import com.szkj.platform.busiz.domain.MemberQuestionRel;
import com.szkj.platform.busiz.domain.Works;
import com.szkj.platform.busiz.service.*;
import com.szkj.platform.system.constants.Constants;
import com.szkj.platform.utils.JsonResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 微信端试卷
 * Created by daixiaofeng on 2018/4/3.
 */
@Controller
public class PaperController {

    private final static Logger logger = LoggerFactory.getLogger(QuestionController.class);
    @Autowired
    private MemberService memberService;
    @Autowired
    private WorksService worksService;
    @Autowired
    private WorksTestPaperService worksTestPaperService;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private MemberQuestionRelService memberQuestionRelService;


    /**
     * 微信我的试卷列表
     *
     * @param request
     * @return
     */
    @RequestMapping("/myTestPaper")
    public ModelAndView paperList(HttpServletRequest request) {
        ModelAndView mv = new ModelAndView();
        Object member1 = request.getSession().getAttribute("member");
        if (member1 != null) {
            Member member = (Member) member1;
            Object object = worksService.getPaperList(member.getMember_id());
            mv.addObject("paperList", object);
            mv.setViewName("mobile/myTestPaper");
        }
        return mv;
    }

    /**
     * 微信试卷练习列表
     *
     * @param request
     * @return
     */
    @RequestMapping("myTestList")
    public ModelAndView paperPracticeList(HttpServletRequest request) {
        ModelAndView mv = new ModelAndView();
        Long works_id = Long.valueOf(request.getParameter("works_id"));
        Object member1 = request.getSession().getAttribute("member");
        if (member1 != null) {
            Member member = (Member) member1;
            Object object = worksTestPaperService.paperPracticeList(works_id, member.getMember_id());
            Works works = worksService.getById(works_id);
            mv.addObject("paperPracticeList", object);
            mv.addObject("works", works);
            mv.setViewName("mobile/myTestList");
        }
        return mv;
    }


    /**
     * 开始答题查询试题
     *
     * @param request
     * @return
     */
    @RequestMapping("/myExam")
    public ModelAndView startQuestion(HttpServletRequest request) {
        ModelAndView mv = new ModelAndView();
        Integer paper_id = Integer.valueOf(request.getParameter("paper_id"));
        Object member1 = request.getSession().getAttribute("member");
        if (member1 != null) {
            Member member = (Member) member1;
            List<MemberQuestionRel> memberQuestionRelList = memberQuestionRelService.getByMemIdAndQuesId(paper_id, member.getMember_id());
            if (memberQuestionRelList.size() > 0) {
                //试卷已答过,清除答案
                memberQuestionRelService.updateMemberQuestion(paper_id, member.getMember_id());
            } else {
                //试卷未答过题备份用户和试题
                memberQuestionRelService.backupMemberQuestion(paper_id, member.getMember_id());
            }
            Object object = questionService.startQuestion(paper_id,member.getMember_id());
            mv.addObject("startQuestion", object);
            mv.setViewName("mobile/myExam");
        }
        return mv;
    }


    /**
     * 会员答题
     *
     * @param request
     * @return
     */
    @RequestMapping("/answerTheQuestions")
    public void memberAnswer(HttpServletRequest request) {
        ModelAndView mv = new ModelAndView();
        Integer paper_id = Integer.valueOf(request.getParameter("paper_id"));
        Object member1 = request.getSession().getAttribute("member");
        Integer question_id = Integer.valueOf(request.getParameter("question_id"));
        String member_answer = request.getParameter("member_answer");
        if (member1 != null) {
            Member member = (Member) member1;
            if (member_answer != null) {
                memberQuestionRelService.updateMemberQuestionRel(paper_id, member.getMember_id(), question_id, member_answer);
            }
        }
    }


    /**
     * 答题结束
     *
     * @param request
     * @return
     */
    @RequestMapping("/answerToEnd")
    public ModelAndView paperFinish(HttpServletRequest request) {
        ModelAndView mv = new ModelAndView();
        Object member1 = request.getSession().getAttribute("member");
        Integer paper_id = Integer.valueOf(request.getParameter("paper_id"));
        if (member1 != null) {
            Member member = (Member) member1;
            Object object = memberQuestionRelService.paperFinish(paper_id, member.getMember_id());
            mv.addObject("paperFinish", object);
            mv.setViewName("mobile/answerToEnd");
        }
        return mv;
    }

}
