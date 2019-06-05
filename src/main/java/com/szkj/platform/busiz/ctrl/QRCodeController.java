package com.szkj.platform.busiz.ctrl;

import com.szkj.platform.busiz.beans.QRCodeConfigListBean;
import com.szkj.platform.busiz.beans.QRCodeListBean;
import com.szkj.platform.busiz.condition.TagSortCondition;
import com.szkj.platform.busiz.domain.QRCode;
import com.szkj.platform.busiz.domain.QRCodeWorksRel;
import com.szkj.platform.busiz.service.QRCodeService;
import com.szkj.platform.busiz.utils.WordUtils;
import com.szkj.platform.system.conditions.IdsCondition;
import com.szkj.platform.system.domain.SysUser;
import com.szkj.platform.system.utils.CheckUserHelper;
import com.szkj.platform.system.utils.MessageAPi;
import com.szkj.platform.system.utils.MsgCodeEnum;
import com.szkj.platform.utils.JsonResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.szkj.platform.busiz.utils.WordUtils.getImageBase;


@Controller
@RequestMapping("/api/busiz")
public class QRCodeController {

    @Autowired
    private QRCodeService qrCodeService;


    @Value("${static_save_path}")
    private String static_save_path;

    @Value("${static_web_path}")
    private String static_web_path;

    /**
     * 二维码列表
     *
     * @param request
     * @param condition
     * @return
     */
    @RequestMapping("/code/list")
    @ResponseBody
    public Object getList(HttpServletRequest request, @RequestBody TagSortCondition condition) {
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        try {
            Sort sort = null;
            //获取排序字段和排序规则
            if (StringUtils.isNotEmpty(condition.getSort_name()) && StringUtils.isNotEmpty(condition.getSort_rule())) {
                sort = new Sort("desc".equals(condition.getSort_rule()) ? Sort.Direction.DESC : Sort.Direction.ASC, "" + condition.getSort_name() + "");
            } else {
                //默认排序规则
                sort = new Sort(Sort.Direction.DESC, "code_id");
            }
            Object result = qrCodeService.getList(condition, sort);
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
            jsonResult.setData(result);
            return jsonResult;
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 新增、修改二维码
     *
     * @param request
     * @return
     */
    @RequestMapping("/code/save")
    @ResponseBody
    public Object save(HttpServletRequest request, @RequestBody QRCode bean) {
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        try {
            JsonResult jsonResult = JsonResult.getSuccess("");
            if (bean.getCode_id() == null) {
                //新增
                String msg = qrCodeService.saveCode(bean);
                if ("FAILED".equals(msg)) {
                    return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
                }
                if ("NO_GOODS".equals(msg)){
                    return JsonResult.getException("该图书对应的商品不存在或商品已下架");
                }
                jsonResult.setMessage(MessageAPi.getMessage(MsgCodeEnum.ADD_SUCCESS.code()));
                return jsonResult;
            } else {
                QRCode code = qrCodeService.getCodeById(bean.getCode_id());
                //修改
                String msg = qrCodeService.updateCode(bean, code);
                if ("NO_GOODS".equals(msg)){
                    return JsonResult.getException("该图书对应的商品不存在或商品已下架");
                }
                jsonResult.setMessage(MessageAPi.getMessage(MsgCodeEnum.UPDATE_SUCCESS.code()));
                return jsonResult;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }


    /**
     * 删除二维码
     *
     * @param request
     * @param condition
     * @return
     */
    @RequestMapping("/code/del")
    @ResponseBody
    public Object del(HttpServletRequest request, @RequestBody IdsCondition condition) {
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        try {
            if (condition.getIds() == null) {
                return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
            }
            qrCodeService.delCodeByIds(condition.getIds());
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.DEL_SUCCESS.code()));
            return jsonResult;
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }


    /**
     * 配置二维码
     *
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/code/config")
    @ResponseBody
    public Object codeConfig(HttpServletRequest request, @RequestBody QRCodeWorksRel bean) {
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        try {
            if (bean.getWorks_id() == null || bean.getCode_id() == null) {
                return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
            }
            QRCodeWorksRel rel = qrCodeService.selectWorksById(bean.getCode_id(), bean.getWorks_id());
            if (rel != null) {
                return JsonResult.getError("已关联该作品！");
            }
            String msg = qrCodeService.codeConfig(bean);
            if (msg.equals("failed")){
                return JsonResult.getError("作品封面不能为空！");
            }
            if (msg.equals("moreError")){
                return JsonResult.getError("配置作品数量超过上限！");
            }
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.SAVE_SUCCESS.code()));
            return jsonResult;
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 移除作品
     *
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/code/rmworks")
    @ResponseBody
    public Object rmworks(HttpServletRequest request, @RequestBody QRCodeWorksRel bean) {
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        try {
            if (bean.getWorks_id() == null || bean.getCode_id() == null) {
                return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
            }
            qrCodeService.removeCodeConfig(bean);
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.UPDATE_SUCCESS.code()));
            return jsonResult;
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 导出二维码
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/code/export")
    @ResponseBody
    public Object export(HttpServletRequest request, HttpServletResponse response, String ids  ) {
        try {
            if (ids == null) {
                return JsonResult.getError("请选择需要导出的二维码ids");
            }
            List<QRCode> list = qrCodeService.getAllCode(ids);
            List<Map<String, Object>> data = new ArrayList<>();
            for (QRCode code : list) {
                Map<String, Object> map = new HashMap<>();
                String img =code.getCode_path();
                String path = static_save_path.replace(static_web_path,"") + img;
                URL url = new URL(path);
                File img_path = new File(url.toURI());
                map.put("img", getImageBase(img_path.getPath()));
                map.put("name", code.getCode_name());
                map.put("remark", code.getRemark());
                data.add(map);
            }
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("list", data);
            WordUtils.exportMillCertificateWord(response, dataMap, "word.ftl");
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 二维码配置作品列表
     *
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/code/workslist")
    @ResponseBody
    public Object getConfigList(HttpServletRequest request, @RequestBody QRCodeConfigListBean bean) {
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        try {
            if (bean.getCode_id() == null) {
                return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
            }
            Object result = qrCodeService.getConfigList(bean);
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
            jsonResult.setData(result);
            return jsonResult;
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 二维码详情
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/code/getcode")
    @ResponseBody
    public Object getCode(HttpServletRequest request ,@RequestBody QRCode bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        try{
            if (bean.getCode_id() == null){
                return JsonResult.getError("codeid");
            }
            QRCodeListBean code = qrCodeService.selectCodeById(bean.getCode_id());
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
            jsonResult.setData(code);
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }

    }

    /**
     * 二维码作品排序
     * @param request
     * @param rel
     * @return
     */
    @RequestMapping("/code/works/order")
    @ResponseBody
    public Object orderWorks(HttpServletRequest request,@RequestBody QRCodeWorksRel rel){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        try{
            if (rel.getWorks_id() == null || rel.getCode_id() == null){
                return JsonResult.getError("参数不全！");
            }
            qrCodeService.updateOrderById(rel.getCode_id(), rel.getWorks_id(),rel.getOrder_weight());
            return JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.UPDATE_SUCCESS.code()));
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }
}
