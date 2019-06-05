package com.szkj.platform.busiz.ctrl;

import com.alibaba.fastjson.JSONObject;
import com.szkj.platform.busiz.domain.Member;
import com.szkj.platform.busiz.service.MemberService;
import com.szkj.platform.busiz.service.WorksService;
import com.szkj.platform.busiz.utils.AccessTokenUtil;
import com.szkj.platform.system.utils.HttpUtil;
import com.szkj.platform.system.utils.PropertiesUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.*;

import static com.szkj.platform.busiz.utils.AccessTokenUtil.*;


@Controller
@RequestMapping(value = "/api/busiz/wechat/")
public class WechatLoginController {

    @Autowired
    private MemberService memberService;
    @Autowired
    private WorksService worksService;

    private static final String APPID = PropertiesUtil.getValue("wechat.properties", "appid");
    private static final String SECRET = PropertiesUtil.getValue("wechat.properties", "secret");
    private static final String ACCESS_TOKEN_URL = PropertiesUtil.getValue("wechat.properties", "access_token_url");
    private static final String GET_TOKEN_URL = PropertiesUtil.getValue("wechat.properties", "get_token_url");
    private static final String GET_MORE_USER_OPENID_URL = PropertiesUtil.getValue("wechat.properties", "get_more_user_openid_url");
    private static final String GET_USER_OPENID_URL = PropertiesUtil.getValue("wechat.properties", "get_user_openid_url");
    private static final String GET_USER_DETAIL_URL = PropertiesUtil.getValue("wechat.properties", "get_user_detail_url");
    private static final String REDIRECT_URI = PropertiesUtil.getValue("wechat.properties", "redirect_uri");
    private static final String USER_INFO_URL = PropertiesUtil.getValue("wechat.properties", "user_info_url");
    private static final String SCOPE = PropertiesUtil.getValue("wechat.properties", "scope");
    private static final String SCOPE_BASE = PropertiesUtil.getValue("wechat.properties", "scope_base");
    private static final String REFRESH_TOKEN_URL = PropertiesUtil.getValue("wechat.properties", "refesh_token_url");


    /**
     * 授权登录
     *
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "login")
    @ResponseBody
    public void wechatAuthorize(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("授权");
        String redirect_uri = URLEncoder.encode(REDIRECT_URI, "utf-8");
//        String wecharUrl = USER_INFO_URL.replace("APPID", APPID).replace("REDIRECT_URI", redirect_uri).replace("SCOPE", SCOPE_BASE).replace("STATE",SCOPE_BASE);
        String wecharUrl = USER_INFO_URL.replace("APPID", APPID).replace("REDIRECT_URI", redirect_uri).replace("SCOPE", SCOPE).replace("STATE", SCOPE);
        response.sendRedirect(wecharUrl);
    }

    /**
     * 回调
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "callback")
    public ModelAndView wechatAuthorizeCallback(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        System.out.println("授权回调此接口");
        String code = request.getParameter("code");
        if (code != null && !code.equals("")) {
            // 这里则获取到了登陆成功后的结果code.
            // 通过code 获取到微信的唯一标识access_token（这里是用于网页授权的access_token）
            String getTokenUrl = GET_TOKEN_URL.replace("APPID", APPID).replace("SECRET", SECRET).replace("CODE", code);
            String result = HttpUtil.httpURLConectionGET(getTokenUrl, null);
            JSONObject jsonObject = JSONObject.parseObject(result);
            System.out.println("获取access_token和openid:" + jsonObject.toString());
            String state = request.getParameter("state");
            //1,会员网页手动授权
            if (state.contains(SCOPE)) {
                JSONObject user = getUser(jsonObject.getString("access_token"), jsonObject.getString("openid"));
                System.out.println("user = " + user.toString());
                String open_id = user.getString("openid");
                Member member = memberService.selectByOpenId(open_id);
                if (member == null) {
                    member = new Member();
                    member.setNick_name(user.getString("nickname"));
                    member.setOpenid(open_id);
                    member.setIcon(user.getString("headimgurl"));
                    member.setSex(user.getInteger("sex"));
                    member.setAddress(user.getString("country") + user.getString("province") + user.getString("city"));
                    memberService.save(member);
                }
                mv.addObject("member", member);
                mv.addObject("member2", member.toString());
            } else {//2,会员静默授权
                String access_token = AccessTokenUtil.getAccessToken().getString("access_token");
                JSONObject userDetail = getUserDetail(access_token, jsonObject.getString("openid"));
                System.out.println("userDetail = " + userDetail.toString());
                //如果可以拿到用户信息则snsapi_base授权结束
                if (!userDetail.containsKey("errcode")) {
                    String open_id = userDetail.getString("openid");
                    Member member = memberService.selectByOpenId(open_id);
                    if (member == null) {
                        member = new Member();
                        member.setNick_name(userDetail.getString("nickname"));
                        member.setOpenid(open_id);
                        member.setIcon(userDetail.getString("headimgurl"));
                        member.setSex(userDetail.getInteger("sex"));
                        member.setSubscribe(userDetail.getInteger("subscribe"));
                        member.setAddress(userDetail.getString("country") + userDetail.getString("province") + userDetail.getString("city"));
                        memberService.save(member);
                    }
                    mv.addObject("member", member);
                    mv.addObject("member2", member.toString());
                } else {
                    //如果静默授权获取会员信息失败则改为用户手动授权
                    System.out.println("手动授权");
                    String redirect_uri = URLEncoder.encode(REDIRECT_URI, "utf-8");
                    String wecharurl = USER_INFO_URL.replace("APPID", APPID).replace("REDIRECT_URI", redirect_uri).replace("SCOPE", SCOPE).replace("STATE", state.replace(SCOPE_BASE, SCOPE));
                    response.sendRedirect(wecharurl);
                }
            }
            mv.setViewName(state.replace(SCOPE, ""));
        } else {
            mv.setViewName("mobile/index");
        }
        return mv;
    }
}
