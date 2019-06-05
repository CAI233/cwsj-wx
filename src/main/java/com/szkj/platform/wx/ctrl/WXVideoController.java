package com.szkj.platform.wx.ctrl;

import com.szkj.platform.busiz.beans.VideoListBean;
import com.szkj.platform.busiz.domain.Video;
import com.szkj.platform.busiz.service.VideoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 在线培训微信端
 * Created by Administrator on 2018/4/9 0009.
 */
@Controller
public class WXVideoController {

    @Autowired
    private VideoService videoService;

    /**
     * 视频详情
     * @param request
     * @return
     */
    @RequestMapping("/video/details")
    public ModelAndView videoInfo(HttpServletRequest request){
        ModelAndView mv = new ModelAndView();
        String video_id = request.getParameter("video_id");
        if (StringUtils.isNotEmpty(video_id)){
            //视频详情
            Video video = videoService.getDetailById(Long.parseLong(video_id));
            mv.addObject("video",video);
            mv.setViewName("mobile/videoDetail");
        }else {
            mv.setViewName("mobile/index");
        }
        return mv;
    }

    /**
     * 视频列表
     * @param request
     * @return
     */
    @RequestMapping("/onlineTrain")
    public ModelAndView videoCat(HttpServletRequest request){
        ModelAndView mv = new ModelAndView();
        List<VideoListBean> list = videoService.getVideoList();
        mv.addObject("videoList", list);
        mv.setViewName("mobile/onlineTrain");
        return mv;
    }

}
