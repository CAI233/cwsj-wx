package com.szkj.platform.wx.ctrl;


import com.szkj.platform.busiz.domain.PayOrder;
import com.szkj.platform.busiz.service.PayOrderService;
import com.szkj.platform.system.constants.Constants;
import com.szkj.platform.system.domain.WeixinPayConfig;
import com.szkj.platform.system.utils.MessageAPi;
import com.szkj.platform.system.utils.MsgCodeEnum;
import com.szkj.platform.system.utils.SignatureUtils;
import com.szkj.platform.utils.JsonResult;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;


@Controller
@RequestMapping("/api/system/pay")
public class PayController{


    @Autowired
    private PayOrderService payOrderService;

    @RequestMapping("/save/order")
    @ResponseBody
    public Object saveOrder(HttpServletRequest request , HttpServletResponse response, @RequestBody PayOrder order){
        try{
           if (order.getPay_type() == null){
               return JsonResult.getError("请选择支付方式！");
           }
           //生成订单
            payOrderService.saveOrder(order);
           if (order.getPay_type() == Constants.ALIPAY){
               //发起支付宝支付
               String result = payOrderService.alipay(order);
               response.setContentType("text/html;charset=utf-8");
               PrintWriter pw = response.getWriter();
               pw.println(result);
               pw.flush();
           }else if (order.getPay_type() == Constants.WECHAT_PAY){
                payOrderService.weixinPay(order,response);
           }
           return null ;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }


    /**
     * 支付宝支付失败回调接口
     * @param request
     * @return
     */
    @RequestMapping("/alipayNotify")
    @ResponseBody
    public String processAlipayNotify(javax.servlet.http.HttpServletRequest request) {
        PayOrder result = payOrderService.onNotify(request);
        if (result.getPay_status() == Constants.PAY_FAILED) {
            return "fail";
        } else {
            //判断该笔订单是否在商户网站中已经做过处理（可参考“集成教程”中“3.4返回数据处理”）
            //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
            //如果有做过处理，不执行商户的业务程序
            String orderNo = result.getOrder_no();
            PayOrder tradeOrder = payOrderService.getPayOderByOrderNo(orderNo);
            if (tradeOrder != null && tradeOrder.getPay_status() == Constants.PAY_SUCCESS) { //已处理
                return "success";
            } else {//未处理
                tradeOrder.setPay_status(result.getPay_status());//（1：未支付，2：成功， 3：失败）
                tradeOrder.setTrade_no(result.getTrade_no());
                tradeOrder.setPay_fee(result.getPay_fee());
                payOrderService.updateRecord(tradeOrder);
                return "success";
            }
        }
    }

    /**
     * 响应支付结果
     *
     * @param request
     * @return
     */
    @RequestMapping("/alipayReturn")
    public ModelAndView processAlipayReturn(javax.servlet.http.HttpServletRequest request) {
        PayOrder result;
        ModelAndView mv = new ModelAndView();
        mv.setViewName("回调成功跳转页面地址");
        String msg = "";
        try {
            result = payOrderService.onReturn(request);//获取支付宝网关返回的支付结果
            String orderNo = result.getOrder_no();//订单号
            Float payAmount = result.getPay_fee();//支付金额
            Integer pay_status = result.getPay_status();//交易状态
            //支付宝的返回结果写入支付记录表
            PayOrder tradeOrder = payOrderService.getPayOderByOrderNo(orderNo);
            if(tradeOrder != null) {
                tradeOrder.setPay_status(pay_status);//（1：未支付，2：成功\， 3：失败）
                tradeOrder.setTrade_no(result.getTrade_no());
                tradeOrder.setPay_fee(payAmount);
                payOrderService.updateRecord(tradeOrder);
                msg = "订单号为" + orderNo + "的订单";
                if (pay_status == Constants.PAY_FAILED) {
                    msg += "支付失败!";
                    mv.addObject("msg", msg);
                    return mv;
                }
                msg += "支付成功,支付金额为:¥" + payAmount + "元";
                /*
                *    代码逻辑
                Member member = memberService.selectById(tradeOrder.getMember_id());
                HttpSession session = request.getSession(true);
                session.setAttribute("MEMBER_INFO", member);
                session.setAttribute("memberinfo_json", JSON.toJSONStringWithDateFormat(member, "yyyy-MM-dd"));
                */
            }else{
                msg += "支付失败!";
            }
        } catch (Exception e) {
            e.printStackTrace();
            msg += "支付失败!";
        }
        mv.addObject("msg", msg);
        return mv;
    }

    /**
     * 微信支付回调函数
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/weixinCallback")
    public void callBack(javax.servlet.http.HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        InputStream is = request.getInputStream();
        HashMap<String, String> map = new HashMap<>();
        // 1、读取传入信息并转换为map
        SAXReader reader = new SAXReader();
        Document document = null;
        try {
            document = reader.read(is);
        } catch (DocumentException e1) {
            e1.printStackTrace();
        }
        Element root = document.getRootElement();
        List<Element> list = root.elements();
        for (Element e : list) {
            if (e.getName().trim().equals("payType") && e.getName().trim().equals("memberId")) {
            } else {
                map.put(e.getName().trim(), e.getText().trim());
            }
        }
        is.close();
        HashMap<String, String> signMap = (HashMap<String, String>) map.clone();
        WeixinPayConfig weixinPayConfig = payOrderService.selectWeixinPayEnabledConfig();
        signMap.remove("sign");
        String key= weixinPayConfig.getApp_key();
        String sign = SignatureUtils.signature(signMap,key);
        if (!sign.equals(map.get("sign"))) {
            System.out.println("-------签名错误-------");
        }
        // 信息处理
        String result_code = map.get("result_code");
        if ("SUCCESS".equals(result_code)) {

        } else if ("FAIL".equals(result_code)) {

        }

        // 返回信息，防止微信重复发送报文
        String result = "<xml>"
                + "<return_code><![CDATA[SUCCESS]]></return_code>"
                + "<return_msg><![CDATA[OK]]></return_msg>" + "</xml>";
        PrintWriter out = new PrintWriter(response.getOutputStream());
        out.print(result);
        out.flush();
        out.close();
    }

}