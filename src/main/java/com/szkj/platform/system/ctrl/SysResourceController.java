package com.szkj.platform.system.ctrl;

import com.github.pagehelper.StringUtil;
import com.szkj.platform.system.beans.DelResBean;
import com.szkj.platform.system.constants.Constants;
import com.szkj.platform.system.domain.Role;
import com.szkj.platform.system.domain.SysResource;
import com.szkj.platform.system.domain.SysUser;
import com.szkj.platform.system.service.RoleResRelService;
import com.szkj.platform.system.service.RoleService;
import com.szkj.platform.system.service.SysResourceService;
import com.szkj.platform.system.service.UserService;
import com.szkj.platform.system.utils.CheckUserHelper;
import com.szkj.platform.system.utils.MessageAPi;
import com.szkj.platform.system.utils.MsgCodeEnum;
import com.szkj.platform.utils.JsonResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 资源管理控制器
 * Created by Administrator on 2016/11/8 0008.
 */
@Controller
public class SysResourceController {

    @Autowired
    private SysResourceService sysResourceService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleResRelService roleResRelService;

    @Autowired
    private UserService userService;

    /**
     * 资源列表 列表数据
     *
     * @param request
     */
    @RequestMapping(value = "/api/system/resource/list", method = {RequestMethod.POST})
    @ResponseBody
    public Object sysResource_list(HttpServletRequest request, @RequestBody Map param) {
        String org_id_str = param.get("org_id") + "";
        if (StringUtil.isEmpty(org_id_str)) {
            JsonResult result = JsonResult.getError("请提供组织编码！");
            return result;
        }
        Long org_id = 0L;
        try {
            org_id = Long.valueOf(org_id_str);
        } catch (Exception e) {
            JsonResult result = JsonResult.getError("请提供正确的组织编码！");
            return result;
        }
        try {
            List<SysResource> sysResources = sysResourceService.selectByOrg_id(org_id);
            List<SysResource> data = sysResourceService.selectAllSysMenuTree(sysResources);
            JsonResult result = JsonResult.getSuccess("获取数据成功！");
            result.setData(data);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JsonResult.getError("获取数据失败！");
    }


    /**
     * 当前用户可见资源
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/system/resource/menus", method = {RequestMethod.POST})
    @ResponseBody
    public Object getMenus(HttpServletRequest request) {
        // 验证当前登录用户
        SysUser sysUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (sysUser == null) {
            return JsonResult.getExpire(MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code()));
        }
        try {
            List<SysResource> menuList = new ArrayList<SysResource>();
            List<Role> roles = roleService.getRolesByUserId(sysUser.getUser_id());
            List<Long> role_ids = new ArrayList<Long>();
            List<String> role_names = new ArrayList<String>();
            List<Integer> role_types = new ArrayList<Integer>();
            for (Role role : roles) {
                role_ids.add(role.getRole_id());
                role_names.add(role.getRole_name());
                role_types.add(role.getRole_type());
            }
            if (role_types.contains(1) && sysUser.getOrg_id() == 1) {
                //超级管理员
                menuList = sysResourceService.selectByOrg_id(sysUser.getOrg_id());
            } else if (role_types.contains(1) && sysUser.getOrg_id() != 1) {
                //机构管理员
                menuList = sysResourceService.selectAllSysMenuByOrgId(sysUser.getOrg_id());
            } else {
                //其他用户
                String roleid = "";
                for (int i = 0; i < roles.size(); i++) {
                    if (i != roles.size() - 1) {
                        roleid += roles.get(i).getRole_id() + ",";
                    } else {
                        roleid += roles.get(i).getRole_id() + "";
                    }
                }
                menuList = sysResourceService.selectAllSysMenuByRoleId(roleid);
            }
            List<SysResource> data = sysResourceService.selectAllSysMenuTree(menuList);
            JsonResult result = JsonResult.getSuccess("获取数据成功！");
            result.setData(data);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException("获取数据失败！");
        }
    }

    /**
     * 资源列表 添加、修改数据
     *
     * @param request
     */
    @RequestMapping(value = "/api/system/resource/update", method = {RequestMethod.POST})
    @ResponseBody
    public Object add_sysResource(HttpServletRequest request, @RequestBody SysResource sysResource) {
        // 验证当前登录用户
        SysUser sysUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (sysUser == null) {
            return JsonResult.getExpire(MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code()));
        }
        //验证长度
        if (sysResource.getRes_name().length() > 40) {
            return JsonResult.getError(MessageAPi.getMessage("resNameLonger"));
        }
        if (sysResource.getRes_key() != null && sysResource.getRes_key().length() > 50) {
            return JsonResult.getError(MessageAPi.getMessage("resKeyLonger"));
        }
        if (sysResource.getRes_url() != null && sysResource.getRes_url().length() > 255) {
            return JsonResult.getError(MessageAPi.getMessage("resUrlLonger"));
        }
        if (sysResource.getRes_icon() != null && sysResource.getRes_icon().length() > 255) {
            return JsonResult.getError(MessageAPi.getMessage("resIconLonger"));
        }
        try {
            JsonResult result = JsonResult.getSuccess("更新数据成功！");
            String full_path = "";
            //根据res_key查找资源
            SysResource resource = null;
            if (sysResource.getFull_ids_array() == null || sysResource.getFull_ids_array().length == 0) {
                return JsonResult.getError(Constants.CLASS_PID_ERROR);
            }
            //获取路径最后一个值
            Long pid = sysResource.getFull_ids_array()[sysResource.getFull_ids_array().length - 1];
            sysResource.setPid(pid);
            //将层次路径数组转换为字符串
            String full_ids = StringUtils.join(sysResource.getFull_ids_array(), ",");
            if (sysResource.getRes_id() == null) {
                //新增
                if (!StringUtil.isEmpty(sysResource.getRes_key())) {
                    resource = sysResourceService.selectByResKey(sysResource.getRes_key(), sysResource.getOrg_id());
                }
                if (resource != null) {
                    return JsonResult.getError("资源标识不能重复！");
                }
                if (sysResource.getPid() != null && sysResource.getPid() != 0) {
                    SysResource p_sysResource = sysResourceService.selectByResId(sysResource.getPid());
                    full_path = p_sysResource.getFull_path();
                } else {
                    sysResource.setPid(0L);
                    full_path = "0|";
                }
                sysResource.setEnabled(1);
                sysResource.setIs_forbid(1);//是否禁止(1: 禁止 2: 不禁止)
                sysResource.setCreate_time(new Date());
                sysResourceService.addSysMenu(sysResource);
                sysResource.setSource_id(sysResource.getRes_id());
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append(full_ids + "," + sysResource.getRes_id());
                sysResource.setFull_ids(stringBuffer.toString());
                sysResource.setFull_path(full_path + sysResource.getRes_id() + "|");
                sysResourceService.updateSysMenu(sysResource);
            } else {
                //修改
                resource = sysResourceService.selectByResId(sysResource.getRes_id());
                if (resource == null) {
                    return JsonResult.getError("资源不存在！");
                }
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append(full_ids + "," + sysResource.getRes_id());
                resource.setFull_ids(stringBuffer.toString());

                //获取当前修改的资源本身和下级资源
                List<SysResource> ownerResources = sysResourceService.getOwnerResources(resource.getOrg_id(), resource.getFull_path());
                for (SysResource ownerResource : ownerResources) {
                    if (ownerResource.getRes_id().equals(resource.getPid())) {
                        return JsonResult.getError(Constants.CLASS_PID_ERROR);
                    }
                }

                if (sysResource.getPid() != null && sysResource.getPid() != 0) {
                    SysResource p_sysResource = sysResourceService.selectByResId(sysResource.getPid());
                    full_path = p_sysResource.getFull_path();
                    resource.setPid(sysResource.getPid());
                } else {
                    resource.setPid(0L);
                    full_path = "0|";
                }

                String old_full_path = resource.getFull_path();
                String new_full_path = full_path + resource.getRes_id() + "|";
                if (!old_full_path.equals(new_full_path)) {
                    sysResourceService.updateSysMenuByFullPath(old_full_path, new_full_path);
                    resource.setFull_path(new_full_path);
                }
                resource.setRes_name(sysResource.getRes_name());
                resource.setRes_key(sysResource.getRes_key());
                resource.setOrg_id(sysResource.getOrg_id());
                resource.setRes_type(sysResource.getRes_type());
                resource.setRes_url(sysResource.getRes_url());
                if (sysResource.getOrder_weight() != null) {
                    resource.setOrder_weight(sysResource.getOrder_weight());
                }
                if (sysResource.getRes_icon() != null) {
                    resource.setRes_icon(sysResource.getRes_icon());
                }
                sysResourceService.updateSysMenu(resource);
            }
            result.setData(sysResource);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JsonResult.getError("更新数据失败！");
    }

    /**
     * 资源列表 修改数据禁止状态(is_forbid(1: 禁止 2: 不禁止))
     * 当前不禁止，上级全部不禁止，下级不变
     * 当前禁止，上级不变，下级全部禁止
     *
     * @param request
     * @param sysResource
     * @return
     */
    @RequestMapping(value = "/api/system/resource/update/forbid", method = {RequestMethod.POST})
    @ResponseBody
    public Object update_is_forbid(HttpServletRequest request, @RequestBody SysResource sysResource) {
        // 验证当前登录用户
        SysUser sysUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (sysUser == null) {
            return JsonResult.getExpire(MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code()));
        }
        try {
            //是否禁止(1: 禁止 2: 不禁止)
            if (sysResource.getIs_forbid() == null || sysResource.getIs_forbid() == 2) {
                //禁止：上级不变，下级全部禁止
                sysResourceService.updateForbidByFullPath(1, sysResource.getFull_path());
            } else {
                //不禁止：上级全部不禁止，下级不变
                String res_ids[] = sysResource.getFull_path().split("\\|");
                sysResourceService.updateForbidByResId(2, StringUtils.join(res_ids, ","));
            }
            JsonResult result = JsonResult.getSuccess("更新数据成功！");
            result.setData(sysResource);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JsonResult.getError("更新数据失败！");
    }

    /**
     * 资源列表 修改数据启用状态(enabled(1: 启用 2: 停用))
     * 当前启用，上级全部启用，下级不变
     * 当前停用，上级不变，下级全部停用
     *
     * @param request
     */
    @RequestMapping(value = "/api/system/resource/update/enabled", method = {RequestMethod.POST})
    @ResponseBody
    public Object update_enabled(HttpServletRequest request, @RequestBody SysResource sysResource) {
        // 验证当前登录用户
        SysUser sysUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (sysUser == null) {
            return JsonResult.getExpire(MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code()));
        }
        try {
            if (sysResource.getEnabled() == 1) {
                //停用：上级不变，下级全部停用
                sysResourceService.updateEnabledByFullPath(2, sysResource.getFull_path());
            } else {
                //启用：上级全部启用，下级不变
                String res_ids[] = sysResource.getFull_path().split("\\|");
                sysResourceService.updateEnabledByResId(1, StringUtils.join(res_ids, ","));
            }
            JsonResult result = JsonResult.getSuccess("更新数据成功！");
            result.setData(sysResource);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JsonResult.getError("更新数据失败！");
    }

    /**
     * 资源列表 一键推送（菜单同步）
     *
     * @param request
     */
    @RequestMapping(value = "/api/system/resource/allot_sysResource", method = {RequestMethod.POST})
    @ResponseBody
    public Object allot_sysResource(HttpServletRequest request, @RequestBody Map param) {
        // 验证当前登录用户
        SysUser sysUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (sysUser == null) {
            return JsonResult.getExpire(MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code()));
        }
        try {
            Object org_id = param.get("org_id");
            Object source_id = param.get("source_id");
            if (org_id == null) {
                return JsonResult.getError("机构id不能为空");
            }
            if (source_id != null && source_id != "") {
                String msg = sysResourceService.allotSysResourceByOrgId(Long.valueOf(org_id.toString()), Long.valueOf(source_id.toString()));
                if (msg.equals("failed")) {
                    return JsonResult.getError("请先更新上级资源");
                }
            } else {
                sysResourceService.allotSysResourceByOrgId(Long.valueOf(org_id.toString()), 0l);
            }
            JsonResult result = JsonResult.getSuccess(Constants.ACTION_UPDATE);
            result.setData(new ArrayList());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JsonResult.getError("更新数据失败！");
    }

    /**
     * 资源列表 一键同步权限
     *
     * @param request
     */
    @RequestMapping(value = "/api/system/resource/allot_sysRole", method = {RequestMethod.POST})
    @ResponseBody
    public Object allot_sysRole(HttpServletRequest request, @RequestBody Map param) {
        // 验证当前登录用户
        SysUser sysUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (sysUser == null) {
            return JsonResult.getExpire(MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code()));
        }
        try {
            String org_id = param.get("org_id").toString();
            sysResourceService.allotSysRoleByOrgId(Long.valueOf(org_id));
            JsonResult result = JsonResult.getSuccess(Constants.ACTION_UPDATE);
            result.setData(new ArrayList());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JsonResult.getError("更新数据失败！");
    }


    /**
     * 资源列表 删除数据
     *
     * @param request
     */
    @RequestMapping(value = "/api/system/resource/delete", method = {RequestMethod.POST})
    @ResponseBody
    public Object delete_sysResource(HttpServletRequest request, @RequestBody DelResBean delResBean) {
        // 验证当前登录用户
        SysUser sysUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (sysUser == null) {
            return JsonResult.getExpire(MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code()));
        }
        try {
//            if (delResBean.getOrg_id()==null){
//                return JsonResult.getExpire("机构id不可为空！");
//            }
            String res_ids_str = StringUtils.join(delResBean.getRes_ids(), ",");
            List<SysResource> resources = sysResourceService.selectByResIds(res_ids_str);
            String full_paths = "";
            for (int i = 0; i < resources.size(); i++) {
                if (i != resources.size() - 1) {
                    full_paths += resources.get(i).getFull_path() + ",";
                } else {
                    full_paths += resources.get(i).getFull_path();
                }
            }
            List<SysResource> sysResources = sysResourceService.selectSysMenuByFullPaths(full_paths);

            if (StringUtils.isEmpty(delResBean.getMark())) {
                if (sysResources.size() > 1) {
                    return JsonResult.getError("是否删除关联资源？");
                }
            }
            String res_ids = "";
            for (int i = 0; i < sysResources.size(); i++) {
                if (i != sysResources.size() - 1) {
                    res_ids += sysResources.get(i).getRes_id() + ",";
                } else {
                    res_ids += sysResources.get(i).getRes_id();
                }
            }

            roleResRelService.deteleByResIds(res_ids);
            sysResourceService.deleteSysMenuByFullPaths(full_paths, delResBean.getOrg_id());

            JsonResult result = JsonResult.getSuccess("删除数据成功！");
            result.setData(new ArrayList());
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException("删除数据失败！");
        }
    }

}