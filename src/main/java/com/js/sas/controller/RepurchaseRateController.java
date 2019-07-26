package com.js.sas.controller;

import com.js.sas.service.RepurchaseRateService;
import com.js.sas.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
@RequestMapping("repurchase")
@Slf4j
public class RepurchaseRateController {

    @Autowired
    private RepurchaseRateService repurchaseRateService;

    @PostMapping(value = "getColums")
    @ResponseBody
    public Object getColums(HttpServletRequest request){
//        Enumeration<String> parameterNames = request.getParameterNames();
//        while (parameterNames.hasMoreElements()){
//            String element = parameterNames.nextElement();
//            log.info(element+"===="+request.getParameter(element));
//        }
        HashMap<String, String> hashMap = new HashMap<>();
        if(StringUtils.isNotBlank(request.getParameter("startDate"))){
            hashMap.put("startDate",request.getParameter("startDate"));
        }
        if(StringUtils.isNotBlank(request.getParameter("endDate"))){
            hashMap.put("endDate",request.getParameter("endDate"));
        }
        List<String> colums = repurchaseRateService.getColums(hashMap);
        List<String> result = new ArrayList<>();
        for (String colum : colums) {
            result.add("下单次数_"+ colum);
            result.add("下单金额_"+ colum);
        }
        return  result;
    }

    @GetMapping(value = "getRepurchaseRate")
    @ResponseBody
    public Object RepurchaseRate(HttpServletRequest request){

//        Enumeration<String> parameterNames = request.getParameterNames();
//        while (parameterNames.hasMoreElements()){
//            String element = parameterNames.nextElement();
//            log.info(element+"===="+request.getParameter(element));
//        }
        Map<String, String> map = new HashMap<>();
        if (StringUtils.isNotBlank(request.getParameter("limit"))) {
            map.put("limit", request.getParameter("limit"));
        } else {
            return null;
        }
        if (StringUtils.isNotBlank(request.getParameter("offset"))) {
            map.put("offset", request.getParameter("offset"));
        } else {
            map.put("offset", "0");
        }
        if (StringUtils.isNotBlank(request.getParameter("startDate"))) {
            map.put("startDate", request.getParameter("startDate"));
        }
        if (StringUtils.isNotBlank(request.getParameter("endDate"))) {
            map.put("endDate", request.getParameter("endDate"));
        }

        if (StringUtils.isNotBlank(request.getParameter("companyname"))) {
            map.put("companyname", request.getParameter("companyname").trim());
        }
        if (StringUtils.isNotBlank(request.getParameter("mobile"))) {
            map.put("mobile", request.getParameter("mobile").trim());
        }
        if (StringUtils.isNotBlank(request.getParameter("username"))) {
            map.put("username", request.getParameter("username").trim());
        }
        if (StringUtils.isNotBlank(request.getParameter("sort"))) {
            map.put("sort", request.getParameter("sort").trim());
        }
        if (StringUtils.isNotBlank(request.getParameter("sortOrder"))) {
            map.put("sortOrder", request.getParameter("sortOrder").trim());
        }
        HashMap<String, Object> resultMap = new HashMap<>();
        List<Map<String,Object>> colums = repurchaseRateService.getRepurchaseRate(map);
        resultMap.put("total",repurchaseRateService.getCount(map) );
        resultMap.put("rows", colums);
        return  resultMap;
    }
}
