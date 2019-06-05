package com.szkj.platform.busiz.ctrl;
import com.szkj.platform.busiz.beans.CatBean;
import com.szkj.platform.busiz.beans.DelProjectCatBean;
import com.szkj.platform.busiz.domain.ProjectCat;
import com.szkj.platform.busiz.service.ProjectCatService;
import com.szkj.platform.system.conditions.IdsCondition;
import com.szkj.platform.system.conditions.SortCondition;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 合作项目分类
 */
@Controller
@RequestMapping(value = "/api/busiz/cat/Cooperation/")
public class CooperationCatController {

        @Autowired
        private ProjectCatService projectCatService;

    /**
     * 分类树
     * @param request
     * @return
     */
    @RequestMapping("list")
    @ResponseBody
    public Object getTree(HttpServletRequest request, @RequestBody SortCondition bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            List<ProjectCat> list = projectCatService.getCatList(bean.getSearchText());
            //转换为树形结构
            List<ProjectCat> result = projectCatService.getTree(list);
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
    @RequestMapping("save")
    @ResponseBody
    public Object save(HttpServletRequest request, @RequestBody ProjectCat bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            JsonResult jsonResult = JsonResult.getSuccess("");
            if (bean.getCat_id() == null){
                //新增
                ProjectCat cat = projectCatService.selectCatByName(bean.getCat_name());
                if (cat != null){
                    return JsonResult.getError("分类名称已存在！");
                }
                projectCatService.saveCat(bean);
                jsonResult.setMessage(MessageAPi.getMessage(MsgCodeEnum.ADD_SUCCESS.code()));
                return jsonResult;
            }else {
                //修改
                ProjectCat cat = projectCatService.selectCatByNameOutId(bean.getCat_name(),bean.getCat_id());
                if (cat != null){
                    return JsonResult.getError("分类名称已存在！");
                }
                projectCatService.updateCat(bean);
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
    @RequestMapping("del")
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
                projectCatService.delCatByPath(cat_id);
            }
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.DEL_SUCCESS.code()));
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 合作项目分类状态
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "enabled")
    @ResponseBody
    public Object enabled(HttpServletRequest request, @RequestBody ProjectCat bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            ProjectCat projectCat = projectCatService.selectById(bean.getCat_id());
            if (projectCat == null){
                return JsonResult.getError("合作项目分类不存在");
            }
            Integer enabled = null;
            JsonResult result = JsonResult.getSuccess("");
            if (projectCat.getEnabled() == 1){
                enabled = 2;
                result.setMessage("停用");
            }else if (projectCat.getEnabled() == 2){
                enabled = 1;
                result.setMessage("启用");
            }
            projectCatService.enabled(bean.getCat_id(), enabled);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

}

