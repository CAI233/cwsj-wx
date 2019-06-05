package com.szkj.platform.system.ctrl;

import com.szkj.platform.system.beans.SocialLoginSettingBean;
import com.szkj.platform.system.constants.Constants;
import com.szkj.platform.system.domain.SocialLoginSetting;
import com.szkj.platform.system.domain.SysUser;
import com.szkj.platform.system.service.SocialLoginSettingService;
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
import java.util.ArrayList;

@Controller
@RequestMapping(value = "/api/system/sologinset/")
public class SocialLoginSettingController {

    @Autowired
    private SocialLoginSettingService socialLoginSettingService;

    @RequestMapping(value = "getlist")
    @ResponseBody
    public Object getList(HttpServletRequest request, @RequestBody SocialLoginSettingBean bean){
        // 验证当前登录用户
        SysUser sysUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (sysUser == null) {
            return JsonResult.getExpire(MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code()));
        }
        try{
            Sort sort = new Sort(Sort.Direction.DESC, "create_time");
            Object object = socialLoginSettingService.pageQuery(bean, sort);
            JsonResult result = JsonResult.getSuccess(Constants.LOAD_SUCCESS);
            result.setData(object);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    @RequestMapping(value = "save")
    @ResponseBody
    public Object save(HttpServletRequest request, @RequestBody SocialLoginSetting bean){
        // 验证当前登录用户
        SysUser sysUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (sysUser == null) {
            return JsonResult.getExpire(MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code()));
        }
        try{
            if (bean.getId() == null){
                socialLoginSettingService.save(bean);
                JsonResult result = JsonResult.getSuccess(Constants.ACTION_ADD);
                result.setData(bean);
                return result;
            }else {
                SocialLoginSetting socialLoginSetting = socialLoginSettingService.selectById(bean.getId());
                if (socialLoginSetting == null){
                    return JsonResult.getError("配置项不存在");
                }
                socialLoginSettingService.update(bean, socialLoginSetting);
                JsonResult result = JsonResult.getSuccess(Constants.ACTION_UPDATE);
                result.setData(socialLoginSetting);
                return result;
            }
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    @RequestMapping(value = "del")
    @ResponseBody
    public Object delete(HttpServletRequest request, @RequestBody SocialLoginSetting bean){
        // 验证当前登录用户
        SysUser sysUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (sysUser == null) {
            return JsonResult.getExpire(MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code()));
        }
        try{
            SocialLoginSetting socialLoginSetting = socialLoginSettingService.selectById(bean.getId());
            if (socialLoginSetting == null){
                return JsonResult.getError("配置项不存在");
            }
            socialLoginSettingService.deleteById(bean.getId());
            JsonResult result = JsonResult.getSuccess(Constants.ACTION_DELETE);
            result.setData(new ArrayList<>());
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }
}
