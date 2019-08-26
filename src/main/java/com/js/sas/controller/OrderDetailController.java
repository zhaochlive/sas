package com.js.sas.controller;

import com.js.sas.service.OrderDetailService;
import com.js.sas.service.StoreDetailService;
import com.js.sas.utils.DateTimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("orderDetail")
public class OrderDetailController {

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 订单详情
     * @param request
     * @return
     */
    @RequestMapping(value = "page",method = RequestMethod.POST)
    @ResponseBody
    public Object orderDetail(HttpServletRequest request) {
        Map<String, String> requestMap = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        if (request.getParameter("startDate")==null|| StringUtils.isBlank(request.getParameter("startDate"))){
            String firstDayOfMonth = DateTimeUtils.firstDayOfMonth(new Date());
            requestMap.put("startDate",firstDayOfMonth);
        }else{
            String startDate = request.getParameter("startDate");
            DateTimeUtils.convert(startDate,DateTimeUtils.DATE_FORMAT);
            requestMap.put("startDate", DateTimeUtils.convert(DateTimeUtils.convert(startDate,DateTimeUtils.DATE_FORMAT),DateTimeUtils.DATE_FORMAT)+" 00:00:00" );
        }
        if (request.getParameter("endDate")==null|| StringUtils.isBlank(request.getParameter("endDate"))){
            String firstDayOfMonth = DateTimeUtils.lastDayOfMonth(new Date());
            requestMap.put("endDate",firstDayOfMonth);
        }else{
            String startDate = request.getParameter("endDate");
            DateTimeUtils.convert(startDate,DateTimeUtils.DATE_FORMAT);
            requestMap.put("endDate", DateTimeUtils.convert(DateTimeUtils.convert(startDate,DateTimeUtils.DATE_FORMAT),DateTimeUtils.DATE_FORMAT)+" 23:59:59" );
        }
        if (StringUtils.isNotBlank(request.getParameter("username"))){
            requestMap.put("username",request.getParameter("username"));
        }
        if (StringUtils.isNotBlank(request.getParameter("limit"))) {
            requestMap.put("limit", request.getParameter("limit"));
        } else {
            return null;
        }
        if (StringUtils.isNotBlank(request.getParameter("offset"))) {
            if (StringUtils.isNotBlank(request.getParameter("offset"))){
                requestMap.put("offset", "0");
            }else{
                requestMap.put("offset", request.getParameter("offset"));
            }
        } else {
            requestMap.put("offset", "0");
        }
        List<Map<String, Object>> page = orderDetailService.getPage(requestMap);
        for (Map<String, Object> map : page) {
            long memberid = Long.parseLong(map.get("memberid").toString());

            List<Map<String, Object>> provinceRate = orderDetailService.getProvinceRateByMemberId(memberid,requestMap);
            map.put("provinceRate",provinceRate);
        }
        result.put("rows", page);
        result.put("total", orderDetailService.getCount(requestMap));
        return result;
    }

    /**
     * 主表
     * @param request
     * @return
     */

    @RequestMapping(value = "getOrderReport",method = RequestMethod.POST)
    @ResponseBody
    public Object getOrderReport(HttpServletRequest request) {
        Map<String, String> requestMap = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        if (request.getParameter("startDate")==null|| StringUtils.isBlank(request.getParameter("startDate"))){
            String firstDayOfMonth = DateTimeUtils.firstDayOfMonth(new Date());
            requestMap.put("startDate",firstDayOfMonth);
        }else{
            String startDate = request.getParameter("startDate");
            DateTimeUtils.convert(startDate,DateTimeUtils.DATE_FORMAT);
            requestMap.put("startDate", DateTimeUtils.convert(DateTimeUtils.convert(startDate,DateTimeUtils.DATE_FORMAT),DateTimeUtils.DATE_FORMAT)+" 00:00:00" );
        }
        if (request.getParameter("endDate")==null|| StringUtils.isBlank(request.getParameter("endDate"))){
            String firstDayOfMonth = DateTimeUtils.lastDayOfMonth(new Date());
            requestMap.put("endDate",firstDayOfMonth);
        }else{
            String startDate = request.getParameter("endDate");
            DateTimeUtils.convert(startDate,DateTimeUtils.DATE_FORMAT);
            requestMap.put("endDate", DateTimeUtils.convert(DateTimeUtils.convert(startDate,DateTimeUtils.DATE_FORMAT),DateTimeUtils.DATE_FORMAT)+" 23:59:59" );
        }

        if (StringUtils.isNotBlank(request.getParameter("limit"))) {
            requestMap.put("limit", request.getParameter("limit"));
        } else {
            requestMap.put("limit", "0");
        }
        if (StringUtils.isNotBlank(request.getParameter("offset"))) {
            requestMap.put("offset", request.getParameter("offset"));
        } else {
            requestMap.put("offset", "0");
        }
        List<Map<String, Object>> page = orderDetailService.getOrderReport(requestMap);

        result.put("rows", page);
        result.put("total", orderDetailService.getReportCount(requestMap));
        return result;
    }

    /**
     * 客单价
     * @param request
     * @return
     */
    @RequestMapping(value = "getUnitPrice",method = RequestMethod.POST)
    @ResponseBody
    public Object getUnitPrice(HttpServletRequest request) {
        Map<String, String> requestMap = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isNotBlank(request.getParameter("username"))){
            requestMap.put("username",request.getParameter("username"));
        }
        if (StringUtils.isNotBlank(request.getParameter("startDate"))) {
            requestMap.put("startDate", request.getParameter("startDate"));
        }
        if (StringUtils.isNotBlank(request.getParameter("endDate"))) {
            requestMap.put("endDate", request.getParameter("endDate"));
        }
        if (StringUtils.isNotBlank(request.getParameter("limit"))) {
            requestMap.put("limit", request.getParameter("limit"));
        } else {
            requestMap.put("limit", "0");
        }
        if (StringUtils.isNotBlank(request.getParameter("offset"))) {
            requestMap.put("offset", request.getParameter("offset"));
        } else {
            requestMap.put("offset", "0");
        }
        List<Map<String, Object>> page = orderDetailService.getUnitPrice(requestMap);

        result.put("rows", page);
        result.put("total", orderDetailService.getUnitPriceCount(requestMap));
        return result;
    }
}
