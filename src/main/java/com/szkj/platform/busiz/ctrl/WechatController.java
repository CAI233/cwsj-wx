package com.szkj.platform.busiz.ctrl;

import com.alibaba.fastjson.JSON;
import com.szkj.platform.busiz.beans.OrderGoodsListBean;
import com.szkj.platform.busiz.domain.*;
import com.szkj.platform.busiz.mapper.BookShelfMapper;
import com.szkj.platform.busiz.mapper.MemberPayRecordMapper;
import com.szkj.platform.busiz.mapper.OrderGoodsRelMapper;
import com.szkj.platform.busiz.mapper.ProblemMapper;
import com.szkj.platform.busiz.service.BookShelfService;
import com.szkj.platform.busiz.service.OrderManageService;
import com.szkj.platform.busiz.service.WechatService;
import com.szkj.platform.busiz.utils.AccessTokenUtil;
import com.szkj.platform.busiz.utils.CheckoutUtil;
import com.szkj.platform.system.utils.MessageAPi;
import com.szkj.platform.system.utils.MsgCodeEnum;
import com.szkj.platform.utils.IDUtil;
import com.szkj.platform.utils.JsonResult;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.*;

@Controller
@RequestMapping(value = "/")
public class WechatController {

    @Autowired
    private WechatService wechatService;

    @Autowired
    private BookShelfMapper bookShelfMapper;
    @Autowired
    private ProblemMapper problemMapper;
    @Autowired
    private OrderManageService orderManageService;
    @Autowired
    private OrderGoodsRelMapper orderGoodsRelMapper;
    @Autowired
    private MemberPayRecordMapper memberPayRecordMapper;

    /**
     * 微信消息接收和token验证
     *
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping("api/busiz/wechat/checktoken")
    @ResponseBody
    public void wxAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
        boolean isGet = request.getMethod().toLowerCase().equals("get");
        PrintWriter print = null;
        if (isGet) {
            // 微信加密签名
            String signature = request.getParameter("signature");
            // 时间戳
            String timestamp = request.getParameter("timestamp");
            // 随机数
            String nonce = request.getParameter("nonce");
            // 随机字符串
            String echostr = request.getParameter("echostr");
            System.out.println("=============  " + echostr + "===============");

            // 通过检验signature对请求进行校验，若校验成功则原样返回echostr，表示接入成功，否则接入失败
            if (signature != null && CheckoutUtil.checkSignature(signature, timestamp, nonce)) {
                try {
                    print = response.getWriter();
                    print.write(echostr);
                    print.flush();
                    print.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } else {
            //设置编码
            request.setCharacterEncoding("UTF-8");
            response.setCharacterEncoding("UTF-8");
            PrintWriter printWriter = null;
            try {
                Map map = parseXml(request);

                //如果是扫码则推送图文消息
                //关注了公众号
                //通过二维码 编号得到所有作品
                String str = wechatService.wxAction(map, request);
//                System.out.println("==== " + str + " ====");
                // 第三步，发送xml的格式信息给微信服务器，服务器转发给用户

                printWriter = response.getWriter();
                printWriter.print(str);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (printWriter != null) {
                    printWriter.close();
                    printWriter = null;
                }
            }
        }
    }

    /**
     * 微信更新底部菜单
     *
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping("api/busiz/wechat/refreshMenu")
    @ResponseBody
    public Object refreshMenu(HttpServletRequest request, HttpServletResponse response) {
        AccessTokenUtil.refreshMenu();
        return JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.UPDATE_SUCCESS.code()));

    }

    //把request信息转化为map
    public Map<String, String> parseXml(HttpServletRequest request) throws Exception {
        // 将解析结果存储在HashMap中
        Map<String, String> map = new HashMap<String, String>();

        // 从request中取得输入流
        InputStream inputStream = request.getInputStream();
        // 读取输入流
        SAXReader reader = new SAXReader();
        Document document = reader.read(inputStream);
        // 得到xml根元素
        Element root = document.getRootElement();
        // 得到根元素的所有子节点
        List<Element> elementList = root.elements();

        // 遍历所有子节点
        for (Element e : elementList) {
//            System.out.println(e.getName() + " = " + e.getText());
            map.put(e.getName(), e.getText());
        }

        // 释放资源
        inputStream.close();
        inputStream = null;
        return map;
    }

    //把xml信息转化为map
    public Map<String, String> parseXml(String xml) throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        Document doc;
        try {
            doc = DocumentHelper.parseText(xml);
            Element el = doc.getRootElement();
            map = recGetXmlElementValue(el, map);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return map;
    }

    //递归得到map
    private Map<String, String> recGetXmlElementValue(Element ele, Map<String, String> map) {
        List<Element> eleList = ele.elements();
        if (eleList.size() == 0) {
            map.put(ele.getName(), ele.getTextTrim());
            return map;
        } else {
            for (Iterator<Element> iter = eleList.iterator(); iter.hasNext(); ) {
                Element innerEle = iter.next();
                recGetXmlElementValue(innerEle, map);
            }
            return map;
        }
    }

    /**
     * 用户提交支付，获取微信支付订单接口
     */
    @RequestMapping(value = "wechatPay")
    public ModelAndView pay(HttpServletRequest request, HttpServletResponse response) {

//        AccessTokenUtil.authorization(request, response, wechatService);
        ModelAndView mv = new ModelAndView();
        String APP_ID = AccessTokenUtil.getAPPID();// 微信公众号id
        String MCH_ID = AccessTokenUtil.getMchId();// 商户号

    /*------1.获取参数信息------- */
        //商户订单号
        String out_trade_no = request.getParameter("order");
        //价格(单位：分)
        String money = request.getParameter("money");
    /*------2.根据code获取微信用户的openId和access_token------- */
        //注： 如果后台程序之前已经得到了用户的openId 可以不需要这一步，直接从存放openId的位置或session中获取就可以。
        //toPay.jsp页面中提交的url路径也就不需要再经过微信重定向。
        String openid = null;
        Object member = request.getSession(true).getAttribute("member");
        if (member == null) {
            mv.addObject("ErrorMsg", "member为空");
            mv.setViewName("mobile/error");
            return mv;
        }
        openid = ((Member) member).getOpenid();

        /*-----------业务逻辑begin-----------*/
        //区分商品作品付款
        if (StringUtils.isEmpty(out_trade_no)) {
            //作品id
            String id = request.getParameter("id");
            //添加到支付表
            MemberPayRecord record = new MemberPayRecord();
            record.setMember_id(((Member) member).getMember_id());
            record.setCreate_time(new Date());
            record.setPay_fee(Double.parseDouble(money));
            record.setBuy_type(2);
            record.setBuy_id(Long.parseLong(id));
            record.setBuy_count(1);
            record.setEnabled(2);//设置状态为未付款
            if (request.getParameter("child_id") != null) {
                //如果有child_id则为问答
                record.setChild_id(Long.parseLong(request.getParameter("child_id")));
                //查询是否有该问答订单
                out_trade_no = memberPayRecordMapper.selectByChild(Long.parseLong(request.getParameter("child_id")));
                if (out_trade_no == null) {
                    out_trade_no = IDUtil.createId();
                    record.setOrder_no(out_trade_no);
                    memberPayRecordMapper.insert(record);
                }
            } else {
                //查询是否有该作品订单
                out_trade_no = memberPayRecordMapper.selectOrderByMemberAndWorks(((Member) member).getMember_id(), Long.parseLong(id));
                if (out_trade_no == null) {
                    out_trade_no = IDUtil.createId();
                    record.setOrder_no(out_trade_no);
                    memberPayRecordMapper.insert(record);
                }
            }
        }
        /*------------业务逻辑end-----------*/

    /*------3.生成预支付订单需要的的package数据------- */
        //随机数
        String nonce_str = AccessTokenUtil.getMessageDigest(String.valueOf(new Random().nextInt(10000)).getBytes());
        //订单生成的机器 IP
        String spbill_create_ip = request.getRemoteAddr();
        //交易类型 ：jsapi代表微信公众号支付
        String trade_type = "JSAPI";
        //这里notify_url是 微信处理完支付后的回调的应用系统接口url。
        String notify_url = AccessTokenUtil.getPayNotifyUri();

        SortedMap<String, String> packageParams = new TreeMap<String, String>();
        packageParams.put("appid", APP_ID);
        packageParams.put("mch_id", MCH_ID);
        packageParams.put("nonce_str", nonce_str);
        packageParams.put("body", "price");
        packageParams.put("out_trade_no", out_trade_no);
//        packageParams.put("total_fee", Double.parseDouble(money) * 100 + "");
        packageParams.put("total_fee", "1");
        packageParams.put("spbill_create_ip", spbill_create_ip);
        packageParams.put("notify_url", notify_url);
        packageParams.put("trade_type", trade_type);
        packageParams.put("openid", openid);

    /*------4.根据package数据生成预支付订单号的签名sign------- */
        String sign = AccessTokenUtil.createSign(packageParams);

    /*------5.生成需要提交给统一支付接口https://api.mch.weixin.qq.com/pay/unifiedorder 的xml数据-------*/
        String xml = "<xml>" +
                "<appid>" + APP_ID + "</appid>" +
                "<mch_id>" + MCH_ID + "</mch_id>" +
                "<nonce_str>" + nonce_str + "</nonce_str>" +
                "<sign>" + sign + "</sign>" +
                "<body><![CDATA[" + "price" + "]]></body>" +
                "<out_trade_no>" + out_trade_no + "</out_trade_no>" +
//                "<total_fee>" + Double.parseDouble(money) * 100 + "</total_fee>" +
                "<total_fee>" + 1 + "</total_fee>" +
                "<spbill_create_ip>" + spbill_create_ip + "</spbill_create_ip>" +
                "<notify_url>" + notify_url + "</notify_url>" +
                "<trade_type>" + trade_type + "</trade_type>" +
                "<openid>" + openid + "</openid>" +
                "</xml>";
        //System.out.println("统一下单： " + xml);

    /*------6.调用统一支付接口https://api.mch.weixin.qq.com/pay/unifiedorder 生产预支付订单----------*/
        String createOrderURL = AccessTokenUtil.getPayOrderUri();
        String prepay_id = "";
        String result = "";
        try {
            result = AccessTokenUtil.sendPost(xml, createOrderURL);
//            System.out.println("统一支付接口返回 : " + result);
            Map<String, String> map = parseXml(result);
            //得到prepay_id
            prepay_id = map.get("prepay_id");
            System.out.println(" prepay_id = " + prepay_id);

            if (result == null) {
                mv.addObject("ErrorMsg", "支付错误");
                mv.setViewName("mobile/error");
                return mv;
            }
        } catch (Exception e) {
            System.out.println("统一支付接口获取预支付订单出错" + e);
            mv.setViewName("mobile/error");
            return mv;
        }

    /*------7.将预支付订单的id和其他信息生成签名并一起返回到jsp页面 ------- */
        nonce_str = AccessTokenUtil.getMessageDigest(String.valueOf(new Random().nextInt(10000)).getBytes());
        SortedMap<String, String> finalpackage = new TreeMap<String, String>();
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String packages = "prepay_id=" + prepay_id;
        finalpackage.put("appId", APP_ID);
        finalpackage.put("timeStamp", timestamp);
        finalpackage.put("nonceStr", nonce_str);
        finalpackage.put("package", packages);
        finalpackage.put("signType", "MD5");
        String finalsign = AccessTokenUtil.createSign(finalpackage);

        mv.addObject("appid", APP_ID);
        mv.addObject("timeStamp", timestamp);
        mv.addObject("nonceStr", nonce_str);
        mv.addObject("packageValue", packages);
        mv.addObject("paySign", finalsign);
        mv.addObject("success", "ok");
        mv.setViewName("mobile/wechatPay");
        return mv;
    }

    /**
     * 提交支付后的微信异步返回接口
     */
    @RequestMapping(value = "wechatNotify")
    public void weixinNotify(HttpServletRequest request, HttpServletResponse response) {
        String out_trade_no = null;
        String return_code = null;

        try {
//            System.out.println("微信支付回调接口");

            Map<String, String> resultMap = parseXml(request);
            String result_code = resultMap.get("result_code");
            String is_subscribe = resultMap.get("is_subscribe");
            String transaction_id = resultMap.get("transaction_id");
            String sign = resultMap.get("sign");
            String time_end = resultMap.get("time_end");
            String bank_type = resultMap.get("bank_type");

            out_trade_no = resultMap.get("out_trade_no");
            return_code = resultMap.get("return_code");
            //通知前台
            //request.setAttribute("out_trade_no", out_trade_no);
            //通知微信.异步确认成功.必写.不然微信会一直通知后台.八次之后就认为交易失败了.
            String xml = "<xml> <return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
            response.getWriter().write(xml);
        } catch (Exception e) {
//            System.out.println("微信回调接口出现错误");
            try {
                String xml = "<xml> <return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[error]]></return_msg></xml>";
                response.getWriter().write(xml);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        if (return_code.equals("SUCCESS")) {
            System.out.println("支付成功，订单号：" + out_trade_no);
            //支付成功的业务逻辑
            if (StringUtils.isNotEmpty(out_trade_no)) {
                List<OrderGoodsListBean> goodsListByOrder = orderGoodsRelMapper.getGoodsListByOrder(out_trade_no);
                int haveBook = 0;
                //判断是否购买的商品
                if (goodsListByOrder != null && goodsListByOrder.size() > 0) {
                    List<MemberPayRecord> records = new ArrayList<MemberPayRecord>();
                    List<BookShelf> shelves = new ArrayList<BookShelf>();
                    for (OrderGoodsListBean orderGoodsListBean : goodsListByOrder) {
                        //添加商品支付记录
                        MemberPayRecord record = new MemberPayRecord();
                        record.setMember_id(orderGoodsListBean.getMember_id());
                        record.setCreate_time(new Date());
                        record.setPay_fee(orderGoodsListBean.getGoods_price());
                        record.setBuy_type(1);
                        record.setBuy_id(orderGoodsListBean.getGoods_id());
                        record.setBuy_count(orderGoodsListBean.getGoods_count());
                        record.setEnabled(1);//设置状态为已付款
                        record.setOrder_no(out_trade_no);
                        records.add(record);

                        //查询购买物品是否为实体书
                        if (orderGoodsListBean.getGoods_type() != 2) {//购买成功加入书架（实体书,电子书）
                            BookShelf bookShelf = new BookShelf();
                            bookShelf.setGoods_id(orderGoodsListBean.getGoods_id());
                            bookShelf.setMember_id(orderGoodsListBean.getMember_id());
                            bookShelf.setIs_delete(2);
                            bookShelf.setCreate_time(new Date());
                            bookShelf.setUpdate_time(new Date());
                            shelves.add(bookShelf);
                            if (orderGoodsListBean.getGoods_type() == 1)
                                haveBook++;
                        }
                    }
                    if (shelves.size() > 0)
                        bookShelfMapper.insertList(shelves);
                    if (records.size() > 0) {
                        memberPayRecordMapper.insertList(records);
                    }
                }
                //更改订单状态为待发货
                orderManageService.updateStatusByOrder(out_trade_no, 2);
                if (haveBook == 0) {
                    //如果没有实体书更改订单状态为已收货
                    orderManageService.updateStatusByOrder(out_trade_no, 4);
                }
                //修改支付记录为正常
                memberPayRecordMapper.updateStatusByOrder(out_trade_no);

                //问答更改购买状态
                MemberPayRecord memberPayRecord = memberPayRecordMapper.selectByOrder(out_trade_no);
                if (memberPayRecord != null && memberPayRecord.getChild_id() != null) {
                    //满足条件则为问答,更改问答为已支付
                    Long child_id = memberPayRecord.getChild_id();
                    problemMapper.updatePayStatus(child_id);
                }
            }
        } else {
            //支付失败的业务逻辑
            System.out.println("支付失败，订单号：" + out_trade_no);
        }
    }
}
