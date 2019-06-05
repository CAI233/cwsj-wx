package com.szkj.platform.system.ctrl;

import com.szkj.platform.system.beans.DelOrgBean;
import com.szkj.platform.system.beans.DepartmentBean;
import com.szkj.platform.system.conditions.IdsCondition;
import com.szkj.platform.system.domain.Department;
import com.szkj.platform.system.domain.SysUser;
import com.szkj.platform.system.service.DepartmentService;
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
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/10/25.
 */
@Controller
@RequestMapping("/api/system/department")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private UserService userService;

    /**
     * 组织树
     * 用户管理下拉的组织树
     *
     * @return
     */
    @RequestMapping("/getList")
    @ResponseBody
    public Object getList(HttpServletRequest request, @RequestBody DelOrgBean delOrg) {
        //验证当前登录用户
        SysUser sysUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (sysUser == null) {
            return JsonResult.getExpire(MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code()));
        }
        try {
            Long org_id;
            Long dept_id;
            //得到机构id
            if (delOrg.getOrg_id() != null) {
                org_id = delOrg.getOrg_id();
            } else {
                org_id = sysUser.getOrg_id();
            }
            //得到传入组织id
            if (delOrg.getDept_id() != null) {
                dept_id = delOrg.getDept_id();
            } else {
                dept_id = null;
            }
            List<DepartmentBean> depts = departmentService.selectDeptByOrgId(org_id);
            if (depts == null || depts.size() == 0) {
                return JsonResult.getError(MessageAPi.getMessage("deptNotExist"));
            }
            //如果组织id为null则查看所有组织层级
            if (dept_id == null) {
                List<DepartmentBean> list = departmentService.getOrgDeptTree(depts);
                JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
                jsonResult.setData(list);
                return jsonResult;
            } else {//如果组织id不为空则获取组织层级
                DepartmentBean dept = departmentService.selectDeptById(dept_id);
                if (depts.size() > 0) {
                    departmentService.getDeptTree(dept, depts);
                }
                List<DepartmentBean> list = new ArrayList<DepartmentBean>();
                list.add(dept);
                JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
                jsonResult.setData(list);
                return jsonResult;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 新增, 修改组织
     *
     * @return
     */
    @RequestMapping("/save")
    @ResponseBody
    public Object save(HttpServletRequest request, @RequestBody Department dept) {
        // 验证当前登录用户
        SysUser sysUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (sysUser == null) {
            return JsonResult.getExpire(MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code()));
        }
        //组织名称不能为空
        if (dept.getDept_name() == null) {
            return JsonResult.getError(MessageAPi.getMessage("deptNameNull"));
        }
        //验证长度
        if (dept.getDept_name().length() > 50) {
            return JsonResult.getError(MessageAPi.getMessage("deptNameLonger"));
        }
        if (dept.getDept_code() != null && dept.getDept_code().length() > 20) {
            return JsonResult.getError(MessageAPi.getMessage("deptCodeLonger"));
        }
        if (dept.getAddress() != null && dept.getAddress().length() > 200) {
            return JsonResult.getError(MessageAPi.getMessage("deptAddressLonger"));
        }
        if (dept.getRemark() != null && dept.getRemark().length() > 500) {
            return JsonResult.getError(MessageAPi.getMessage("deptRemarkLonger"));
        }

        try {
            //机构id不能为空
            if (dept.getOrg_id() == null) {
                return JsonResult.getError(MessageAPi.getMessage("parentOrgNull"));
            }
            //如果组织id为空则为新增组织
            if (dept.getDept_id() == null) {

                Long deptPid = dept.getDept_pid();
                //验证该机构下没有同名组织
                Department department = departmentService.checkDeptName(dept.getOrg_id(), dept.getDept_name(), 0l);
                if (department != null) {
                    return JsonResult.getError(MessageAPi.getMessage("deptNameHave"));
                }
                //新增组织必须要pid
                if (deptPid != null) {
                    if (deptPid == 0l) {
                        //禁止在机构下直接创建组织，必须在根组织下创建
                        return JsonResult.getError(MessageAPi.getMessage("parentDeptNull"));
                    }
                    //如果有pid则验证上级组织是否存在
                    if (departmentService.checkPDept(dept.getOrg_id(), deptPid) == null) {
                        return JsonResult.getError(MessageAPi.getMessage("parentDeptNull"));
                    }
                } else {
                    return JsonResult.getError(MessageAPi.getMessage("parentDeptNull"));
                }
                departmentService.saveDepartment(dept);
                JsonResult jsonResult = new JsonResult().getSuccess(MessageAPi.getMessage("addSuccess"));
                //返回新增的数据
                //Long deptId = departmentService.getMaxDeptId();
                //Department data = departmentService.selectById(deptId);
                //jsonResult.setData(data);
                return jsonResult;
            } else {//不为空则为修改组织
                //判断该组织是否存在
                Department department = departmentService.selectById(dept.getDept_id());
                if (department == null) {
                    return JsonResult.getError(MessageAPi.getMessage("deptNotExist"));
                }
                //验证该机构下没有同名组织
                if (departmentService.checkDeptName(dept.getOrg_id(), dept.getDept_name(), dept.getDept_id()) != null) {
                    return JsonResult.getError(MessageAPi.getMessage("deptNameHave"));
                }
                //获取当前修改的组织本身和下级组织
                List<Department> ownerDepts = departmentService.getOwnerDepts(dept.getDept_path());
                for (Department ownerDept : ownerDepts) {
                    if (ownerDept.getDept_id().equals(dept.getDept_pid())) {
                        return JsonResult.getError(MessageAPi.getMessage("classPidError"));
                    }
                }
                department.setDept_pid(dept.getDept_pid());
                department.setDept_name(dept.getDept_name());
                department.setDept_code(dept.getDept_code());
                department.setRemark(dept.getRemark());
                department.setAddress(dept.getAddress());
                String msg = departmentService.updateDepartment(department);
                if (msg.equals("failed")) {
                    return JsonResult.getError(MessageAPi.getMessage("classPidError"));
                }
                JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage("updateSuccess"));
                //返回修改数据
                //Department data = departmentService.selectById(department.getDept_id());
                //jsonResult.setData(data);
                return jsonResult;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }

    }

    /**
     * 组织启用停用
     *
     * @return
     */
    @RequestMapping("/setEnabled")
    @ResponseBody
    public Object setEnabled(HttpServletRequest request, @RequestBody IdsCondition enabledOrg) {
        // 验证当前登录用户
        SysUser sysUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (sysUser == null) {
            return JsonResult.getExpire(MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code()));
        }
        try {
            String dept_ids = StringUtils.join(enabledOrg.getIds(), ",");
            //更新该组织启用停用信息
            departmentService.updateDeptEnabled(dept_ids, enabledOrg.getEnabled());
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage("updateSuccess"));
            return jsonResult;

        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }

    }

    /**
     * 删除组织
     *
     * @return
     */
    @RequestMapping("/delete")
    @ResponseBody
    public Object delete(HttpServletRequest request, @RequestBody IdsCondition delOrg) {
        // 验证当前登录用户
        SysUser sysUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (sysUser == null) {
            return JsonResult.getExpire(MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code()));
        }
        String dept_ids = StringUtils.join(delOrg.getIds(), ",");
        try {
            String msg = departmentService.deleteDepts(dept_ids);
            if (msg == "failed") {
                return JsonResult.getError(MessageAPi.getMessage("delFailed"));
            } else {
                JsonResult result = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.DEL_SUCCESS.code()));
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }
}
