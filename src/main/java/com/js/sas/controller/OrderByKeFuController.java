package com.js.sas.controller;

import com.js.sas.service.OrderByKeFuService;
import com.js.sas.service.RepurchaseRateService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("customerService")
public class OrderByKeFuController {

    @Autowired
    private OrderByKeFuService orderByKeFuService;

    @GetMapping("getData")
    public Object getData(HttpServletRequest request){

        Map<String, String> map = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
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
        if (StringUtils.isNotBlank(request.getParameter("clerkname"))) {
            map.put("clerkname", request.getParameter("clerkname").trim());
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
        result.put("rows",orderByKeFuService.getPage(map));
        result.put("total",orderByKeFuService.getCount(map));
        return result;
    }


    @PostMapping(value = "getColums")
    @ResponseBody
    public Object getColums(HttpServletRequest request){
        HashMap<String, String> hashMap = new HashMap<>();
        if(StringUtils.isNotBlank(request.getParameter("startDate"))){
            hashMap.put("startDate",request.getParameter("startDate"));
        }
        if(StringUtils.isNotBlank(request.getParameter("endDate"))){
            hashMap.put("endDate",request.getParameter("endDate"));
        }
        List<String> colums = orderByKeFuService.getColums(hashMap);
        List<String> result = new ArrayList<>();
        for (String colum : colums) {
            result.add("月份_"+ colum);
        }
        return  result;
    }


}
