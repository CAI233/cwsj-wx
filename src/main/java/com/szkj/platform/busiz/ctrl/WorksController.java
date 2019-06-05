package com.szkj.platform.busiz.ctrl;

import com.szkj.platform.busiz.beans.WorksListBean;
import com.szkj.platform.busiz.constants.Constants;
import com.szkj.platform.busiz.domain.*;
import com.szkj.platform.busiz.enums.AuditStatusEnum;
import com.szkj.platform.busiz.service.QRCodeService;
import com.szkj.platform.busiz.service.WorksService;
import com.szkj.platform.busiz.utils.AccessTokenUtil;
import com.szkj.platform.system.conditions.IdsCondition;
import com.szkj.platform.system.domain.SysUser;
import com.szkj.platform.system.service.BaseService;
import com.szkj.platform.system.utils.*;
import com.szkj.platform.utils.JsonResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/api/busiz")
public class WorksController {

    @Autowired
    private WorksService worksService;
    @Autowired
    private QRCodeService qrCodeService;

    @Autowired
    private BaseService baseService;

    @Value("${image_save_path}")
    private String image_save_path;

    @Value("${image_web_path}")
    private String image_web_path;

    /**
     * 作品分页列表
     *
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/works/list")
    @ResponseBody
    public Object getList(HttpServletRequest request, @RequestBody WorksListBean bean) {
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try {
            Sort sort = null;
            //获取排序字段和排序规则
            if (StringUtils.isNotEmpty(bean.getSort_name()) && StringUtils.isNotEmpty(bean.getSort_rule())) {
                sort = new Sort("desc".equals(bean.getSort_rule()) ? Sort.Direction.DESC : Sort.Direction.ASC, "" + bean.getSort_name() + "");
            } else {
                //默认排序规则
                sort = new Sort(Sort.Direction.DESC, "create_time");
            }
            Object result = worksService.getList(bean, sort);
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
            jsonResult.setData(result);
            return jsonResult;
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 新增、修改作品
     *
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/works/save")
    @ResponseBody
    public Object save(HttpServletRequest request, @RequestBody Works bean) {
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try {
            if (bean.getWorks_cat_id() == null) {
                return JsonResult.getError("请选择分类！");
            }
            JsonResult jsonResult = JsonResult.getSuccess("");
            if (bean.getWorks_id() == null) {
                //新增
                Works works = worksService.selectWorksByName(bean.getWorks_name());
                if (works != null) {
                    return JsonResult.getError("作品名称已存在！");
                }
                //处理压缩图片
                if (StringUtils.isNotEmpty(bean.getWorks_cover())) {
                    String works_cover_small = baseService.getImgScale(bean.getWorks_cover(), "small", 0.5);
                    bean.setWorks_cover_small(works_cover_small);
                }
                Works work = worksService.saveWorks(bean);
                jsonResult.setData(work);
                jsonResult.setMessage(MessageAPi.getMessage(MsgCodeEnum.ADD_SUCCESS.code()));
                return jsonResult;
            } else {
                //修改
                Works work = worksService.selectWorksByNameOutId(bean.getWorks_name(), bean.getWorks_id());
                if (work != null) {
                    return JsonResult.getError("作品名称已存在！");
                }
                //处理压缩图片
                Works works = worksService.selectById(bean.getWorks_id());
                if (StringUtils.isNotEmpty(bean.getWorks_cover()) && !bean.getWorks_cover().equals(works.getWorks_cover())) {
                    String works_cover_small = baseService.getImgScale(bean.getWorks_cover(), "small", 0.5);
                    bean.setWorks_cover_small(works_cover_small);
                    //如果二维码存在关系则改变该wx图片地址
                    if (StringUtils.isNotEmpty(bean.getWorks_wx_url())) {
                        //得到微信图片
                        String wxCoverUrl = AccessTokenUtil.getWXCoverUrl(bean.getWorks_cover());
                        bean.setWorks_wx_url(wxCoverUrl);
                    }
                }
                //非上架操作时判断审核状态
                if (bean.getStatus() == 2) {
                    if (works.getAudit_status() == AuditStatusEnum.REJECT.code() || works.getAudit_status() == AuditStatusEnum.PASS.code()) {
                        bean.setAudit_status(AuditStatusEnum.WAIT.code());
                    }
                }
                worksService.updateWorks(bean);
                jsonResult.setMessage(MessageAPi.getMessage(MsgCodeEnum.UPDATE_SUCCESS.code()));
                return jsonResult;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 删除作品
     *
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/works/del")
    @ResponseBody
    public Object del(HttpServletRequest request, @RequestBody IdsCondition bean) {
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try {
            if (bean.getIds() == null) {
                return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
            }
            String ids = StringUtils.join(bean.getIds(), ",");
            worksService.delWorks(ids);
            //删除二维码作品关系
            qrCodeService.removeCodeConfigByWorksIds(ids);
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.DEL_SUCCESS.code()));
            return jsonResult;
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 更改审核状态
     *
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/works/audit")
    @ResponseBody
    public Object audit(HttpServletRequest request, @RequestBody IdsCondition bean) {
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try {
            if (bean.getIds() == null) {
                return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
            }
            if (bean.getAudit_status() == null) {
                bean.setAudit_status(Constants.AUDIT_WAIT);
            }
            String ids = StringUtils.join(bean.getIds(), ",");
            worksService.updateAudit(ids, bean.getAudit_status());
            JsonResult jsonResult = JsonResult.getSuccess("操作成功！");
            return jsonResult;
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 教育表格信息
     *
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/works/form/list")
    @ResponseBody
    public Object formList(HttpServletRequest request, @RequestBody Works bean) {
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try {
            List<WorksForm> list = worksService.getFromListById(bean.getWorks_id());
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
            jsonResult.setData(list);
            return jsonResult;
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 保存教育表格单个内容
     *
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/works/form/save")
    @ResponseBody
    public Object saveForm(HttpServletRequest request, @RequestBody WorksForm bean) {
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try {
            if (bean.getWorks_id() == null) {
                return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
            }
            if (StringUtils.isEmpty(bean.getAnswer()) && bean.getResources_id() == null) {
                return JsonResult.getError("请设置答复内容！");
            }
            if (bean.getId() == null) {
                //新增
                WorksForm form = worksService.saveForm(bean);
                JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.ADD_SUCCESS.code()));
                jsonResult.setData(form);
                return jsonResult;
            } else {
                //修改
                worksService.updateForm(bean);
                return JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.UPDATE_SUCCESS.code()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 删除教育表格内容
     *
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/works/form/del")
    @ResponseBody
    public Object delForm(HttpServletRequest request, @RequestBody WorksForm bean) {
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try {
            if (bean.getId() == null) {
                return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.DEL_SUCCESS.code()));
            }
            worksService.delForm(bean.getId());
            return JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.DEL_SUCCESS.code()));
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.DEL_SUCCESS.code()));
        }
    }


    /**
     * 资源包信息
     *
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/works/resources/list")
    @ResponseBody
    public Object resourcesList(HttpServletRequest request, @RequestBody Works bean) {
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try {
            List<WorksResources> list = worksService.getResourcesList(bean.getWorks_id());
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
            jsonResult.setData(list);
            return jsonResult;
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 添加资源包内容
     *
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/works/resources/save")
    @ResponseBody
    public Object saveResources(HttpServletRequest request, @RequestBody WorksResources bean) {
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try {
            if (bean.getWorks_id() == null) {
                return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
            }
            if (bean.getId() == null) {
                WorksResources res = worksService.selectResources(bean.getResources_id(), bean.getWorks_id());
                if (res != null) {
                    return JsonResult.getError("资源已存在！");
                }
                //新增
                WorksResources resources = worksService.saveResources(bean);
                JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.ADD_SUCCESS.code()));
                jsonResult.setData(resources);
                return jsonResult;
            } else {
                //修改
                worksService.updateResources(bean);
                return JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.UPDATE_SUCCESS.code()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 删除资源
     *
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/works/resources/del")
    @ResponseBody
    public Object delResources(HttpServletRequest request, @RequestBody WorksResources bean) {
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try {
            if (bean.getId() == null) {
                return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.DEL_SUCCESS.code()));
            }
            worksService.delResources(bean.getId());
            return JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.DEL_SUCCESS.code()));
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.DEL_SUCCESS.code()));
        }
    }

    /**
     * 题库试卷列表
     *
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/works/testpaper/list")
    @ResponseBody
    public Object testPaperList(HttpServletRequest request, @RequestBody Works bean) {
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try {
            List<WorksTestPaper> list = worksService.getTestPaperList(bean.getWorks_id());
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
            jsonResult.setData(list);
            return jsonResult;
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 试卷题目列表
     *
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/works/question/list")
    @ResponseBody
    public Object questionList(HttpServletRequest request, @RequestBody PaperQuestionRel bean) {
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try {
            List<Question> list = worksService.getPaperQuestionList(bean.getPaper_id());
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
            jsonResult.setData(list);
            return jsonResult;
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.DEL_SUCCESS.code()));
        }
    }

    /**
     * 保存试题试卷关系
     *
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/works/question/save")
    @ResponseBody
    public Object saveQuestion(HttpServletRequest request, @RequestBody PaperQuestionRel bean) {
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try {
            if (bean.getRel_id() == null) {
                PaperQuestionRel rel = worksService.selectQuestion(bean.getQuestion_id(), bean.getPaper_id());
                if (rel != null) {
                    return JsonResult.getError("试题已存在！");
                }
                //新增
                worksService.saveQuestion(bean);
                return JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.ADD_SUCCESS.code()));
            } else {
                //修改
                worksService.updateQuestion(bean);
                return JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.UPDATE_SUCCESS.code()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 删除试题
     *
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/works/question/del")
    @ResponseBody
    public Object delQuestion(HttpServletRequest request, @RequestBody PaperQuestionRel bean) {
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try {
            if (bean.getRel_id() == null) {
                return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
            }
            worksService.delQuestion(bean.getRel_id());
            return JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.DEL_SUCCESS.code()));
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 新增、修改题库试卷
     *
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/works/testpaper/save")
    @ResponseBody
    public Object saveTestPaper(HttpServletRequest request, @RequestBody WorksTestPaper bean) {
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try {
            if (bean.getId() == null) {
                //新增
                worksService.saveTestPaper(bean);
                return JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.ADD_SUCCESS.code()));
            } else {
                //修改
                worksService.updateTestPaper(bean);
                return JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.UPDATE_SUCCESS.code()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 题库删除试卷
     *
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/works/testpaper/del")
    @ResponseBody
    public Object delTestPaper(HttpServletRequest request, @RequestBody WorksTestPaper bean) {
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try {
            if (bean.getId() == null) {
                return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
            }
            worksService.delTestPaper(bean.getId());
            return JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.DEL_SUCCESS.code()));
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }
}
