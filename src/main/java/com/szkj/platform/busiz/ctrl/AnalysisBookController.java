package com.szkj.platform.busiz.ctrl;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 图书解析
 * Created by henry on 2017/08/08 0022.
 */
@Controller
@RequestMapping("/api/busiz")
public class AnalysisBookController {
/*

    Map runJob = new HashMap<>();
    @Autowired
    private BookChapterService bookChapterService;
    @Autowired
    private AnalysisBookService analysisBookService;
    @Autowired
    private BookErrorMapper bookErrorMapper;
    @Autowired
    private BookService bookService;

    */
/**
     * 创建解析任务
     *
     * @param request
     * @param bean
     * @return
     *//*

    @RequestMapping(value = "/book/analysis/save", method = {RequestMethod.POST})
    @ResponseBody
    public Object unBook_add(HttpServletRequest request, @RequestBody AnalysisBook bean) {
        try {
            if (bean.getId() == null) {
                analysisBookService.insertUnBook(bean);
            } else if (bean.getAnalysis_status() == null || bean.getAnalysis_status() == 0 || bean.getAnalysis_status() == 3) {
                analysisBookService.saveUnBook(bean);
            } else {
                return JsonResult.getException("该任务当前状态无法修改!");
            }
            JsonResult jsonResult = JsonResult.getSuccess(Constants.ACTION_ADD);
            jsonResult.setData(bean);
            return jsonResult;
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    */
/**
     * 获取列表
     *
     * @param request
     * @param ben
     * @return
     *//*

    @RequestMapping(value = "/book/analysis/dataList", method = {RequestMethod.POST})
    @ResponseBody
    public Object unBook_data_list(HttpServletRequest request, @RequestBody PageConditionBean ben) {
        try {
            Sort sort = new Sort(Sort.Direction.DESC, "create_time");
            if (ben.getPageNum() == null || ben.getPageSize() == null) {
                return JsonResult.getException("参数错误!");
            }
            Object obj = analysisBookService.getUnBookAll(ben, sort);
            JsonResult jsonResult = JsonResult.getSuccess(Constants.LOAD_SUCCESS);
            jsonResult.setData(obj);
            return jsonResult;
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    */
/**
     * 开始解析
     *
     * @param request
     * @param bean
     * @return
     *//*

    @RequestMapping(value = "/book/analysis", method = {RequestMethod.POST})
    @ResponseBody
    public Object unBook_data_start(HttpServletRequest request, @RequestBody AnalysisBook bean) {
        try {
            if (bean.getId() == null) {
                return JsonResult.getError("参数异常!");
            }
            if (bean.getAnalysis_path() == null) {
                return JsonResult.getError("解析路径异常!");
            }
            if (bean.getAnalysis_status() == 1) {
                return JsonResult.getError("解析中!请勿重复操作");
            }
            if (runJob.containsKey(bean.getId())) {
                return JsonResult.getError("解析中!请勿重复操作");
            }
            if (runJob.size() >= 1) {
                return JsonResult.getError("服务器线程已满， 请等待其他解析任务完成后在继续执行!");
            }
            //立即修改状态
            bean.setAnalysis_status(1);
            analysisBookService.saveUnBook(bean);
            //开启线程
            new Thread(new SplitFilesJob(bean)).start();

            JsonResult jsonResult = JsonResult.getSuccess("文件已开始解析!");
            jsonResult.setData(bean);
            return jsonResult;
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }


     */
/**
     * 图书上传解析
     *
     * @return
     *//*

    @RequestMapping(value = "/book/uploadbook", method = {RequestMethod.POST})
    @ResponseBody
    public Object uploadBookRepo(HttpServletRequest request, @RequestBody MultiFileForm multiFileForm) {
        try {
            List<FileForm> fileForms = multiFileForm.getData();
            if (null != fileForms && fileForms.size() > 0) {
                //analysisBookService.uploadBooks(fileForms);
                return JsonResult.getSuccess(Constants.ACTION_UPDATE);
            } else {
                return JsonResult.getSuccess(Constants.ACTION_ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getError(Constants.EXCEPTION);
        }
    }


    class SplitFilesJob implements Runnable {
        private AnalysisBook unbook;

        */
/**
         * 图书
         *
         * @param unbook
         *//*

        public SplitFilesJob(AnalysisBook unbook) {
            this.unbook = unbook;
        }

        public void getBookChapter(String path, Long id) {
            File file = null;
            try {
                file = SpringContextUtil.getApplicationContext().getResource("classpath:/").getFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String rootPath = "/books" + path;  //解析相对路径
            String rootName = file.getPath() + rootPath;    //绝对路径
            File datesDir = new File(rootName);
            if (!datesDir.exists()) {
                System.out.println("===================找不到路径！======================");
                return;
            }
            File[] datesFile = datesDir.listFiles();
            if (datesFile == null || datesFile.length < 1) {
                System.out.println("===================找不到文件！======================");
                return;
            }
            //更新解析文件数量
            analysisBookService.updateFileNum(datesFile.length, id);
            for (File datefile : datesFile) {
                analysisBooks(datefile,rootPath);
            }
            //解析完成更新时间状态
            analysisBookService.updateEndTime(id);
        }

        public void analysisBooks(File datafile, String rootPath) {
            if (datafile.getPath().endsWith(".pdf")) {
                Date date1 = new Date();
                System.out.println("================开始解析时间===================== " + DateUtils.getDateTime(new Date()));
                System.out.println("================开始解析pdf===================== " + datafile.getName());
                String file_name = datafile.getName();  //文件名
                String pdf_name = file_name.replace(".pdf", "");
                try {
                    BookError bookError = bookErrorMapper.getByFileName(file_name);
                    if (bookError == null) {
                        //Book book = bookService.findBookByFileName(file_name);
                        Book book = null;
                        if (book == null) {
                            File file = SpringContextUtil.getApplicationContext().getResource("").getFile(); //目录前路径
                            //图片存放地址
                            String img_path =  rootPath + File.separator + "data" + File.separator + pdf_name ; //PDF文件处创建data目录存放图片
                            String img_save_path = file.getPath() + rootPath + File.separator + "data" + File.separator + pdf_name ;
                            File img_dir = new File(img_path);
                            if (!img_dir.exists()){
                                img_dir.mkdirs();
                            }
                            PdfReader reader = new PdfReader(datafile.getPath());
                            Integer pageNum = reader.getNumberOfPages();
                            NewHtmlUtil.getNewImg(datafile.getPath(),img_save_path,pageNum);
                            //封面
                            String book_cover = img_path + File.separator + "img1.png";
                            String book_cover_small = img_path + File.separator + "img1_small.png";
                            //新增图书
                            book = new Book();
                            book.setBook_isbn(pdf_name);
                           */
/* book.setBook_url(rootPath + "/" + file_name);
                            book.setEnabled(2);
                            book.setFile_name(file_name);
                            book.setBook_cover(book_cover);
                            book.setBook_cover_small(book_cover_small);
                            book.setUpload_time(new Date());*//*

                            book.setUpdate_time(new Date());
                            book.setCreate_time(new Date());
//                            bookService.saveBook(book);

                            reader.close();

                            System.gc();
                            Thread.sleep(100);
                            Date date2 = new Date();
                            Long m_second = date2.getTime() - date1.getTime();
                            System.out.println("===============解析完毕===========耗时：" + m_second / 1000);
                            System.out.println("===============解析完毕===========");
                        } else {
                            System.out.println("===============曾经解析成功！===========" + file_name);
                        }
                    } else {
                        System.out.println("===============解析失败===========" + file_name);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    BookError error = new BookError();
                    error.setFile_name(file_name);
                    error.setFile_url(rootPath + file_name);
                    error.setCreate_time(new Date());
                    bookErrorMapper.insert(error);
                    System.out.println("===============解析失败===========" + file_name);
                    return;
                }

            } else {
                System.out.println("================解析失败，非pdf文件===================== " + datafile.getName());
            }
        }

        @Override
        public void run() {
            if (runJob.containsKey(unbook.getId())) {
                return;
            }
            runJob.put(unbook.getId(), "run");
            System.out.println("开启线程：" + unbook.getId());
            getBookChapter(unbook.getAnalysis_path(), unbook.getId());
            System.out.println("完成解析-结束线程：" + unbook.getId());
            runJob.remove(unbook.getId());
        }


    }


*/



}
