package com.szkj.platform.system.ctrl;

import com.github.pagehelper.StringUtil;
import com.szkj.platform.condition.PageCondition;
import com.szkj.platform.system.beans.MailSettingBean;
import com.szkj.platform.system.constants.Constants;
import com.szkj.platform.system.domain.MailSetting;
import com.szkj.platform.system.domain.MailTemplate;
import com.szkj.platform.system.service.MailSettingService;
import com.szkj.platform.system.service.MailTemplateService;
import com.szkj.platform.system.utils.SendEmailUtil;
import com.szkj.platform.utils.JsonResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/12/6 0006.
 */
@Controller
public class MailSettingController {
    @Autowired
    private MailSettingService mailSettingService;

    @Autowired
    private MailTemplateService mailTemplateService;

    /**
     * 邮件设置列表————分页查询
     * @param bean
     * @return
     */
    @RequestMapping(value = "/api/system/mailsetting/pagequery", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public Object pageQuery(@RequestBody PageCondition bean) {
        try{
            Sort sort = new Sort(Sort.Direction.ASC, "mail_setting_id");
            Object data = mailSettingService.pageQuery(sort, bean);
            JsonResult result = JsonResult.getSuccess(Constants.LOAD_SUCCESS);
            result.setData(data);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    /**
     * 邮件设置 新增修改
     * @param mailSetting
     * @return
     */
    @RequestMapping(value = "/api/system/mailsetting/update", method = RequestMethod.POST)
    @ResponseBody
    public Object update(@RequestBody MailSetting mailSetting) {
        try {
            JsonResult result = JsonResult.getSuccess("");
            if (mailSetting.getMail_setting_id() == null){
                mailSetting.setIs_authenticate(1);//是否需要身份验证(0：否，1：是)
                mailSetting.setIs_ssl(0);//是否SSL加密(0：否，1：是)
                mailSetting.setUsed_times(0);
                mailSetting.setAvailable_times(600);
                mailSetting.setAuto_zero(1);//是否自动清零(0：否，1：是)
                mailSetting = mailSettingService.save(mailSetting);
                result.setMessage(Constants.ACTION_ADD);
            }
            else {
                mailSetting = mailSettingService.update(mailSetting);
                result.setMessage(Constants.ACTION_UPDATE);
            }
            result.setData(mailSetting);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    /**
     * 邮件设置 删除
     * @param bean
     * @return
     */
    @RequestMapping(value = "/api/system/mailsetting/del", method = RequestMethod.POST)
    @ResponseBody
    public Object deleteByIdStr(@RequestBody MailSettingBean bean) {
        try{
            Long[] ids = bean.getIds();
            if (ids == null || ids.length == 0) {
                return JsonResult.getError(Constants.EXCEPTION);
            }
            String idStr = StringUtils.join(bean.getIds(), ",");
            if (StringUtil.isNotEmpty(idStr)) {
                mailSettingService.deleteByIdStr(idStr);
            }
            JsonResult result = JsonResult.getSuccess(Constants.ACTION_DELETE);
            result.setData(new ArrayList());
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }


    /**
    * 邮件发送 测试
    * @param bean
    * @return
    */
    @RequestMapping(value = "/api/system/mailsetting/sendmail", method = RequestMethod.POST)
    @ResponseBody
    public Object sendMail(@RequestBody MailSettingBean bean) {
        try {
            JsonResult result = JsonResult.getSuccess("");
            if (bean == null){
                result.setMessage(Constants.ACTION_ERROR);
            }
            else {
                MailSetting mailSetting = mailSettingService.selectById(bean.getMail_setting_id());
                MailTemplate mailTemplate = mailTemplateService.findByCode(1);
                SendEmailUtil.send(mailSetting, bean.getEmail(), mailTemplate.getMail_template_name(), mailTemplate.getContent());
                result.setMessage(Constants.EMAIL_SEND_SUCCESS);
            }
            result.setData(new ArrayList());
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }
}
