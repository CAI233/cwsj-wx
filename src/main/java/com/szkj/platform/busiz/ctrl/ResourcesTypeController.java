package com.szkj.platform.busiz.ctrl;

import com.szkj.platform.busiz.service.ResourcesTypeService;
import com.szkj.platform.system.utils.MessageAPi;
import com.szkj.platform.system.utils.MsgCodeEnum;
import com.szkj.platform.utils.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by victor on 2018-03-26.
 */
@Controller
public class ResourcesTypeController {

    @Autowired
    private ResourcesTypeService resourcesTypeService;

    @PostMapping(value = "/api/busiz/res/type/getlist")
    @ResponseBody
    public Object getList(){
        try{
            List<String> list = resourcesTypeService.getlist();
            JsonResult result = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
            result.setData(list);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

}
