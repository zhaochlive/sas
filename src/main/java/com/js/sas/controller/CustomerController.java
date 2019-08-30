package com.js.sas.controller;

import com.js.sas.dto.CustomerOfOrder;
import com.js.sas.dto.SalesperHead;
import com.js.sas.service.CustomerService;
import com.js.sas.utils.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
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
            Double lo = customerService.getCountFromAllCustomer(map);
            resultMap.put("countFromAllCustomer",lo);
            resultMap.put("total", customerService.getCount(map));
            resultMap.put("rows", page);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultMap;
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
            map.put("companyname", request.getParameter("companyname"));
        }
        if (StringUtils.isNotBlank(request.getParameter("mobile"))) {
            map.put("mobile", request.getParameter("mobile"));
        }
        if (StringUtils.isNotBlank(request.getParameter("waysalesman"))) {
            map.put("waysalesman", request.getParameter("waysalesman"));
        }
        List<String > columnNameList = new ArrayList<>();
        columnNameList.add("会员名称");
        columnNameList.add("企业名称");
        columnNameList.add("首次下单时间");
        columnNameList.add("所在省市");
        columnNameList.add("地址");
        columnNameList.add("联系人");
        columnNameList.add("手机");
        columnNameList.add("座机");
        columnNameList.add("业务员");
        columnNameList.add("总金额");
        try {
            List<List<Object>> result = new ArrayList<>();
            List<CustomerOfOrder> customerOfOrder = customerService.getCustomerOfOrder(map);
            List<Object> objects ;
            for (CustomerOfOrder order : customerOfOrder) {
                objects = new ArrayList<>();
                objects.add(order.getUsername());
                objects.add(order.getCompanyname());
                objects.add(order.getFirsttime());
                objects.add(order.getCity());
                objects.add(order.getAddress());
                objects.add(order.getRealname());
                objects.add(order.getMobile());
                objects.add(order.getTelephone());
                objects.add(order.getWaysalesman());
                objects.add(order.getTotalprice());
                result.add(objects);
            }
            CommonUtils.exportByList(response, columnNameList, result, "新客户下单统计");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
