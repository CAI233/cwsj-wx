package com.szkj.platform.wx.ctrl;

import com.sun.org.apache.xpath.internal.operations.Mod;
import com.szkj.platform.busiz.beans.BookResourcesBean;
import com.szkj.platform.busiz.beans.PageConditionBean;
import com.szkj.platform.busiz.domain.*;
import com.szkj.platform.busiz.mapper.MemberPayRecordMapper;
import com.szkj.platform.busiz.service.*;
import com.szkj.platform.core.page.PageList;
import com.szkj.platform.system.domain.Adv;
import com.szkj.platform.system.domain.AdvCat;
import com.szkj.platform.system.service.AdvCatService;
import com.szkj.platform.system.service.AdvService;
import com.szkj.platform.utils.JsonResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品分类、列表、详情
 */
@Controller
public class WXGoodsController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private CommentsService commentsService;

    @Autowired
    private GoodsCatService goodsCatService;

    @Autowired
    private AdvCatService advCatService;

    @Autowired
    private AdvService advService;

    @Autowired
    private BookService bookService;

    @Autowired
    private VideoService videoService;

    @Autowired
    private WorksCatService worksCatService;

    @Autowired
    private WorksService  worksService;

    @Autowired
    private CollectionService collectionService;

    @Autowired
    private MemberPayRecordMapper memberPayRecordMapper;

    /**
     * 商品详情
     * @param request
     * @return
     */
    @RequestMapping("/goodsInfo")
    public ModelAndView goodsInfo(HttpServletRequest request){
        ModelAndView mv = new ModelAndView();
        Object member1 = request.getSession().getAttribute("member");
        if (member1 !=null) {
            Member member = (Member)member1;
            String goods_id = request.getParameter("goods_id");
            if (StringUtils.isNotEmpty(goods_id)) {
                //商品详情
                Goods goods = goodsService.selectById(Long.parseLong(goods_id));
                if (goods != null) {
                    mv.addObject("goods", goods);
                    //是否收藏
                    Long id = collectionService.selectCollection(goods.getGoods_id(),member.getMember_id());
                    mv.addObject("collection",id);
                    if (goods.getGoods_type() == 3) {
                        Book book = bookService.selectBookById(goods.getRes_id());
                        List<BookResourcesBean> bookResourcesList = bookService.getBookResList(book.getBook_id());
                        mv.addObject("bookResourcesList", bookResourcesList);
                    }
                    if (goods.getGoods_type() == 2) {
                        Video video = videoService.selectVideoById(goods.getRes_id());
                        List<VideoResRel> videoResourcesList = videoService.getVideoResList(video.getVideo_id());
                        mv.addObject("videoResourcesList", videoResourcesList);
                    }
                    //判断是否支付过
                    MemberPayRecord memberPayRecord = memberPayRecordMapper.selectByMemberAndGoods(member.getMember_id(),goods.getGoods_id());
                    if (memberPayRecord!=null){
                        mv.addObject("buy", 1);
                    }
                    //评论列表
                    PageConditionBean bean = new PageConditionBean();
                    bean.setPageNum(1);
                    bean.setPageSize(5);

                    Object comments = commentsService.getCommentsListById(goods.getGoods_id(), bean,member.getMember_id());
                    mv.addObject("comments", comments);
                    if (((PageList) comments).getRows() != null) {
                        mv.addObject("count", ((PageList) comments).getRows().size());
                    }
                    //相关推荐
                    List<Goods> recommends = goodsService.selectRecommend(goods.getGoods_cat_id(), goods.getGoods_type(), 3 ,goods.getGoods_id());
                    mv.addObject("recommends", recommends);
                    mv.setViewName("mobile/goodsInfo");
                    //购物车数量
                    Integer car_count = memberService.selectShoppingCarCount(member.getMember_id());
                    mv.addObject("car_count",car_count);
                    //保存用户浏览记录
                    MemberSawRecord record = memberService.selectSawRecordByIdAndType(goods.getGoods_id(), 1, member.getMember_id());
                    if (record != null) {
                        int count = record.getSaw_count();
                        count++;
                        record.setSaw_count(count);
                        memberService.updateSawRecord(record);
                    } else {
                        record = new MemberSawRecord();
                        record.setMember_id(member.getMember_id());
                        record.setSaw_count(1);
                        record.setCreate_time(new Date());
                        record.setSaw_type(1);
                        record.setUpdate_time(new Date());
                        record.setSaw_id(goods.getGoods_id());
                        memberService.saveSawRecord(record);
                    }
                } else {
                    mv.setViewName("mobile/index");
                }
            } else {
                mv.setViewName("mobile/index");
            }
        }else {
            mv.setViewName("mobile/index");
        }
        return mv;
    }

    /**
     * 分类列表
     * @param request
     * @return
     */
    @RequestMapping("/classify")
    public ModelAndView goodsCat(HttpServletRequest request){
        ModelAndView mv = new ModelAndView();
        Object member1 = request.getSession().getAttribute("member");
        if (member1 !=null) {
            List<GoodsCat> cats = goodsCatService.getCatList(null);
            List<GoodsCat> catTree = goodsCatService.getTree(cats);
            mv.addObject("catList", catTree);
            List<WorksCat> worksCats = worksCatService.getCatList(null);
            List<WorksCat> worksCatTree = worksCatService.getTree(worksCats);
            mv.addObject("worksList",worksCatTree);
            AdvCat advCat = advCatService.selectByCatCode("00001");
            if (advCat != null) {
                List<Adv> advList = advService.selectAdvsByCatIdAndCount(advCat.getAdv_cat_id(), 5);
                mv.addObject("advList", advList);
            }
            mv.setViewName("mobile/classify");
        }else {
            mv.setViewName("mobile/index");
        }
        return mv;
    }

    /**
     * 分类商品列表
     * @param request
     * @return
     */
    @RequestMapping("/classify/goods")
    public ModelAndView getGoodsList(HttpServletRequest request){
        ModelAndView mv  = new ModelAndView();
        Object member1 = request.getSession().getAttribute("member");
        if (member1 !=null) {
            String goods_cat_id = request.getParameter("id");
            if (StringUtils.isNotEmpty(goods_cat_id)) {
                Long cat_id = Long.parseLong(goods_cat_id);
                GoodsCat cat = goodsCatService.selectCatById(cat_id);
                Map<String, Object> map = new HashMap<>();
                List<Goods> bookGoods = goodsService.selectCatList(cat_id, 1);
                map.put("bookGoods", bookGoods);
                List<Goods> ebookGoods = goodsService.selectCatList(cat_id, 2);
                map.put("ebookGoods", ebookGoods);
                List<Goods> videoGoods = goodsService.selectCatList(cat_id, 3);
                map.put("videoGoods", videoGoods);
                map.put("cat_name", cat.getCat_name());
                mv.addObject("goods", map);
                mv.setViewName("mobile/classify/goods");
            } else {
                mv.setViewName("mobile/index");
            }
        }else {
            mv.setViewName("mobile/index");
        }
        return mv;
    }

    /**
     * 搜索列表
     * @param request
     * @return
     */
    @RequestMapping("/search")
    public ModelAndView search(HttpServletRequest request){
        ModelAndView mv = new ModelAndView();
        Object member1 = request.getSession().getAttribute("member");
        if (member1 != null) {
            Member member = (Member)member1;
            String searchText = request.getParameter("str");
            if (StringUtils.isNotEmpty(searchText)) {
                Boolean fag = commentsService.containsEmoji(searchText);
                if (!fag) {
                    //添加搜索记录
                    MemberSearchRecord memberSearchRecord = memberService.selectSearchRecordByName(member.getMember_id(),searchText);
                    if (memberSearchRecord != null){
                        //增加搜索次数
                        int count = memberSearchRecord.getSearch_count();
                        count ++;
                        memberSearchRecord.setSearch_count(count);
                        memberService.updateSearchRecord(memberSearchRecord);
                    }else {
                        //新增搜索记录
                        memberSearchRecord = new MemberSearchRecord();
                        memberSearchRecord.setSearch_count(0);
                        memberSearchRecord.setUpdate_time(new Date());
                        memberSearchRecord.setMember_id(member.getMember_id());
                        memberSearchRecord.setCreate_time(new Date());
                        memberSearchRecord.setSearch_text(searchText);
                        memberService.saveSearchRecord(memberSearchRecord);
                    }
                    List<Works> worksList = worksService.selectBySearchText(searchText, 10);
                    mv.addObject("worksList", worksList);
                    List<Goods> goodsList = goodsService.selectBySearchText(searchText, 10);
                    mv.addObject("goodsList", goodsList);
                    mv.setViewName("mobile/search");
                    return mv;
                }

            }
        }
        mv.setViewName("mobile/index");
        return mv;
    }

    /**
     * 分类商品x详情
     * @param request
     * @return
     */
    @RequestMapping("/classify/goods/detail")
    public ModelAndView getGoodsDetail(HttpServletRequest request){
        ModelAndView mv  = new ModelAndView();
        String goods_detail_id = request.getParameter("id");

        mv.setViewName("mobile/classify/details");
        return mv;
    }

    /**
     * 音视频详情
     * @param request
     * @return
     */
    @RequestMapping("/mediaDetails")
    public ModelAndView getMediaDetails(HttpServletRequest request){
        ModelAndView mv  = new ModelAndView();
//        String resource_id = request.getParameter("id");
        mv.setViewName("mobile/mediaDetails");
        return mv;
    }
    /**
     * 音视频详情
     * @param request
     * @return
     */
    @RequestMapping("/errorPage")
    public ModelAndView getError(HttpServletRequest request){
        ModelAndView mv  = new ModelAndView();
//        String resource_id = request.getParameter("id");
        mv.setViewName("mobile/error");
        return mv;
    }
}
