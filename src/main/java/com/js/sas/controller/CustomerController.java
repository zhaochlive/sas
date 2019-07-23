package com.js.sas.controller;

import com.js.sas.dto.CustomerOfOrder;
import com.js.sas.service.CustomerService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("customer")
@Controller
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping("customerOfNewOrders")
    @ResponseBody
    public Object customerOfNewOrders(HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<>();
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
            map.put("companyname", request.getParameter("companyname"));
        }
        if (StringUtils.isNotBlank(request.getParameter("mobile"))) {
            map.put("mobile", request.getParameter("mobile"));
        }
        if (StringUtils.isNotBlank(request.getParameter("waysalesman"))) {
            map.put("waysalesman", request.getParameter("waysalesman"));
        }

        List<CustomerOfOrder> page = null;
        try {
            page = customerService.getCustomerOfOrder(map);
            resultMap.put("total", customerService.getCount(map));
            resultMap.put("rows", page);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultMap;
    }
}
