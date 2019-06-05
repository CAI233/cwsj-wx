package com.szkj.platform.system.ctrl;

import com.szkj.platform.system.beans.VoteContestantListBean;
import com.szkj.platform.system.beans.VoteEnabledBean;
import com.szkj.platform.system.beans.VoteHtmlBean;
import com.szkj.platform.system.conditions.IdsCondition;
import com.szkj.platform.system.conditions.SortCondition;
import com.szkj.platform.system.constants.Constants;
import com.szkj.platform.system.domain.VoteContestant;
import com.szkj.platform.system.domain.VoteManage;
import com.szkj.platform.system.domain.VoteSetting;
import com.szkj.platform.system.domain.VoteUser;
import com.szkj.platform.system.service.BaseService;
import com.szkj.platform.system.service.VoteManageService;
import com.szkj.platform.system.utils.CreateHtmlUtils;
import com.szkj.platform.system.utils.MessageAPi;
import com.szkj.platform.system.utils.MsgCodeEnum;
import com.szkj.platform.utils.ExportExcel;
import com.szkj.platform.utils.JsonResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;


@Controller
@RequestMapping("/api/system/vote")
public class VoteManageController {

    @Autowired
    private VoteManageService voteManageService;

    @Autowired
    private BaseService baseService;

    /**
     * 获取投票设置
     * @param request
     * @return
     */
    @RequestMapping("/setting/getSetting")
    @ResponseBody
    public Object getSetting(HttpServletRequest request){
        try{
            VoteSetting setting = voteManageService.getSetting();
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
            jsonResult.setData(setting);
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 保存投票设置
     * @param request
     * @param setting
     * @return
     */
    @RequestMapping("/setting/save")
    @ResponseBody
    public Object saveSetting(HttpServletRequest request,@RequestBody VoteSetting setting){
        try {
            voteManageService.saveSetting(setting);
            JsonResult jsonResult =JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.SAVE_SUCCESS.code()));
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }


    /**
     * 投票列表
     * @param request
     * @return
     */
    @RequestMapping("/manage/list")
    @ResponseBody
    public Object manageList(HttpServletRequest request , @RequestBody SortCondition condition){
        try{
            Object result = voteManageService.getList(condition);
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
            jsonResult.setData(result);
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 新增、修改投票
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/manage/save")
    @ResponseBody
    public Object manageSave(HttpServletRequest request, @RequestBody VoteManage bean){
        try{
            JsonResult jsonResult = JsonResult.getSuccess("");
            if (bean.getVote_id() == null){
                //新增
                if (StringUtils.isNotEmpty(bean.getVote_img())) {
                    //压缩图
                    String vote_img_small = baseService.getImgScale(bean.getVote_img(), "small", 0.5);
                    bean.setVote_img_small(vote_img_small);
                }
                voteManageService.saveVote(bean);
                jsonResult.setMessage(MessageAPi.getMessage(MsgCodeEnum.ADD_SUCCESS.code()));
            }else {
                //修改
                VoteManage vote = voteManageService.selectVoteByVoteId(bean.getVote_id());
                if (StringUtils.isNotEmpty(bean.getVote_img())  && !bean.getVote_img().equals(vote.getVote_img() )){
                    //压缩图
                    String vote_img_small = baseService.getImgScale(bean.getVote_img(), "small", 0.5);
                    bean.setVote_img_small(vote_img_small);
                }
                voteManageService.updateVote(bean);
                jsonResult.setMessage(MessageAPi.getMessage(MsgCodeEnum.UPDATE_SUCCESS.code()));
            }
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 删除投票
     * @param request
     * @param condition
     * @return
     */
    @RequestMapping("/manage/del")
    @ResponseBody
    public Object manageDel(HttpServletRequest request , @RequestBody IdsCondition condition){
        try{
            String vote_ids = StringUtils.join(condition.getIds(),",");
            voteManageService.delVotes(vote_ids);
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.DEL_SUCCESS.code()));
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 投票选手列表
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/contestant/list")
    @ResponseBody
    public Object contestantList(HttpServletRequest request , @RequestBody VoteContestantListBean bean){
        try{
            if (bean.getVote_id() == null){
                return JsonResult.getError("请选择投票！");
            }
            Object result = voteManageService.getContestantList(bean);
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
            jsonResult.setData(result);
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 投票新增修改选手
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/contestant/save")
    @ResponseBody
    public Object contestantSave(HttpServletRequest request,@RequestBody VoteContestant bean){
        try{
           JsonResult jsonResult = JsonResult.getSuccess("");
           if (bean.getVote_id() == null){
               return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
           }
           if (bean.getId() == null){
               //新增选手
               if (StringUtils.isNotEmpty(bean.getCover())) {
                   //压缩图
                   String cover_small = baseService.getImgScale(bean.getCover(), "small", 0.5);
                   bean.setCover_small(cover_small);
               }
               //处理选手相册集压缩
               if (StringUtils.isNotEmpty(bean.getContestant_imgs())){
                   String contestant_imgs_small = getImgSmall(bean.getContestant_imgs());
                   bean.setContestant_imgs_small(contestant_imgs_small);
               }
               voteManageService.saveContestant(bean);
               jsonResult.setMessage(MessageAPi.getMessage(MsgCodeEnum.ADD_SUCCESS.code()));
           }else {
               //修改选手
                VoteContestant contestant = voteManageService.selectContestantById(bean.getId());
                if (StringUtils.isNotEmpty(bean.getCover()) && !bean.getCover().equals(contestant.getCover())){
                    //压缩图
                    String cover_small = baseService.getImgScale(bean.getCover(), "small", 0.5);
                    bean.setCover_small(cover_small);
                }
                voteManageService.updateContestant(bean);
                jsonResult.setMessage(MessageAPi.getMessage(MsgCodeEnum.UPDATE_SUCCESS.code()));
           }
           return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 删除投票选手
     * @param request
     * @param condition
     * @return
     */
    @RequestMapping("/contestant/del")
    @ResponseBody
    public Object contestantDel(HttpServletRequest request , @RequestBody IdsCondition condition){
        try{
            String ids = StringUtils.join(condition.getIds(),",");
            voteManageService.DelContestant(ids);
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.DEL_SUCCESS.code()));
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 审核选手
     * @param request
     * @param condition
     * @return
     */
    @RequestMapping("/contestant/enabled")
    @ResponseBody
    public Object contestantEnabled(HttpServletRequest request ,@RequestBody VoteEnabledBean condition){
        try{
            String ids = StringUtils.join(condition.getIds(),",");
            if (condition.getEnabled() == null){
                condition.setEnabled(Constants.ENABLE);
            }
            voteManageService.updateEnabled(ids,condition.getEnabled());
            JsonResult jsonResult = JsonResult.getSuccess("审核通过！");
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 锁定选手
     * @param request
     * @param condition
     * @return
     */
    @RequestMapping("/contestant/lock")
    @ResponseBody
    public Object contestantLock(HttpServletRequest request ,@RequestBody VoteEnabledBean condition){
        try{
            String ids = StringUtils.join(condition.getIds(),",");
            if (condition.getIs_lock() == null){
                condition.setIs_lock(2);
            }
            voteManageService.updateLock(ids,condition.getIs_lock());
            JsonResult jsonResult = JsonResult.getSuccess("选手已锁定！");
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 获取投票选手用户列表
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/user/list")
    @ResponseBody
    public Object contestantUserList(HttpServletRequest request, @RequestBody VoteContestantListBean bean){
        try {
            if (bean.getContestant_id() == null){
                return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
            }
            Object list = voteManageService.getVoteUserList(bean);
            JsonResult jsonResult = JsonResult.getSuccess(MessageAPi.getMessage(MsgCodeEnum.LOAD_SUCCESS.code()));
            jsonResult.setData(list);
            return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 投票
     * @param request
     * @param user
     * @return
     */
    @RequestMapping("/cast")
    @ResponseBody
    public Object castVote(HttpServletRequest request,@RequestBody VoteUser user){
        try {
           JsonResult jsonResult = voteManageService.castVote(user);
           return jsonResult;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
        }
    }

    /**
     * 导出排行数据
     * @param request
     * @param response
     * @param vote_id
     */
    @RequestMapping("/export")
    @ResponseBody
    public void export(HttpServletRequest request, HttpServletResponse response , @RequestParam("vote_id") String vote_id){
        try{
            List<VoteContestant> list = voteManageService.getRankList(vote_id);
            String title = "排行榜";
            String[] rowName = {"排名","编号","手机号","名称","票数"};
            List<Object[]>  dataList = new ArrayList<Object[]>();
            for (int i = 0;i<list.size();i++){
                VoteContestant contestant = list.get(i);
                Object[] obj = {"",contestant.getSerial_num(),contestant.getPhone(),contestant.getContestant_name(),contestant.getVotes()};
                dataList.add(obj);
            }
            ExportExcel excel = new ExportExcel(title,rowName,dataList,response);
            excel.export();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 处理选手相册集压缩
     * @param img_paths
     * @return
     */
    public String getImgSmall(String img_paths){
        String img_small = "";
        if (StringUtils.isNotEmpty(img_paths)) {
            String[] img_path_small = img_paths.split(",");
            for (int i = 0; i < img_path_small.length; i++) {
                img_path_small[i] = baseService.getImgScale(img_path_small[i], "small", 0.5);
            }
            img_small = StringUtils.join(img_path_small, ",");
        }
        return img_small;
    }


    /**
     * 生成模板文件
     * @param request
     * @param bean
     * @return
     */
    @RequestMapping("/tohtml")
    @ResponseBody
    public Object toHtml(HttpServletRequest request,@RequestBody VoteHtmlBean bean){
        try{
            if (bean.getVote_id() == null){
                return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
            }
            //投票信息
            VoteManage vote = voteManageService.selectVoteByVoteId(bean.getVote_id());
            //选手列表
            List<VoteContestant> contestants = voteManageService.getContestantsById(vote.getVote_id());
            //生成html文件
            if (vote == null || (contestants == null && contestants.size() < 1  ) ){
                return JsonResult.getError(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));
            }
            String msg = CreateHtmlUtils.getHtml().createHtml(vote,contestants,bean.getTemplate());
            if ("success".equals(msg)){
                vote.setIs_html(1);
                voteManageService.updateVote(vote);
                JsonResult jsonResult = JsonResult.getSuccess(msg);
                return jsonResult;
            }else {
                JsonResult jsonResult = JsonResult.getError(msg);
                return jsonResult;
            }
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(MessageAPi.getMessage(MsgCodeEnum.EXCEPTION.code()));

        }
    }
}
