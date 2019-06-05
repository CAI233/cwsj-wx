package com.szkj.filter;

import com.szkj.platform.system.domain.GlobalSettings;
import com.szkj.platform.system.domain.SysUser;
import com.szkj.platform.system.service.Impl.UserServiceImpl;
import com.szkj.platform.system.service.SpringContextUtil;
import com.szkj.platform.system.service.UserService;
import com.szkj.platform.system.utils.CheckUserHelper;
import com.szkj.platform.system.utils.Log;
import com.szkj.platform.system.utils.RequestContextUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebFilter(filterName = "noCacheFilter",urlPatterns = "/*")
public class NoCacheFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(NoCacheFilter.class);

    private List<String> exeludes;
    private String logfileExcludes;
    private String urlExcludes;


    private static Pattern p = null;


    @Override
    public void init(FilterConfig filterConfig) {
        logger.info("------------init param begin");
        initMyParameter();
        logger.info("------------init param ok");

    }
    public void initMyParameter() {
        try {
            if(StringUtils.isEmpty(urlExcludes)) {
                GlobalSettings globalSettings = (GlobalSettings) SpringContextUtil.getBean("globalSettings");
                urlExcludes = globalSettings.getUrlexcludes();
            }
            if (urlExcludes != null) {
                exeludes = Arrays.asList(urlExcludes.split(","));
            }
            if(StringUtils.isEmpty(logfileExcludes)) {
                GlobalSettings globalSettings = (GlobalSettings) SpringContextUtil.getBean("globalSettings");
                logfileExcludes = globalSettings.getLogexcludes();
            }
            if (logfileExcludes != null && logfileExcludes.length() > 0) {
                try {
                    p = Pattern.compile("http://(?!(" + logfileExcludes + ")).+?(" + logfileExcludes + ")");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            logger.debug("------------urlExcludes:"+urlExcludes);
            logger.debug("------------logfileExcludesstr:"+logfileExcludes);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws ServletException {
        String ctxPath = req.getServletContext().getContextPath();
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;
        HttpSession session = request.getSession();

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Allow","GET,POST,PUT,DELETE,OPTIONS");
        response.setHeader("Access-Control-Allow-Methods", "POST, PUT, GET, OPTIONS, DELETE");
        response.setHeader("Access-Control-Allow-Headers", " Origin, X-Requested-With, Content-Type, Accept, token");
        response.setHeader("Access-Control-Max-Age", "3600");




        boolean iscontain = false;
        String url = request.getRequestURI().replace(ctxPath, "");
        if(url.contains("%") || url.contains("alert") || url.contains(".aspx") || url.contains(".php")) {
            return;
        }
        if (exeludes.size() > 0) {
            for (String chkurl : exeludes) {
                if (url.startsWith(chkurl)) {
                    iscontain = true;
                    if(request.getMethod().equalsIgnoreCase("post")) {
                        iscontain = false;
                    }
                    if(request.getMethod().equalsIgnoreCase("get")) {
                        if(url.startsWith("/admin/") && url.length()>7) {
                            iscontain = false;
                        }
                    }
                    break;
                }
            }
        }

        if (iscontain) { // ||
            try {
                chain.doFilter(req, res);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            String token = request.getHeader("token");
            String session_token = (String) (session.getAttribute("token"));

            boolean matchRes = false;
            try {
                if(p!=null) {
                    Matcher m =p.matcher(request.getRequestURL());
                    if(m.find()) {
                        matchRes = true;
                    }
                }
                if(!matchRes) {
                    if(url.startsWith("/admin") || url.startsWith("/static/admin")) {
                        UserService userService = (UserService) SpringContextUtil.getBean("userServiceImpl");
                        SysUser sysUser = CheckUserHelper.checkUserInfo(request.getHeader("token"));// 当前登录用户
                        if (StringUtils.isEmpty(token) && StringUtils.isNotEmpty(session_token)) {
                            response.setHeader("token", session_token);
                        }
                        if (sysUser == null) {
                            Log.record(request, -1L,"匿名", "admin",null);
                        }else {
                            Log.record(request, sysUser.getUser_id(), sysUser.getUser_name(), "admin", null);
                        }
                    }else {
                        Boolean isMobileDevice = RequestContextUtil.isMobileDevice(request);
                        String subsys = "web";
                        if(isMobileDevice) {
                            subsys = "mobile";
                        }
//                        Member member = MemberContextUtil.getMember(request);
//                        if(member==null) {
//                            Log.record(request, -1L,"匿名",subsys, null);
//                        }else {
//                            Log.record(request, member.getMember_id(), member.getMember_name(),subsys, null);
//                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
                try {
                    chain.doFilter(req, res);
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
//            }
        }

    }

    @Override
    public void destroy() {

    }

    private boolean checkToken(String token, String session_token, HttpServletRequest request) {
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(token) || !token.equals(session_token)) {
            return false;
        }
        UserServiceImpl userService = (UserServiceImpl) SpringContextUtil.getBean("userServiceImpl");

        SysUser user = userService.findByToken(token);
        if (user != null) {
            return true;
        }
        return false;
    }


    public String getLogfileExcludes() {
        return logfileExcludes;
    }

    public void setLogfileExcludes(String logfileExcludes) {
        this.logfileExcludes = logfileExcludes;
    }

    public String getUrlExcludes() {
        return urlExcludes;
    }

    public void setUrlExcludes(String urlExcludes) {
        this.urlExcludes = urlExcludes;
    }
}
