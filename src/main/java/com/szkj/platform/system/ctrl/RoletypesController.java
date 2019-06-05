package com.szkj.platform.system.ctrl;

import com.szkj.platform.condition.PageCondition;
import com.szkj.platform.system.beans.DelResBean;
import com.szkj.platform.system.constants.Constants;
import com.szkj.platform.system.domain.Roletypes;
import com.szkj.platform.system.service.RoletypesService;
import com.szkj.platform.utils.JsonResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by Administrator on 2017/7/25 0025.
 */
@Controller
public class RoletypesController {

    @Autowired
    private RoletypesService roletypesService;


    /**
     * 添加,修改
     *
     * @return
     */
    @RequestMapping(value = "/admin/roletypes/save", method = RequestMethod.POST)
    @ResponseBody
    public Object saveTypes(HttpServletRequest request, @RequestBody Roletypes roletypes){
        try {
            if(null == roletypes || StringUtils.isEmpty(roletypes.getType_name())){
                return JsonResult.getError(Constants.ACTION_ERROR);
            }
            List<Roletypes> list = roletypesService.selectByName(roletypes.getType_name());
            if (roletypes.getType_id() == null) {
                if (list.size() > 0) {
                    return JsonResult.getError(Constants.NAME_EXIST);
                }
                roletypesService.save(roletypes);
                return JsonResult.getSuccess(Constants.ACTION_ADD);
            } else {
                Roletypes reposave = roletypesService.selectById(roletypes.getType_id());
                if (reposave == null) {
                    return JsonResult.getError(Constants.ACTION_ERROR);
                }
                if (!reposave.getType_name().equals(roletypes.getType_name()) && list.size() > 0) {
                    return JsonResult.getError(Constants.NAME_EXIST);
                }
                roletypesService.updateInfo(roletypes, reposave);
                return JsonResult.getSuccess(Constants.ACTION_UPDATE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }

    }

    /**
     * 删除
     *
     * @return
     */
    @RequestMapping(value = "/admin/roletypes/delTypes", method = RequestMethod.POST)
    @ResponseBody
    public Object delTypes(@RequestBody DelResBean bean) {
        JsonResult result = new JsonResult();
        roletypesService.deleteInfoList(bean);
        result.setCode(0);
        result.setMessage("删除数据成功");
        return result;
    }

    /**
     * 查询
     *
     * @return
     */
    @RequestMapping(value = "/admin/roletypes/list", method = RequestMethod.POST)
    @ResponseBody
    public Object list(HttpServletRequest request) {
        JsonResult result = new JsonResult();
        List<Roletypes> list = roletypesService.listAll();
        result.setCode(0);
        result.setData(list);
        result.setMessage("查询数据成功！");
        return result;

    }

    /**
     * 分页查询
     *
     * @return
     */
    @RequestMapping(value = "/admin/roletypes/page", method = RequestMethod.POST)
    @ResponseBody
    public Object page(HttpServletRequest request, @RequestBody PageCondition bean) {
        try {
            Object data = roletypesService.pageQuery(bean);
            System.out.println("长度::::::::" + data.toString().length());
            JsonResult jsonResult = JsonResult.getSuccess("查询成功!");
            jsonResult.setData(data);
            return jsonResult;
        } catch (Exception e) {
            return JsonResult.getError("查询失败:" + e.getLocalizedMessage());
        }
    }


}
