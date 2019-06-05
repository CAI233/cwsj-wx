package com.szkj.platform.busiz.ctrl;

import com.szkj.platform.busiz.beans.CourierBean;
import com.szkj.platform.busiz.domain.Courier;
import com.szkj.platform.busiz.service.CourierService;
import com.szkj.platform.system.constants.Constants;
import com.szkj.platform.system.domain.SysUser;
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

/**
 * 快递
 */
@Controller
@RequestMapping(value = "/api/busiz/courier/")
public class CourierController {

    @Autowired
    private CourierService courierService;

    /**
     * 新增
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "save")
    @ResponseBody
    public Object save(HttpServletRequest request, @RequestBody Courier bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            if (bean.getCourier_id() == null){
                Courier courier = courierService.selectByOrderId(bean.getOrder_id());
                if (courier != null){
                    return JsonResult.getError("一个订单号只能有一个快递");
                }
                courierService.save(bean);
                JsonResult result = JsonResult.getSuccess(Constants.ACTION_ADD);
                result.setData(bean);
                return result;
            }else {
                Courier courier = courierService.selectById(bean.getCourier_id());
                if (courier == null){
                    return JsonResult.getError("快递信息不存在");
                }
                Courier courier1 = courierService.selectOtherByIdAndOrderId(bean.getCourier_id(), bean.getOrder_id());
                if (courier1 != null){
                    return JsonResult.getError("一个订单号只能有一个快递");
                }
                courierService.update(bean, courier);
                JsonResult result = JsonResult.getSuccess(Constants.ACTION_UPDATE);
                result.setData(courier);
                return result;
            }
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

}
