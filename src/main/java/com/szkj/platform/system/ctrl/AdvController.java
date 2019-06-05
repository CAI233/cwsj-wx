package com.szkj.platform.system.ctrl;

import com.szkj.platform.system.beans.DelAdvBean;
import com.szkj.platform.system.conditions.AdvCondition;
import com.szkj.platform.system.constants.Constants;
import com.szkj.platform.system.domain.Adv;
import com.szkj.platform.system.domain.SysUser;
import com.szkj.platform.system.service.AdvService;
import com.szkj.platform.system.service.BaseService;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/1/22 0022.
 */
@Controller
public class AdvController {

    @Autowired
    private AdvService advService;

    @Autowired
    private BaseService baseService;

    @Autowired
    private UserService userService;

    /**
     * 分页列表
     * @param request
     * @param advCondition
     * @return
     */
    @RequestMapping(value = "/api/system/adv/json/getadvlist", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Object getAdvList(HttpServletRequest request, @RequestBody AdvCondition advCondition){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        try{
            Sort sort = new Sort(Sort.Direction.DESC, "order_weight").and(new Sort(Sort.Direction.ASC, "enabled"))
                    .and(new Sort(Sort.Direction.DESC, "create_time"));
            JsonResult result = JsonResult.getSuccess(Constants.LOAD_SUCCESS);
            Object obj = advService.pageQuery(sort,  advCondition);
            result.setData(obj);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    /**
     * 新增、修改
     * @param request
     * @param adv
     * @return
     */
    @RequestMapping(value = "/api/system/adv/json/updateadv", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Object updateAdv(HttpServletRequest request, @RequestBody Adv adv){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        try{
            JsonResult result = JsonResult.getSuccess("");
            if(adv.getAdv_id() == null){
                //新增
                //图片处理，压缩图片
                if (StringUtils.isNotEmpty(adv.getAdv_img())) {
                    String adv_img_small = baseService.getImgScale(adv.getAdv_img(), "small", 0.5);
                    adv.setAdv_img_small(adv_img_small);
                }
                adv.setOrg_id(user.getOrg_id());
                if(user.getOrg_id() == 1) {
                    adv.setEnabled(2);//不启用
                }else{
                    adv.setEnabled(1);
                }
                adv.setCreate_time(new Date());
                adv.setUpdate_time(new Date());
                adv.setIs_delete(2);
                adv.setIs_show(2);
                advService.saveAdv(adv);

                result.setMessage(Constants.ACTION_ADD);
            }else{
                //修改
                if(!user.getOrg_id().equals(adv.getOrg_id())  && user.getOrg_id() !=1){
                    return JsonResult.getError("您不具有操作该数据的权限");
                }
                Adv org_adv = advService.selectAdvById(adv.getAdv_id());
                if(StringUtils.isNotEmpty(adv.getAdv_img()) && !adv.getAdv_img().equals(org_adv.getAdv_img())){
                    //图片处理，压缩图片
                    String adv_img_small = baseService.getImgScale(adv.getAdv_img(), "small", 0.5);
                    adv.setAdv_img_small(adv_img_small);
                }
                adv.setIs_delete(2);
                adv.setUpdate_time(new Date());
                advService.updateAdv(adv);
                result.setMessage(Constants.ACTION_UPDATE);
            }
            result.setData(adv);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }


    /**
     * 启用、停用
     * @param request
     * @param
     * @return
     */
    @RequestMapping(value = "/api/system/adv/json/updateenable", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Object updateEnable(HttpServletRequest request, @RequestBody Adv bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        try{
            Adv adv = advService.selectAdvById(bean.getAdv_id());
            if (adv == null){
                return JsonResult.getError("广告不存在");
            }
            Integer enabled;
            JsonResult result = JsonResult.getSuccess(Constants.ACTION_UPDATE);
            if (adv.getEnabled() == 1){
                enabled = 2;
            }else {
                enabled = 1;
            }
            advService.updateEnable(bean.getAdv_id(), enabled);
            result.setData(adv);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    /**
     * 删除
     * @param request
     * @param delAdvBean
     * @return
     */
    @RequestMapping(value = "/api/system/adv/json/deleteadvs", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Object deleteAdvs(HttpServletRequest request, @RequestBody DelAdvBean delAdvBean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        try{
            Long[] advIds = delAdvBean.getAdv_ids();
            String advIdsStr = StringUtils.join(advIds, ",");

            List<Adv> advList = advService.selectAdvByIds(advIdsStr);
            Long user_org_id = user.getOrg_id();
            for(Adv adv : advList){
                if(!adv.getOrg_id().equals(user_org_id)){
                    return JsonResult.getError("数据中包含总部数据，不可删除");
                }
            }
            advService.deleteByIds(advIdsStr);
            JsonResult result = JsonResult.getSuccess(Constants.ACTION_DELETE);
            result.setData(new ArrayList());
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    /**
     *广告是否显示
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "/api/system/adv/json/updateshow", method = RequestMethod.POST)
    @ResponseBody
    public Object updateShow(HttpServletRequest request, @RequestBody Adv bean) {
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        try {
            Adv adv = advService.selectAdvById(bean.getAdv_id());
            if (adv == null){
                return JsonResult.getError("广告不存在");
            }
            if (adv.getEnabled() == 2) {
                return JsonResult.getError("广告未启用！");
            }
            Integer is_show;
            if (adv.getIs_show() == 1){
                is_show = 2;
            }else {
                is_show = 1;
            }
            advService.updateIsShow(is_show, bean.getAdv_id());
            JsonResult result = JsonResult.getSuccess(Constants.ACTION_UPDATE);
            result.setData(adv);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    /**
     * 广告各机构修改排序
     * @param request
     * @param bean
     * @return
     */
//    @RequestMapping(value = "/admin/adv/json/updateOrder", method = RequestMethod.POST)
//    @ResponseBody
//    public Object updateOrder(HttpServletRequest request, @RequestBody Adv bean){
//        try{
//            SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
//            if (user == null){
//                return JsonResult.getExpire(WeiboConstants.TOKEN_FAILED);
//            }
//            advService.updateOrder(bean.getAdv_id(),bean.getOrg_order_weight(),user.getOrg_id());
//            JsonResult result = JsonResult.getSuccess(WeiboConstants.ACTION_UPDATE);
//            result.setData(bean);
//            return result;
//        }catch (Exception e){
//            e.printStackTrace();
//            return JsonResult.getException(WeiboConstants.EXCEPTION);
//        }
//    }
}
