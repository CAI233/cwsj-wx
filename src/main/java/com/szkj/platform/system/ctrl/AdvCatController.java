package com.szkj.platform.system.ctrl;

import com.github.pagehelper.StringUtil;
import com.szkj.platform.condition.PageCondition;
import com.szkj.platform.system.beans.DelAdvCatBean;
import com.szkj.platform.system.constants.Constants;
import com.szkj.platform.system.domain.Adv;
import com.szkj.platform.system.domain.AdvCat;
import com.szkj.platform.system.domain.SysUser;
import com.szkj.platform.system.service.AdvCatService;
import com.szkj.platform.system.service.AdvService;
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
public class AdvCatController {

    @Autowired
    private AdvCatService advCatService;

    @Autowired
    private AdvService advService;

    @Autowired
    private UserService userService;

    /**
     * 广告分类列表————分页
     * @return
     */
    @RequestMapping(value = "/api/system/advcat/json/advcatlist", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Object advCatList(HttpServletRequest request, @RequestBody PageCondition bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        try{
            Sort sort = new Sort(Sort.Direction.DESC, "order_weight").and(new Sort(Sort.Direction.DESC, "create_time"));
            //广告分类公用，使用org_id = 1 的分类
            Long org_id = 1L;
            Object data = advCatService.pageQuery(sort, org_id, bean);
            JsonResult result = JsonResult.getSuccess(Constants.LOAD_SUCCESS);
            result.setData(data);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    /**
     * 广告分类列表
     * @return
     */
    @RequestMapping(value = "/api/system/advcat/json/getalladvcat", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Object getAllAdvCat(HttpServletRequest request){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        try{
            List<AdvCat> advCats = new ArrayList<AdvCat>();
            advCats = advCatService.getAllAdvCat(user.getOrg_id());
            JsonResult result = JsonResult.getSuccess(Constants.LOAD_SUCCESS);
            result.setData(advCats);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    /**
     * 广告分类 新增、修改
     * @param request
     * @param advCat
     * @return
     */
    @RequestMapping(value = "/api/system/advcat/json/updateadvcat", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Object updateAdvCat(HttpServletRequest request, @RequestBody AdvCat advCat){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        try{
            JsonResult result = JsonResult.getSuccess("");
            if(advCat.getAdv_cat_id() == null){
                //新增
                //分类名称重复(分类名查找)
                List<AdvCat> advCatList = advCatService.selectByCatName(advCat.getAdv_cat_name());
                if(advCatList.size()>0){
                    return JsonResult.getError(Constants.CATNAME_REPETITION);
                }
                //分类编号重复(分类编号查找)
                AdvCat cat = advCatService.selectByCatCode(advCat.getAdv_cat_code());
                if(cat != null){
                    return JsonResult.getError(Constants.CATCODE_REPETITION);
                }
                advCat.setOrg_id(user.getOrg_id());
                advCat.setCreate_time(new Date());
                advCat.setUpdate_time(new Date());
                advCat.setIs_delete(2);
                advCatService.saveAdvCat(advCat);
                result.setMessage(Constants.ACTION_ADD);
            }else{
                //修改
                //分类名称重复(分类名查找不包括本身)
                List<AdvCat> advCatList = advCatService.selectOtherByCatName(advCat.getAdv_cat_name(), advCat.getAdv_cat_id());
                if(advCatList.size()>0){
                    return JsonResult.getError(Constants.CATNAME_REPETITION);
                }
                //分类编号重复(分类编号查找)
                AdvCat cat = advCatService.selectOtherByCatCode(advCat.getAdv_cat_code(), advCat.getAdv_cat_id());
                if(cat != null){
                    return JsonResult.getError(Constants.CATCODE_REPETITION);
                }
                advCat.setIs_delete(2);
                advCat.setUpdate_time(new Date());
                advCatService.updateAdvCat(advCat);
                result.setMessage(Constants.ACTION_UPDATE);
            }
            result.setData(advCat);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    /**
     * 广告分类删除
     * @param request
     * @param delAdvCatBean
     * @return
     */
    @RequestMapping(value = "/api/system/advcat/json/deleteadvcats", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Object deleteAdvCats(HttpServletRequest request, @RequestBody DelAdvCatBean delAdvCatBean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        try{
            Long[] catIds = delAdvCatBean.getIds();
            String catIdsStr = StringUtils.join(catIds, ",");
            //广告分类Ids查找广告
            List<Adv> advs = advService.selectAdvByCatIds(catIdsStr);

            if(StringUtil.isEmpty(delAdvCatBean.getMark()) && advs.size() > 0){
                //分类下有数据，提示
                return JsonResult.getError("分类下存在广告，是否删除？");
            }else{
                //删除同时删除分类下广告
                advCatService.deleteAdvCats(catIdsStr);
            }
            JsonResult result = JsonResult.getSuccess(Constants.ACTION_DELETE);
            result.setData(new ArrayList());
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }
}
