package com.szkj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {
        JpaRepositoriesAutoConfiguration.class//禁止springboot自动加载jpa的自动配置
})

//@ServletComponentScan("com.szkj.platform,com.szkj.platform.system")
public class WebApplication extends SpringBootServletInitializer {


    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(WebApplication.class).web(true);
    }

    public static void main(String[] args) {
        System.setProperty("org.terracotta.quartz.skipUpdateCheck","true");
        ApplicationContext context = SpringApplication.run(WebApplication.class, args);
    }



//    @Bean
//    public ApplicationStartup schedulerRunner() {
//        return new ApplicationStartup();
//    }

}
