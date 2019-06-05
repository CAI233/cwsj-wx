package com.szkj.platform.busiz.ctrl;


import com.szkj.platform.busiz.domain.ResourcesUpload;
import com.szkj.platform.busiz.enums.AuditStatusEnum;
import com.szkj.platform.busiz.service.ResourcesService;
import com.szkj.platform.busiz.service.ResourcesUploadService;
import com.szkj.platform.core.page.PageList;
import com.szkj.platform.system.conditions.SortCondition;
import com.szkj.platform.system.utils.MessageAPi;
import com.szkj.platform.system.utils.MsgCodeEnum;
import com.szkj.platform.utils.JsonResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by victor on 2018-03-26.
 * 上传
 */
@Controller
public class ResourcesUploadController {

    @Autowired
    ResourcesUploadService resourcesUploadService;

    @Autowired
    ResourcesService resourcesService;

    @Value("${static_save_path}")
    private String static_save_path;

    @Value("${static_web_path}")
    private String static_web_path;

    @Value("${resources_path}")
    private String resources_path;

    private static String WAIT = "waitAnalysis";

    private static String WAIT_NEXT = "waitNext";

    Map runJob = new HashMap<>();

    /**
     * 资源上传
     *
     * @param file
     * @return
     */
    @RequestMapping(value = "/api/busiz/res/upload", method = RequestMethod.POST,
            consumes = "multipart/form-data", produces = {"application/json", "application/xml"})
    @ResponseBody
    public Object upload(@RequestParam("file") MultipartFile file) {
        if (StringUtils.isNotEmpty(static_save_path)) {
            if (file != null) {
                String timeVar = String.valueOf(System.currentTimeMillis());
                String rootPath = static_save_path + resources_path + File.separator + timeVar;
                try {
                    String fileName = file.getOriginalFilename();
                    resourcesUploadService.processUpload(file, rootPath);
                    ResourcesUpload upload = resourcesUploadService.saveResUpload(fileName, timeVar);
                    //状态立即修改为解析中
                    resourcesUploadService.updateStatusById(AuditStatusEnum.WAIT.code(),
                            "读取文件中，请勿重复解析", upload.getUpload_id());
                    //开启线程
                    new Thread(new SplitFilesJob(upload)).start();
                } catch (Exception e) {
                    e.printStackTrace();
                    return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
                }
                return JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.UPDATE_SUCCESS.code()));
            }
        }
        return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
    }

    /**
     * 分页列表
     *
     * @param bean
     * @return
     */
    @PostMapping(value = "/api/busiz/res/upload/getlist")
    @ResponseBody
    public Object getList(@RequestBody SortCondition bean) {
        try {
            Sort sort;
            //获取排序字段和排序规则
            if (StringUtils.isNotEmpty(bean.getSort_name()) && StringUtils.isNotEmpty(bean.getSort_rule())) {
                sort = new Sort("desc".equals(bean.getSort_rule()) ? Sort.Direction.DESC : Sort.Direction.ASC, bean.getSort_name());
            } else {
                //默认排序规则
                sort = new Sort(Sort.Direction.DESC, "create_time");
            }
            PageList data = resourcesUploadService.getUploadList(bean, sort);
            JsonResult result = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
            result.setData(data);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 新增修改
     *
     * @param bean
     * @return
     */
    @PostMapping(value = "/api/busiz/res/upload/save")
    @ResponseBody
    public Object updateInfo(@RequestBody ResourcesUpload bean) {
        try {
            boolean flag = bean != null && StringUtils.isNotEmpty(bean.getUpload_name()) &&
                    StringUtils.isNotEmpty(bean.getUpload_url());
            if (!flag) {
                return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
            }
            if (bean.getUpload_id() == null) {
                //查询该路径是否已存在
                boolean isExist = resourcesUploadService.getCountByUrl(bean.getUpload_url());
                if (isExist) {
                    return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.EXIST.code()));
                }
                //新增
                resourcesUploadService.saveResUpload(bean.getUpload_name(), bean.getUpload_url());
            } else {
                //修改
                ResourcesUpload upload = resourcesUploadService.selectById(bean.getUpload_id());
                if (upload == null) {
                    return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
                }
                if (AuditStatusEnum.WAIT.code().equals(upload.getStatus())) {
                    return JsonResult.getError(MessageAPi.getMessage(WAIT));
                }
                //查询该路径是否已存在
                boolean isExist = resourcesUploadService.getCountByUrlAndId(bean.getUpload_url(),
                        bean.getUpload_id());
                if (isExist) {
                    return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.EXIST.code()));
                }
                resourcesUploadService.updateInfo(upload, bean);
            }
            return JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.SAVE_SUCCESS.code()));
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 解析任务开启
     *
     * @param bean
     * @return
     */
    @PostMapping(value = "/api/busiz/res/upload/start")
    @ResponseBody
    public Object unBook_data_start(@RequestBody ResourcesUpload bean) {
        try {
            if (bean == null || bean.getUpload_id() == null) {
                return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.ACTION_ERROR.code()));
            }
            ResourcesUpload upload = resourcesUploadService.selectById(bean.getUpload_id());
            if (upload == null) {
                return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
            }
            if (AuditStatusEnum.WAIT.code().equals(upload.getStatus())) {
                return JsonResult.getError(MessageAPi.getMessage(WAIT));
            }
            if (runJob.containsKey(upload.getUpload_id())) {
                return JsonResult.getError(MessageAPi.getMessage(WAIT));
            }
            if (runJob.size() >= 1) {
                return JsonResult.getError(MessageAPi.getMessage(WAIT_NEXT));
            }
            //状态立即修改为解析中
            resourcesUploadService.updateStatusById(AuditStatusEnum.WAIT.code(),
                    "读取文件中，请勿重复解析", upload.getUpload_id());
            //开启线程
            new Thread(new SplitFilesJob(upload)).start();
            return JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.ACTION_SUCCESS.code()));
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    public class SplitFilesJob implements Runnable {
        private ResourcesUpload upload;

        public SplitFilesJob(ResourcesUpload upload) {
            this.upload = upload;
        }

        @Override
        public void run() {
            Long id = upload.getUpload_id();
            if (runJob.containsKey(id)) {
                return;
            }
            runJob.put(id, "run");
            System.out.println("开启线程：" + id);
            try {
                resourcesService.readRes(upload);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("解析失败：" + id);
                resourcesUploadService.updateStatusById(AuditStatusEnum.REJECT.code(),
                        "解析失败，路径转换失败", upload.getUpload_id());
            }
            System.out.println("完成解析-结束线程：" + id);
            runJob.remove(id);
        }
    }
}
