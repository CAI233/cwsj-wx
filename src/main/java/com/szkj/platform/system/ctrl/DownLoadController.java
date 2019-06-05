package com.szkj.platform.system.ctrl;

import com.szkj.platform.system.service.SpringContextUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;

/**
 * Created by Administrator on 2017/8/24 0024.
 */
@Controller
public class DownLoadController {


    /**
     * 文件下载接口
     *
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/admin/fileDown", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public void fileDown(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String file_path = request.getParameter("fpath");
        String file_id = request.getParameter("fid");
        String file_name = request.getParameter("fname");
        if(StringUtils.isEmpty(file_path) || StringUtils.isEmpty(file_id)|| StringUtils.isEmpty(file_name)){
            return;
        }
        int inx = file_path.lastIndexOf(".");
        if(inx<0 || inx == file_path.length()-1) {
            return;
        }
        String extname = file_path.substring(inx+1);

        String realPath = SpringContextUtil.getApplicationContext().getResource("").getFile().getPath() + File.separator;
        String filepath = realPath+file_path;
        File file = new File(filepath);
        if(!file.exists()) {
            return;
        }
        FileInputStream fis = null;
        fis = new FileInputStream(file);
        long s= fis.available();

        OutputStream os = response.getOutputStream();
        try {
            response.reset();

//			response.setHeader("Content-Disposition", "attachment; filename="+zipFile.getName());
            response.setHeader("Content-Disposition", "attachment; filename="+
                    URLEncoder.encode(file_name +"."+ extname, "UTF-8"));
            response.setHeader("Content-Length",s+"");
            response.setContentType("application/octet-stream; charset=utf-8");
            os.write(FileUtils.readFileToByteArray(file));
            os.flush();
        } finally {
            if (os != null) {
                os.close();
            }
        }

    }

}
