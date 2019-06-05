package com.szkj;

import com.szkj.filter.NoCacheFilter;
import com.szkj.platform.system.utils.CacheUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.support.ErrorPageFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;


/**
 * 启动时加载
 * Created by shiaihua on 16/10/3.
 */
@Component
public class ApplicationStartup implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationStartup.class);

    @Value("${job.enabled}")
    private String jobEnabled;

    @Value("${runserver.urlexcludes}")
    private String urlexcludes;

    @Value("${runserver.logexcludes}")
    private String logexcludes;


    @Bean
    public FilterRegistrationBean contextFilterRegistrationBean() {
        NoCacheFilter filter = new NoCacheFilter();
        filter.setLogfileExcludes(logexcludes);
        filter.setUrlExcludes(urlexcludes);
//        filter.initMyParameter();

        FilterRegistrationBean myFilterBean = new FilterRegistrationBean();
        myFilterBean.setName("noCacheFilter");
        myFilterBean.addUrlPatterns("/*");

        myFilterBean.setFilter(filter);
        myFilterBean.setOrder(1);
        myFilterBean.setEnabled(true);

        logger.debug("--------------reg filter");
        return myFilterBean;
    }

    @Bean
    public ErrorPageFilter errorPageFilter() {
        return new ErrorPageFilter();
    }
    @Bean
    public FilterRegistrationBean disableSpringBootErrorFilter(ErrorPageFilter filter) {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(filter);
        filterRegistrationBean.setEnabled(false);
        return filterRegistrationBean;
    }

    @Override
    public void run(String... args) throws Exception {
        CacheUtil.set("test",1);
        System.out.println("test redis:"+CacheUtil.get("test"));
    }

}