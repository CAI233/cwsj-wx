package com.szkj.platform.system.ctrl;

import com.github.pagehelper.StringUtil;
import com.szkj.platform.system.beans.PassWordBean;
import com.szkj.platform.system.beans.UserListAllBean;
import com.szkj.platform.system.conditions.IdsCondition;
import com.szkj.platform.system.conditions.UserCondition;
import com.szkj.platform.system.constants.Constants;
import com.szkj.platform.system.domain.SysUser;
import com.szkj.platform.system.domain.SysUserRoleRel;
import com.szkj.platform.system.service.OrganizationService;
import com.szkj.platform.system.service.RoleService;
import com.szkj.platform.system.service.UserRoleRelService;
import com.szkj.platform.system.service.UserService;
import com.szkj.platform.system.utils.CheckUserHelper;
import com.szkj.platform.system.utils.MessageAPi;
import com.szkj.platform.system.utils.MsgCodeEnum;
import com.szkj.platform.utils.JsonResult;
import com.szkj.platform.utils.PasswordUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2016/10/25.
 */
@Controller
@RequestMapping("/api/system/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRoleRelService userRoleRelService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private OrganizationService organizationService;

    /**
     * 用户列表
     *
     * @return
     */
    @RequestMapping("/list")
    @ResponseBody
    public Object listAll(HttpServletRequest request, @RequestBody UserCondition userCondition) {
        SysUser sUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (sUser == null) {
            return JsonResult.getExpire(MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code()));
        }
        try {
            Sort sort = null;
            //获取排序字段和排序规则
            if (StringUtils.isNotEmpty(userCondition.getSort_name()) && StringUtils.isNotEmpty(userCondition.getSort_rule())) {
                sort = new Sort("desc".equals(userCondition.getSort_rule()) ? Sort.Direction.DESC : Sort.Direction.ASC, "" + userCondition.getSort_name() + "");
            } else {
                //默认排序规则
                sort = new Sort(Sort.Direction.DESC, "user_id");
            }
            //判断机构是否未机构1
            if (!sUser.getOrg_id().toString().equals("1")) {
                userCondition.setOrg_id(sUser.getOrg_id());
            }
            Object object = userService.pageQuery(sort, userCondition);
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
            jsonResult.setData(object);
            return jsonResult;
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }


    /**
     * 新增用户
     *
     * @return
     */
    @RequestMapping("/save")
    @ResponseBody
    public Object saveUser(HttpServletRequest request, @RequestBody SysUser sysUser) {
        SysUser sUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));// 当前登录用户
        if (sUser == null) {
            return JsonResult.getExpire(MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code()));
        }
        if (StringUtil.isEmpty(sysUser.getUser_name())) {
            return JsonResult.getError(Constants.USER_NAME_NULL);
        }
        if (StringUtil.isEmpty(sysUser.getUser_pwd())) {
            return JsonResult.getError(Constants.USER_PWD_NULL);
        }
        if (sysUser.getOrg_id() == null || sysUser.getOrg_id() == 0) {
            return JsonResult.getError(Constants.PARENT_ORG_NULL);
        }
        if (sysUser.getDept_ids_array() == null || sysUser.getDept_ids_array().length == 0) {
            return JsonResult.getError(Constants.ORG_PID_NULL);
        }
        if (sysUser.getRole_ids_array() == null || sysUser.getRole_ids_array().length == 0) {
            return JsonResult.getError(Constants.ROLE_NOT_NULL);
        }
        //获取部门最后一个值
        Long dept_id = sysUser.getDept_ids_array()[sysUser.getDept_ids_array().length - 1];
        sysUser.setDept_id(dept_id);
        //将部门全路径数组转换为字符串
        String dept_ids = StringUtils.join(sysUser.getDept_ids_array(), ",");
        sysUser.setDept_ids(dept_ids);
        //将角色数组转换为字符串
        String role_ids = StringUtils.join(sysUser.getRole_ids_array(), ",");
        sysUser.setRole_ids(role_ids);
        try {
            if (sysUser.getUser_id() == null) {
                //新增时，检查账号名在全平台是否重复
                SysUser userInfo = userService.findByUserName(sysUser.getUser_name());
                if (userInfo != null) {
                    return JsonResult.getError(Constants.USER_NAME_EXIST);
                }
                //新增时，检查手机号在本机构是否重复
                if (StringUtils.isNotEmpty(sysUser.getPhone())) {
                    SysUser userInfoByPhone = userService.findByUserPhone(sysUser.getPhone(), sysUser.getOrg_id());
                    if (userInfoByPhone != null) {
                        return JsonResult.getError(Constants.USER_PHONE_EXIST);
                    }
                }
                //新增时，检查用户真实姓名在本机构是否重复
                if (StringUtils.isNotEmpty(sysUser.getUser_real_name())) {
                    SysUser userInfoByUserRealName = userService.findByUserRealName(sysUser.getUser_real_name(), sysUser.getOrg_id());
                    if (userInfoByUserRealName != null) {
                        return JsonResult.getError(Constants.USER_REAL_NAME_EXIST);
                    }
                }
                sysUser.setOrg_id(sUser.getOrg_id());
                SysUser user = userService.saveUserAndRel(sysUser);
                user.setUser_pwd("");
                JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.SAVE_SUCCESS.code()));
                jsonResult.setData(new ArrayList());
                return jsonResult;
            } else {
                if (!sysUser.getOrg_id().equals(sUser.getOrg_id())) {
                    return JsonResult.getError("不能修改其他机构用户！");
                }
                SysUser user = userService.selectById(sysUser.getUser_id());
                if (user == null) {
                    return JsonResult.getError(Constants.USER_NOT_EXIST);
                }
                //修改时，检查手机号在本机构是否重复
                if (StringUtils.isNotEmpty(sysUser.getPhone())) {
                    if (!sysUser.getPhone().equals(user.getPhone())) {
                        SysUser userInfoByPhone = userService.findByUserPhone(sysUser.getPhone(), sysUser.getOrg_id());
                        if (userInfoByPhone != null) {
                            return JsonResult.getError(Constants.USER_PHONE_EXIST);
                        }
                    }
                }
                //修改时，检查用户真实姓名在本机构是否重复
                if (StringUtils.isNotEmpty(sysUser.getUser_real_name())) {
                    if (!sysUser.getUser_real_name().equals(user.getUser_real_name())) {
                        SysUser userInfoByUserRealName = userService.findByUserRealName(sysUser.getUser_real_name(), sysUser.getOrg_id());
                        if (userInfoByUserRealName != null) {
                            return JsonResult.getError(Constants.USER_REAL_NAME_EXIST);
                        }
                    }
                }
                SysUser data = userService.updateUserAndRel(sysUser);
                data.setUser_pwd("");
                JsonResult jsonResult = JsonResult.getSuccess(Constants.ACTION_UPDATE);
                jsonResult.setData(new ArrayList());
                return jsonResult;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 删除用户
     *
     * @param request
     * @param delUser
     * @return
     */
    @RequestMapping("/delete")
    @ResponseBody
    public Object delUser(HttpServletRequest request, @RequestBody IdsCondition delUser) {
        String ids = Arrays.toString(delUser.getIds());
        String user_ids = ids.substring(1, ids.length() - 1);
        SysUser sysUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));// 当前登录用户
        if (sysUser == null) {
            return JsonResult.getExpire(MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code()));
        }
        try {
            //检查是否包含其他机构用户
            List<SysUser> other_users = userService.getOtherUserByIds(user_ids, sysUser.getOrg_id());
            if (other_users.size() > 0) {
                return JsonResult.getError("其他机构用户不能删除！");
            }
            //检查是否包含系统管理员用户
            List<SysUser> admin_users = userService.getAdminUserByIds(user_ids);
            if (admin_users.size() > 0) {
                return JsonResult.getError(Constants.USER_NOT_DELETE);
            }
            userService.updateIsDelete(user_ids);
            List<SysUserRoleRel> list = userRoleRelService.getRelListByUser(user_ids);
            if (list.size() > 0) {
                userRoleRelService.updateIsDeleteByUsers(user_ids);
            }
            JsonResult result = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.DEL_SUCCESS.code()));
            result.setData(new ArrayList());
            return result;
        } catch (Exception e) {
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }

    }

    /**
     * 修改密码
     *
     * @param request
     * @param pwd
     * @return
     */
    @RequestMapping("/updatePwd")
    @ResponseBody
    public Object updatePwd(HttpServletRequest request, @RequestBody PassWordBean pwd) {
        SysUser sysUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));// 当前登录用户
        if (sysUser == null) {
            return JsonResult.getExpire(MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code()));
        }
        try {
            //检查旧密码输入是否正确
            String oldPwd = PasswordUtil.entryptPassword(pwd.getOldpwd());
            SysUser user = userService.selectById(sysUser.getUser_id());
            if (!oldPwd.equals(user.getUser_pwd())) {
                return JsonResult.getError(Constants.OLDPWD_ERROR);
            }
            //更新密码
            String newPwd = PasswordUtil.entryptPassword(pwd.getNewpwd());
            user.setUser_pwd(newPwd);
            userService.updateUser(user);
            JsonResult result = JsonResult.getSuccess(Constants.ACTION_UPDATE);
            //返回值里面去掉密码
            sysUser.setUser_pwd("");
            result.setData(user);
            return result;
        } catch (Exception e) {
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }


    /**
     * 启用停用用户
     *
     * @param request
     * @param sysUser
     * @return
     */
    @RequestMapping("/enabled")
    @ResponseBody
    public Object enabled(HttpServletRequest request, @RequestBody IdsCondition sysUser) {
        try {
            SysUser sUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));// 当前登录用户
            if (sUser == null) {
                return JsonResult.getExpire(MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code()));
            }
            userService.enabled(sysUser);
            JsonResult jsonResult = JsonResult.getSuccess(Constants.ACTION_UPDATE);
            jsonResult.setData(new ArrayList());
            return jsonResult;
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }


    /**
     * 绑定微信号和QQ号
     *
     * @param request
     * @return
     */
    @RequestMapping("/binding/wechat/and/qq")
    @ResponseBody
    public Object BindingWeChatQQ(HttpServletRequest request, @RequestBody UserListAllBean bean) {
        try {
            SysUser sUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));// 当前登录用户
            if (sUser == null) {
                return JsonResult.getExpire(MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code()));
            }
            if (StringUtils.isEmpty(bean.getQq()) && StringUtils.isEmpty(bean.getMicro_signal())) {
                return JsonResult.getExpire(MessageAPi.getMessage(MsgCodeEnum.EMPTY.code()));
            }
            userService.BindingWeChatQQ(sUser.getUser_id(), bean);
            JsonResult jsonResult = JsonResult.getSuccess("绑定成功！");
            jsonResult.setData(new ArrayList());
            return jsonResult;
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }
}
