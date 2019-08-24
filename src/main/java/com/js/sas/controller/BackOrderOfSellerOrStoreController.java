package com.js.sas.controller;

import com.js.sas.service.BackOrderOfSellerOrStoreService;
import com.js.sas.service.OrderDetailService;
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
@RequestMapping("backOfSellerOrStore")
public class BackOrderOfSellerOrStoreController {

    @Autowired
    private BackOrderOfSellerOrStoreService back;

    /**
     * 仓库退单信息
     * @param request
     * @return
     */
    @RequestMapping(value = "backOfStore",method = RequestMethod.POST)
    @ResponseBody
    public Object backOfStore(HttpServletRequest request) {
        Map<String, String> requestMap = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
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
        List<Map<String, Object>> page = back.getBackOfStore(requestMap);

        result.put("rows", page);
        result.put("total", back.getBackOfStoreCount(requestMap));
        return result;
    }

    @RequestMapping(value = "backOfSeller",method = RequestMethod.POST)
    @ResponseBody
    public Object backOfSeller(HttpServletRequest request) {
        Map<String, String> requestMap = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
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
        List<Map<String, Object>> page = back.getBackOfSeller(requestMap);
        result.put("rows", page);
        result.put("total", back.getBackOfSellerCount(requestMap));
        return result;
    }
}
