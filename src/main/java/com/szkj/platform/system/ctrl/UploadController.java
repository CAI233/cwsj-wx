package com.szkj.platform.system.ctrl;


import com.szkj.platform.system.constants.Constants;
import com.szkj.platform.system.domain.SysFile;
import com.szkj.platform.system.domain.SysUser;
import com.szkj.platform.system.service.SpringContextUtil;
import com.szkj.platform.system.service.SysFileService;
import com.szkj.platform.system.utils.CheckUserHelper;
import com.szkj.platform.system.utils.FileTypeEnum;
import com.szkj.platform.system.utils.Guid;
import com.szkj.platform.system.utils.UploadCallBack;
import com.szkj.platform.utils.JsonResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by sah on 2016/1/8.
 * 上传
 */
@Configuration
@Controller
public class UploadController {


    @Value("${image_save_path}")
    private String uploadPath;
    @Value("${image_web_path}")
    private String mImagesWebPath;


    public static ThreadLocal<SimpleDateFormat> dateFormatThreadLocal = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("MMdd");
        }
    };

    public static ThreadLocal<SimpleDateFormat> yearFormatThreadLocal = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy");
        }
    };

    @RequestMapping(value = "/api/system/file/upload", method = RequestMethod.POST, consumes = "multipart/form-data",
            produces = {"application/json", "application/xml"})
    @ResponseBody
    public Object upload(@RequestParam("file") MultipartFile file, final HttpServletRequest request) {
        String type = request.getParameter("type");
        if (StringUtils.isNotEmpty(type) && type.equals("admin")) {
            String token = request.getParameter("token");
            SysUser user = CheckUserHelper.checkUserInfo(token);
            if (user == null) {
                return JsonResult.getExpire(Constants.OVER_TIME);
            }
        }
        return processUpload(file, request, new UploadCallBack() {
            @Override
            public Object onSuccess(MultipartFile file, String fileName, String realPath , String webPath) {
                SysFileService sysFileService = (SysFileService) SpringContextUtil.getBean("sysFileServiceImpl");
                SysUser sysUser = CheckUserHelper.checkUserInfo(request.getParameter("token"));
                SysFile sysFile = new SysFile();
                sysFile.setCreate_time(new Date());
                sysFile.setUpdate_time(new Date());
                sysFile.setFile_size(file.getSize());
                sysFile.setSavePath(webPath);
                sysFile.setFile_name(fileName);
                int inx = fileName.lastIndexOf(".");
                String fileExtName = fileName.substring(inx + 1);
                sysFile.setFile_format(fileExtName);
                sysFile.setFile_type(FileTypeEnum.getFileType(fileExtName).code());
                if (sysUser != null) {
                    sysFile.setCreate_userid(sysUser.getUser_id());
                    sysFile.setOrg_id(sysUser.getOrg_id());
                }
                sysFileService.addFile(sysFile);
                List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("url", webPath);
                String name = file.getOriginalFilename();
                map.put("name", name);
                items.add(map);
                JsonResult result = new JsonResult();
                result.setCode(0);
                result.setData(items);
                return result;
            }

            @Override
            public Object onFailure(Exception e) {
                JsonResult result = new JsonResult();
                result.setCode(1);
                return result;
            }

        });
    }


    /**
     * 处理文件上传
     *
     * @param file
     * @param request
     * @return 上传文件的路径
     * @throws IOException
     */
    public Object processUpload(MultipartFile file, HttpServletRequest request, UploadCallBack callBack) {
        /**构建保存的目录**/
        File filePath;
        if (StringUtils.isNotEmpty(uploadPath)) {
            try {
                URL url = new URL(uploadPath);
                filePath = new File(url.toURI());
                if (!filePath.exists()) {
                    filePath.mkdirs();
                }

                //根据年、月日构建目录和路径
                Date tmpDate = new Date();
                String yearVar = yearFormatThreadLocal.get().format(tmpDate);
                String dateVar = dateFormatThreadLocal.get().format(tmpDate);
                String pathDir = filePath.getPath() + File.separator + yearVar + File.separator + dateVar;
                String webPath = mImagesWebPath +"/"+yearVar+"/" + dateVar  ;

                /**根据真实路径创建目录**/
                File saveFileDir = new File(pathDir);
                if (!saveFileDir.exists()) {
                    saveFileDir.mkdirs();
                }

                String fileName = file.getOriginalFilename();
                int inx = fileName.lastIndexOf(".");
                String newfileName = Guid.newId() + fileName.substring(inx);
                /**拼成完整的文件保存路径加文件**/
                String localFilePath = pathDir + File.separator + newfileName;
                File localfile = new File(localFilePath);
                String uploadFilePath = webPath +"/" + newfileName;
                file.transferTo(localfile);
                return callBack.onSuccess(file, fileName, localFilePath,uploadFilePath);
            } catch (Exception e) {
                return callBack.onFailure(e);
            }

        }
        return null;
    }


}
