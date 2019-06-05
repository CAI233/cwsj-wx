package com.szkj.platform.busiz.ctrl;


import com.szkj.platform.busiz.domain.QuestionCat;
import com.szkj.platform.busiz.service.CatService;
import com.szkj.platform.busiz.service.QuestionCatService;
import com.szkj.platform.system.conditions.IdsCondition;
import com.szkj.platform.system.conditions.SortCondition;
import com.szkj.platform.system.domain.SysUser;
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
import java.util.List;

@Controller
@RequestMapping("/api/busiz/question")
public class QuestionCatController {

    @Autowired
    private QuestionCatService questionCatService;

    /**
     * 分类树
     * @param request
     * @return
     */
    @RequestMapping("/QuestionCat/list")
    @ResponseBody
    public Object getTree(HttpServletRequest request,@RequestBody SortCondition bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
           List<QuestionCat> list = questionCatService.getCatList(bean.getSearchText());
           //转换为树形结构
           List<QuestionCat> result = questionCatService.getTree(list);
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
    public Object save(HttpServletRequest request, @RequestBody QuestionCat bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            JsonResult jsonResult = JsonResult.getSuccess("");
            if (bean.getCat_id() == null){
                //新增
                QuestionCat cat = questionCatService.selectCatByName(bean.getCat_name());
                if (cat != null){
                    return JsonResult.getError("分类名称已存在！");
                }
                questionCatService.saveCat(bean);
                jsonResult.setMessage(MessageAPi.getMessage(MsgCodeEnum.ADD_SUCCESS.code()));
                return jsonResult;
            }else {
                //修改
                QuestionCat cat = questionCatService.selectCatByNameOutId(bean.getCat_name(),bean.getCat_id());
                if (cat != null){
                    return JsonResult.getError("分类名称已存在！");
                }
                questionCatService.updateCat(bean);
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
            for (int i =0;i<bean.getIds().length;i++){
                Long cat_id= bean.getIds()[i];
                questionCatService.delCatByPath(cat_id);
            }
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.DEL_SUCCESS.code()));
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }
}
