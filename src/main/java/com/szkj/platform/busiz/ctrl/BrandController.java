package com.szkj.platform.busiz.ctrl;

import com.szkj.platform.busiz.beans.BrandBean;
import com.szkj.platform.busiz.beans.DelBrandBean;
import com.szkj.platform.busiz.domain.Brand;
import com.szkj.platform.busiz.service.BrandService;
import com.szkj.platform.system.constants.Constants;
import com.szkj.platform.system.domain.SysUser;
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
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(value = "/api/busiz/brand")
public class BrandController {

    @Autowired
    private BrandService brandService;

    @RequestMapping(value = "getlist")
    @ResponseBody
    public Object getlist(HttpServletRequest request, @RequestBody BrandBean bean){
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
            Object object = brandService.pageQuery(bean, sort);
            JsonResult result = JsonResult.getSuccess(Constants.LOAD_SUCCESS);
            result.setData(object);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    @RequestMapping(value = "save")
    @ResponseBody
    public Object save(HttpServletRequest request, @RequestBody Brand bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try {
            if (bean.getBrand_id() == null){
                Brand brand = brandService.selectByName(bean.getBrand_name());
                if (brand != null){
                    return JsonResult.getError("品牌名称重复");
                }
                Brand brand1 = brandService.selectByCode(bean.getBrand_code());
                if (brand1 != null){
                    return JsonResult.getError("品牌编码重复");
                }
                brandService.save(bean);
                JsonResult result = JsonResult.getSuccess(Constants.ACTION_ADD);
                result.setData(bean);
                return result;
            }else {
                Brand brand = brandService.selectById(bean.getBrand_id());
                if (brand == null){
                    return JsonResult.getError("品牌不存在");
                }
                Brand brand1 = brandService.selectOtherByIdAndName(bean.getBrand_id(), bean.getBrand_name());
                if (brand1 != null){
                    return JsonResult.getError("品牌名称重复");
                }
                Brand brand2 = brandService.selectOtherByIdAndCode(bean.getBrand_id(), bean.getBrand_code());
                if (brand2 != null){
                    return JsonResult.getError("品牌编码重复");
                }
                brandService.update(bean, brand);
                JsonResult result = JsonResult.getSuccess(Constants.ACTION_UPDATE);
                result.setData(brand);
                return result;
            }
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    @RequestMapping(value = "del")
    @ResponseBody
    public Object del(HttpServletRequest request, @RequestBody DelBrandBean bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            Long[] ids = bean.getIds();
            String ids_str = StringUtils.join(ids, ",");
            brandService.deletes(ids_str);
            JsonResult result = JsonResult.getSuccess(Constants.ACTION_DELETE);
            result.setData(new ArrayList<>());
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    @RequestMapping(value = "brandlist")
    @ResponseBody
    public Object brandlist(HttpServletRequest request){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            List<Brand> list = brandService.selectList();
            JsonResult result = JsonResult.getSuccess(Constants.LOAD_SUCCESS);
            result.setData(list);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

}
