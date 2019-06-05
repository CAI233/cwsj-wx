package com.szkj.platform.busiz.ctrl;

import com.szkj.platform.busiz.beans.PageConditionBean;
import com.szkj.platform.busiz.beans.SearchBean;
import com.szkj.platform.busiz.domain.SearchCount;
import com.szkj.platform.busiz.service.SearchCountService;
import com.szkj.platform.system.constants.Constants;
import com.szkj.platform.utils.JsonResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;

/**
 * 搜索记录统计
 * Created by LuoLi on 2017/3/23 0023.
 */
@Controller
public class SearchCountController {

    @Autowired
    private SearchCountService searchCountService;

    /**
     * 搜索记录————分页
     * @return
     */
    @RequestMapping(value = "/api/book/searchCount/list", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Object searchList(HttpServletRequest request, @RequestBody PageConditionBean bean){
        try{
            Sort sort = new Sort(Sort.Direction.DESC, "search_count").and(new Sort(Sort.Direction.DESC, "create_time"));
            Object data = searchCountService.pageQuery(sort, bean);
            JsonResult result = JsonResult.getSuccess(Constants.LOAD_SUCCESS);
            result.setData(data);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    /**
     * 搜索记录————新增、修改
     * @return
     */
    @RequestMapping(value = "/api/book/searchCount/save", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Object updateSearchCount(HttpServletRequest request, @RequestBody SearchCount searchCount){
        try{
            JsonResult result = JsonResult.getSuccess("");
            if(searchCount.getSearch_id() == null){
                //新增
                SearchCount njswSearchCount = searchCountService.selectByNameAndOrgId(searchCount.getName());
                if(njswSearchCount != null){
                    return JsonResult.getError("搜索词汇重复");
                }
                if(searchCount.getSearch_count() == null){
                    searchCount.setSearch_count(0L);
                }
                searchCount.setStatus(2);//状态（1：启用；2：停用）
                searchCountService.saveSearchCount(searchCount);
                result.setMessage(Constants.ACTION_ADD);
            }else{
                //修改
                SearchCount njswSearchCount = searchCountService.selectById(searchCount.getSearch_id());
                if( !njswSearchCount.getName().equals(searchCount.getName())){
                    SearchCount njswSearchCount1 = searchCountService.selectByNameAndOrgId(searchCount.getName());
                    if(njswSearchCount1 != null){
                        return JsonResult.getError("搜索词汇重复");
                    }
                }
                searchCountService.updateSearchCount(searchCount);
                result.setMessage(Constants.ACTION_UPDATE);
            }
            result.setData(searchCount);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    /**
     * 搜索记录————修改状态
     * @return
     */
    @RequestMapping(value = "/api/book/searchCount/enabled", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Object updateStatus(HttpServletRequest request, @RequestBody SearchCount searchCount){
        try{
            JsonResult result = JsonResult.getSuccess("");
            //状态（1：启用；2：停用）
            String ids= StringUtils.join(searchCount.getSearch_ids(),",");
            searchCountService.updateStatus(ids,searchCount.getStatus());
            result.setMessage(Constants.ACTION_UPDATE);
            result.setData(searchCount);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

    /**
     * 搜索记录————删除
     * @return
     */
    @RequestMapping(value = "/api/book/searchCount/del", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Object delSearchCounts(HttpServletRequest request, @RequestBody SearchBean bean){
        try{
            searchCountService.delSearchCounts(StringUtils.join(bean.getIds(), ","));
            JsonResult result = JsonResult.getSuccess(Constants.ACTION_DELETE);
            result.setData(new ArrayList<>());
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return JsonResult.getException(Constants.EXCEPTION);
        }
    }

}
