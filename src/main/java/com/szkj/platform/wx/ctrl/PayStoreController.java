package com.szkj.platform.wx.ctrl;

import com.szkj.platform.busiz.beans.ShoppingCarListBean;
import com.szkj.platform.busiz.domain.*;
import com.szkj.platform.busiz.mapper.MemberPayRecordMapper;
import com.szkj.platform.busiz.service.GoodsService;
import com.szkj.platform.busiz.service.MemberAddressService;
import com.szkj.platform.busiz.service.OrderManageService;
import com.szkj.platform.busiz.service.PayStoreService;
import com.szkj.platform.utils.JsonResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class PayStoreController {

    @Autowired
    private PayStoreService payStoreService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private MemberAddressService memberAddressService;
    @Autowired
    private MemberPayRecordMapper memberPayRecordMapper;
    @Autowired
    private OrderManageService orderManageService;

    /**
     * 添加购物车、修改、移除商品
     * @param request
     * @return
     */
    @RequestMapping("/api/wx/shoppingcar/save")
    @ResponseBody
    public Object addShoppingCar(HttpServletRequest request){
        try{
            Object member1 = request.getSession().getAttribute("member");
            if (member1 == null){
                return JsonResult.getExpire("登录超时！");
            }
            Member member = (Member)member1;
            String goods_car_id = request.getParameter("id");
            if (StringUtils.isEmpty(goods_car_id)) {
                //新增商品到购物车
                String goodsid = request.getParameter("goods_id");
                if (StringUtils.isEmpty(goodsid)){
                    return JsonResult.getError("请选择商品！");
                }
                Long goods_id = Long.parseLong(goodsid);
                Goods goods = goodsService.selectById(goods_id);
                if (goods == null){
                    return JsonResult.getError("商品不存在!");
                }
                if (goods.getGoods_type() == 1 && goods.getInventory() < 1){
                    return JsonResult.getError("商品没有库存！");
                }
                //验证虚拟商品重复购买
                MemberPayRecord record = memberPayRecordMapper.selectByMemberAndGoods(member.getMember_id(),goods.getGoods_id());
                if (record != null){
                    return JsonResult.getError("商品已购买！");
                }
                ShoppingCar car = payStoreService.selectGoodsById(goods_id, member.getMember_id());
                if (car != null) {
                    if (goods.getGoods_type() == 1){
                        int count = car.getGoods_num();
                        count ++;
                        car.setGoods_num(count);
                        payStoreService.updateCarGoods(car);
                    }else {
                        return JsonResult.getError("电子商品只需购买一次！");
                    }
                } else {
                    car = new ShoppingCar();
                    car.setGoods_num(1);
                    car.setGoods_id(goods_id);
                    car.setMember_id(member.getMember_id());
                    payStoreService.addCarGoods(car);
                }
                return JsonResult.getSuccess("成功加入购物车！");
            }else {
                String num = request.getParameter("goods_num");
                Integer goods_num = Integer.parseInt(num);
                Long id = Long.parseLong(goods_car_id);
                if (goods_num == 0){
                    //商品数量为0时删除该商品
                    payStoreService.delGoods(id);
                }else {
                    ShoppingCar car = payStoreService.selectById(id);
                    Goods goods = goodsService.selectById(car.getGoods_id());
                    if (goods.getGoods_type().intValue() != 1 && goods_num.intValue() > 1){
                        return JsonResult.getError("电子商品不可重复购买！");
                    }
                    car.setGoods_num(goods_num);
                    payStoreService.updateCarGoods(car);
                }
                return JsonResult.getSuccess("操作成功！");
            }
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException("数据异常！");
        }
    }

    /**
     * 用户购物车列表
     * @param request
     * @return
     */
    @RequestMapping("/api/wx/shoppingcar/list")
    @ResponseBody
    public Object getList(HttpServletRequest request){
        Object member1 = request.getSession().getAttribute("member");
        if (member1 == null){
            return JsonResult.getExpire("登录超时！");
        }
        Member member = (Member)member1;
        List<ShoppingCarListBean> list = payStoreService.getShoppingCarList(member.getMember_id());
        JsonResult jsonResult = JsonResult.getSuccess("数据加载成功！");
        jsonResult.setData(list);
        return jsonResult;
    }

    /**
     * 查询订单运费和价格
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/api/wx/order/price")
    @ResponseBody
    public Object getPrice(HttpServletRequest request ,@RequestBody OrderFormBean bean){
        Object member1 = request.getSession().getAttribute("member");
        if (member1 == null){
            return JsonResult.getExpire("登录超时！");
        }
        Member member = (Member)member1;
        try{
            bean.setMember_id(member.getMember_id());
            if (bean.getIds() == null || bean.getIds().length < 1){
                return JsonResult.getError("请选择购物车商品！");
            }
            if (StringUtils.isEmpty(bean.getProvince())){
                return JsonResult.getError("请选择收货地址！");
            }
            return payStoreService.getPrice(bean);
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException("数据异常！");
        }
    }

    /**
     * 提交订单
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/api/wx/order/submit")
    @ResponseBody
    public Object submitOrder(HttpServletRequest request ,@RequestBody OrderFormBean bean){
        try {
            Object member1 = request.getSession().getAttribute("member");
            if (member1 == null){
                return JsonResult.getExpire("登录超时！");
            }
            Member member = (Member)member1;

            if (bean.getId() == null) {
                return JsonResult.getError("数据不全！");
            }
            if (bean.getOrder_status() == null){
                bean.setOrder_status(1);
            }
            if (bean.getIds() == null || bean.getIds().length <1){
                return JsonResult.getError("请选择需要提交订单的商品！");
            }
            bean.setMember_id(member.getMember_id());
            return payStoreService.submitOrder(bean);
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException("数据异常！");
        }
    }

    /**
     * 提交订单页面数据
     * @param request
     * @return
     */
    @RequestMapping("/orderPay")
    public ModelAndView orderPay(HttpServletRequest request){
        ModelAndView mv = new ModelAndView();
        Object member1 = request.getSession().getAttribute("member");
        if (member1 != null){
            Member member = (Member)member1;
            //没有id则选择默认地址
            MemberAddress address =  memberAddressService.getDefaultAddress(member.getMember_id());
            mv.addObject("address",address);
            String ids = request.getParameter("ids");
            if (StringUtils.isNotEmpty(ids)){
                List<ShoppingCarListBean> list = payStoreService.getOrderPayGoods(ids);
                mv.addObject("goods",list);
                //查询价格
                if (address == null){
                    //没有收获地址运费为0
                    mv.addObject("express_fee",0.0);
                    JsonResult result = payStoreService.getPriceByIds(ids);
                    if (result.getCode() == 0) {
                        mv.addObject("price", result.getData());
                        mv.setViewName("mobile/orderPay");
                    }else {
                        mv.setViewName("mobile/index");
                    }
                }else {
                    JsonResult jsonResult = payStoreService.getExpress_fee(ids,address.getProvince());
                    if (jsonResult.getCode() == 0){
                        mv.addObject("express_fee",jsonResult.getData());
                        JsonResult result = payStoreService.getPriceByIds(ids);
                        if (result.getCode() == 0) {
                            mv.addObject("price", result.getData());
                            mv.setViewName("mobile/orderPay");
                        }else {
                            mv.setViewName("mobile/index");
                        }
                    }else {
                        mv.setViewName("mobile/index");
                    }
                }
            }else {
                mv.setViewName("mobile/index");
            }
        }else {
            mv.setViewName("mobile/index");
        }
        return mv;
    }

    /**
     * 确认收货
     * @param request
     * @return
     */
    @RequestMapping("/api/wx/finish/order")
    @ResponseBody
    public Object finishOrder(HttpServletRequest request){
        Object member1 = request.getSession().getAttribute("member");
        if (member1 == null){
            return JsonResult.getExpire("登录超时！");
        }
        String order_id = request.getParameter("order_id");
        if (StringUtils.isEmpty(order_id)){
            return JsonResult.getError("请选择订单！");
        }
        //修改订单状态
        orderManageService.updateStatus(Long.parseLong(order_id),4);
        return JsonResult.getSuccess("确认完成！");
    }
}
