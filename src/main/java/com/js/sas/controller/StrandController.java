package com.js.sas.controller;

import com.js.sas.service.StrandService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author daniel
 * @description: 品牌类处理
 * @create: 2019-09-20 11:30
 */
@RequestMapping("strand")
@Controller
public class StrandController {

    @Autowired
    private StrandService strandService;
    /**
     * 查询各品牌完成情况列表
     * @return
     */
    @PostMapping(value ="strandSales")
    @ResponseBody
    public Object getStrandSalesPage(HttpServletRequest request){
        Map<String, String> params = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        String year =null;
        if (request.getParameter("year")!=null|| StringUtils.isNotBlank(request.getParameter("year"))){
            year =request.getParameter("year");
        }else{
            result.put("304","缺少年份参数year，例如：year = 2018");
            return result;
        }
        if (StringUtils.isNotBlank(request.getParameter("brand"))){
            params.put("brand",request.getParameter("brand").trim());
        }
        if (StringUtils.isNotBlank(request.getParameter("startDate"))) {
            params.put("startDate", request.getParameter("startDate"));
        }
        if (StringUtils.isNotBlank(request.getParameter("endDate"))) {
            params.put("endDate", request.getParameter("endDate"));
        }
        if (StringUtils.isNotBlank(request.getParameter("limit"))) {
            params.put("limit", request.getParameter("limit"));
        } else {
            params.put("limit", "0");
        }
        if (StringUtils.isNotBlank(request.getParameter("offset"))) {
            params.put("offset", request.getParameter("offset"));
        } else {
            params.put("offset", "0");
        }
        result.put("rows",strandService.getStrandSalesPage(params,year));
        result.put("total",strandService.getStrandSalesCount(params,year));
        return result;
    }
}
