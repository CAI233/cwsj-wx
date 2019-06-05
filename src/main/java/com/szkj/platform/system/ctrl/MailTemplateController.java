package com.szkj.platform.system.ctrl;

import com.szkj.platform.system.beans.MailTemplateBean;
import com.szkj.platform.system.constants.Constants;
import com.szkj.platform.system.domain.MailTemplate;
import com.szkj.platform.system.domain.SysUser;
import com.szkj.platform.system.service.MailTemplateService;
import com.szkj.platform.system.service.UserService;
import com.szkj.platform.system.utils.CheckUserHelper;
import com.szkj.platform.utils.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * Created by Administrator on 2017/12/6 0006.
 */
@Controller
public class MailTemplateController {
    @Autowired
    MailTemplateService mailTemplateService;
    @Autowired
    UserService userService;

    @RequestMapping(value = "/api/system/mailtemplate/pagequery", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public Object pageQuery(@RequestBody MailTemplateBean bean){
        try{
            Sort sort = new Sort(Sort.Direction.DESC, "update_time");
            Object data = mailTemplateService.pageQuery(sort, bean);
            JsonResult result = JsonResult.getSuccess(Constants.LOAD_SUCCESS);
            result.setData(data);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    @RequestMapping(value = "/api/system/mailtemplate/update", method = RequestMethod.POST)
    @ResponseBody
    public Object update(HttpServletRequest request, @RequestBody MailTemplate mailTemplate) {
        try {
            SysUser sysUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));
            if(sysUser == null){
                return JsonResult.getExpire(Constants.OVER_TIME);
            }
            JsonResult result = JsonResult.getSuccess("");
            mailTemplate.setUser_id(sysUser.getUser_id());
            mailTemplate.setUpdate_time(new Date());
            mailTemplate = mailTemplateService.update(mailTemplate);
            result.setMessage(Constants.ACTION_UPDATE);

            result.setData(mailTemplate);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }
}
