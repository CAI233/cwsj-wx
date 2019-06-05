package com.szkj.platform.system.ctrl;

import com.szkj.platform.system.utils.Guid;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by shiaihua on 18/2/7.
 */
@RestController
@RequestMapping(value = "/admin/v1")
public class TestSessionController {


        @RequestMapping(value = "/first", method = RequestMethod.GET)
        public Map<String, Object> firstResp (HttpServletRequest request){
            Map<String, Object> map = new HashMap<>();
            request.getSession().setAttribute("request Url", request.getRequestURL());
            map.put("request Url", request.getRequestURL());
            return map;
        }

        @RequestMapping(value = "/sessions", method = RequestMethod.GET)
        public Object sessions (HttpServletRequest request){
            Map<String, Object> map = new HashMap<>();
            map.put("sessionId", request.getSession().getId());
            map.put("message", request.getSession().getAttribute("map"));
            return map;
        }


    @RequestMapping(value = "/idtest", method = RequestMethod.GET)
    public Object idtest (HttpServletRequest request){
        String f = request.getParameter("f");
        int fi = 3;
        try {
            fi = Integer.parseInt(f);
        }catch (Exception e) {

        }
        return Guid.newFromName("test",fi);
    }



}
