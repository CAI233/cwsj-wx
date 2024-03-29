package com.szkj.platform.system.ctrl;

import com.szkj.platform.system.conditions.SysLogCondition;
import com.szkj.platform.system.domain.SysLog;
import com.szkj.platform.system.domain.SysUser;
import com.szkj.platform.system.service.SysLogService;
import com.szkj.platform.system.service.UserService;
import com.szkj.platform.system.utils.CheckUserHelper;
import com.szkj.platform.system.utils.MessageAPi;
import com.szkj.platform.system.utils.MsgCodeEnum;
import com.szkj.platform.utils.JsonResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;

/**   
 * @Title: SysLogController
 * @Description: 
 * @author Bruce
 * @date 2016-11-09 13:26:25
 * @version V1.0   
 *
 */
@Controller
public class SysLogController {


	@Autowired
	private SysLogService sysLogService;

	@Autowired
	private UserService userService;

	/**
	 * 当系统用异常记录日志
	 * @param request
	 * @param sysLogCondition
     * @return
     */
	@RequestMapping("/api/system/sys_log/save")
	@ResponseBody
	public Object save(HttpServletRequest request, @RequestBody SysLogCondition sysLogCondition){
		Long code = sysLogCondition.getSys_log_code();
		String content = sysLogCondition.getSys_log_content();
		if(null == code) {
			return JsonResult.getError("编码不能为空!");
		}
		if(StringUtils.isBlank(content)) {
			return JsonResult.getError("内容不能为空!");
		}
		SysLog sysLog = new SysLog();
		BeanUtils.copyProperties(sysLogCondition, sysLog);
		sysLogService.saveLog(sysLog);
		JsonResult result = JsonResult.getSuccess("新增日志成功!");
		result.setData(new ArrayList());
		return result;
	}

	/**
	 * 按分页显示系统日志
	 * @param request
	 * @param sysLogCondition
     * @return
     */
	@RequestMapping("/api/system/sys_log/list")
	@ResponseBody
	public Object list(HttpServletRequest request, @RequestBody SysLogCondition sysLogCondition){

		try {
			// 验证当前登录用户
			SysUser sysUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));
			if (sysUser == null) {
				return JsonResult.getExpire(MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code()));
			}
			Object data = sysLogService.pageQuery(sysLogCondition);
			JsonResult jsonResult = JsonResult.getSuccess("查询成功!");
			jsonResult.setData(data);
			return jsonResult;
		} catch (Exception e) {
			return JsonResult.getError("查询失败:" + e.getLocalizedMessage());
		}
	}

	/**
	 * 备份系统日志
	 * @param request
	 * @param sysLogCondition
     * @return
     */
	@RequestMapping("/api/system/sys_log/backup")
	@ResponseBody
	public Object backup(HttpServletRequest request, @RequestBody SysLogCondition sysLogCondition){

		try {
			// 验证当前登录用户
			SysUser sysUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));
			if (sysUser == null) {
				return JsonResult.getExpire(MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code()));
			}
			String logRootDir = request.getServletContext().getRealPath("/backup-logs");
			File dir = new File(logRootDir);
			if(!dir.exists())
				dir.mkdir();
			sysLogService.backup(logRootDir, sysLogCondition);
			JsonResult result = JsonResult.getSuccess("备份成功!");
			result.setData(new ArrayList());
			return result;
		} catch (Exception e) {
			return JsonResult.getError("备份失败:" + e.getLocalizedMessage());
		}
	}
}
