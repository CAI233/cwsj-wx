package com.szkj.platform.job;


import com.alibaba.fastjson.JSONObject;
import com.szkj.platform.busiz.beans.ImportResultsBean;
import com.szkj.platform.busiz.domain.QuestionFile;
import com.szkj.platform.busiz.mapper.ProblemMapper;
import com.szkj.platform.busiz.service.ImportSubService;
import com.szkj.platform.busiz.service.QuestionFileService;
import com.szkj.platform.busiz.utils.DocToHtml;
import com.szkj.platform.system.mapper.OrganizationMapper;
import com.szkj.platform.system.service.SpringContextUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by Administrator on 2018/3/16 0016.
 * 机构授权时间检测job
 */
@Component
@Configurable
@EnableScheduling
public class OrgAuthorization {
    @Autowired
    private ProblemMapper problemMapper;
//    @Autowired
//    private OrganizationMapper organizationMapper;
//
//    @Scheduled(cron = "0 0 3 * * ?")//每天凌晨3点检测机构授权时间是否到期
//    public void checkAuthorization() {
//        organizationMapper.checkAuthorization();
//    }

//    @Scheduled(cron = "0 0 * * * ?")//每小时监测问答是否过期
    public void checkWorksQuestionTimeOut() {
        problemMapper.checkWorksQuestionTimeOut();
    }


}
