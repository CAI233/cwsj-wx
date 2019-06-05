package com.szkj.platform.system.ctrl;

import com.szkj.platform.system.conditions.IdsCondition;
import com.szkj.platform.system.domain.SysModuleConfig;
import com.szkj.platform.system.domain.SysUser;
import com.szkj.platform.system.service.ModuleConfigService;
import com.szkj.platform.system.utils.CheckUserHelper;
import com.szkj.platform.system.utils.MessageAPi;
import com.szkj.platform.system.utils.MsgCodeEnum;
import com.szkj.platform.utils.JsonResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/api/system/module")
public class ModuleConfigController {

    @Autowired
    private ModuleConfigService moduleConfigService;

    /**
     * @param request
     * @return
     */
    @RequestMapping("/list")
    @ResponseBody
    public Object getList(HttpServletRequest request ){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        try {
            List<SysModuleConfig> result = moduleConfigService.getList();
            //数据加载成功
            String successMsg = MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code());
            JsonResult jsonResult = JsonResult.getSuccess(successMsg);
            jsonResult.setData(result);
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            String errMsg = MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code());
            return JsonResult.getException(errMsg);
        }
    }

    /**
     * 模块配置新增、修改保存
     * @param request
     * @param moduleConfig
     * @return
     */
    @RequestMapping("/save")
    @ResponseBody
    public Object save(HttpServletRequest request , @RequestBody SysModuleConfig moduleConfig){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        //判断为空返回消息
        String emptyMsg = MessageAPi.getMessage(MsgCodeEnum.EMPTY.code());
        //保存成功返回消息
        //名称重复返回信息
        String existMsg = MessageAPi.getMessage(MsgCodeEnum.EXIST.code());
        JsonResult jsonResult = JsonResult.getSuccess("");
        try{
            if (StringUtils.isEmpty(moduleConfig.getModule_name())){
                return JsonResult.getError(emptyMsg);
            }
            if (StringUtils.isEmpty(moduleConfig.getModule_short_name())){
                return JsonResult.getError(emptyMsg);
            }
            if (moduleConfig.getMod_id() == null){
                //新增
                SysModuleConfig config = moduleConfigService.selectByName(moduleConfig.getModule_name(),moduleConfig.getModule_short_name());
                if (config != null){
                    return JsonResult.getError(existMsg);
                }
                moduleConfigService.save(moduleConfig);
                jsonResult.setMessage(MessageAPi.getMessage(MsgCodeEnum.ADD_SUCCESS.code()));
                return jsonResult;
            }else {
                //修改
                SysModuleConfig config = moduleConfigService.selectByNameEx(moduleConfig.getModule_name(),moduleConfig.getModule_short_name(),moduleConfig.getMod_id());
                if (config != null){
                    return JsonResult.getError(existMsg);
                }
                moduleConfigService.update(moduleConfig);
                jsonResult.setMessage(MessageAPi.getMessage(MsgCodeEnum.UPDATE_SUCCESS.code()));
                return jsonResult;
            }
        }catch (Exception e){
            e.printStackTrace();
            String errMsg = MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code());
            return JsonResult.getException(errMsg);
        }
    }

    /**
     * 删除模块
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/del")
    @ResponseBody
    public Object del(HttpServletRequest request , @RequestBody IdsCondition bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        //数据异常信息
        String errMsg = MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code());
        try{
            if (bean.getIds() == null || bean.getIds().length < 1){
                return JsonResult.getError(errMsg);
            }
            for (int i = 0;i<bean.getIds().length;i++) {
                if (bean.getIds()[i] == 1L) {
                    return JsonResult.getError(MessageAPi.getMessage("commonNotDel"));
                }
            }
            String ids = StringUtils.join(bean.getIds(),",");
            moduleConfigService.delMod(ids);
            //删除成功返回消息
            String del_msg = MessageAPi.getMessage(MsgCodeEnum.DEL_SUCCESS.code());
            JsonResult jsonResult = JsonResult.getSuccess(del_msg);
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(errMsg);
        }
    }
}
