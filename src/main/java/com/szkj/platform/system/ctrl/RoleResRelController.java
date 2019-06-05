package com.szkj.platform.system.ctrl;

import com.szkj.platform.core.page.PageList;
import com.szkj.platform.system.beans.RoleResRelBean;
import com.szkj.platform.system.beans.SysRoleResRelBean;
import com.szkj.platform.system.constants.Constants;
import com.szkj.platform.system.domain.SysResource;
import com.szkj.platform.system.domain.SysRoleResRel;
import com.szkj.platform.system.domain.SysUser;
import com.szkj.platform.system.service.RoleResRelService;
import com.szkj.platform.system.service.SysResourceService;
import com.szkj.platform.system.service.UserService;
import com.szkj.platform.system.utils.CheckUserHelper;
import com.szkj.platform.system.utils.ListUtils;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 权限管理控制器
 * Created by Administrator on 2016/11/9 0009.
 */
@Controller
public class RoleResRelController {

    @Autowired
    private RoleResRelService roleResRelService;

    @Autowired
    private SysResourceService sysResourceService;

    @Autowired
    private UserService userService;

    /**
     * 权限列表 列表数据
     *
     * @param request
     */
    @RequestMapping(value = "/api/system/role/res/rel/list", method = {RequestMethod.POST})
    @ResponseBody
    public Object sysRoleResRel_list(HttpServletRequest request, @RequestBody RoleResRelBean relBean) {
        try {
            SysUser sysUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));
            if (sysUser == null) {
                return JsonResult.getExpire(MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code()));
            }
            Sort sort = null;
            //获取排序字段和排序规则
            if (StringUtils.isNotEmpty(relBean.getSort_name()) && StringUtils.isNotEmpty(relBean.getSort_rule())) {
                sort = new Sort("desc".equals(relBean.getSort_rule()) ? Sort.Direction.DESC : Sort.Direction.ASC, "" + relBean.getSort_name() + "");
            } else {
                //默认排序规则
                sort = new Sort(Sort.Direction.DESC, "res_id");
            }
            PageList data = (PageList) roleResRelService.pageQuery(sort, relBean, sysUser.getOrg_id());
            //PageList data = (PageList)roleResRelService.pageQuery(sort, relBean, 1L );
            JsonResult result = JsonResult.getSuccess("获取数据成功！");
            result.setData(data);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JsonResult.getError("获取数据失败！");
    }

    /**
     * 增加/删除菜单权限——单条
     *
     * @param request
     * @param relBean
     * @return
     */
    @RequestMapping(value = "/api/system/role/res/rel/save", method = {RequestMethod.POST})
    @ResponseBody
    public Object update_sysRoleResRel(HttpServletRequest request, @RequestBody RoleResRelBean relBean) {
        try {
            SysUser sysUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));
            if (sysUser == null) {
                return JsonResult.getExpire(MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code()));
            }
            if (relBean.getRole_res_rel_id() == null) {
                SysResource resource = sysResourceService.selectByResId(relBean.getRes_id());
                String full_path = resource.getFull_path();
                String[] res_ids = full_path.substring(0, full_path.length()).split("\\|");
                List<String> res_id_list = Arrays.asList(res_ids);
                List<String> own_res_id_list = new ArrayList<String>();
                List<SysRoleResRel> roleResRels = roleResRelService.getRelListByRole(relBean.getRole_id().toString());
                for (SysRoleResRel roleResRel : roleResRels) {
                    own_res_id_list.add(roleResRel.getRes_id().toString());
                }
                res_id_list = ListUtils.diff(res_id_list, own_res_id_list);

                List<SysRoleResRel> sysRoleResRels = new ArrayList<SysRoleResRel>();
                for (int i = 1; i < res_id_list.size(); i++) {
                    SysRoleResRel sysRoleResRel = new SysRoleResRel();
                    sysRoleResRel.setRole_id(relBean.getRole_id());
                    sysRoleResRel.setRes_id(Long.valueOf(res_id_list.get(i)));
                    sysRoleResRel.setEnabled(1);
                    sysRoleResRel.setCreate_time(new Date());
                    sysRoleResRel.setUpdate_time(new Date());
                    sysRoleResRels.add(sysRoleResRel);
                }
                roleResRelService.saveRels(sysRoleResRels);
            } else {
                SysRoleResRel roleResRel = roleResRelService.selectById(relBean.getRole_res_rel_id());
                SysResource resource = sysResourceService.selectByResId(roleResRel.getRes_id());
                String full_path = resource.getFull_path();
                List<SysResource> resources = sysResourceService.selectSysMenuByFullPath(full_path);
                String res_ids = "";
                for (int i = 0; i < resources.size(); i++) {
                    if (i != resources.size() - 1) {
                        res_ids += resources.get(i).getRes_id() + ",";
                    } else {
                        res_ids += resources.get(i).getRes_id();
                    }
                }
                roleResRelService.deleteByRoleIdAndResIds(res_ids, roleResRel.getRole_id());
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
     * 启用/停用 菜单权限——单条,多条
     * 当前启用，上级全部启用，下级不变
     * 当前停用，上级不变，下级全部停用
     *
     * @param request
     * @param relBean
     * @return
     */
    @RequestMapping(value = "/api/system/role/res/rel/enabled", method = {RequestMethod.POST})
    @ResponseBody
    public Object update_relEnabled(HttpServletRequest request, @RequestBody RoleResRelBean relBean) {
        SysUser sysUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (sysUser == null) {
            return JsonResult.getExpire(MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code()));
        }
        try {
            //如果关系id为空或关系id不存在则新增关系
            if (relBean.getRole_res_rel_id() == null || roleResRelService.selectById(relBean.getRole_res_rel_id()) == null) {
                List<SysRoleResRel> roleResRels = new ArrayList<SysRoleResRel>();
//                SysRoleResRel sysRoleResRel = new SysRoleResRel();
//                sysRoleResRel.setRole_id(relBean.getRole_id());
//                sysRoleResRel.setRes_id(relBean.getRes_id());
//                sysRoleResRel.setEnabled(1);
//                sysRoleResRel.setCreate_time(new Date());
//                sysRoleResRel.setUpdate_time(new Date());
//                roleResRels.add(sysRoleResRel);
                //检查上级是否被启用（检查上级是否存在关系）
                List<SysResource> sysResources = sysResourceService.selectParentByResId(relBean.getRes_id());
                if (sysResources != null) {
                    for (SysResource sysResource : sysResources) {
                        SysRoleResRel sysRoleResRel1 = roleResRelService.selectByRoleAndRelId(relBean.getRole_id(), sysResource.getRes_id());
                        if (sysRoleResRel1 != null) {
                            //如果存在关系则改为启用
                            roleResRelService.updateRelEnabled(sysRoleResRel1.getRole_res_rel_id(), 1);
                        } else {
                            //如果不存在则创建关系
                            SysRoleResRel sysRoleResRel2 = new SysRoleResRel();
                            sysRoleResRel2.setRole_id(relBean.getRole_id());
                            sysRoleResRel2.setRes_id(sysResource.getRes_id());
                            sysRoleResRel2.setEnabled(1);
                            sysRoleResRel2.setCreate_time(new Date());
                            sysRoleResRel2.setUpdate_time(new Date());
                            roleResRels.add(sysRoleResRel2);
                        }
                    }
                }
                roleResRelService.saveRels(roleResRels);
            } else {
                //单条启用停用
                if (relBean.getEnabled() == 1) {
                    relBean.setEnabled(2);
                    //停用则停用所有下级
                    List<SysResource> sysResources = sysResourceService.selectChildByResId(relBean.getRes_id());
                    if (sysResources != null) {
                        for (SysResource sysResource : sysResources) {
                            SysRoleResRel sysRoleResRel1 = roleResRelService.selectByRoleAndRelId(relBean.getRole_id(), sysResource.getRes_id());
                            if (sysRoleResRel1 != null) {
                                //如果存在关系则改为停用
                                roleResRelService.updateRelEnabled(sysRoleResRel1.getRole_res_rel_id(), 2);
                            }
                        }
                    }
                } else {
                    relBean.setEnabled(1);
                    //检查上级是否被启用
                    List<SysResource> sysResources = sysResourceService.selectParentByResId(relBean.getRes_id());
                    if (sysResources != null) {
                        List<SysRoleResRel> roleResRels = new ArrayList<SysRoleResRel>();
                        for (SysResource sysResource : sysResources) {
                            //（检查上级是否存在关系）
                            SysRoleResRel sysRoleResRel1 = roleResRelService.selectByRoleAndRelId(relBean.getRole_id(), sysResource.getRes_id());
                            if (sysRoleResRel1 != null) {
                                //如果存在关系则改为启用
                                roleResRelService.updateRelEnabled(sysRoleResRel1.getRole_res_rel_id(), 1);
                            } else {
                                //如果不存在则创建关系
                                SysRoleResRel sysRoleResRel2 = new SysRoleResRel();
                                sysRoleResRel2.setRole_id(relBean.getRole_id());
                                sysRoleResRel2.setRes_id(sysResource.getRes_id());
                                sysRoleResRel2.setEnabled(1);
                                sysRoleResRel2.setCreate_time(new Date());
                                sysRoleResRel2.setUpdate_time(new Date());
                                roleResRels.add(sysRoleResRel2);
                            }
                        }
                        if (roleResRels!=null&&roleResRels.size()>0){
                            roleResRelService.saveRels(roleResRels);
                        }
                    }
                }
                roleResRelService.updateRelEnabled(relBean.getRole_res_rel_id(), relBean.getEnabled());
            }
            //如果传入多条则启用停用多条数据
            /*if (relBean.getRole_res_rel_ids()!=null){
                String role_res_rel_ids = StringUtils.join(relBean.getRole_res_rel_ids(), ",");
                roleResRelService.updateRelEnableds(role_res_rel_ids,relBean.getEnabled());
                JsonResult result = JsonResult.getSuccess("更新数据成功");
                result.setData(relBean);
                return result;
            }*/
            JsonResult result = JsonResult.getSuccess("更新数据成功");
            result.setData(relBean);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JsonResult.getError("更新数据失败！");
    }

    /**
     * 增加/删除菜单权限——多条
     *
     * @param request
     * @param relBean
     * @return
     */
    @RequestMapping(value = "/api/system/role/res/rel/update/saves", method = {RequestMethod.POST})
    @ResponseBody
    public Object update_sysRoleResRels(HttpServletRequest request, @RequestBody SysRoleResRelBean relBean) {
        SysUser sysUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (sysUser == null) {
            return JsonResult.getExpire(MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code()));
        }
        try {
            roleResRelService.deteleByRole(relBean.getRole_id());
            List<SysRoleResRelBean.RelIds> relIdsList = relBean.getRel_ids();
            List<SysRoleResRel> roleResRels = new ArrayList<SysRoleResRel>();
            for (int i = 0; i < relIdsList.size(); i++) {
                SysRoleResRel sysRoleResRel = new SysRoleResRel();
                sysRoleResRel.setRole_id(relBean.getRole_id());
                sysRoleResRel.setRes_id(relIdsList.get(i).res_id);
                sysRoleResRel.setEnabled(1);
                sysRoleResRel.setCreate_time(new Date());
                sysRoleResRel.setUpdate_time(new Date());
                roleResRels.add(sysRoleResRel);
            }
            roleResRelService.saveRels(roleResRels);
            JsonResult result = JsonResult.getSuccess(Constants.ACTION_UPDATE);
            result.setData(new ArrayList());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JsonResult.getError("更新数据失败！");
    }
}
