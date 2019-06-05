package com.szkj.platform.wx.ctrl;

import com.szkj.platform.busiz.beans.PageConditionBean;
import com.szkj.platform.busiz.domain.*;
import com.szkj.platform.busiz.mapper.MemberPayRecordMapper;
import com.szkj.platform.busiz.service.*;
import com.szkj.platform.system.service.AdvCatService;
import com.szkj.platform.system.service.AdvService;
import com.szkj.platform.system.utils.MessageAPi;
import com.szkj.platform.system.utils.MsgCodeEnum;
import com.szkj.platform.utils.JsonResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * 商品分类、列表、详情
 */
@Controller
public class WXWorksController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private WorksService worksService;

    @Autowired
    private CommentsService commentsService;

    @Autowired
    private WorksCatService worksCatService;
    @Autowired
    private GoodsCatService goodsCatService;
    @Autowired
    private AdvCatService advCatService;

    @Autowired
    private AdvService advService;

    @Autowired
    private CollectionService collectionService;
    @Autowired
    private MemberPayRecordMapper memberPayRecordMapper;

    /**
     * 作品详细信息
     *
     * @param request
     * @return
     */
    @RequestMapping("/worksInfo")
    public ModelAndView worksInfo(HttpServletRequest request) {
        ModelAndView mv = new ModelAndView();
        Member member = (Member) request.getSession().getAttribute("member");
        if (member != null) {
            String works_id = request.getParameter("works_id");
            if (StringUtils.isNotEmpty(works_id)) {
                //作品详情
                Works works = worksService.selectById(Long.parseLong(works_id));
                if (works != null) {
                    //判断是否支付过
                    MemberPayRecord memberPayRecord = memberPayRecordMapper.selectByMemberAndWorks(member.getMember_id(), Long.parseLong(works_id));
                    if (memberPayRecord!=null){
                        mv.addObject("buy", 1);
                    }
                    mv.addObject("works", works);
                    //判断是否收藏
                    Long id = collectionService.selectWorksCollection(works.getWorks_id(), member.getMember_id());
                    mv.addObject("collection", id);
                    if (works.getWorks_type().equals("资源包")) {
                        List<WorksResources> list = worksService.getResourcesList(works.getWorks_id());
                        mv.addObject("list", list);
                        mv.setViewName("mobile/resource");
                    } else if (works.getWorks_type().equals("教育表格")) {
                        List<WorksForm> list = worksService.getFromListById(works.getWorks_id());
                        mv.addObject("list", list);
                        mv.setViewName("mobile/eduForm");
                    } else if (works.getWorks_type().equals("试题")) {
                        List<WorksTestPaper> list = worksService.getTestPaperList(works.getWorks_id());
                        mv.addObject("list", list);
                        mv.setViewName("mobile/testList");
                    } else {
                        mv.setViewName("mobile/myQuestionsStart");
                    }
                    //相关推荐
                    List<Works> recommends = worksService.selectRecommend(works.getWorks_cat_id(), works.getWorks_type(), 3, works.getWorks_id());
                    mv.addObject("recommends", recommends);
                    //保存用户浏览记录
                    MemberSawRecord record = memberService.selectSawRecordByIdAndType(works.getWorks_id(), 2, member.getMember_id());
                    if (record != null) {
                        int count = record.getSaw_count();
                        count++;
                        record.setSaw_count(count);
                        memberService.updateSawRecord(record);
                    } else {
                        record = new MemberSawRecord();
                        record.setMember_id(member.getMember_id());
                        record.setSaw_count(1);
                        record.setCreate_time(new Date());
                        record.setSaw_type(2);
                        record.setUpdate_time(new Date());
                        record.setSaw_id(works.getWorks_id());
                        memberService.saveSawRecord(record);
                    }
                } else {
                    mv.setViewName("mobile/index");
                }
            } else {
                mv.setViewName("mobile/index");
            }
        } else {
            mv.setViewName("mobile/index");
        }
        return mv;
    }


    /**
     * 分类作品列表
     *
     * @param request
     * @return
     */
    @RequestMapping("/classify/works")
    public ModelAndView getGoodsList(HttpServletRequest request) {
        ModelAndView mv = new ModelAndView();
        Object member = request.getSession().getAttribute("member");
//        Member member = memberService.selectByOpenId("oT3tm1KpxKcnBDu25HF2pB3lJ5wA");
        if (member != null) {
            String goods_cat_id = request.getParameter("id");
            if (StringUtils.isNotEmpty(goods_cat_id)) {
                Long cat_id = Long.parseLong(goods_cat_id);
                WorksCat cat = worksCatService.selectCatById(cat_id);
                mv.addObject("cat_name", cat.getCat_name());
                PageConditionBean bean = new PageConditionBean();
                bean.setPageSize(10);
                bean.setPageNum(1);
                Object list = worksService.selectByCatId(cat_id, bean);
                mv.addObject("list", list);
                mv.setViewName("mobile/classify/works");
            } else {
                mv.setViewName("mobile/index");
            }
        } else {
            mv.setViewName("mobile/index");
        }
        return mv;
    }

    /**
     * 分类作品列表
     *
     * @param request
     * @return
     */
    @RequestMapping("/checkAuthority")
    public Object checkAuthority(HttpServletRequest request) {
        try {
            String works_id = request.getParameter("works_id");
            if (StringUtils.isEmpty(works_id)) {
                return JsonResult.getException("作品id为空");
            }
            Member member = (Member) request.getSession(true).getAttribute("member");
            MemberPayRecord memberPayRecord = memberPayRecordMapper.selectByMemberAndWorks(member.getMember_id(), Long.parseLong(works_id));
            return JsonResult.getSuccess("已购买");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JsonResult.getException("请先购买后查看");
    }

}
