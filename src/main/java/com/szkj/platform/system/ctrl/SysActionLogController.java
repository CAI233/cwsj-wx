package com.szkj.platform.system.ctrl;


import com.szkj.platform.system.conditions.SysActionLogCondition;
import com.szkj.platform.system.domain.SysActionLog;
import com.szkj.platform.system.domain.SysUser;
import com.szkj.platform.system.service.SysActionLogService;
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
 * @Title: SysActionLogController
 * @Description: 
 * @author Bruce
 * @date 2016-11-09 13:26:26
 * @version V1.0   
 *
 */
@Controller
public class SysActionLogController {

	@Autowired
	private SysActionLogService sysActionLogService;

	@Autowired
	private UserService userService;

	/**
	 * 当系统操作日志
	 * @param request
	 * @param sysActionLogCondition
	 * @return
	 */
	@RequestMapping("/api/system/action_log/save")
	@ResponseBody
	public Object save(HttpServletRequest request, @RequestBody SysActionLogCondition sysActionLogCondition){

		String action_type = sysActionLogCondition.getAction_type();
		String action_log_content = sysActionLogCondition.getAction_log_content();
		if(null == action_type) {
			return JsonResult.getError("编码不能为空!");
		}
		if(StringUtils.isBlank(action_log_content)) {
			return JsonResult.getError("内容不能为空!");
		}
		// 验证当前登录用户
		SysUser sysUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));
		if (sysUser == null) {
			return JsonResult.getExpire(MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code()));
		}
		SysActionLog sysActionLog = new SysActionLog();
		BeanUtils.copyProperties(sysActionLogCondition, sysActionLog);
		sysActionLog.setOrg_id(sysUser.getOrg_id());
		sysActionLogService.saveLog(sysActionLog);
		JsonResult result = JsonResult.getSuccess("新增日志成功!");
		result.setData(new ArrayList());
		return result;
	}

	/**
	 * 分页显示操作日志
	 * @param request
	 * @param sysActionLogCondition
     * @return
     */
	@RequestMapping("/api/system/action_log/list")
	@ResponseBody
	public Object list(HttpServletRequest request, @RequestBody SysActionLogCondition sysActionLogCondition){
		try {
			// 验证当前登录用户
			SysUser sysUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));
			if (sysUser == null) {
				return JsonResult.getExpire(MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code()));
			}
			sysActionLogCondition.setOrg_id(sysUser.getOrg_id());
			Object data = sysActionLogService.pageQuery(sysActionLogCondition);
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
	 * @param sysActionLogCondition
	 * @return
	 */
	@RequestMapping("/api/system/action_log/backup")
	@ResponseBody
	public Object backup(HttpServletRequest request, @RequestBody SysActionLogCondition sysActionLogCondition){

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
			sysActionLogService.backup(logRootDir, sysActionLogCondition);
			JsonResult result = JsonResult.getSuccess("备份成功!");
			result.setData(new ArrayList());
			return result;
		} catch (Exception e) {
			return JsonResult.getError("备份失败:" + e.getLocalizedMessage());
		}
	}
}
