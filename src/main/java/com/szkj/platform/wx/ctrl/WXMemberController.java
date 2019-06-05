package com.szkj.platform.wx.ctrl;

import com.szkj.platform.busiz.beans.CommentsBean;
import com.szkj.platform.busiz.beans.MemberPayRecordBean;
import com.szkj.platform.busiz.beans.PageConditionBean;
import com.szkj.platform.busiz.domain.*;
import com.szkj.platform.busiz.service.*;
import com.szkj.platform.busiz.utils.AccessTokenUtil;
import com.szkj.platform.system.constants.Constants;
import com.szkj.platform.utils.JsonResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class WXMemberController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private OrderManageService orderManageService;

    @Autowired
    private BookService bookService;

    @Autowired
    private PayStoreService payStoreService;

    @Autowired
    private CommentsService commentsService;

    @Autowired
    private WechatService wechatService;

    /**
     * 会员订单列表
     * @param request
     * @return
     */
    @RequestMapping("/api/wx/member/order")
    @ResponseBody
    public Object orderForm(HttpServletRequest request, PageConditionBean bean) {
        Object member1 = request.getSession().getAttribute("member");
        if (member1 == null){
            return JsonResult.getExpire("登录超时！");
        }
        Member member = (Member)member1;
        try {
            String order_type = request.getParameter("type");
            if (StringUtils.isEmpty(order_type)) {
                order_type = "0";
            }
            Integer type = Integer.parseInt(order_type);
            if (bean.getPageNum() == null || bean.getPageSize() == null) {
                bean.setPageNum(1);
                bean.setPageSize(10);
            }
            Object list = orderManageService.getMemberOrder(bean, type, member.getMember_id());
            JsonResult jsonResult = JsonResult.getSuccess("数据加载成功！");
            jsonResult.setData(list);
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException("数据异常！");
        }
    }

    /**
     * 保存图书阅读记录
     * @param request
     * @return
     */
    @RequestMapping("/api/wx/read/record/save")
    @ResponseBody
    public Object saveReadRecord(HttpServletRequest request) {
        Object member1 = request.getSession().getAttribute("member");
        if (member1 == null){
            return JsonResult.getExpire("登录超时！");
        }
        Member member = (Member)member1;
        String readid = request.getParameter("id");
        if (StringUtils.isEmpty(readid)) {
            return JsonResult.getError("没有商品id！");
        }
        Long read_id = Long.parseLong(readid);
        String type = request.getParameter("type");
        if (StringUtils.isEmpty(type)){
            return JsonResult.getError("请选择阅读资源类型！");
        }
        String schedule = request.getParameter("schedule");
        if (StringUtils.isEmpty(schedule)){
            return JsonResult.getError("没有阅读进度！");
        }
        Integer read_type = Integer.parseInt(type);
        MemberReadRecord record = bookService.selectReadRecord(member.getMember_id(), read_id,read_type);
        if (record != null) {
            record.setSchedule(schedule);
            bookService.updateReadRecord(record);
        } else {
            record = new MemberReadRecord();
            record.setRead_id(read_id);
            record.setRead_type(read_type);
            record.setMember_id(member.getMember_id());
            record.setCreate_time(new Date());
            record.setUpdate_time(new Date());
            record.setSchedule(schedule);
            bookService.saveReadRecord(record);
        }
        return JsonResult.getSuccess("保存成功！");

    }

    /**
     * 阅读记录
     * @param request
     * @return
     */
    @RequestMapping("/readRecord")
    public ModelAndView readRecord(HttpServletRequest request){
        ModelAndView mv = new ModelAndView();
        Object member1 = request.getSession().getAttribute("member");
        if (member1 !=null) {
            Member member = (Member)member1;
            List<MemberReadRecord> bookRecord = bookService.getReadRecord(member.getMember_id(),1);
            mv.addObject("bookRecord",bookRecord);
            List<MemberReadRecord> videoRecord = bookService.getReadRecord(member.getMember_id(),2);
            mv.addObject("videoRecord",videoRecord);
            mv.setViewName("mobile/readRecord");
        }else {
            mv.setViewName("mobile/index");
        }
        return mv;
    }

    /**
     * 会员支付记录
     * @param request
     * @return
     */
    @RequestMapping("/payRecord")
    public ModelAndView memberPayRecord(HttpServletRequest request){
        ModelAndView mv = new ModelAndView();
        Object member1 = request.getSession().getAttribute("member");
        if (member1 !=null) {
            Member member = (Member)member1;
            //商品支付记录
            List<MemberPayRecordBean> goodsList = payStoreService.getMemberPayRecord(member.getMember_id(), 1);
            mv.addObject("goodsList",goodsList);
            //作品支付记录
            List<MemberPayRecordBean> worksList = payStoreService.getMemberPayRecord(member.getMember_id(), 2);
            mv.addObject("worksList",worksList);
            mv.setViewName("mobile/payRecord");
        }else {
            mv.setViewName("mobile/index");
        }
        return mv;
    }

    /**
     * 保存、更新会员浏览记录
     * @param request
     * @return
     */
    @RequestMapping("/api/wx/saw/record/save")
    @ResponseBody
    public Object sawRecordSave(HttpServletRequest request){
        Object member1 = request.getSession().getAttribute("member");
        if (member1 == null){
            return JsonResult.getExpire("登录超时！");
        }
        Member member = (Member)member1;
        String sawid = request.getParameter("id");
        if (StringUtils.isEmpty(sawid)) {
            return JsonResult.getError("没有id");
        }
        Long saw_id = Long.parseLong(sawid);
        String type = request.getParameter("type");
        if (StringUtils.isEmpty(type)){
            return JsonResult.getError("请选择阅读资源类型！");
        }
        Integer saw_type = Integer.parseInt(type);
        MemberSawRecord record = memberService.selectSawRecordByIdAndType(saw_id,saw_type,member.getMember_id());
        if (record != null){
            int count = record.getSaw_count();
            count ++;
            record.setSaw_count(count);
            memberService.updateSawRecord(record);
        }else {
            record = new MemberSawRecord();
            record.setMember_id(member.getMember_id());
            record.setSaw_count(1);
            record.setCreate_time(new Date());
            record.setSaw_type(saw_type);
            record.setUpdate_time(new Date());
            record.setSaw_id(saw_id);
            memberService.saveSawRecord(record);
        }
        return JsonResult.getSuccess("保存浏览记录成功！");
    }


    /**
     * 删除用户浏览记录
     * @param request
     * @return
     */
    @RequestMapping("/api/wx/saw/record/del")
    @ResponseBody
    public Object delRecord(HttpServletRequest request){
        Object member1 = request.getSession().getAttribute("member");
        if (member1!=null) {
            Member member = (Member) member1;
            String sawid = request.getParameter("id");
            String type = request.getParameter("type");
            if (sawid == null) {
                if (type!=null) {
                    //删除所有浏览记录
                    memberService.delRecord(member.getMember_id(), type);
                    JsonResult result =  JsonResult.getSuccess("删除成功！");
                    result.setData(1);
                    return result;
                }else {
                    JsonResult result =  JsonResult.getSuccess("删除失败！");
                    result.setData(2);
                    return result;
                }
            } else {
                //删除单个浏览记录
                memberService.delRecordById(member.getMember_id(), sawid);
                JsonResult result =  JsonResult.getSuccess("删除成功！");
                result.setData(1);
                return result;
            }
        }else {
            return JsonResult.getSuccess("删除失败！");
        }

    }

    /**
     * 用户浏览记录
     * @param request
     * @return
     */
    @RequestMapping("/lookRecord")
    public ModelAndView lookRecord(HttpServletRequest request){
        ModelAndView mv = new ModelAndView();
        Object member1 = request.getSession().getAttribute("member");
        if (member1 !=null) {
            Member member = (Member)member1;
            List<MemberSawRecord> worksList = memberService.selectSawListByType(member.getMember_id(),2);
            mv.addObject("worksList",worksList);
            List<MemberSawRecord> goodsList = memberService.selectSawListByType(member.getMember_id(),1);
            mv.addObject("goodsList",goodsList);
            mv.setViewName("mobile/lookRecord");
        }else {
            mv.setViewName("mobile/index");
        }
        return mv;
    }

    /**
     * 会员信息修改
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "/api/wx/member/update")
    @ResponseBody
    public Object update(HttpServletRequest request, Member bean) {
        String openid = request.getParameter("openid");
        if (StringUtils.isEmpty(openid)) {
            return JsonResult.getExpire("用户不存在！");
        }
        Member member = memberService.selectByOpenId(openid);
        if (member == null) {
            return JsonResult.getExpire("登录超时！");
        }
        try {
            memberService.update(bean, member);
            JsonResult result = JsonResult.getSuccess(Constants.ACTION_UPDATE);
            result.setData(member);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    /**
     * 会员个人信息
     * @param request
     * @return
     */
    @RequestMapping("/api/wx/member/details")
    public ModelAndView details(HttpServletRequest request){
        ModelAndView mv = new ModelAndView();
        Object member1 = request.getSession().getAttribute("member");
        if (member1 != null){
            Member member2 = (Member)member1;
            Member member = memberService.selectByOpenId(member2.getOpenid());
            mv.addObject("member",member);
        }else {
            mv.setViewName("mobile/index");
        }
        return mv;
    }

    /**
     * 评论新增
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/wx/comments/save")
    @ResponseBody
    public Object save(HttpServletRequest request){
        Object member1 = request.getSession().getAttribute("member");
        if (member1 == null){
            return JsonResult.getExpire("登录超时！");
        }
        Member member = (Member)member1;
        String goodsid = request.getParameter("goods_id");
        if (StringUtils.isEmpty(goodsid)){
            return JsonResult.getError("商品id为空！");
        }
        Long goods_id = Long.parseLong(goodsid);
        String comments_content = request.getParameter("comments_content");
        if (StringUtils.isEmpty(comments_content)){
            return JsonResult.getError("没有评论内容！");
        }
        Boolean fag = commentsService.containsEmoji(comments_content);
        if (fag == true) {
            return JsonResult.getError("请勿输入非法字符");
        }
        try{
            Comments comments = new Comments();
            comments.setGoods_id(goods_id);
            comments.setComments_content(comments_content);
            comments.setMember_id(member.getMember_id());
            comments.setMember_name(member.getMember_name());
            commentsService.save(comments);
            JsonResult result = JsonResult.getSuccess(Constants.ACTION_ADD);
            result.setData(comments);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    /**
     * 评论列表
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/wx/comments/list", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Object getlist(HttpServletRequest request, CommentsBean bean){
        Object member1 = request.getSession().getAttribute("member");
        if (member1 == null){
            return JsonResult.getExpire("登录超时！");
        }
        Member member = (Member)member1;
        try{
            Object object = commentsService.CommentspageQuery(bean,member.getMember_id());
            JsonResult result = JsonResult.getSuccess(Constants.LOAD_SUCCESS);
            result.setData(object);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }


    /**
     * 评论点赞
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/wx/comments/saveliked")
    @ResponseBody
    public Object saveliked(HttpServletRequest request){
        Object member1 = request.getSession().getAttribute("member");
        if (member1 == null){
            return JsonResult.getExpire("登录超时！");
        }
        Member member = (Member)member1;
        String id1 = request.getParameter("id");
        if (StringUtils.isEmpty(id1)){
            return JsonResult.getError("评论id为空！");
        }
        Long id = Long.parseLong(id1);
        CommentsLiked commentsLiked = commentsService.selectLikedId(id,member.getMember_id());
        if (commentsLiked != null ){
            return JsonResult.getError("你已点赞过！");
        }
        try{
            commentsService.saveliked(id);
            commentsService.savelikeds(id,member.getMember_id());
            JsonResult result = JsonResult.getSuccess(Constants.ACTION_SUCCESS);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    /**
     * 用户搜索记录
     * @param request
     * @return
     */
    @RequestMapping("/indexSearch")
    public ModelAndView indexSearch(HttpServletRequest request){
        ModelAndView mv = new ModelAndView();
        Object member1 = request.getSession().getAttribute("member");
        if (member1 != null){
            Member member = (Member)member1;
            List<MemberSearchRecord> list = memberService.getSearchList(member.getMember_id());
            mv.addObject("list",list);
            mv.setViewName("mobile/indexSearch");
            return mv;
        }
        mv.setViewName("mobile/index");
        return mv;
    }

    /**
     * 我的
     *
     * @return
     */
    @RequestMapping("/my")
    public ModelAndView my(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView();
        AccessTokenUtil.authorization(request, response, wechatService);
        Object member1 = request.getSession().getAttribute("member");
        if (member1 != null){
            Member member = (Member)member1;
            //待付款
            Integer wait_pay = memberService.selectCountByMember(member.getMember_id(),1);
            mv.addObject("wait_pay",wait_pay);
            //待发货
            Integer wait_send = memberService.selectCountByMember(member.getMember_id(),2);
            mv.addObject("wait_send",wait_send);
            //待收货
            Integer wait_get = memberService.selectCountByMember(member.getMember_id(),3);
            mv.addObject("wait_get",wait_get);
            mv.setViewName("mobile/my");
            return mv;
        }
        mv.setViewName("mobile/index");
        return mv;
    }
}