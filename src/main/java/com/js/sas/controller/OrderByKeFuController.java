package com.js.sas.controller;

import com.js.sas.service.OrderByKeFuService;
import com.js.sas.service.RepurchaseRateService;
import com.js.sas.utils.CommonUtils;
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

    @PostMapping(value = "/download/excel")
    public void download(HttpServletResponse response, HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        map.put("limit", "9999999999");
        map.put("offset", "0");
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
        List<String > columnNameList = new ArrayList<>();
        columnNameList.add("客服名称");
        columnNameList.add("用户名称");
        columnNameList.add("公司名称");
        List<String> colums = orderByKeFuService.getColums(map);
        for (String colum : colums) {
            columnNameList.add("月份_"+ colum);
        }

        try {
            List<List<Object>> result = new ArrayList<>();
            List<Map<String,Object>> data = orderByKeFuService.getPage(map);
            List<Object> objects =null;
            for (Map<String,Object> order : data) {
                objects = new ArrayList<>();
                objects.add(order.get("clerkname"));
                objects.add(order.get("username"));
                objects.add(order.get("companyname"));
                for (String s : colums) {
                    objects.add(order.get("月份_"+s));
                }
                result.add(objects);
            }
            CommonUtils.exportByList(response, columnNameList, result, "客服客户下单统计");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
