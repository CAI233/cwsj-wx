package com.szkj.platform.busiz.ctrl;

import com.szkj.platform.busiz.beans.OrderBargainBean;
import com.szkj.platform.busiz.beans.OrderFormListBean;
import com.szkj.platform.busiz.beans.OrderGoodsListBean;
import com.szkj.platform.busiz.domain.OrderExpressSetting;
import com.szkj.platform.busiz.domain.OrderForm;
import com.szkj.platform.busiz.service.OrderManageService;
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
import java.util.List;

@Controller
@RequestMapping("/api/busiz")
public class OrderManageController {

    @Autowired
    private OrderManageService orderManageService;

    /**
     * 订单分页列表
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/order/list")
    @ResponseBody
    public Object getList(HttpServletRequest request, @RequestBody OrderFormListBean bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            Sort sort = null;
            //获取排序字段和排序规则
            if (StringUtils.isNotEmpty(bean.getSort_name()) && StringUtils.isNotEmpty(bean.getSort_rule())) {
                sort = new Sort("desc".equals(bean.getSort_rule()) ? Sort.Direction.DESC : Sort.Direction.ASC,  bean.getSort_name() );
            } else {
                //默认排序规则
                sort = new Sort(Sort.Direction.DESC, "update_time");
            }
            Object result = orderManageService.getList(bean,sort);
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
            jsonResult.setData(result);
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }


    /**
     * 发货更新订单物流信息
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/order/send")
    @ResponseBody
    public Object sendGoods(HttpServletRequest request, @RequestBody OrderForm bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            OrderForm order = orderManageService.selectById(bean.getOrder_id());
            if (order.getOrder_status() != 2){
                return JsonResult.getError("当前状态不可修改物流信息！");
            }
            if (bean.getOrder_id() == null || StringUtils.isEmpty(bean.getExpress()) || StringUtils.isEmpty(bean.getExpress_num())){
                return JsonResult.getError("请填写物流信息!");
            }
            orderManageService.updateExpressMsg(bean);
            return JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.UPDATE_SUCCESS.code()));
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }


    /**
     * 订单商品列表
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/order/goods/list")
    @ResponseBody
    public Object sendMsg(HttpServletRequest request,@RequestBody OrderForm bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            if (bean.getOrder_id() == null){
                return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
            }
            List<OrderGoodsListBean> list = orderManageService.getGoodsList(bean.getOrder_id());
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
            jsonResult.setData(list);
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }


    /**
     * 协商议价修改价格
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/order/bargain")
    @ResponseBody
    public Object bargain(HttpServletRequest request , @RequestBody OrderBargainBean bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            if (StringUtils.isEmpty(bean.getType())){
                return JsonResult.getError("请选择修改价格类型！");
            }
            OrderForm form = orderManageService.selectById(bean.getOrder_id());
            if (form.getOrder_status() > 1){
                return JsonResult.getError("当前订单状态不可修改！");
            }
            if ("price".equals(bean.getType())){
                //修改商品单价
                orderManageService.updateGoodsPrice(bean.getPrice(),bean.getGoods_id(),bean.getOrder_id());
            }else if ("express".equals(bean.getType())){
                orderManageService.updateExpressFee(bean.getOrder_id(),bean.getPrice());
            }
            return JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.UPDATE_SUCCESS.code()));
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 运费配置
     * @param request
     * @return
     */
    @RequestMapping("/order/express")
    @ResponseBody
    public Object getExpressSetting(HttpServletRequest request) {
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            OrderExpressSetting result = orderManageService.getSetting();
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
            jsonResult.setData(result);
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 新增、修改运费配置
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/order/express/setting")
    @ResponseBody
    public Object expressSetting(HttpServletRequest request,@RequestBody OrderExpressSetting bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            if (bean.getId() == null){
                //新增
                OrderExpressSetting setting = orderManageService.getSetting();
                if (setting != null){
                    return JsonResult.getError("配置已存在！");
                }
                orderManageService.saveSetting(bean);
                return JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.ADD_SUCCESS.code()));
            }else {
                //修改
                orderManageService.updateSetting(bean);
                return JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.UPDATE_SUCCESS.code()));
            }
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }


    /**
     * 更新订单状态
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/order/status")
    @ResponseBody
    public Object updateStatus(HttpServletRequest request,@RequestBody OrderForm bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            if (bean.getOrder_id() == null || bean.getOrder_status() == null){
                return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
            }
            orderManageService.updateStatus(bean.getOrder_id(),bean.getOrder_status());
            return JsonResult.getSuccess("操作成功！");
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }
}
