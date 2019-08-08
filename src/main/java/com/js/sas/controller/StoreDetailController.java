package com.js.sas.controller;

import com.js.sas.service.StoreDetailService;
import com.js.sas.utils.DateTimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

import static java.math.RoundingMode.HALF_DOWN;

@Controller
@RequestMapping("storeDetail")
public class StoreDetailController {

    @Autowired
    private StoreDetailService storeDetailService;

    @RequestMapping(value = "page",method = RequestMethod.POST)
    @ResponseBody
    public Object storeDetail(HttpServletRequest request) {

        Map<String, String> requestMap = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        if (request.getParameter("startDate")==null|| StringUtils.isBlank(request.getParameter("startDate"))){
            String firstDayOfMonth = DateTimeUtils.firstDayOfMonth(new Date());
            System.err.println(firstDayOfMonth);
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
        if (StringUtils.isNotBlank(request.getParameter("shopname"))){
            requestMap.put("shopname",request.getParameter("shopname"));
        }
        if (StringUtils.isNotBlank(request.getParameter("limit"))) {
            requestMap.put("limit", request.getParameter("limit"));
        } else {
            return null;
        }
        if (StringUtils.isNotBlank(request.getParameter("offset"))) {
            requestMap.put("offset", request.getParameter("offset"));
        } else {
            requestMap.put("offset", "0");
        }
        List<Map<String, Object>> page = storeDetailService.getPage(requestMap);
        Map<String, Object> count = storeDetailService.getTotal(requestMap);
        result.putAll(count);
        result.put("rows", page);
        result.put("total", storeDetailService.getCount(requestMap));
        return result;
    }
}
