package com.js.sas.controller;

import com.js.sas.service.FacilitatorGoldsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: daniel
 * @date: 2020/5/13 0013 12:30
 * @Description:
 */
@Controller
@RequestMapping("/facilitator")
public class FacilitatorGoldsController {


    @Autowired
    private FacilitatorGoldsService facilitatorGoldsService;


    @ResponseBody
    @RequestMapping("getFacilitatorGolds")
    public Object getFacilitatorGolds(HttpServletRequest request){
        Map<String, String> params = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isNotBlank(request.getParameter("goldType"))){
            params.put("goldType",request.getParameter("goldType").trim());
        }
        if (StringUtils.isNotBlank(request.getParameter("year"))) {
            params.put("year", request.getParameter("year"));
        }
        if (StringUtils.isNotBlank(request.getParameter("limit"))) {
            params.put("limit", request.getParameter("limit"));
        } else {
            params.put("limit", "10");
        }
        if (StringUtils.isNotBlank(request.getParameter("offset"))){
            params.put("offset", request.getParameter("offset"));
        }else{
            params.put("offset", "0");
        }
        if ("jinshang".equals(request.getParameter("goldType").trim())){
            result.put("rows",facilitatorGoldsService.getFacilitatorGoldsForJinShang(params));
        }else if ("aozhan".equals(request.getParameter("goldType").trim())){
            result.put("rows",facilitatorGoldsService.getFacilitatorGoldsForAoZhan(params));
        }
        result.put("total",facilitatorGoldsService.getFacilitatorCount(params));
        return result;
    }
}
