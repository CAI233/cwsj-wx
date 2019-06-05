package com.szkj.platform.busiz.ctrl;

import com.szkj.platform.busiz.beans.ProblemSearchBean;
import com.szkj.platform.busiz.domain.Problem;
import com.szkj.platform.busiz.service.ProblemService;
import com.szkj.platform.system.domain.SysUser;
import com.szkj.platform.system.utils.CheckUserHelper;
import com.szkj.platform.system.utils.MessageAPi;
import com.szkj.platform.system.utils.MsgCodeEnum;
import com.szkj.platform.utils.JsonResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/api/busiz/problem")
public class ProblemController {

    @Autowired
    private ProblemService problemService;

    /**
     * 后台问答列表
     *
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/list")
    @ResponseBody
    public Object problemList(HttpServletRequest request, @RequestBody ProblemSearchBean bean) {
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        try {
            Object result = problemService.getList(bean);
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
            jsonResult.setData(result);
            return jsonResult;
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 问答回复、拒绝回答
     *
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/update")
    @ResponseBody
    public Object update(HttpServletRequest request, @RequestBody Problem bean) {
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());        //登录超时
            return JsonResult.getExpire(message);
        }
        try {
            if (bean.getId() == null) {
                return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
            }
            if (bean.getStatus() == 4) {
                //拒绝回答
                problemService.updateStatus(bean.getId(), bean.getStatus());
                return JsonResult.getSuccess("更新成功！");
            } else if (bean.getStatus() == 2) {
                //回答
                if (StringUtils.isEmpty(bean.getAnswer())) {
                    return JsonResult.getError("请填写回复内容！");
                }
                problemService.answerProblem(bean);
                return JsonResult.getSuccess("回复成功！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
    }
}
