package com.szkj.platform.system.ctrl;

import com.github.pagehelper.StringUtil;
import com.szkj.platform.core.page.PageList;
import com.szkj.platform.system.beans.RoleListBean;
import com.szkj.platform.system.conditions.IdsCondition;
import com.szkj.platform.system.conditions.SortCondition;
import com.szkj.platform.system.constants.Constants;
import com.szkj.platform.system.domain.Role;
import com.szkj.platform.system.domain.SysRoleResRel;
import com.szkj.platform.system.domain.SysUser;
import com.szkj.platform.system.domain.SysUserRoleRel;
import com.szkj.platform.system.service.RoleResRelService;
import com.szkj.platform.system.service.RoleService;
import com.szkj.platform.system.service.UserRoleRelService;
import com.szkj.platform.system.service.UserService;
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
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2016/10/25.
 */
@Controller
@RequestMapping("/api/system/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserRoleRelService userRoleRelService;

    @Autowired
    private RoleResRelService roleResRelService;

    @Autowired
    private UserService userService;

    /**
     * 角色列表
     *
     * @return
     */
    @RequestMapping("/list")
    @ResponseBody
    public Object listAll(HttpServletRequest request, @RequestBody SortCondition roleCondition) {
        // 验证当前登录用户
        SysUser sysUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (sysUser == null) {
            return JsonResult.getExpire(MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code()));
        }
        try {
            Sort sort = null;
            //获取排序字段和排序规则
            if (StringUtils.isNotEmpty(roleCondition.getSort_name()) && StringUtils.isNotEmpty(roleCondition.getSort_rule()) ) {
                sort = new Sort("desc".equals(roleCondition.getSort_rule()) ? Sort.Direction.DESC : Sort.Direction.ASC, ""+roleCondition.getSort_name()+"");
            } else if (StringUtils.isNotEmpty(roleCondition.getSort_name()) && StringUtils.isEmpty(roleCondition.getSort_rule()) ) {
                sort = new Sort(Sort.Direction.DESC, ""+roleCondition.getSort_name()+"");
            } else if (StringUtils.isEmpty(roleCondition.getSort_name()) && StringUtils.isNotEmpty(roleCondition.getSort_rule()) ) {
                sort = new Sort("desc".equals(roleCondition.getSort_rule()) ? Sort.Direction.DESC : Sort.Direction.ASC, "role_id");
            } else {
                //默认排序规则
                sort = new Sort(Sort.Direction.DESC, "role_id");
            }
            PageList result = (PageList) roleService.pageQuery(sort, roleCondition, sysUser.getOrg_id());
            JsonResult jsonResult = JsonResult.getSuccess(Constants.LOAD_SUCCESS);
            jsonResult.setData(result);
            return jsonResult;
        } catch (Exception e) {
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    /**
     * 新增角色
     *
     * @return
     */
    @RequestMapping("/save")
    @ResponseBody
    public Object saveRole(HttpServletRequest request, @RequestBody Role role) {
        // 验证当前登录用户
        SysUser sysUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (sysUser == null) {
            return JsonResult.getExpire(MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code()));
        }
        try {
            if (role.getRole_id() == null) {
                //同机构下的角色名不能重复
                Role ro = roleService.getRoleByNameAndOrgId(role.getRole_name(), sysUser.getOrg_id());
                if (ro != null) {
                    return JsonResult.getError(Constants.ROLE_NAME_EXIST);
                }
                role.setOrg_id(sysUser.getOrg_id());
                role.setRole_type(Constants.NO_ADMIN_ROLE);
                roleService.saveRole(role);
                return JsonResult.getSuccess(Constants.ACTION_ADD);
            } else {
                Role exrole = roleService.selectById(role.getRole_id());
                //检查角色是否存在
                if (exrole == null) {
                    return JsonResult.getError(Constants.ROLE_NOT_EXIST);
                } else {
                    if (!role.getRole_name().equals(exrole.getRole_name())) {
                        //检查角色名称在当前组织是否存在
                        Role ro = roleService.getRoleByNameAndOrgId(role.getRole_name(), sysUser.getOrg_id());
                        if (ro != null) {
                            return JsonResult.getError(Constants.ROLE_NAME_EXIST);
                        }
                    }
                    exrole.setRole_name(role.getRole_name());
                    exrole.setRemark(role.getRemark());
                    roleService.updateRole(exrole);
                    return JsonResult.getSuccess(Constants.ACTION_UPDATE);
                }
            }
        } catch (Exception e) {
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    /**
     * 删除
     *
     * @return
     */
    @RequestMapping("/delete")
    @ResponseBody
    public Object delRole(HttpServletRequest request, @RequestBody IdsCondition delRole) {

        // 验证当前登录用户
        SysUser sysUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (sysUser == null) {
            return JsonResult.getExpire(MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code()));
        }
        String ids = Arrays.toString(delRole.getIds());
        String role_ids = ids.substring(1, ids.length() - 1);
        try {
            for(int i=0;i<delRole.getIds().length;i++) {
                if(delRole.getIds()[i].equals("1")) {
                    return JsonResult.getError("超级管理员不能删除");
                }
            }
            //管理员角色不可删除
            List<RoleListBean> list = roleService.getRoleList(null, null, role_ids, Constants.IS_ADMIN_ROLE);
            if (list.size() > 0) {
                return JsonResult.getError(Constants.ROLE_NOT_DELETE);
            }
            //角色下有未删除掉的用户时，该角色不可删除
            List<SysUserRoleRel> userRoleRels = userRoleRelService.getRelListByRole(role_ids);
            if (userRoleRels.size() > 0) {
                return JsonResult.getError(Constants.USER_IN_ROLE);
            }
            //角色关联资源时，删除角色时也同时删除相关资源
            List<SysRoleResRel> roleResRels = roleResRelService.getRelListByRole(role_ids);
            if (StringUtil.isEmpty(delRole.getMark())) {
                if (roleResRels.size() > 0) {
                    return JsonResult.getError(Constants.DELETE_ROLE_RES_REL);
                } else {
                    roleService.updateIsDelete(role_ids);
                }
            } else {
                roleService.updateIsDelete(role_ids);
                if (userRoleRels.size() > 0) {
                    userRoleRelService.updateIsDeleteByRole(role_ids);
                }
                if (roleResRels.size() > 0) {
                    roleResRelService.deteleByRoles(role_ids);
                }
            }
            return JsonResult.getSuccess(Constants.ACTION_DELETE);
        } catch (Exception e) {
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }
}
