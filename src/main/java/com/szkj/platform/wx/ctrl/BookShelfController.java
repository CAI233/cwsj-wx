package com.szkj.platform.wx.ctrl;

import com.szkj.platform.busiz.beans.BookShelfBean;
import com.szkj.platform.busiz.beans.DelVideoBean;
import com.szkj.platform.busiz.domain.BookShelf;
import com.szkj.platform.busiz.domain.Goods;
import com.szkj.platform.busiz.domain.Member;
import com.szkj.platform.busiz.service.BookShelfService;
import com.szkj.platform.busiz.service.GoodsService;
import com.szkj.platform.busiz.service.MemberService;
import com.szkj.platform.system.constants.Constants;
import com.szkj.platform.system.utils.MsgCodeEnum;
import com.szkj.platform.utils.JsonResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户书架
 * Created by Administrator on 2018/4/9 0009.
 */
@Controller
@RequestMapping(value = "/api/wx/bookshelf/")
public class BookShelfController {

    @Autowired
    private BookShelfService bookShelfService;

    /**
     * 书架图书列表
     * @param request
     * @return
     */
    @RequestMapping(value = "list", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Object getlist(HttpServletRequest request, BookShelfBean bean){
        Object member1 = request.getSession().getAttribute("member");
        if (member1 == null){
            return JsonResult.getExpire("登录超时！");
        }
        Member member = (Member)member1;
        bean.setMember_id(member.getMember_id());
        try{
            Object object = bookShelfService.pageQuery(bean);
            JsonResult result = JsonResult.getSuccess(Constants.LOAD_SUCCESS);
            result.setData(object);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    /**
     * 手动书架商品新增、修改
     * @param request
     * @return
     */
    @RequestMapping(value = "save")
    @ResponseBody
    public Object save(HttpServletRequest request){
        Object member1 = request.getSession().getAttribute("member");
        if (member1 == null){
            return JsonResult.getExpire("登录超时！");
        }
        Member member = (Member)member1;
        String goodsid = request.getParameter("goods_id");
        if (StringUtils.isEmpty(goodsid)){
            return JsonResult.getError("商品id为空！");
        }
        Long goods_id = Long.parseLong(goodsid);
        try{
            BookShelf bookShelf = new BookShelf();
            bookShelf.setGoods_id(goods_id);
            bookShelf.setMember_id(member.getMember_id());
            bookShelfService.save(bookShelf);
            JsonResult result = JsonResult.getSuccess(Constants.ACTION_ADD);
            result.setData(bookShelf);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    /**
     * 删除书架图书
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "del")
    @ResponseBody
    public Object del(HttpServletRequest request,@RequestBody DelVideoBean bean){
        Object member1 = request.getSession().getAttribute("member");
        if (member1 == null){
            return JsonResult.getExpire("登录超时！");
        }
        try{
            if(bean.getIds()==null) {
                return JsonResult.getError("请选择要删除的图书！");
            }
            String ids_str = StringUtils.join(bean.getIds(), ",");
            List<BookShelf> bookShelves = bookShelfService.selectByIds(ids_str);
            if (bookShelves.size() == 0){
                return JsonResult.getError("图书不存在！");
            }
            bookShelfService.deletebookShelfByIds(ids_str);
            JsonResult result = JsonResult.getSuccess(MsgCodeEnum.DEL_SUCCESS.code());
            result.setData(new ArrayList<>());
            return result;

        }catch (Exception e){
            e.printStackTrace();
        }
        return JsonResult.getException(MsgCodeEnum.EXCEPTION.code());
    }

}
