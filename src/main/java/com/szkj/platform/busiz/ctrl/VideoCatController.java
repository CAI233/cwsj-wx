package com.szkj.platform.busiz.ctrl;

import com.szkj.platform.busiz.domain.VideoCat;
import com.szkj.platform.busiz.service.VideoCatService;
import com.szkj.platform.system.conditions.IdsCondition;
import com.szkj.platform.system.conditions.SortCondition;
import com.szkj.platform.system.constants.Constants;
import com.szkj.platform.system.domain.SysUser;
import com.szkj.platform.system.utils.CheckUserHelper;
import com.szkj.platform.system.utils.MessageAPi;
import com.szkj.platform.system.utils.MsgCodeEnum;
import com.szkj.platform.utils.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by Administrator on 2018/4/4 0004.
 */
@Controller
@RequestMapping("/api/busiz/video")
public class VideoCatController {

    @Autowired
    private VideoCatService videoCatService;


    /**
     * 分类树
     * @param request
     * @return
     */
    @RequestMapping("/cat/list")
    @ResponseBody
    public Object getTree(HttpServletRequest request, @RequestBody SortCondition bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            List<VideoCat> list = videoCatService.getCatList(bean.getSearchText());
            //转换为树形结构
            List<VideoCat> result = videoCatService.getTree(list);
            JsonResult jsonResult =JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
            jsonResult.setData(result);
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 分类新增修改
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/cat/save")
    @ResponseBody
    public Object save(HttpServletRequest request, @RequestBody VideoCat bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            JsonResult jsonResult = JsonResult.getSuccess("");
            if (bean.getCat_id() == null){
                //新增
                VideoCat cat = videoCatService.selectCatByName(bean.getCat_name());
                if (cat != null){
                    return JsonResult.getError("分类名称已存在！");
                }
                videoCatService.saveCat(bean);
                jsonResult.setMessage(MessageAPi.getMessage(MsgCodeEnum.ADD_SUCCESS.code()));
                return jsonResult;
            }else {
                //修改
                VideoCat cat = videoCatService.selectCatByNameOutId(bean.getCat_name(),bean.getCat_id());
                if (cat != null){
                    return JsonResult.getError("分类名称已存在！");
                }
                videoCatService.updateCat(bean);
                jsonResult.setMessage(MessageAPi.getMessage(MsgCodeEnum.UPDATE_SUCCESS.code()));
                return jsonResult;
            }
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 删除分类
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/cat/del")
    @ResponseBody
    public Object del(HttpServletRequest request, @RequestBody IdsCondition bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            if (bean.getIds() == null){
                return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
            }
            for (int i = 0;i<bean.getIds().length;i++){
                Long cat_id = bean.getIds()[i];
                videoCatService.delCatByPath(cat_id);
            }
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.DEL_SUCCESS.code()));
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 视频分类状态
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "/cat/enabled")
    @ResponseBody
    public Object enabled(HttpServletRequest request, @RequestBody VideoCat bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            VideoCat videoCat = videoCatService.selectById(bean.getCat_id());
            if (videoCat == null){
                return JsonResult.getError("视频分类不存在");
            }
            Integer enabled = null;
            JsonResult result = JsonResult.getSuccess("");
            if (videoCat.getEnabled() == 1){
                enabled = 2;
                result.setMessage("停用");
            }else if (videoCat.getEnabled() == 2){
                enabled = 1;
                result.setMessage("启用");
            }
            videoCatService.enabled(bean.getCat_id(), enabled);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }
}
