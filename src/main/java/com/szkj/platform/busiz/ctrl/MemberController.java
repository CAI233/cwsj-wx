package com.szkj.platform.busiz.ctrl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.szkj.platform.busiz.beans.MemberBean;
import com.szkj.platform.busiz.domain.Member;
import com.szkj.platform.busiz.service.MemberService;
import com.szkj.platform.busiz.utils.AccessTokenUtil;
import com.szkj.platform.system.constants.Constants;
import com.szkj.platform.system.domain.SysUser;
import com.szkj.platform.system.utils.CheckUserHelper;
import com.szkj.platform.system.utils.MessageAPi;
import com.szkj.platform.system.utils.MsgCodeEnum;
import com.szkj.platform.utils.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/api/busiz/member/")
public class MemberController {

    @Autowired
    private MemberService memberService;

    /**
     * 会员列表
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "getlist")
    @ResponseBody
    public Object getList(HttpServletRequest request, @RequestBody MemberBean bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            //登录超时
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            Sort sort = new Sort(Sort.Direction.DESC, "create_time").and(new Sort(Sort.Direction.ASC,"is_delete"));
            Object object = memberService.pageQuery(bean, sort);
            JsonResult result = JsonResult.getSuccess(Constants.LOAD_SUCCESS);
            result.setData(object);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }/**
     * 会员登录（测试）
     * @param request
     * @param member
     * @return
     */
    @RequestMapping(value = "login")
    @ResponseBody
    public Object login(HttpServletRequest request, @RequestBody Member member){

        try{
            if (member.getNick_name()==null){
                return JsonResult.getError("会员名称不能为空");
            }
            memberService.save(member);
            HttpSession session = request.getSession(true);
            session.setAttribute("MEMBER_INFO", member);
            JsonResult result = JsonResult.getSuccess(Constants.LOAD_SUCCESS);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }
}
