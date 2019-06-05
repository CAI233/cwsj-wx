package com.szkj.platform.busiz.ctrl;

import com.alibaba.fastjson.JSONObject;
import com.szkj.platform.busiz.beans.DelQuestionBean;
import com.szkj.platform.busiz.beans.ImportResultsBean;
import com.szkj.platform.busiz.beans.QuestionBean;
import com.szkj.platform.busiz.domain.Question;
import com.szkj.platform.busiz.domain.QuestionFile;
import com.szkj.platform.busiz.service.ImportSubService;
import com.szkj.platform.busiz.service.QuestionFileService;
import com.szkj.platform.busiz.service.QuestionService;
import com.szkj.platform.busiz.utils.DocToHtml;
import com.szkj.platform.job.OrgAuthorization;
import com.szkj.platform.system.constants.Constants;
import com.szkj.platform.system.domain.SysUser;
import com.szkj.platform.system.service.SpringContextUtil;
import com.szkj.platform.system.utils.CheckUserHelper;
import com.szkj.platform.system.utils.Guid;
import com.szkj.platform.system.utils.MessageAPi;
import com.szkj.platform.system.utils.MsgCodeEnum;
import com.szkj.platform.utils.JsonResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 试题管理
 * Created by daixiaofeng on 2018/3/22.
 */
@Controller
@RequestMapping(value = "/api/busiz/question")
public class QuestionController {

    private final static Logger logger = LoggerFactory.getLogger(QuestionController.class);

    @Autowired
    private QuestionService questionService;
    @Autowired
    private QuestionFileService questionFileService;
    @Autowired
    ImportSubService importSubService;

    /**
     * 试题列表
     *
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "/getlist")
    @ResponseBody
    public Object getList(HttpServletRequest request, @RequestBody QuestionBean bean) {
        try {
            SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
            if (user == null) {
                String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
                return JsonResult.getExpire(message);
            }
            Sort sort = null;
            //获取排序字段和排序规则
            if (StringUtils.isNotEmpty(bean.getSort_name()) && StringUtils.isNotEmpty(bean.getSort_rule())) {
                sort = new Sort("desc".equals(bean.getSort_rule()) ? Sort.Direction.DESC : Sort.Direction.ASC, "" + bean.getSort_name() + "");
            } else {
                //默认排序规则
                sort = new Sort(Sort.Direction.DESC, "create_time");
            }
            Object object = questionService.pageQuery(sort, bean);
            JsonResult result = JsonResult.getSuccess(Constants.LOAD_SUCCESS);
            result.setData(object);
            return result;
        } catch (Exception e) {
            logger.error("failed", e);
            return JsonResult.getException(Constants.EXCEPTION + "原因：" + e.getMessage());
        }
    }

    /**
     * 新增修改试题
     *
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "/save")
    @ResponseBody
    public Object save(HttpServletRequest request, @RequestBody QuestionBean bean) {
        try {
            SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
            if (user == null) {
                String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
                return JsonResult.getExpire(message);
            }
            if (bean.getType() == null || bean.getType() == 0) {
                return JsonResult.getError("试题类型不能为空！");
            }
            if (bean.getScore() == null || bean.getScore() == 0) {
                return JsonResult.getError("试题分值不能为空或者为0！");
            }
            if (bean.getId() == null) {
                //新增
                JsonResult jsonResult = questionService.save(bean);
                return jsonResult;
            } else {
                //修改
                JsonResult jsonResult = questionService.updateQuestion(bean);
                return jsonResult;
            }
        } catch (Exception e) {
            logger.error("failed", e);
            return JsonResult.getException(Constants.EXCEPTION + "原因：" + e.getMessage());
        }
    }


    /**
     * 查看试题详情
     *
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "/details")
    @ResponseBody
    public Object details(HttpServletRequest request, @RequestBody QuestionBean bean) {
        try {
            SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
            if (user == null) {
                String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
                return JsonResult.getExpire(message);
            }
            Object object = questionService.details(bean.getId());
            JsonResult result = JsonResult.getSuccess(Constants.LOAD_SUCCESS);
            result.setData(object);
            return result;
        } catch (Exception e) {
            logger.error("failed", e);
            return JsonResult.getException(Constants.EXCEPTION + "原因：" + e.getMessage());
        }
    }


    /**
     * 删除试题
     *
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "/delete")
    @ResponseBody
    public Object delete(HttpServletRequest request, @RequestBody DelQuestionBean bean) {
        try {
            SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
            if (user == null) {
                String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
                return JsonResult.getExpire(message);
            }
            String ids = StringUtils.join(bean.getIds(), ",");
            questionService.delete(ids);
            JsonResult result = JsonResult.getSuccess(Constants.ACTION_DELETE);
            result.setData(new ArrayList<>());
            return result;
        } catch (Exception e) {
            logger.error("failed", e);
            return JsonResult.getException(Constants.EXCEPTION + "原因：" + e.getMessage());
        }
    }


    /**
     * 试题发布
     *
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "/audit")
    @ResponseBody
    public Object audit(HttpServletRequest request, @RequestBody QuestionBean bean) {
        try {
            SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
            if (user == null) {
                String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
                return JsonResult.getExpire(message);
            }
//            Question question = questionService.getById(bean.getId());
//            if (question.getEnabled()==1){
//                return JsonResult.getError("已发布试题无法再次发布！");
//            }
            questionService.audit(bean.getId(), bean.getEnabled());
            JsonResult result = JsonResult.getSuccess(Constants.ACTION_SUCCESS);
            result.setData(new ArrayList<>());
            return result;
        } catch (Exception e) {
            logger.error("failed", e);
            return JsonResult.getException(Constants.EXCEPTION + "原因：" + e.getMessage());
        }
    }


    /**
     * 保存试题doc
     * @return
     */
    @RequestMapping(value = "/file/question", method = RequestMethod.POST, consumes = "multipart/form-data",
            produces = {"application/json", "application/xml"})
    @ResponseBody
    public Object importQuestion(@RequestParam("file") MultipartFile[] file, HttpServletRequest request){
        try {
            List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
            List<QuestionFile> fileList = new ArrayList<>();
            String rootPath = request.getSession().getServletContext().getRealPath("/uploads");
            String dateVar = dateFormatThreadLocal.get().format(new Date());
            String webPath = "/uploads/" + dateVar;
            String pathDir = rootPath + "/" + dateVar;
            for (int i = 0; i < file.length; i++) {
                if (!file[i].getOriginalFilename().toLowerCase().endsWith(".doc")) {
                    continue;
                }
                String savePath = processUploadDoc(file[i], pathDir, webPath);
                String doc_name = file[i].getOriginalFilename();
                QuestionFile questionFile = new QuestionFile();
                questionFile.setFile_name(doc_name);
                questionFile.setFile_url(savePath);
                questionFile.setStatus(0);
                questionFile.setCreate_time(new Date());
                fileList.add(questionFile);
            }
            JsonResult result = new JsonResult();
            if (fileList.size()>0){
                questionFileService.saveList(fileList);
                Map<String, Object> map = new HashMap<>();
                map.put("url", fileList.get(0).getFile_url());
                map.put("name", fileList.get(0).getFile_name());
                map.put("id", fileList.get(0).getId());
                items.add(map);
            }
            if (items.size() > 0) {
                result.setCode(0);
                result.setData(items.get(0));
            } else {
                result.setCode(1);
            }
            return result;
        }catch (Exception e){
            logger.error("failed", e);
            return JsonResult.getException(Constants.EXCEPTION + "原因：" + e.getMessage());
        }
    }


    public static ThreadLocal<SimpleDateFormat> dateFormatThreadLocal = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMddHHmmss");
        }
    };

    /**
     * 处理doc上传
     *
     * @param file
     * @return 上传文件的路径
     * @throws IOException
     */
    public static final String processUploadDoc(MultipartFile file, String rootPath, String webPath) throws IOException {
        /**根据真实路径创建目录**/
        File saveFileDir = new File(rootPath);
        if (!saveFileDir.exists()) {
            saveFileDir.mkdirs();
        }
        String fileName = file.getOriginalFilename();
        int inx = fileName.lastIndexOf(".");
        String newfileName = Guid.newId() + fileName.substring(inx);
        /**拼成完整的文件保存路径加文件**/
        String localFilePath = rootPath + File.separator + newfileName;
        File localfile = new File(localFilePath);
        file.transferTo(localfile);
        return webPath + "/" + newfileName;
    }



    @RequestMapping(value = "/save/file")
    @ResponseBody
    public Object savaQuestionFile(HttpServletRequest request,@RequestBody QuestionFile file){
        try {
            SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
            if (user == null) {
                String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
                return JsonResult.getExpire(message);
            }
            Object result = questionFileService.updateQuestionFile(file);
            //解析doc
            JsonResult jsonResult = docToHtml();
            return jsonResult;
        }catch (Exception e){
            logger.error("failed", e);
            return JsonResult.getException(Constants.EXCEPTION + "原因：" + e.getMessage());
        }

    }


    /**
     * 查询未解析的doc，转换成html。
     * 读取html，生成试题。
     */
    public JsonResult docToHtml(){
        try {
            String rootfile = SpringContextUtil.getApplicationContext().getResource("").getFile().getPath();
            //status 1：未解析，2：已解析成html,未读取试题，3：读取试题成功，4：读取试题失败，5：解析html失败
            List<QuestionFile> docList = questionFileService.getEMFileListByStatus(10);
            if (docList.size() > 0) {
                for (QuestionFile doc : docList) {
                    String savePath = rootfile + doc.getFile_url();
                    String webPath = StringUtils.substringBeforeLast(doc.getFile_url(), "/") + "/";
                    Integer status = 2;
                    doc.setStatus(status);
                    questionFileService.updateFile(doc);
                    if (!DocToHtml.getDocContent(savePath, webPath)) {
                        status = 5;
                        doc.setStatus(status);
                        questionFileService.updateFile(doc);
                        return JsonResult.getOther("上传失败！");
                    }
                }
                List<QuestionFile> htmlList = questionFileService.getEMFileListByStatus(2);
                if (htmlList.size() > 0) {
                    for (QuestionFile ht : htmlList) {
                        String savePath = rootfile + ht.getFile_url().replace(".doc", ".html");
                        Integer status = 3;
                        ImportResultsBean importResultsBean = importSubService.analysisHtmlNew(savePath, ht);
                        if (importResultsBean.getSub_num() == 0 || importResultsBean.getSub_num() == null) {
                            status = 4;
                            ht.setStatus(status);
                            questionFileService.updateFile(ht);
                            return JsonResult.getOther("上传失败！");
                        }
                        ht.setStatus(status);
                        questionFileService.updateFile(ht);
                        return JsonResult.getSuccess("上传成功！");
                    }
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getOther("上传失败！");
        }
    }

}
