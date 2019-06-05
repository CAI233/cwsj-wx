package com.szkj.platform.wx.ctrl;

import com.szkj.platform.busiz.domain.Collection;
import com.szkj.platform.busiz.domain.Goods;
import com.szkj.platform.busiz.domain.Member;
import com.szkj.platform.busiz.domain.Works;
import com.szkj.platform.busiz.service.CollectionService;
import com.szkj.platform.system.utils.MessageAPi;
import com.szkj.platform.system.utils.MsgCodeEnum;
import com.szkj.platform.utils.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 微信收藏
 * Created by daixiaofeng on 2018/4/20.
 */
@Controller
public class WXCollectionController {

    @Autowired
    private CollectionService collectionService;

    /**
     * 我的收藏列表
     *
     * @param request
     * @return
     */
    @RequestMapping("/myCollect")
    private ModelAndView controllerList(HttpServletRequest request) {
        ModelAndView mv = new ModelAndView();
        Object member1 = request.getSession().getAttribute("member");
        if (member1 != null) {
            Member member = (Member) member1;
            //收藏商品列表
            List<Goods> goodsList = collectionService.getMemberGoodsList(member.getMember_id());
            mv.addObject("goodsList", goodsList);
            //收藏作品列表
            List<Works> worksList = collectionService.getMemberWorksList(member.getMember_id());
            mv.addObject("worksList", worksList);
            mv.setViewName("mobile/myCollect");
        }
        return mv;
    }

    /**
     * 收藏/取消商品作品
     *
     * @param request
     */
    @RequestMapping("/saveCollect")
    @ResponseBody
    public Object addController(HttpServletRequest request) {
        Object member1 = request.getSession().getAttribute("member");
        String id = request.getParameter("id");
        String type = request.getParameter("type");
        if (id == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.EMPTY.code());
            return JsonResult.getExpire(message);
        }
        if (type == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.EMPTY.code());
            return JsonResult.getExpire(message);
        }
        Long collection_id = Long.valueOf(id);
        Integer collection_type = Integer.valueOf(type);
        if (member1 != null) {
            Member member = (Member) member1;
            //查询作品是否收藏
            Collection collection = collectionService.getMemberCollectionId(member.getMember_id(), collection_id, collection_type);
            if (collection == null) {
                //收藏作品
                collectionService.addController(member.getMember_id(), collection_id, collection_type);
                JsonResult result =  JsonResult.getSuccess("收藏成功!");
                result.setData(1);
                return result;
            } else {
                if (collection.getIs_delete() == 1) {
                    collectionService.updateControllerIsdelete(collection.getId(), 2);
                    JsonResult result =  JsonResult.getSuccess("收藏成功!");
                    result.setData(1);
                    return result;
                } else {
                    collectionService.updateControllerIsdelete(collection.getId(), 1);
                    JsonResult result =  JsonResult.getSuccess("取消收藏成功!");
                    result.setData(2);
                    return result;
                }
            }
        }
        return null;
    }


}
