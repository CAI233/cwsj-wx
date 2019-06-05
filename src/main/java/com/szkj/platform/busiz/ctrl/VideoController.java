package com.szkj.platform.busiz.ctrl;

import com.szkj.platform.busiz.beans.AduitVideoBean;
import com.szkj.platform.busiz.beans.DelVideoBean;
import com.szkj.platform.busiz.beans.VideoBean;
import com.szkj.platform.busiz.domain.Video;
import com.szkj.platform.busiz.domain.VideoResRel;
import com.szkj.platform.busiz.domain.WindowGoodsRel;
import com.szkj.platform.busiz.enums.AuditStatusEnum;
import com.szkj.platform.busiz.service.VideoResRelService;
import com.szkj.platform.busiz.service.VideoService;
import com.szkj.platform.system.constants.Constants;
import com.szkj.platform.system.domain.SysUser;
import com.szkj.platform.system.utils.CheckUserHelper;
import com.szkj.platform.system.utils.MessageAPi;
import com.szkj.platform.system.utils.MsgCodeEnum;
import com.szkj.platform.utils.JsonResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 视频管理
 * Created by Administrator on 2018/3/23 0023.
 */
@Controller
@RequestMapping(value = "/api/busiz/video/")
public class VideoController {


    @Autowired
    private VideoService videoService;

    @Autowired
    private VideoResRelService videoResRelService;

    /**
     * 视频分页列表
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "getlist")
    @ResponseBody
    public Object getlist(HttpServletRequest request, @RequestBody VideoBean bean){
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
            Object object = videoService.pageQuery(bean, sort);
            JsonResult result = JsonResult.getSuccess(MsgCodeEnum.LOAD_SUCCESS.code());
            result.setData(object);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MsgCodeEnum.EXCEPTION.code());
        }
    }

    /**
     * 新增修改
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "save")
    @ResponseBody
    public Object save(HttpServletRequest request, @RequestBody Video bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{//视频新增
            if (bean.getVideo_id() == null){
                Video video = videoService.selectVideoName(bean.getVideo_name());
                if (video != null){
                    return JsonResult.getError("视频名称重复");
                }
                videoService.save(bean);
                JsonResult result = JsonResult.getSuccess(MsgCodeEnum.ADD_SUCCESS.code());
                result.setData(bean);
                return result;
            }else{//视频信息修改
                Video video = videoService.selectVideoById(bean.getVideo_id());
                if (video == null){
                    return JsonResult.getError("该视频不存在");
                }
                videoService.update(bean, video);
                JsonResult result = JsonResult.getSuccess(MsgCodeEnum.UPDATE_SUCCESS.code());
                result.setData(video);
                return result;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return JsonResult.getException(MsgCodeEnum.EXCEPTION.code());
    }

    /**
     * 批量删除
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "del")
    @ResponseBody
    public Object del(HttpServletRequest request, @RequestBody DelVideoBean bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            if(bean.getIds()==null) {
                return JsonResult.getError("请选择要删除的视频！");
            }
            String ids_str = "'"+StringUtils.join(bean.getIds(), "','")+"'";
            List<Video> videos = videoService.selectByIds(ids_str);
            if (videos.size() == 0){
                return JsonResult.getError("视频不存在！");
            }
            List<Video> videos1 = videoService.selectByVideoIds(ids_str);
            if (videos1.size() > 0){
                return JsonResult.getError("审核通过的视频是不能删除的！");
            }
            videoService.deleteVideoMenuByIds(ids_str);
            JsonResult result = JsonResult.getSuccess(MsgCodeEnum.DEL_SUCCESS.code());
            result.setData(new ArrayList<>());
            return result;

        }catch (Exception e){
            e.printStackTrace();
        }
        return JsonResult.getException(MsgCodeEnum.EXCEPTION.code());
    }

    /**
     * 审核
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "audit")
    @ResponseBody
    public Object aduit(HttpServletRequest request, @RequestBody AduitVideoBean bean){
        // 当前登录用户
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            String[] ids = bean.getIds();
            String ids_str = "'" + StringUtils.join(ids, ",").replaceAll(",","','") + "'";
            JsonResult result = JsonResult.getSuccess("");
            List<Video> list = videoService.selectByIds(ids_str);
            if (list.size() == 0){
                return JsonResult.getError("视频不存在！");
            }
            List<Video> videos = videoService.selectAduitByVideoIds(ids_str);
            if (videos.size() > 0){
                return JsonResult.getError("视频审核状态不对,不能审核！");
            }
            for (int i = 0; i< list.size(); i++){
                Video video = list.get(i);
                if (bean.getAudit_status() == 3 ){
                    video.setAudit_status(AuditStatusEnum.PASS.code());
                    result = JsonResult.getSuccess("审核通过");
                }else if (bean.getAudit_status() == 4){
                    video.setAudit_status(AuditStatusEnum.REJECT.code());
                    result = JsonResult.getSuccess("审核未通过");
                }
                videoService.aduit(video);
                result.setData(video);
            }
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getError(MsgCodeEnum.EXCEPTION.code());
        }
    }

    /**
     * 提交审核
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "submitaudit")
    @ResponseBody
    public Object submitAudit(HttpServletRequest request, @RequestBody AduitVideoBean bean){
        // 当前登录用户
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            String[] ids = bean.getIds();
            String ids_str = "'" + StringUtils.join(ids, ",").replaceAll(",","','") + "'";
            JsonResult result = JsonResult.getSuccess("");
            List<Video> list = videoService.selectByIds(ids_str);
            if (list.size() == 0){
                return JsonResult.getError("视频不存在！");
            }
            List<Video> videos = videoService.selectsubmitauditByIds(ids_str);
            if (videos.size() > 0){
                return JsonResult.getError("视频审核状态不对,不能提交审核！");
            }
            for (int i = 0; i< list.size(); i++){
                Video video = list.get(i);
                if (bean.getAudit_status() == 2 ){
                    video.setAudit_status(AuditStatusEnum.WAIT.code());
                    result = JsonResult.getSuccess("已提交审核!");
                }
                videoService.aduit(video);
                result.setData(video);
            }
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getError(MsgCodeEnum.EXCEPTION.code());
        }
    }


    /**
     * 视频添加资源
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("config")
    @ResponseBody
    public Object config(HttpServletRequest request, @RequestBody VideoResRel bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        try{
            if (bean.getVideo_id() == null || bean.getVideo_id() ==0){
                return JsonResult.getError("请选择视频");
            }
            String[] ids = bean.getRes_ids();
            if(ids==null || ids.length==0) {
                return JsonResult.getError("请选择资源");
            }
            for (int i = 0; i < ids.length; i++) {
                Long ids_str = Long.valueOf(ids[i]);
                Long res_id = ids_str;
                VideoResRel videoResRel = videoResRelService.selectByResId(res_id,bean.getVideo_id());
                if (videoResRel != null){
                    videoResRelService.deleteById(bean.getVideo_id(),res_id);
                }
                VideoResRel videoResRel1 = new VideoResRel();
                videoResRel1.setVideo_id(bean.getVideo_id());
                videoResRel1.setRes_id(res_id);
                videoResRel1.setCreate_time(new Date());
                videoResRelService.save(videoResRel1);
            }
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.ADD_SUCCESS.code()));
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }


    /**
     * 删除视频资源
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "delitem")
    @ResponseBody
    public Object delitem(HttpServletRequest request, @RequestBody VideoResRel bean){
        // 当前登录用户
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        try{
            String ids_str = "'"+StringUtils.join(bean.getRes_ids(), "','")+"'";
            if(ids_str == null) {
                return JsonResult.getError("请选择要删除的资源");
            }
            videoResRelService.deleteByIds(bean.getVideo_id(),ids_str);
            JsonResult result = JsonResult.getSuccess(Constants.ACTION_DELETE);
            result.setData(new ArrayList<>());
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    /**
     * 视频详情
     * @param request
     * @return
     */
    @RequestMapping(value = "detail")
    @ResponseBody
    public Object detail(HttpServletRequest request, @RequestBody Video bean){
        // 当前登录用户
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        try{
            Video video = videoService.getDetailById(bean.getVideo_id());
            JsonResult result = JsonResult.getSuccess("加载成功");
            result.setData(video);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getError("无法获取数据");
        }
    }

}
