package com.szkj.platform.busiz.ctrl;

import com.szkj.platform.busiz.beans.WindowConfigListBean;
import com.szkj.platform.busiz.domain.Window;
import com.szkj.platform.busiz.domain.WindowGoodsRel;
import com.szkj.platform.busiz.service.WindowService;
import com.szkj.platform.system.conditions.IdsCondition;
import com.szkj.platform.system.conditions.SortCondition;
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

@Controller
@RequestMapping("/api/busiz")
public class WindowController {

    @Autowired
    private WindowService windowService;

    /**
     * 橱窗分页列表
     * @param request
     * @param condition
     * @return
     */
    @RequestMapping("/window/list")
    @ResponseBody
    public Object getList(HttpServletRequest request, @RequestBody SortCondition condition){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        try{
            Sort sort = new Sort(Sort.Direction.ASC, "order_weight");
            Object result = windowService.getList(condition,sort);
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
            jsonResult.setData(result);
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 橱窗新增、修改
     * @param request
     * @param window
     * @return
     */
    @RequestMapping("/window/save")
    @ResponseBody
    public Object save(HttpServletRequest request,@RequestBody Window window){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        try {
            JsonResult jsonResult = JsonResult.getSuccess("");
            if (window.getWindow_id() == null){
                //新增
                //判断橱窗名是否重复
                Window win = windowService.selectOneByName(window.getWindow_name());
                if (win != null){
                    return JsonResult.getError("橱窗名已存在！");
                }
                windowService.save(window);
                jsonResult.setMessage(MessageAPi.getMessage(MsgCodeEnum.ADD_SUCCESS.code()));
            }else {
                //修改
                //判断橱窗名是否重复
                Window win = windowService.selectOneByNameAndId(window.getWindow_name(),window.getWindow_id());
                if (win != null){
                    return JsonResult.getError("橱窗名已存在！");
                }
                windowService.updateWindow(window);
                jsonResult.setMessage(MessageAPi.getMessage(MsgCodeEnum.UPDATE_SUCCESS.code()));
            }
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 删除橱窗
     * @param request
     * @param condition
     * @return
     */
    @RequestMapping("/window/del")
    @ResponseBody
    public Object del(HttpServletRequest request, @RequestBody IdsCondition condition){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        try{
            if (condition.getIds() == null){
                return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
            }
            String window_ids = StringUtils.join(condition.getIds(),",");
            windowService.del(window_ids);
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.DEL_SUCCESS.code()));
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 橱窗配置商品
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/window/config")
    @ResponseBody
    public Object configGoods(HttpServletRequest request, @RequestBody WindowGoodsRel bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        try{
            WindowGoodsRel rel = windowService.selectGoodsById(bean.getWindow_id(),bean.getGoods_id());
            if (rel != null){
                return JsonResult.getError("商品已存在！");
            }
            Window window = windowService.selectById(bean.getWindow_id());
            if (window.getReal_count().intValue() >= window.getMax_count().intValue()){
                return JsonResult.getError("已超过橱窗最大数量，添加失败！");
            }
            if (bean.getGoods_id() == null || bean.getWindow_id() == null){
                return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
            }
            windowService.saveConfig(bean);
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.SAVE_SUCCESS.code()));
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 橱窗移除商品
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/window/rmconfig")
    @ResponseBody
    public Object rmConfig(HttpServletRequest request,@RequestBody WindowGoodsRel bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        try{
            if (bean.getWindow_id() == null || bean.getGoods_id() == null){
                return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
            }
            windowService.removeConfig(bean);
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.UPDATE_SUCCESS.code()));
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 橱窗商品列表
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/window/goodslist")
    @ResponseBody
    public Object getConfigList(HttpServletRequest request, @RequestBody WindowConfigListBean bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        try{
            if (bean.getWindow_id() == null){
                return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
            }
            Sort sort = new Sort(Sort.Direction.ASC, "order_weight");
            Object result = windowService.getGoodsList(bean,sort);
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
            jsonResult.setData(result);
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }


    /**
     * 更新商品排序
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/window/goods/orderby")
    @ResponseBody
    public Object GoodsOrder(HttpServletRequest request,@RequestBody WindowGoodsRel bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        try{
            if (bean.getRel_id() == null || bean.getOrder_weight() == null){
                return JsonResult.getError("参数错误！");
            }
            windowService.updateGoodsOrder(bean);
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.UPDATE_SUCCESS.code()));
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }
}
