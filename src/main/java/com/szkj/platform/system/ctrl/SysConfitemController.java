package com.szkj.platform.system.ctrl;

import com.szkj.platform.condition.PageCondition;
import com.szkj.platform.system.beans.DelSysConfitemBean;
import com.szkj.platform.system.constants.Constants;
import com.szkj.platform.system.domain.SysConfitem;
import com.szkj.platform.system.domain.SysUser;
import com.szkj.platform.system.service.SysConfitemService;
import com.szkj.platform.system.service.UserService;
import com.szkj.platform.system.utils.CheckUserHelper;
import com.szkj.platform.utils.IDUtil;
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

/**
 * Created by pc on 2017/7/18.
 */
@Controller
public class SysConfitemController {
    @Autowired
    private UserService userService;

    @Autowired
    private SysConfitemService sysConfitemService;

    /*
    * 添加配置项
    * */
    @RequestMapping(value = "/admin/sysconf/save" ,method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public Object saveSysConf(HttpServletRequest request, @RequestBody SysConfitem sysConfitem) {
        SysUser sUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));// 当前登录用户
        if (sUser == null) {
            return JsonResult.getExpire(Constants.OVER_TIME);
        }
        sysConfitem.setCreate_user_id(sUser.getUser_id());//用户信息
        sysConfitem.setCreate_user_name(sUser.getUser_name());
        SysConfitem aa = sysConfitemService.findSysCon(sysConfitem.getName());//是否存在相同配置
        try {
            if (StringUtils.isEmpty(sysConfitem.getId())){
                sysConfitem.setId(IDUtil.createId());
                sysConfitemService.saveSysConfitem(sysConfitem);
                JsonResult jsonResult = new JsonResult().getSuccess(Constants.ACTION_ADD);
                jsonResult.setData(sysConfitem);
                return jsonResult;
            }else{
                //修改
                SysConfitem sysConf = sysConfitemService.findSysConfitem(sysConfitem.getId());
                if(sysConf == null){
                    return JsonResult.getError(Constants.EXCEPTION);
                }else {
                    SysConfitem scf = sysConfitemService.selectOhterById(sysConfitem.getId(), sysConfitem.getName());
                    if(scf != null){
                        JsonResult.getException("名字已存在！");
                    }
                    sysConf.setName(sysConfitem.getName());
                    sysConfitemService.updateSysConfitem(sysConf);
                    JsonResult jsonResult = new JsonResult().getSuccess(Constants.ACTION_UPDATE);
                    jsonResult.setData(sysConf);
                    return jsonResult;
                }
            }
        } catch (Exception e) {
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    /*
    * 删除配置项
    * */
    @RequestMapping(value = "/admin/sysconf/del",method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public Object deleteSysConf(HttpServletRequest request, @RequestBody DelSysConfitemBean bean) {
        SysUser sUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));// 当前登录用户
        if (sUser == null) {
            return JsonResult.getExpire(Constants.OVER_TIME);
        }
        try{
            String[] ids = bean.getIds();
            String ids_str = StringUtils.join(ids,",");
            sysConfitemService.delByIds(ids_str);
            JsonResult result = JsonResult.getSuccess(Constants.ACTION_DELETE);
            result.setData(new ArrayList<>());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    //
    @RequestMapping(value = "/admin/sysconf/list",method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public Object selectSysConflist(HttpServletRequest request, @RequestBody PageCondition pageConditionBean){
        SysUser sUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));// 当前登录用户
        if (sUser == null) {
            return JsonResult.getExpire(Constants.OVER_TIME);
        }
        Sort sort = new Sort(Sort.Direction.DESC,"create_time");
        Object object = sysConfitemService.getAllSysCon(sort, pageConditionBean);
        JsonResult jsonResult = new JsonResult().getSuccess(Constants.LOAD_SUCCESS);
        jsonResult.setData(object);
        return jsonResult;
    }
}
