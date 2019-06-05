package com.szkj.platform.system.ctrl;

import com.szkj.platform.core.page.PageList;
import com.szkj.platform.system.conditions.IdsCondition;
import com.szkj.platform.system.conditions.OrgCondition;
import com.szkj.platform.system.domain.Organization;
import com.szkj.platform.system.domain.SysUser;
import com.szkj.platform.system.service.OrganizationService;
import com.szkj.platform.system.service.SystemlApiHelper;
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
import java.util.List;

/**
 * @author Administrator
 * @date 2016/10/25
 */
@Controller
@RequestMapping("/api/system/organization")
public class OrganizationController {

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private UserService userService;

    /**
     * 新增, 修改机构
     *
     * @return
     */
    @RequestMapping("/save")
    @ResponseBody
    public Object save(HttpServletRequest request, @RequestBody Organization org) {
        // 验证当前登录用户
        SysUser sysUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (sysUser == null) {
            return JsonResult.getExpire(MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code()));
        }
        //验证机构名称不能为空
        if (org.getOrg_name() == null) {
            return JsonResult.getError(MessageAPi.getMessage("orgNameNull"));
        }
        //验证机构简称不能为空
        if (org.getOrg_code() == null) {
            return JsonResult.getError(MessageAPi.getMessage("orgCodeNull"));
        }
        //验证长度
        if (org.getOrg_name().length() > 50) {
            return JsonResult.getError(MessageAPi.getMessage("orgNameLonger"));
        }
        if (org.getOrg_code().length() > 20) {
            return JsonResult.getError(MessageAPi.getMessage("orgCodeLonger"));
        }
        if (org.getOffice_address() != null && org.getOffice_address().length() > 200) {
            return JsonResult.getError(MessageAPi.getMessage("orgAddressLonger"));
        }
        if (org.getLink_man() != null && org.getLink_man().length() > 40) {
            return JsonResult.getError(MessageAPi.getMessage("orgLinkManLonger"));
        }
        if (org.getLink_mobile() != null && org.getLink_mobile().length() > 40) {
            return JsonResult.getError(MessageAPi.getMessage("orgLinkLonger"));
        }
        if (org.getRemark() != null && org.getRemark().length() > 500) {
            return JsonResult.getError(MessageAPi.getMessage("orgRemarkLonger"));
        }
        try {
            //如果传入机构id为空则为新增
            if (org.getOrg_id() == null) {
                //新增
                //检查机构名和机构简称是否已存在
                if (organizationService.selectOrgByName(org.getOrg_name(), 0l) != null) {
                    return JsonResult.getError(MessageAPi.getMessage("orgNameHave"));
                }
                if (organizationService.selectOrgByCode(org.getOrg_code(), 0l) != null) {
                    return JsonResult.getError(MessageAPi.getMessage("orgCodeHave"));
                }
                //机构简称转大写
                String org_code = org.getOrg_code().toUpperCase();
                org.setOrg_code(org_code);
                org.setCreate_user_id(sysUser.getUser_id());
                org.setCreate_user_name(sysUser.getUser_name());

                organizationService.saveOrganization(org);
                JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage("addSuccess"));
                //返回新增的机构信息
                //Long orgId = organizationService.getMaxOrgId();
                //Organization data = organizationService.selectOrgById(orgId);
                //jsonResult.setData(data);
                return jsonResult;
            } else {
                //传入机构不为空则先查是否存在该机构
                Organization organization = organizationService.selectOrgById(org.getOrg_id());
                if (organization == null) {
                    return JsonResult.getError(MessageAPi.getMessage("orgNotExist"));
                }
                //检查机构名和机构简称是否已存在
                if (organizationService.selectOrgByName(org.getOrg_name(), org.getOrg_id()) != null) {
                    return JsonResult.getError(MessageAPi.getMessage("orgNameHave"));
                }
                if (organizationService.selectOrgByCode(org.getOrg_code(), org.getOrg_id()) != null) {
                    return JsonResult.getError(MessageAPi.getMessage("orgCodeHave"));
                }

                //更新该机构信息
                organizationService.updateOrganization(organization, org);
                JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage("updateSuccess"));
                //返回修改的机构信息
                //Organization data = organizationService.selectOrgById(organization.getOrg_id());
                //jsonResult.setData(data);
                return jsonResult;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }

    }

    /**
     * 机构启用停用
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
            String org_ids = StringUtils.join(enabledOrg.getIds(), ",");
            //传入机构不为空则先查是否存在该机构
            List<Organization> orgs = organizationService.getOrgsByIds(org_ids);
            if (orgs == null || orgs.size() == 0) {
                return JsonResult.getError(MessageAPi.getMessage("orgNotExist"));
            }
            for (Organization org : orgs){
                if (org.getOrg_id() == 1 && enabledOrg.getEnabled() == 2){
                    return JsonResult.getError("主机构不允许停用！");
                }
            }
            //更新该机构启用停用信息
            organizationService.updateOrgEnabled(org_ids, enabledOrg.getEnabled());
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage("updateSuccess"));
            return jsonResult;

        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 删除机构
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
        try {
            //获取删除机构的ids
            if (delOrg.getIds() != null) {
                for (int i = 0; i < delOrg.getIds().length;i++){
                    if (delOrg.getIds()[i] == 1){
                        return JsonResult.getError("主机构无法删除！");
                    }
                }
            }
            String org_ids = StringUtils.join(delOrg.getIds(), ",");
            organizationService.deleteOrgs(org_ids);
//			if(msg == "failed"){
//				return JsonResult.getError(WeiboConstants.DELETE_USER_REL);
//			}else{
            JsonResult result = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.DEL_SUCCESS.code()));
            return result;
//			}
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 机构管理列表
     *
     * @return
     */
    @RequestMapping("/getList")
    @ResponseBody
    public Object getList(HttpServletRequest request, @RequestBody OrgCondition condition) {
        // 当前登录用户
        SysUser sysUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (sysUser == null) {
            return JsonResult.getExpire(MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code()));
        }
        try {
            Sort sort;
            if (StringUtils.isNotEmpty(condition.getSort_name()) && StringUtils.isNotEmpty(condition.getSort_rule())) {
                sort = new Sort("desc".equals(condition.getSort_rule()) ? Sort.Direction.DESC : Sort.Direction.ASC, "" + condition.getSort_name() + "");
            } else {
                //默认查询规则
                sort = new Sort(Sort.Direction.DESC, "create_time");
            }
            //检查当前用户是否为超级管理员
            if (SystemlApiHelper.isLoginRootOrg(sysUser)) {
                //分页查询
                PageList result = (PageList) organizationService.pageQuery(sort, condition);
                JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
                jsonResult.setData(result);
                return jsonResult;
            } else {
                Long org_id;
                //传入机构id为空则取当前用户所属机构
                if (condition.getOrg_id() == null) {
                    org_id = sysUser.getOrg_id();
                } else {
                    org_id = condition.getOrg_id();
                }
                //查询
                Organization org = organizationService.selectOrgById(org_id);
                JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
                jsonResult.setData(org);
                return jsonResult;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }
}
