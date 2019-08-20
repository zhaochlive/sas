package com.js.sas.controller;

import com.js.sas.service.OrderProductService;
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

@RequestMapping("orderProduct")
@Controller
public class OrderProductController {

    @Autowired
    private OrderProductService orderProductService;

    @RequestMapping(value = "getPage",method = RequestMethod.POST)
    @ResponseBody
    public Object orderDetail(HttpServletRequest request) {
        Map<String, String> requestMap = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isNotBlank(request.getParameter("startDate"))) {
            requestMap.put("startDate", request.getParameter("startDate"));
        }
        if (StringUtils.isNotBlank(request.getParameter("endDate"))) {
            requestMap.put("endDate", request.getParameter("endDate"));
        }
        if (StringUtils.isNotBlank(request.getParameter("orderno"))){
            requestMap.put("orderno",request.getParameter("orderno"));
        }
        if (StringUtils.isNotBlank(request.getParameter("sort"))) {
            requestMap.put("sort", request.getParameter("sort").trim());
        }
        if (StringUtils.isNotBlank(request.getParameter("sortOrder"))) {
            requestMap.put("sortOrder", request.getParameter("sortOrder").trim());
        }
        if (StringUtils.isNotBlank(request.getParameter("limit"))) {
            requestMap.put("limit", request.getParameter("limit"));
        } else {
            requestMap.put("limit", "10");
        }
        if (StringUtils.isNotBlank(request.getParameter("offset"))) {
            requestMap.put("offset", request.getParameter("offset"));
        } else {
            requestMap.put("offset", "0");
        }
        List<Map<String, Object>> page = orderProductService.getPage(requestMap);
        result.put("rows", page);
        result.put("total", orderProductService.getCount(requestMap));
        return result;
    }

}
