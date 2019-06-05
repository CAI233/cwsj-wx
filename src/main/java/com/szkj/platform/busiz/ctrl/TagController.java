package com.szkj.platform.busiz.ctrl;

import com.szkj.platform.busiz.domain.Tag;
import com.szkj.platform.busiz.service.TagService;
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
@RequestMapping(value = "/api/busiz")
public class TagController {

    @Autowired
    private TagService tagService;

    /**
     * 标签分页列表
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/tag/list")
    @ResponseBody
    public Object getList(HttpServletRequest request , @RequestBody SortCondition bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
           if (bean.getPageNum() == null || bean.getPageSize() == null){
               return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
           }
            Sort sort = null;
            //获取排序字段和排序规则
            if (StringUtils.isNotEmpty(bean.getSort_name()) && StringUtils.isNotEmpty(bean.getSort_rule())) {
                sort = new Sort("desc".equals(bean.getSort_rule()) ? Sort.Direction.DESC : Sort.Direction.ASC, "" + bean.getSort_name() + "");
            } else {
                //默认排序规则
                sort = new Sort(Sort.Direction.DESC, "create_time");
            }
           Object result = tagService.getList(bean,sort);
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
            jsonResult.setData(result);
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
        }
        return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
    }


    /**
     * 新增、修改标签
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "/tag/save")
    @ResponseBody
    public Object save(HttpServletRequest request, @RequestBody Tag bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            if (StringUtils.isEmpty(bean.getTag_name())){
                return JsonResult.getError("标签名称不能为空");
            }
            JsonResult jsonResult = JsonResult.getSuccess("");
            if (bean.getTag_id() == null){
                //新增
                Tag tag = tagService.selectTagByName(bean.getTag_name());
                if (tag != null){
                    return JsonResult.getError("标签名称已存在！");
                }
                tagService.saveTag(bean);
                jsonResult.setMessage(MessageAPi.getMessage(MsgCodeEnum.ADD_SUCCESS.code()));
                return jsonResult;
            }else {
                //修改
                Tag tag = tagService.selectTagByNameOutId(bean.getTag_name(),bean.getTag_id());
                if (tag != null){
                    return JsonResult.getError("标签名称已存在！");
                }
                tagService.updateTag(bean);
                jsonResult.setMessage(MessageAPi.getMessage(MsgCodeEnum.UPDATE_SUCCESS.code()));
                return jsonResult;
            }
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }


    /**
     * 删除标签
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "/tag/del")
    @ResponseBody
    public Object del(HttpServletRequest request, @RequestBody IdsCondition bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            if (bean.getIds() == null){
                return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
            }
            String ids_str = StringUtils.join(bean.getIds(), ",");
            tagService.delTag(ids_str);
            JsonResult result = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.DEL_SUCCESS.code()));
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }
}
