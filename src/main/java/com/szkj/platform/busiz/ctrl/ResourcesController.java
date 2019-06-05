package com.szkj.platform.busiz.ctrl;

import com.szkj.platform.busiz.beans.DelResourcesBean;
import com.szkj.platform.busiz.beans.ResourcesBean;
import com.szkj.platform.busiz.domain.Resources;
import com.szkj.platform.busiz.enums.AuditStatusEnum;
import com.szkj.platform.busiz.service.ResourcesService;
import com.szkj.platform.core.page.PageList;
import com.szkj.platform.system.utils.MessageAPi;
import com.szkj.platform.system.utils.MsgCodeEnum;
import com.szkj.platform.utils.JsonResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by victor on 2018-03-26.
 */
@Controller
public class ResourcesController {

    @Autowired
    private ResourcesService resourcesService;

    /**
     * 待审核状态,不可修改
     */
    private static String WAIT_AUDIT = "waitAuditError";

    /**
     * 分页列表
     *
     * @param bean
     * @return
     */
    @PostMapping(value = "/api/busiz/res/getlist")
    @ResponseBody
    public Object getList(@RequestBody ResourcesBean bean) {
        try {
            Sort sort;
            //获取排序字段和排序规则
            if (StringUtils.isNotEmpty(bean.getSort_name()) && StringUtils.isNotEmpty(bean.getSort_rule())) {
                sort = new Sort("desc".equals(bean.getSort_rule()) ? Sort.Direction.DESC : Sort.Direction.ASC, bean.getSort_name());
            } else {
                //默认排序规则
                sort = new Sort(Sort.Direction.DESC, "create_time");
            }
            PageList data = resourcesService.pageQuery(bean, sort);
            JsonResult result = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
            result.setData(data);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 批量删除
     *
     * @param bean
     * @return
     */
    @RequestMapping(value = "/api/busiz/res/del")
    @ResponseBody
    public Object del(@RequestBody DelResourcesBean bean) {
        try {
            Long[] ids = bean.getIds();
            String ids_str = StringUtils.join(ids, ",");
            if (StringUtils.isNotEmpty(ids_str)) {
                resourcesService.delByIds(ids_str);
            }
            return JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.DEL_SUCCESS.code()));
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 资源变更状态
     *
     * @param bean
     * @return
     */
    @PostMapping(value = "/api/busiz/res/updatestatus")
    @ResponseBody
    public Object updateStatus(@RequestBody Resources bean) {
        try {
            if (bean == null || bean.getRes_id() == null || bean.getAudit_status() == null) {
                return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.ACTION_ERROR.code()));
            }
            Resources res = resourcesService.selectById(bean.getRes_id());
            if (res == null) {
                return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.ACTION_ERROR.code()));
            }
            return resourcesService.updateStatus(bean.getRes_id(), bean.getAudit_status(), res.getAudit_status());
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 详情
     *
     * @param bean
     * @return
     */
    @PostMapping(value = "/api/busiz/res/detail")
    @ResponseBody
    public Object getDetail(@RequestBody Resources bean) {
        try {
            if (bean == null || bean.getRes_id() == null) {
                return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.ACTION_ERROR.code()));
            }
            Resources res = resourcesService.selectById(bean.getRes_id());
            if (res == null) {
                return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.ACTION_ERROR.code()));
            }
            if (StringUtils.isNotEmpty(res.getRes_tag_ids())) {
                res.setTag_ids(res.getRes_tag_ids().split(","));
            }
            if (StringUtils.isNotEmpty(res.getRes_tag_names())) {
                res.setTag_names(res.getRes_tag_names().split(","));
            }
            JsonResult result = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
            result.setData(res);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 资源更新
     *
     * @param bean
     * @return
     */
    @PostMapping(value = "/api/busiz/res/update")
    @ResponseBody
    public Object updateInfo(@RequestBody Resources bean) {
        try {
            if (bean == null || bean.getRes_id() == null) {
                return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.ACTION_ERROR.code()));
            }
            Resources res = resourcesService.selectById(bean.getRes_id());
            if (res == null) {
                return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.ACTION_ERROR.code()));
            }
            boolean flag = res.getAudit_status() != null &&
                    res.getAudit_status().intValue() == AuditStatusEnum.WAIT.code();
            if (flag) {
                return JsonResult.getError(MessageAPi.getMessage(WAIT_AUDIT));
            }
            resourcesService.updateInfo(res, bean);
            return JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.UPDATE_SUCCESS.code()));
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

}
