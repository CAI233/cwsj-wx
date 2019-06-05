package com.szkj.platform.system.ctrl;


import com.szkj.platform.system.beans.LoginUserBean;
import com.szkj.platform.system.constants.Constants;
import com.szkj.platform.system.domain.Organization;
import com.szkj.platform.system.domain.SysUser;
import com.szkj.platform.system.service.OrganizationService;
import com.szkj.platform.system.service.SysUserLogService;
import com.szkj.platform.system.service.UserService;
import com.szkj.platform.system.utils.*;
import com.szkj.platform.system.verify.ToolVerifyFactory;
import com.szkj.platform.utils.JsonResult;
import com.szkj.platform.utils.PasswordUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by Administrator on 2016/10/25.
 */
@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private SysUserLogService sysUserLogService;

    @Autowired
    private OrganizationService organizationService;


    /**
     * 登录
     *
     * @param request,sysUser
     * @return
     */
    @RequestMapping(value = "/api/system/login", method = {RequestMethod.POST})
    @ResponseBody
    public Object login(HttpServletRequest request, HttpServletResponse response, @RequestBody LoginUserBean loginbean) {
        SysUser user;
        //验证码校验
        String clientcode = loginbean.getCode();
        String clientype = loginbean.getClient_type();
        boolean isMobile = RequestContextUtil.isMobileDevice(request);
        if(isMobile) {
            clientype = "h5";
        }else {
            if (StringUtils.isEmpty(clientype)) {
                clientype = "web";
            }
        }
        System.out.println("checkcode sessionid:"+request.getSession().getId());
        JsonResult checkresult = ToolVerifyFactory.getInstance().getVerify().checkValidCode(request,response,loginbean.getUser_name(),clientype,clientcode);
        if(checkresult.getCode()>0) {
            return checkresult;
        }
        //用户名或密码不为空，用户名或密码登陆
        // 检查账号是否注册(用户名)
        user = userService.findByUserName(loginbean.getUser_name());
        if (user == null) {
            return JsonResult.getError(MessageAPi.getMessage("userNotExist"));
        } else {
            String md5Pwd = PasswordUtil.entryptPassword(loginbean.getUser_pwd());
            // 2.校验密码
            if (!md5Pwd.equals(user.getUser_pwd())) {
                return JsonResult.getError(MessageAPi.getMessage("pwdError"));
            }
        }
        if (user.getEnabled() != 1) {
            return JsonResult.getError(Constants.USER_DISABLE);
        }

        Organization org = organizationService.selectOrgById(user.getOrg_id());
        if (org == null){
            return JsonResult.getError(MessageAPi.getMessage("orgNotExist"));
        }
        if (org.getEnabled() != 1){
            return JsonResult.getError(MessageAPi.getMessage("orgNotEnabled"));
        }

        //根据userID重新查询用户信息
        user = userService.getUserById(user.getUser_id());

        //创建token
        String token = TokenManagerUtil.createToken(user.getUser_id());
        //如果该用户已登录，则清除token重新加入缓存
        if (CacheUtil.get("userId_" + user.getUser_id()) != null) {
            CacheUtil.del("token_" + (String) CacheUtil.get("userId_" + user.getUser_id()));
            CacheUtil.del("userId_" + user.getUser_id());
        }
        CacheUtil.set("userId_" + user.getUser_id(), token, 1800);
        CacheUtil.set("token_" + token, user.getUser_id(), 1800);
        CacheUtil.set("userInfo_" + user.getUser_id(), user);
        //保存登录记录
        sysUserLogService.saveLoginLog(user);

        user.setToken(token);
        user.setUser_pwd(null);
        user.setClient_type(loginbean.getClient_type());
        JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage("loginSuccess"));
        jsonResult.setData(user);
        return jsonResult;
    }

    /**
     * 注销
     *
     * @param request
     * @return
     */
    @RequestMapping("/api/system/logout")
    @ResponseBody
    public Object logout(HttpServletRequest request) {
        //清楚缓存中所有用户信息
        String token = request.getHeader("token");
        if (CacheUtil.get("token_" + token) != null) {
            Long user_id = (Long) CacheUtil.get("token_" + token);
            CacheUtil.del("token_" + token);
            CacheUtil.del("userId_" + user_id);
            CacheUtil.del("userInfo_" + user_id);
        }
        return JsonResult.getSuccess(MessageAPi.getMessage("logoutSuccess"));
    }


    /**
     * 扫码登录
     *
     * @param request
     * @param sysUser
     * @return
     */
    @RequestMapping(value = "/api/system/sweep/login", method = {RequestMethod.POST})
    @ResponseBody
    public Object sweepLogin(HttpServletRequest request, @RequestBody SysUser sysUser) {
        if (StringUtils.isEmpty(sysUser.getQq()) && StringUtils.isEmpty(sysUser.getMicro_signal())) {
            return JsonResult.getExpire(MessageAPi.getMessage(MsgCodeEnum.EMPTY.code()));
        }
        //通过微信号和QQ号查询用户
        SysUser user = userService.findByUserWeChatAndQQ(sysUser.getMicro_signal(), sysUser.getQq());
        if (user == null) {
            return JsonResult.getError(MessageAPi.getMessage("userNotExist"));
        }
        if (user.getEnabled() != 1) {
            return JsonResult.getError(Constants.USER_DISABLE);
        }
        //根据userID重新查询用户信息
        sysUser = userService.getUserById(user.getUser_id());
        //创建token
        String token = TokenManagerUtil.createToken(sysUser.getUser_id());
        //如果该用户已登录，则清除token重新加入缓存
        if (CacheUtil.get("userId_" + user.getUser_id()) != null) {
            CacheUtil.del("token_" + (String) CacheUtil.get("userId_" + user.getUser_id()));
            CacheUtil.del("userId_" + user.getUser_id());
        }
        CacheUtil.set("userId_" + sysUser.getUser_id(), token, 1800);
        CacheUtil.set("token_" + token, user.getUser_id(), 1800);
        CacheUtil.set("userInfo_" + sysUser.getUser_id(), sysUser);
        //保存登录记录
        sysUserLogService.saveLoginLog(sysUser);

        sysUser.setToken(token);
        sysUser.setUser_pwd(null);
        JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage("loginSuccess"));
        jsonResult.setData(sysUser);
        return jsonResult;
    }

    @RequestMapping(value = "/api/system/validcode", method = { RequestMethod.GET })
    public void validecode(HttpServletRequest request, HttpServletResponse resp, Map<String, Object> model)  {
        try{
            String clientype = request.getParameter("client_type");
            String account = request.getParameter("user_name");
            boolean isMobile = RequestContextUtil.isMobileDevice(request);
            if(isMobile) {
                clientype = "h5";
            }else {
                if (StringUtils.isEmpty(clientype)) {
                    clientype = "web";
                }
            }
            System.out.println("getcode sessionid:"+request.getSession().getId());
            ToolVerifyFactory.getInstance().getVerify().getValidCode(request,resp,account,clientype);
            //ValidCodeUtil.getValidCode(request, resp);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
}
