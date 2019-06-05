package com.szkj.platform.wx.ctrl;

import com.szkj.platform.busiz.domain.Member;
import com.szkj.platform.busiz.domain.MemberAddress;
import com.szkj.platform.busiz.service.MemberAddressService;
import com.szkj.platform.busiz.service.MemberService;
import com.szkj.platform.system.constants.Constants;
import com.szkj.platform.utils.JsonResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.szkj.platform.system.utils.MessageAPi;
import com.szkj.platform.system.utils.MsgCodeEnum;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(value = "/api/wx/member/address/")
public class MemberAddressController {

    @Autowired
    private MemberAddressService memberAddressService;
    @Autowired
    private MemberService memberService;

    /**
     * 会员地址列表
     * @param request
     * @return
     */
    @RequestMapping(value = "list")
    @ResponseBody
    public Object getlist(HttpServletRequest request){
        Object member1 = request.getSession().getAttribute("member");
        if (member1 == null){
            return JsonResult.getExpire("登录超时！");
        }
        Member member = (Member)member1;
        try{
            List<MemberAddress> list = memberAddressService.pageQuery(member.getMember_id());
            JsonResult result = JsonResult.getSuccess(Constants.LOAD_SUCCESS);
            result.setData(list);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    /**
     * 会员收货地址新增、修改
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "save")
    @ResponseBody
    public Object save(HttpServletRequest request,  MemberAddress bean){
        Object member1 = request.getSession().getAttribute("member");
        if (member1 == null){
            return JsonResult.getExpire("登录超时！");
        }
        Member member = (Member)member1;
        bean.setMember_id(member.getMember_id());
        try{
            if (bean.getMember_address_id() == null){
                memberAddressService.save(bean);
                JsonResult result = JsonResult.getSuccess(Constants.ACTION_ADD);
                result.setData(bean);
                return result;
            }else {
                MemberAddress memberAddress = memberAddressService.selectById(bean.getMember_address_id());
                if (memberAddress == null){
                    return JsonResult.getError("收货地址不存在");
                }
                memberAddressService.update(bean, memberAddress);
                JsonResult result = JsonResult.getSuccess(Constants.ACTION_UPDATE);
                result.setData(memberAddress);
                return result;
            }
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    /**
     * 删除收货地址
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "del")
    @ResponseBody
    public Object del(HttpServletRequest request, MemberAddress bean){
        try{
            MemberAddress memberAddress = memberAddressService.selectById(bean.getMember_address_id());
            if (memberAddress == null){
                return JsonResult.getError("收货信息不存在");
            }
            memberAddressService.delete(bean.getMember_address_id());
            JsonResult result = JsonResult.getSuccess(Constants.ACTION_DELETE);
            result.setData(new ArrayList<>());
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    /**
     * 设置默认收货地址
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "setdefault")
    @ResponseBody
    public Object setDefault(HttpServletRequest request,MemberAddress bean){
        Object member1 = request.getSession().getAttribute("member");
        if (member1 == null){
            return JsonResult.getExpire("登录超时！");
        }
        Member member = (Member)member1;
        try{
            MemberAddress memberAddress = memberAddressService.selectById(bean.getMember_address_id());
            if (memberAddress == null){
                return JsonResult.getError("收货信息不存在");
            }
            //把客户的所有的收货信息改为不推荐
            memberAddressService.cancelDefault(member.getMember_id());
            //把选中的信息改为默认
            memberAddressService.setDefault(bean.getMember_address_id());
            JsonResult result = JsonResult.getSuccess(Constants.ACTION_SUCCESS);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    /**
     * 获取收货地址
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("default")
    @ResponseBody
    public Object getAddress(HttpServletRequest request,MemberAddress bean){
        Object member1 = request.getSession().getAttribute("member");
        if (member1 == null){
            return JsonResult.getExpire("登录超时！");
        }
        Member member = (Member)member1;
        try{
            MemberAddress address;
            if (bean.getMember_address_id() != null){
                address = memberAddressService.selectById(bean.getMember_address_id());
            }else {
                address = memberAddressService.getDefaultAddress(member.getMember_id());
            }
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
            jsonResult.setData(address);
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException("数据异常！");
        }
    }
}
