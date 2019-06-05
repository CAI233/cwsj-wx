package com.szkj.platform.system.ctrl;

import com.szkj.platform.system.constants.Constants;
import com.szkj.platform.system.domain.MessageSetting;
import com.szkj.platform.system.domain.SysUser;
import com.szkj.platform.system.service.MessageSettingService;
import com.szkj.platform.system.service.MessageTemplateService;
import com.szkj.platform.system.service.MsgFactory;
import com.szkj.platform.system.service.MsgSendFactory;
import com.szkj.platform.system.utils.CheckUserHelper;
import com.szkj.platform.system.utils.MessageAPi;
import com.szkj.platform.system.utils.MsgCodeEnum;
import com.szkj.platform.utils.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
public class MessageSettingController {

    @Autowired
    private MessageSettingService messageSettingService;

    @Autowired
    private MessageTemplateService messageTemplateService;


    /**
     * 新增/修改
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "/api/system/msgset/save")
    @ResponseBody
    public Object save(HttpServletRequest request, @RequestBody MessageSetting bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            //登录超时
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            if (bean.getId() == null){
                MessageSetting messageSetting = messageSettingService.selectByAk(bean.getAccesskeyid());
                if (messageSetting != null){
                    return JsonResult.getError("该ak已经存在");
                }
                MessageSetting messageSetting1 = messageSettingService.selectByApiKey(bean.getApikey());
                if (messageSetting1 != null){
                    return JsonResult.getError("该apikey已经存在");
                }
                messageSettingService.save(bean);
                JsonResult result = JsonResult.getSuccess(Constants.ACTION_ADD);
                result.setData(bean);
                return result;
            }else {
                MessageSetting messageSetting = messageSettingService.selectById(bean.getId());
                if (messageSetting == null){
                    return JsonResult.getError("该配置不存在");
                }
                MessageSetting messageSetting1 = messageSettingService.selectOtherByIdAndAk(bean.getId(), bean.getAccesskeyid());
                if (messageSetting1 != null){
                    return JsonResult.getError("该ak已经存在");
                }
                MessageSetting messageSetting2 = messageSettingService.selectOtherByIdAndApiKey(bean.getId(), bean.getApikey());
                if (messageSetting2 != null){
                    return JsonResult.getError("该apikey已经存在");
                }
                messageSettingService.update(bean, messageSetting);
                JsonResult result = JsonResult.getSuccess(Constants.ACTION_UPDATE);
                result.setData(messageSetting);
                return result;
            }
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    /**
     * 列表
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/system/msgset/list")
    @ResponseBody
    public Object list(HttpServletRequest request){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            //登录超时
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            List<MessageSetting> list = messageSettingService.getList();
            JsonResult result = JsonResult.getSuccess(Constants.LOAD_SUCCESS);
            result.setData(list);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    /**
     * 列表
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/system/msgset/yunpianlist")
    @ResponseBody
    public Object yunPianList(HttpServletRequest request){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            //登录超时
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            List<MessageSetting> list = messageSettingService.getYunPianList();
            JsonResult result = JsonResult.getSuccess(Constants.LOAD_SUCCESS);
            result.setData(list);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    /**
     * 短信配置删除
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "/api/system/msgset/del")
    @ResponseBody
    public Object del(HttpServletRequest request, @RequestBody MessageSetting bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            //登录超时
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            MessageSetting messageSetting = messageSettingService.selectById(bean.getId());
            if (messageSetting == null){
                return JsonResult.getException("该配置不存在");
            }
            messageSettingService.delete(bean.getId());
            JsonResult result = JsonResult.getSuccess(Constants.ACTION_DELETE);
            result.setData(new ArrayList<>());
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    /**
     * 启用停用
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "/api/system/msgset/enabled")
    @ResponseBody
    public Object enabled(HttpServletRequest request, @RequestBody MessageSetting bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            //登录超时
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            messageSettingService.unEnabled();
            MessageSetting messageSetting = messageSettingService.selectById(bean.getId());
            if (messageSetting == null){
                return JsonResult.getError("短信配置不存在");
            }
            JsonResult result = JsonResult.getSuccess("");
            if (bean.getEnabled() == 1){
                result.setMessage("启用");
            }else if (bean.getEnabled() == 2){
                result.setMessage("停用");
            }
            messageSettingService.enabled(messageSetting.getId(), bean.getEnabled());
            result.setData(messageSetting);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }


    /**
     * 短信调用样例接口规范
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "/api/system/msgset/sendmsg")
    @ResponseBody
    public Object sendMsg(HttpServletRequest request, @RequestBody MessageSetting bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            //登录超时
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            MsgFactory msgFactory = new MsgSendFactory();
            msgFactory.sendMsg(bean.getMobile(), bean.getContent(), bean.getTpl_id(), bean.getTpl_value());
            JsonResult result = JsonResult.getSuccess("发送成功");
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }
}
