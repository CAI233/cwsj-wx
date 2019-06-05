package com.szkj.platform.busiz.ctrl;


import com.szkj.platform.busiz.domain.WorksCat;
import com.szkj.platform.busiz.domain.WorksCat;
import com.szkj.platform.busiz.service.WorksCatService;
import com.szkj.platform.system.conditions.IdsCondition;
import com.szkj.platform.system.conditions.SortCondition;
import com.szkj.platform.system.domain.SysUser;
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
@RequestMapping("/api/busiz/works")
public class WorksCatController {

    @Autowired
    private WorksCatService worksCatService;

    /**
     * 分类树
     * @param request
     * @return
     */
    @RequestMapping("/cat/list")
    @ResponseBody
    public Object getTree(HttpServletRequest request,@RequestBody SortCondition bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
           List<WorksCat> list = worksCatService.getCatList(bean.getSearchText());
           //转换为树形结构
           List<WorksCat> result = worksCatService.getTree(list);
           JsonResult jsonResult =JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
           jsonResult.setData(result);
           return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 分类新增修改
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/cat/save")
    @ResponseBody
    public Object save(HttpServletRequest request, @RequestBody WorksCat bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            JsonResult jsonResult = JsonResult.getSuccess("");
            if (bean.getCat_id() == null){
                //新增
                WorksCat cat = worksCatService.selectCatByName(bean.getCat_name());
                if (cat != null){
                    return JsonResult.getError("分类名称已存在！");
                }
                worksCatService.saveCat(bean);
                jsonResult.setMessage(MessageAPi.getMessage(MsgCodeEnum.ADD_SUCCESS.code()));
                return jsonResult;
            }else {
                //修改
                WorksCat cat = worksCatService.selectCatByNameOutId(bean.getCat_name(),bean.getCat_id());
                if (cat != null){
                    return JsonResult.getError("分类名称已存在！");
                }
                worksCatService.updateCat(bean);
                jsonResult.setMessage(MessageAPi.getMessage(MsgCodeEnum.UPDATE_SUCCESS.code()));
                return jsonResult;
            }
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 删除分类
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/cat/del")
    @ResponseBody
    public Object del(HttpServletRequest request, @RequestBody IdsCondition bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            if (bean.getIds() == null){
                return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
            }
            for (int i = 0;i<bean.getIds().length;i++){
                Long cat_id = bean.getIds()[i];
                worksCatService.delCatByPath(cat_id);
            }
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.DEL_SUCCESS.code()));
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }
}
