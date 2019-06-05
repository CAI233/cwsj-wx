package com.szkj.platform.busiz.ctrl;

import com.szkj.platform.busiz.domain.PayConfigSetting;
import com.szkj.platform.busiz.service.PayOrderService;
import com.szkj.platform.system.conditions.IdsCondition;
import com.szkj.platform.system.domain.AlipayConfigBean;
import com.szkj.platform.system.domain.SysUser;
import com.szkj.platform.system.domain.WeixinPayConfig;
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
@RequestMapping("/api/system/pay/config")
public class PayConfigController {

    @Autowired
    private PayOrderService payOrderService;

    /**
     * 获取支付宝配置
     * @param request
     * @return
     */
    @RequestMapping("/alipay")
    @ResponseBody
    public Object getAlipayConfig(HttpServletRequest request){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        try{
            AlipayConfigBean config = payOrderService.selectAlipayConfig();
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
            jsonResult.setData(config);
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 获取微信支付配置
     * @param request
     * @return
     */
    @RequestMapping("/weixinPay")
    @ResponseBody
    public Object getWeixinPayConfig(HttpServletRequest request){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        try{
            WeixinPayConfig config = payOrderService.selectWeixinPayConfig();
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
            jsonResult.setData(config);
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 保存支付宝配置
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/alipay/save")
    @ResponseBody
    public Object saveAlipayConfig(HttpServletRequest request , @RequestBody AlipayConfigBean bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        try {
            payOrderService.saveAlipayConfig(bean);
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.SAVE_SUCCESS.code()));
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 保存微信支付配置
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/weixinpay/save")
    @ResponseBody
    public Object saveWeixinPayConfig(HttpServletRequest request , @RequestBody WeixinPayConfig bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        try {
            payOrderService.saveWeixinPayConfig(bean);
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.SAVE_SUCCESS.code()));
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 支付配置列表
     * @param request
     * @return
     */
    @RequestMapping("/list")
    @ResponseBody
    public Object getPayConfigList(HttpServletRequest request){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        try{
            List<PayConfigSetting> list = payOrderService.getList();
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
            jsonResult.setData(list);
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }


    /**
     * 启用、停用
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/enabled")
    @ResponseBody
    public Object enabledConfig(HttpServletRequest request , @RequestBody IdsCondition bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        try{
            String pay_type_ids = StringUtils.join(bean.getIds(),",");
            payOrderService.updateEnabled(pay_type_ids,bean.getEnabled());
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.UPDATE_SUCCESS.code()));
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }
}
