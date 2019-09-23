package com.js.sas.controller;

import com.js.sas.service.ClerkManService;
import com.js.sas.service.SettlementCustomerService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author daniel
 * @description: 客户结算相关服务类
 * @create: 2019-09-21 14:00
 */
@RestController
@RequestMapping("/settle")
public class SettlementCustomerController {

    @Autowired
    private SettlementCustomerService customerService;

    @PostMapping(value ="settlementCustomer")
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
        if (StringUtils.isNotBlank(request.getParameter("invoiceheadup"))){
            params.put("invoiceheadup",request.getParameter("invoiceheadup").trim());
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
        result.put("rows",customerService.getSettlementCustomerPage(params,year));
        result.put("total",customerService.getSettlementCustomerCount(params,year));
        return result;
    }
}
