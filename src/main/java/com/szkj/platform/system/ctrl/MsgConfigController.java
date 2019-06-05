package com.szkj.platform.system.ctrl;

import com.szkj.platform.system.conditions.IdsCondition;
import com.szkj.platform.system.conditions.MsgListCondition;
import com.szkj.platform.system.domain.SysMsgConfig;
import com.szkj.platform.system.domain.SysUser;
import com.szkj.platform.system.service.MsgConfigService;
import com.szkj.platform.system.utils.CacheUtil;
import com.szkj.platform.system.utils.CheckUserHelper;
import com.szkj.platform.system.utils.MessageAPi;
import com.szkj.platform.system.utils.MsgCodeEnum;
import com.szkj.platform.utils.JsonResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/api/system/msg")
public class MsgConfigController {

    @Autowired
    private MsgConfigService msgConfigService;

    /**
     * 消息配置分页列表
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/list")
    @ResponseBody
    public Object getList(HttpServletRequest request, @RequestBody MsgListCondition bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        try{
            Sort sort = null;
            //获取排序字段和排序规则
            if (StringUtils.isNotEmpty(bean.getSort_name()) && StringUtils.isNotEmpty(bean.getSort_rule()) ) {
                sort = new Sort("desc".equals(bean.getSort_rule()) ? Sort.Direction.DESC : Sort.Direction.ASC, bean.getSort_name());
            } else {
                //默认排序规则
                sort = new Sort(Sort.Direction.DESC, "create_time");
            }
            Object result = msgConfigService.getMessageList(bean,sort);
            //查询配置的获取列表成功的反馈信息
            String msgSuccess = MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code());
            JsonResult jsonResult = JsonResult.getSuccess(msgSuccess);
            jsonResult.setData(result);
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            String message = MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code());
            return JsonResult.getException(message);
        }
    }

    /**
     * 消息新增、修改保存
     * @param request
     * @param msgConfig
     * @return
     */
    @RequestMapping("/save")
    @ResponseBody
    public Object save(HttpServletRequest request , @RequestBody SysMsgConfig msgConfig){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = msgConfigService.getMessageByCode(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try {
            JsonResult jsonResult = JsonResult.getSuccess("");
            //数据效验
            String msg_code_empty = msgConfigService.getMessageByCode(MsgCodeEnum.EMPTY.code());
            if (msgConfig.getCode() == null) {
                return JsonResult.getError(msg_code_empty);
            }
            if (StringUtils.isEmpty(msgConfig.getMessage())) {
                return JsonResult.getError(msg_code_empty);
            }
            if (msgConfig.getMod_id() == null) {
                return JsonResult.getError(msg_code_empty);
            }
            //名称重复信息
            String exist_msg  = MessageAPi.getMessage(MsgCodeEnum.EXIST.code());
            if (msgConfig.getMsg_id() == null) {
                //新增
                String msgConfigByCode = msgConfigService.getMessageByCode(msgConfig.getCode());
                if (StringUtils.isNotEmpty(msgConfigByCode)) {
                    return JsonResult.getError(exist_msg);
                }
                msgConfigService.save(msgConfig);
                jsonResult.setMessage(MessageAPi.getMessage(MsgCodeEnum.ADD_SUCCESS.code()));
                return jsonResult;
            } else {
                //修改
                String msgConfigByCode = msgConfigService.getMessageByCodeEx(msgConfig.getCode(), msgConfig.getMsg_id());
                if (StringUtils.isNotEmpty(msgConfigByCode)) {
                    return JsonResult.getError(exist_msg);
                }
                msgConfigService.update(msgConfig);
                //清除缓存中该信息的数据，下次调用可以直接查询数据库最新数据
                CacheUtil.del("msg_code_" + msgConfig.getCode());
                jsonResult.setMessage(MessageAPi.getMessage(MsgCodeEnum.UPDATE_SUCCESS.code()));
                return jsonResult;
            }
        }catch (Exception e){
            e.printStackTrace();
            String message = MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code());
            return JsonResult.getException(message);
        }
    }

    /**
     * 消息配置批量删除
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/del")
    @ResponseBody
    public Object del(HttpServletRequest request , @RequestBody IdsCondition bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = msgConfigService.getMessageByCode(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            if (bean.getIds() == null || bean.getIds().length < 1){
                return JsonResult.getError("");
            }
            for (int i = 0;i<bean.getIds().length;i++){
                SysMsgConfig config = msgConfigService.selectCommon(bean.getIds()[i]);
                if (config != null){
                    return JsonResult.getError(MessageAPi.getMessage("commonMsgNotDel"));
                }
            }
            //批量删除
            String ids = StringUtils.join(bean.getIds(),",");
            msgConfigService.delMsgConfig(ids);
            String del_msg = MessageAPi.getMessage(MsgCodeEnum.DEL_SUCCESS.code());
            JsonResult jsonResult = JsonResult.getSuccess(del_msg);
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            String message = MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code());
            return JsonResult.getException(message);
        }
    }
}
