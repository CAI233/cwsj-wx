package com.szkj.platform.busiz.ctrl;

import com.szkj.platform.busiz.beans.BookListBean;
import com.szkj.platform.busiz.beans.BookResRelBean;
import com.szkj.platform.busiz.beans.BookResourcesBean;
import com.szkj.platform.busiz.domain.Book;
import com.szkj.platform.busiz.domain.BookResRel;
import com.szkj.platform.busiz.domain.QRCode;
import com.szkj.platform.busiz.enums.AuditStatusEnum;
import com.szkj.platform.busiz.service.BookService;
import com.szkj.platform.busiz.service.QRCodeService;
import com.szkj.platform.busiz.utils.AccessTokenUtil;
import com.szkj.platform.core.page.PageList;
import com.szkj.platform.system.conditions.IdsCondition;
import com.szkj.platform.system.constants.Constants;
import com.szkj.platform.system.domain.SysUser;
import com.szkj.platform.system.service.BaseService;
import com.szkj.platform.system.utils.CheckUserHelper;
import com.szkj.platform.system.utils.MessageAPi;
import com.szkj.platform.system.utils.MsgCodeEnum;
import com.szkj.platform.utils.JsonResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 图书管理
 * Created by shiaihua on 16/12/17.
 */
@Controller
@RequestMapping("/api/busiz")
public class BookController {

    @Autowired
    private BookService bookService;
    @Autowired
    private QRCodeService qRCodeService;

    @Autowired
    private BaseService baseService;

    /**
     * 图书查询
     *
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "/book/list")
    @ResponseBody
    public Object getList(HttpServletRequest request, @RequestBody BookListBean bean) {
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try {
            Sort sort = null;
            //获取排序字段和排序规则
            if (StringUtils.isNotEmpty(bean.getSort_name()) && StringUtils.isNotEmpty(bean.getSort_rule())) {
                sort = new Sort("desc".equals(bean.getSort_rule()) ? Sort.Direction.DESC : Sort.Direction.ASC,
                        bean.getSort_name());
            } else {
                //默认排序规则
                sort = new Sort(Sort.Direction.DESC, "create_time");
            }
            PageList obj = bookService.pageQuery(sort, bean);
            JsonResult result = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
            result.setData(obj);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 图书新增、修改
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/book/save")
    @ResponseBody
    public Object save(HttpServletRequest request , @RequestBody Book bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            if (bean.getBook_id() == null){
                //新增
                if (StringUtils.isNotEmpty(bean.getBook_cover())){
                    String book_cover_small = baseService.getImgScale(bean.getBook_cover(),"small",0.5);
                    bean.setBook_cover_small(book_cover_small);
                }
                Book book = bookService.saveBook(bean);
                JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.ADD_SUCCESS.code()));
                jsonResult.setData(book);
                return jsonResult;
            }else {
                //修改
                if (bean.getStatus() == Constants.ENABLE && bean.getAudit_status() != AuditStatusEnum.PASS.code()){
                    return JsonResult.getError("未审核通过不允许发布！");
                }
                Book book = bookService.selectBookById(bean.getBook_id());
                if (book == null){
                    return JsonResult.getError("图书不存在！");
                }
                if (StringUtils.isNotEmpty(bean.getBook_cover()) && !bean.getBook_cover().equals(book.getBook_cover())){
                    String book_cover_small = baseService.getImgScale(bean.getBook_cover(),"small",0.5);
                    bean.setBook_cover_small(book_cover_small);
                    //得到微信图片
                    String wxCoverUrl = AccessTokenUtil.getWXCoverUrl(bean.getBook_cover());
                    qRCodeService.updateBookInfoByBookId(bean.getBook_name(),wxCoverUrl, bean.getBook_id());
                }
                bookService.updateBook(bean);
                return JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.UPDATE_SUCCESS.code()));
            }
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 图书资源关系
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/book/res/save")
    @ResponseBody
    public Object resSave(HttpServletRequest request,@RequestBody BookResRelBean bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            if (bean.getBook_id() == null || bean.getRes_id() == null || StringUtils.isEmpty(bean.getType())){
                return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
            }
            if ("add".equals(bean.getType())){
                BookResRel rel = bookService.selectRes(bean.getBook_id(),bean.getRes_id());
                if (rel != null){
                    return JsonResult.getError("图书资源关系已存在！");
                }
            }
            bookService.changeBookResRel(bean);
            return JsonResult.getSuccess("操作成功！");
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }


    /**
     * 图书关联二维码列表
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/book/qrcode/list")
    @ResponseBody
    public Object qrcodeList(HttpServletRequest request,@RequestBody Book bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            List<QRCode> list = bookService.getBookQRCodeList(bean.getBook_id());
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
            jsonResult.setData(list);
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 删除图书
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/book/del")
    @ResponseBody
    public Object del(HttpServletRequest request,@RequestBody IdsCondition bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            if (bean.getIds() == null || bean.getIds().length < 1){
                return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
            }
            String ids = StringUtils.join(bean.getIds(),",");
            bookService.delBooks(ids);
            return JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.DEL_SUCCESS.code()));
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 图书资源列表
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/book/res/list")
    @ResponseBody
    public Object resList(HttpServletRequest request,@RequestBody Book bean){
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null){
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try{
            if (bean.getBook_id() == null){
                return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
            }
            List<BookResourcesBean> list = bookService.getBookResList(bean.getBook_id());
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
            jsonResult.setData(list);
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }
}
