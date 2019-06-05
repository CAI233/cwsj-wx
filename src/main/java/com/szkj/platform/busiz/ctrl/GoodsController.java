package com.szkj.platform.busiz.ctrl;

import com.szkj.platform.busiz.beans.DelGoodsBean;
import com.szkj.platform.busiz.beans.GoodsBean;
import com.szkj.platform.busiz.beans.GoodsSortBean;
import com.szkj.platform.busiz.domain.Goods;
import com.szkj.platform.busiz.service.GoodsService;
import com.szkj.platform.busiz.service.QRCodeService;
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
 * 商品管理
 */
@Controller
@RequestMapping(value = "/api/busiz/goods/")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;
    @Autowired
    private QRCodeService qrCodeService;

    @Autowired
    private BaseService baseService;

    /**
     * 分页列表
     *
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "getlist")
    @ResponseBody
    public Object getList(HttpServletRequest request, @RequestBody GoodsSortBean bean) {
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try {
            Sort sort = null;
            //获取排序字段和排序规则
            if (StringUtils.isNotEmpty(bean.getSort_name()) && StringUtils.isNotEmpty(bean.getSort_rule())) {
                sort = new Sort("desc".equals(bean.getSort_rule()) ? Sort.Direction.DESC : Sort.Direction.ASC, "" + bean.getSort_name() + "");
            } else {
                //默认排序规则
                sort = new Sort(Sort.Direction.DESC, "create_time");
            }
            Object object = goodsService.pageQuery(bean, sort);
            JsonResult result = JsonResult.getSuccess(Constants.LOAD_SUCCESS);
            result.setData(object);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    /**
     * 新增、修改
     *
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "save")
    @ResponseBody
    public Object save(HttpServletRequest request, @RequestBody GoodsBean bean) {
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try {
            JsonResult jsonResult = JsonResult.getSuccess("");
            if (bean.getGoods_id() == null) {//商品id为空则为新增
                if (bean.getGoods_name() == null) {
                    return JsonResult.getError("商品名称不能为空");
                }
                if (bean.getPrice() == null) {
                    return JsonResult.getError("商品定价不能为空");
                }
                //处理压缩图片
                if (StringUtils.isNotEmpty(bean.getGoods_cover())) {
                    String goods_cover_small = baseService.getImgScale(bean.getGoods_cover(), "small", 0.5);
                    bean.setGoods_cover_small(goods_cover_small);
                }
                if (bean.getGoods_tag_ids()==null){
                    return JsonResult.getError("商品标签不能为空");
                }
                if (bean.getGoods_cat_id()==null){
                    return JsonResult.getError("商品标签不能为空");
                }
                goodsService.save(bean);
                jsonResult.setMessage(MessageAPi.getMessage(MsgCodeEnum.ADD_SUCCESS.code()));
                return jsonResult;

            } else {//修改
                Goods goods = goodsService.selectById(bean.getGoods_id());
                if (goods == null) {
                    return JsonResult.getError("商品不存在");
                }
                //处理压缩图片
                if (StringUtils.isNotEmpty(bean.getGoods_cover())) {
                    String goods_cover_small = baseService.getImgScale(bean.getGoods_cover(), "small", 0.5);
                    bean.setGoods_cover_small(goods_cover_small);
                }
                goodsService.update(goods, bean);
                jsonResult.setMessage(MessageAPi.getMessage(MsgCodeEnum.UPDATE_SUCCESS.code()));
                return jsonResult;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return JsonResult.getException(Constants.EXCEPTION);
    }

    /**
     * 批量删除
     *
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "del")
    @ResponseBody
    public Object del(HttpServletRequest request, @RequestBody DelGoodsBean bean) {
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try {

            //如果该商品关联对应的二维码则不能删除
            String codeName = qrCodeService.selectCodeByGoodsId(bean.getIds());
            if (StringUtils.isNotEmpty(codeName)) {
                return JsonResult.getException("该实体书商品关联了二维码：《" + codeName + "》,请先删除对应二维码");
            }
            goodsService.deleteByIds(bean.getIds());
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.DEL_SUCCESS.code()));
            return jsonResult;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JsonResult.getException(Constants.EXCEPTION);
    }

    /**
     * 商品上架/下架(批量)
     *
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "shelves")
    @ResponseBody
    public Object shelves(HttpServletRequest request, @RequestBody DelGoodsBean bean) {
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try {
            if (bean.getIds() == null || bean.getIds().length == 0) {
                return JsonResult.getError("商品id不能为空");
            }
            if (bean.getEnabled() == null) {
                return JsonResult.getError("上架状态不能为空");
            }
            Long[] ids = bean.getIds();
            String ids_str = StringUtils.join(ids, ",");
            goodsService.changeShelvesByIds(ids_str, bean.getEnabled());
            JsonResult result = JsonResult.getSuccess(Constants.ACTION_UPDATE);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JsonResult.getException(Constants.EXCEPTION);
    }


    /**
     * 商品详情
     *
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "detail")
    @ResponseBody
    public Object detail(HttpServletRequest request, @RequestBody Goods bean) {
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
        return JsonResult.getException(Constants.EXCEPTION);
    }

    /**
     * 更改审核状态
     *
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "audit")
    @ResponseBody
    public Object audit(HttpServletRequest request, @RequestBody IdsCondition bean) {
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try {
            if (bean.getIds() == null) {
                return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
            }
            if (bean.getAudit_status() == null) {
                bean.setAudit_status(com.szkj.platform.busiz.constants.Constants.AUDIT_WAIT);
            }
            String ids = StringUtils.join(bean.getIds(), ",");
            goodsService.updateAudit(ids, bean.getAudit_status());
            JsonResult jsonResult = JsonResult.getSuccess("操作成功！");
            return jsonResult;
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 分页列表（带评论数）
     *
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping(value = "getcommentlist")
    @ResponseBody
    public Object getCommentList(HttpServletRequest request, @RequestBody GoodsSortBean bean) {
        SysUser user = CheckUserHelper.checkUserInfo(request.getHeader("token"));
        if (user == null) {
            String message = MessageAPi.getMessage(MsgCodeEnum.TOKEN_FAILED.code());
            return JsonResult.getExpire(message);
        }
        try {
            Sort sort = null;
            //获取排序字段和排序规则
            if (StringUtils.isNotEmpty(bean.getSort_name()) && StringUtils.isNotEmpty(bean.getSort_rule())) {
                sort = new Sort("desc".equals(bean.getSort_rule()) ? Sort.Direction.DESC : Sort.Direction.ASC, "" + bean.getSort_name() + "");
            } else {
                //默认排序规则
                sort = new Sort(Sort.Direction.DESC, "create_time");
            }
            Object object = goodsService.pageQueryComment(bean, sort);
            JsonResult result = JsonResult.getSuccess(Constants.LOAD_SUCCESS);
            result.setData(object);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }
}
