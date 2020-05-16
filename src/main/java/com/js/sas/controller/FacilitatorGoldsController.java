package com.js.sas.controller;

import com.js.sas.service.FacilitatorGoldsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
    @RequestMapping(value = "getFacilitatorGolds",method = RequestMethod.POST)
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
    @ResponseBody
    @RequestMapping(value = "getFacilitatorGoldInfo",method = RequestMethod.POST)
    public Object getFacilitatorGoldInfo(HttpServletRequest request){
        Map<String, String> params = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isNotBlank(request.getParameter("goldType"))){
            params.put("goldType",request.getParameter("goldType").trim());
        }else return null;
        if (StringUtils.isNotBlank(request.getParameter("facilitator"))) {
            params.put("facilitator", request.getParameter("facilitator"));
        }else return null;
        if (StringUtils.isNotBlank(request.getParameter("startDate"))) {
            params.put("startDate", request.getParameter("startDate"));
        }
        if (StringUtils.isNotBlank(request.getParameter("endDate"))) {
            params.put("endDate", request.getParameter("endDate"));
        }
        if (StringUtils.isNotBlank(request.getParameter("limit"))) {
            params.put("limit", request.getParameter("limit"));
        } else {
            params.put("limit", "20");
        }
        if (StringUtils.isNotBlank(request.getParameter("offset"))){
            params.put("offset", request.getParameter("offset"));
        }else{
            params.put("offset", "0");
        }
        if ("jinshang".equals(request.getParameter("goldType").trim())){
            result.put("rows",facilitatorGoldsService.getFacilitatorGoldsInfoForJinShang(params));
        }else if ("aozhan".equals(request.getParameter("goldType").trim())){
            result.put("rows",facilitatorGoldsService.getFacilitatorGoldsInfoForAoZhan(params));
        }
        result.put("total",facilitatorGoldsService.getFacilitatorGoldsInfoCount(params));
        return result;
    }

    /**
     * 查看奥展详情
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "facilitatorOrderInfo",method = RequestMethod.POST)
    public Object facilitatorOrderInfo(HttpServletRequest request){
        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isBlank(request.getParameter("orderNo"))){
            return null;
        }
        result.put("rows",facilitatorGoldsService.facilitatorOrderInfo(request.getParameter("orderNo")));
        result.put("total",100);
        return result;
    }
}
