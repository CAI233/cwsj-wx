package com.szkj.platform.busiz.ctrl;

import com.szkj.platform.busiz.beans.CommentsBean;
import com.szkj.platform.busiz.beans.DelVideoBean;
import com.szkj.platform.busiz.domain.Comments;
import com.szkj.platform.busiz.service.CommentsService;
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
import java.util.ArrayList;
import java.util.List;

/**
 * 评论管理
 * Created by Administrator on 2018/3/29 0029.
 */
@Controller
@RequestMapping(value = "/api/busiz/comments/")
public class CommentsController {

    @Autowired
    private CommentsService commentsService;

    /**
     * 视频分页列表
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "getlist")
    @ResponseBody
    public Object getlist(HttpServletRequest request, @RequestBody CommentsBean bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            Sort sort = null;
            //获取排序字段和排序规则
            if (StringUtils.isNotEmpty(bean.getSort_name()) && StringUtils.isNotEmpty(bean.getSort_rule())) {
                sort = new Sort("desc".equals(bean.getSort_rule()) ? Sort.Direction.DESC : Sort.Direction.ASC, "" + bean.getSort_name() + "");
            } else {
                //默认排序规则
                sort = new Sort(Sort.Direction.DESC, "create_time");
            }
            Object object = commentsService.pageQuery(bean, sort);
            JsonResult result = JsonResult.getSuccess(MsgCodeEnum.LOAD_SUCCESS.code());
            result.setData(object);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MsgCodeEnum.EXCEPTION.code());
        }
    }

    /**
     * 新增修改
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "save")
    @ResponseBody
    public Object save(HttpServletRequest request, @RequestBody Comments bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{//评论新增
            if (bean.getId() == null){
                commentsService.save(bean);
                JsonResult result = JsonResult.getSuccess(MsgCodeEnum.ADD_SUCCESS.code());
                result.setData(bean);
                return result;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return JsonResult.getException(MsgCodeEnum.EXCEPTION.code());
    }

    /**
     * 批量删除
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "del")
    @ResponseBody
    public Object del(HttpServletRequest request, @RequestBody DelVideoBean bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            if(bean.getIds()==null) {
                return JsonResult.getError("请选择要删除的评论！");
            }
            String ids_str = "'"+StringUtils.join(bean.getIds(), "','")+"'";
            List<Comments> comments = commentsService.selectByIds(ids_str);
            if (comments.size() == 0){
                return JsonResult.getError("评论不存在！");
            }
            commentsService.deleteCommentsByIds(ids_str);
            JsonResult result = JsonResult.getSuccess(MsgCodeEnum.DEL_SUCCESS.code());
            result.setData(new ArrayList<>());
            return result;

        }catch (Exception e){
            e.printStackTrace();
        }
        return JsonResult.getException(MsgCodeEnum.EXCEPTION.code());
    }


    /**
     * 商品评论列表
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("list")
    @ResponseBody
    public Object resList(HttpServletRequest request,@RequestBody CommentsBean bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            if (bean.getGoods_id() == null){
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
            Object object = commentsService.pageQuerys(bean, sort);
            JsonResult result = JsonResult.getSuccess(MsgCodeEnum.LOAD_SUCCESS.code());
            result.setData(object);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }
}

