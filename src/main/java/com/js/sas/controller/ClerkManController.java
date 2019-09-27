package com.js.sas.controller;

import com.js.sas.service.ClerkManService;
import com.js.sas.utils.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author daniel
 * @description: 客服相关服务类
 * @create: 2019-09-21 14:00
 */
@RestController
@RequestMapping("/clerkman")
public class ClerkManController {

    @Autowired
    private ClerkManService clerkManService;

    @PostMapping(value ="clerkmanSales")
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
        if (StringUtils.isNotBlank(request.getParameter("clerkname"))){
            params.put("clerkname",request.getParameter("clerkname").trim());
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
        if (StringUtils.isNotBlank(request.getParameter("sort"))) {
            params.put("sort", request.getParameter("sort").trim());
        }
        if (StringUtils.isNotBlank(request.getParameter("sortOrder"))) {
            params.put("sortOrder", request.getParameter("sortOrder").trim());
        }
        result.put("rows",clerkManService.getClerkManPage(params,year));
        result.put("total",clerkManService.getClerkManCount(params,year));
        return result;
    }
    @PostMapping(value = "/download/excel")
    public void download(HttpServletResponse response, HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        params.put("limit", "9999999999");
        params.put("offset", "0");
        String year =null;
        if (request.getParameter("years")!=null|| StringUtils.isNotBlank(request.getParameter("years"))){
            year =request.getParameter("years");
        }
        if (StringUtils.isNotBlank(request.getParameter("clerkname"))){
            params.put("clerkname",request.getParameter("clerkname").trim());
        }
        if (StringUtils.isNotBlank(request.getParameter("startDate"))) {
            params.put("startDate", request.getParameter("startDate"));
        }
        if (StringUtils.isNotBlank(request.getParameter("endDate"))) {
            params.put("endDate", request.getParameter("endDate"));
        }
        params.put("sort", "desc");
        params.put("sortOrder", "total");
        List<String > columnNameList = new ArrayList<>();
        columnNameList.add("品牌");
        columnNameList.add("总计");
        columnNameList.add("一月份");
        columnNameList.add("二月份");
        columnNameList.add("三月份");
        columnNameList.add("四月份");
        columnNameList.add("五月份");
        columnNameList.add("六月份");
        columnNameList.add("七月份");
        columnNameList.add("八月份");
        columnNameList.add("九月份");
        columnNameList.add("十月份");
        columnNameList.add("十一月份");
        columnNameList.add("十二月份");

        try {
            List<List<Object>> result = new ArrayList<>();
            List<Map<String,Object>> data =  clerkManService.getClerkManPage(params,year);
            if(data==null){
                return;
            }
            List<Object> objects =null;
            for (Map<String,Object> order : data) {
                objects = new ArrayList<>();
                objects.add(order.get("clerkname"));
                objects.add(order.get("total"));
                objects.add(order.get("一月"));
                objects.add(order.get("二月"));
                objects.add(order.get("三月"));
                objects.add(order.get("四月"));
                objects.add(order.get("五月"));
                objects.add(order.get("六月"));
                objects.add(order.get("七月"));
                objects.add(order.get("八月"));
                objects.add(order.get("九月"));
                objects.add(order.get("十月"));
                objects.add(order.get("十一月"));
                objects.add(order.get("十二月"));
//                System.out.println(objects.toString());
                result.add(objects);
            }
            CommonUtils.exportByList(response, columnNameList, result, "客服销售业绩统计");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
