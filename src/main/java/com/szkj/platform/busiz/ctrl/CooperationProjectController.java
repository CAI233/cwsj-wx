package com.szkj.platform.busiz.ctrl;

import com.szkj.platform.busiz.beans.CooperationProjectBean;
import com.szkj.platform.busiz.beans.DelCooperationProjectBean;
import com.szkj.platform.busiz.domain.CooperationProject;
import com.szkj.platform.busiz.service.CooperationProjectService;
import com.szkj.platform.system.constants.Constants;
import com.szkj.platform.system.domain.SysUser;
import com.szkj.platform.system.service.BaseService;
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

@Controller
@RequestMapping(value = "/api/busiz/cooproject/")
public class CooperationProjectController {

    @Autowired
    private CooperationProjectService cooperationProjectService;
    @Autowired
    private BaseService baseService;

    /**
     * 合作项目分页列表
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "getlist")
    @ResponseBody
    public Object getlist(HttpServletRequest request, @RequestBody CooperationProjectBean bean){
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
            Object object = cooperationProjectService.pageQuery(bean, sort);
            JsonResult result = JsonResult.getSuccess(Constants.LOAD_SUCCESS);
            result.setData(object);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    /**
     * 合作项目新增修改
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "save")
    @ResponseBody
    public Object save(HttpServletRequest request, @RequestBody CooperationProject bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            if (bean.getProject_id() == null){
                CooperationProject cooperationProject = cooperationProjectService.selectByName(bean.getProject_name());
                if (cooperationProject != null){
                    return JsonResult.getError("名称重复");
                }
                if (StringUtils.isNotEmpty(bean.getProject_cover())){
                    String goods_cover_small = baseService.getImgScale(bean.getProject_cover(), "small", 0.5);
                    bean.setProject_cover_small(goods_cover_small);
                }
                cooperationProjectService.save(bean);
                JsonResult result = JsonResult.getSuccess(Constants.ACTION_ADD);
                result.setData(bean);
                return result;
            }else {
                CooperationProject cooperationProject = cooperationProjectService.selectById(bean.getProject_id());
                if (cooperationProject == null){
                    return JsonResult.getError("合作项目不存在");
                }
                CooperationProject cooperationProject1 = cooperationProjectService.selectOtherByIdAndName(bean.getProject_id(), bean.getProject_name());
                if (cooperationProject1 != null){
                    return JsonResult.getError("名称重复");
                }
                if (StringUtils.isNotEmpty(bean.getProject_cover()) && !bean.getProject_cover().equals(cooperationProject.getProject_cover())){
                    String goods_cover_small = baseService.getImgScale(bean.getProject_cover(), "small", 0.5);
                    bean.setProject_cover_small(goods_cover_small);
                }
                cooperationProjectService.update(bean, cooperationProject);
                JsonResult result = JsonResult.getSuccess(Constants.ACTION_UPDATE);
                result.setData(cooperationProject);
                return result;
            }
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    /**
     * 批量删除
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "del")
    @ResponseBody
    public Object del(HttpServletRequest request, @RequestBody DelCooperationProjectBean bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            Long[] ids = bean.getIds();
            String ids_str = StringUtils.join(ids, ",");
            cooperationProjectService.updateIsDeletes(ids_str);
            JsonResult result = JsonResult.getSuccess(Constants.ACTION_DELETE);
            result.setData(new ArrayList<>());
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    /**
     * 合作项目状态
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "enabled")
    @ResponseBody
    public Object enabled(HttpServletRequest request, @RequestBody CooperationProject bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            CooperationProject cooperationProject = cooperationProjectService.selectById(bean.getProject_id());
            if (cooperationProject == null){
                return JsonResult.getError("合作项目不存在");
            }
            Integer enabled = null;
            JsonResult result = JsonResult.getSuccess("");
            if (cooperationProject.getEnabled() == 1){
                enabled = 2;
                result.setMessage("停用");
            }else if (cooperationProject.getEnabled() == 2){
                enabled = 1;
                result.setMessage("启用");
            }
            cooperationProjectService.enabled(bean.getProject_id(), enabled);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

}
