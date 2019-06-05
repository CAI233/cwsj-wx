package com.szkj.platform.wx.ctrl;

import com.szkj.platform.busiz.beans.GoodsWindowBean;
import com.szkj.platform.busiz.domain.Window;
import com.szkj.platform.busiz.service.WindowService;
import com.szkj.platform.system.domain.Adv;
import com.szkj.platform.system.domain.AdvCat;
import com.szkj.platform.system.service.AdvCatService;
import com.szkj.platform.system.service.AdvService;
import com.szkj.platform.utils.JsonResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 首页橱窗管理
 */
@Controller
public class WXWindowsController {

    @Autowired
    private WindowService windowService;

    @Autowired
    private AdvCatService advCatService;

    @Autowired
    private AdvService advService;

    /**
     * 橱窗列表
     * @param request
     * @return
     */
    @RequestMapping("/api/wx/windows/list")
    @ResponseBody
    public Object getWindows(HttpServletRequest request){
        List<Window> windows = windowService.getWXList();
        JsonResult jsonResult = JsonResult.getSuccess("数据加载成功！");
        jsonResult.setData(windows);
        return jsonResult;
    }

    /**
     * 橱窗商品列表
     * @param request
     * @return
     */
    @RequestMapping("/api/wx/windows/goods")
    @ResponseBody
    public Object getGoods(HttpServletRequest request){
        String id = request.getParameter("id");
        if (StringUtils.isEmpty(id)){
            return JsonResult.getError("请选择橱窗!");
        }
        Long windows_id = Long.parseLong(id);
        List<GoodsWindowBean> winGoods = windowService.getGoodsListById(windows_id);
        JsonResult jsonResult = JsonResult.getSuccess("数据加载成功！");
        jsonResult.setData(winGoods);
        return jsonResult;
    }

    /**
     * 首页广告
     * @param request
     * @return
     */
    @RequestMapping("/api/wx/windows/adv")
    @ResponseBody
    public Object getAdv(HttpServletRequest request){
        AdvCat advCat = advCatService.selectByCatCode("00001");
        if (advCat == null) {
            return JsonResult.getError("数据异常！");
        }
        List<Adv> advList = advService.selectAdvsByCatIdAndCount(advCat.getAdv_cat_id(),5);
        JsonResult jsonResult = JsonResult.getSuccess("数据加载成功！");
        jsonResult.setData(advList);
        return jsonResult;
    }
}
