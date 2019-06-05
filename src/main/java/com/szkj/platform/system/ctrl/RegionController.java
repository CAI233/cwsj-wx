package com.szkj.platform.system.ctrl;

import com.szkj.platform.system.beans.RegionBean;
import com.szkj.platform.system.domain.Region;
import com.szkj.platform.system.service.RegionService;
import com.szkj.platform.system.utils.MessageAPi;
import com.szkj.platform.system.utils.MsgCodeEnum;
import com.szkj.platform.utils.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * 地区
 * Created by daixiaofeng on 2018/2/26.
 */
@Controller
@RequestMapping("/api/system/region")
public class RegionController {

    @Autowired
    private RegionService regionService;

    /**
     * 查询地区(树)
     *
     * @param request
     * @return
     */
    @RequestMapping("/list/tree")
    @ResponseBody
    public Object getRegionTree(HttpServletRequest request, @RequestBody RegionBean bean) {
        try {
            Object object = regionService.getRegionTree(bean);
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
            jsonResult.setData(object);
            return jsonResult;
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }


    /**
     * 新增地区
     *
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/save")
    @ResponseBody
    public Object saveRegion(HttpServletRequest request, @RequestBody RegionBean bean) {
        try {
//            if (bean.getCode() != null) {
//                Region region = regionService.getRegionCode(bean.getCode());
//                if (region != null) {
//                    return JsonResult.getOther("区域编码重复，请重试！");
//                }
//            } else {
//                return JsonResult.getOther("区域编码不能为空！");
//            }
//            if (bean.getRegion_name() == null) {
//                return JsonResult.getOther("区域名称不能为空！");
//            }
            Region region = regionService.saveRegion(bean);
            JsonResult jsonResult = JsonResult.getSuccess("success");
            jsonResult.setData(region);
            return jsonResult;
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException("err");
        }
    }

}
