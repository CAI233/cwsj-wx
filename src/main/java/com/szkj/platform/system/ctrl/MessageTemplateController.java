package com.szkj.platform.system.ctrl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.szkj.platform.system.beans.MessageTemplateBean;
import com.szkj.platform.system.constants.Constants;
import com.szkj.platform.system.domain.MessageSetting;
import com.szkj.platform.system.domain.MessageTemplate;
import com.szkj.platform.system.domain.SysUser;
import com.szkj.platform.system.service.MessageTemplateService;
import com.szkj.platform.system.utils.CheckUserHelper;
import com.szkj.platform.system.utils.MessageAPi;
import com.szkj.platform.system.utils.MsgCodeEnum;
import com.szkj.platform.utils.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;


@Controller
public class MessageTemplateController {

    @Autowired
    private MessageTemplateService messageTemplateService;

    private static final String TPL_URL = "https://sms.yunpian.com/v2/tpl/get.json";

    /**
     * 云片获取模板
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "/api/system/msgtpl/yunpian/getmsgtpl")
    @ResponseBody
    public Object gettpl(HttpServletRequest request, @RequestBody MessageSetting bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            //登录超时
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            String param = "apikey=" + bean.getApikey();
            String jsonstr = sendPost("https://sms.yunpian.com/v2/tpl/get.json", param);
            jsonstr = jsonstr.substring(1, jsonstr.length()-1);
            jsonstr = jsonstr.replaceAll("\\\\","");
            System.out.print(jsonstr);
            messageTemplateService.deleteByApiKeyAndFlag(bean.getApikey());
            JSONArray object = JSONArray.parseArray(jsonstr);
            for (int i = 0; i < object.size(); i++){
                JSONObject jsonObject = object.getJSONObject(i);
                MessageTemplate messageTemplate = new MessageTemplate();
                messageTemplate.setTpl_id(jsonObject.getString("tpl_id"));
                messageTemplate.setTpl_content(jsonObject.getString("tpl_content"));
                messageTemplate.setCheck_status(jsonObject.getString("check_status"));
                messageTemplate.setApikey(bean.getApikey());
                messageTemplate.setFlag(1);
                messageTemplateService.save(messageTemplate);
            }
            JsonResult result = JsonResult.getSuccess(Constants.ACTION_ADD);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    /**
     * 短信模板列表
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "/api/system/msgtpl/getlist")
    @ResponseBody
    public Object getList(HttpServletRequest request, @RequestBody MessageTemplateBean bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            //登录超时
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            Sort sort = new Sort(Sort.Direction.DESC, "flag");
            Object object = messageTemplateService.getList(bean, sort);
            JsonResult result = JsonResult.getSuccess(Constants.LOAD_SUCCESS);
            result.setData(object);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    /**
     * 短信模板新增,删除
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "/api/system/msgtpl/save")
    @ResponseBody
    public Object save(HttpServletRequest request, @RequestBody MessageTemplate bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            //登录超时
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            if (bean.getId() == null){
                MessageTemplate messageTemplate = messageTemplateService.selectByTplId(bean.getTpl_id());
                if (messageTemplate != null){
                    return JsonResult.getError("模板已经存在");
                }
                messageTemplateService.save(bean);
                JsonResult result = JsonResult.getSuccess(Constants.ACTION_ADD);
                result.setData(bean);
                return result;
            }else {
                MessageTemplate messageTemplate = messageTemplateService.selectById(bean.getId());
                if (messageTemplate == null){
                    return JsonResult.getError("模板不存在");
                }
                MessageTemplate messageTemplate1 = messageTemplateService.selectOtherByTplIdAndId(bean.getTpl_id(),bean.getId());
                if (messageTemplate1 != null){
                    return JsonResult.getError("模板已经存在");
                }
                messageTemplateService.update(bean, messageTemplate);
                JsonResult result = JsonResult.getSuccess(Constants.ACTION_UPDATE);
                result.setData(messageTemplate);
                return result;
            }
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    /**
     * 短信模板删除
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "/api/system/msgtpl/del")
    @ResponseBody
    public Object del(HttpServletRequest request, @RequestBody MessageTemplate bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            //登录超时
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            MessageTemplate messageTemplate = messageTemplateService.selectById(bean.getId());
            if (messageTemplate == null){
                return JsonResult.getError("模板不存在");
            }
            messageTemplateService.deleteById(bean.getId());
            JsonResult result = JsonResult.getSuccess(Constants.ACTION_DELETE);
            result.setData(new ArrayList<>());
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }


    public String sendPost(String url, String param){
        PrintWriter out = null;
        BufferedReader in = null;
        String jsonObject = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流（设置请求编码为UTF-8）
            out = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 获取请求返回数据（设置返回数据编码为UTF-8）
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            jsonObject = JSONObject.toJSONString(result);
            System.out.println(jsonObject);
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }

        return jsonObject;
    }
}

