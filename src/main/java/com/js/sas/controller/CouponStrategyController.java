package com.js.sas.controller;

import com.js.sas.service.CouponStrategyService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("couponStrategy")
public class CouponStrategyController {

    @Autowired
    private CouponStrategyService couponStrategyService;

    @PostMapping("page")
    @ResponseBody
    public Object getPage(HttpServletRequest request){
        Map<String ,Object > result = new HashMap<>();
        try {
            Map<String ,String > map = new HashMap<>();

            if (StringUtils.isNotBlank(request.getParameter("limit"))) {
                map.put("limit", request.getParameter("limit"));
            } else {
                map.put("limit", "10");
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
            if (StringUtils.isNotBlank(request.getParameter("ticket"))) {
                map.put("ticket", request.getParameter("ticket"));
            }
            result.put("rows",couponStrategyService.getData(map)) ;
            result.put("total",couponStrategyService.getCount(map)) ;
            result.put("code",200);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code",500);
            result.put("msg","请刷新重试");
        }
        return result;
    }

}
