package com.js.sas.controller;

import com.js.sas.service.ProductDetailService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("productDetail")
public class ProductDetailController {

    @Autowired
    private ProductDetailService productDetailService;

    @PostMapping("getPage")
    @ResponseBody
    public Object getPage(HttpServletRequest request){
        Map<String ,Object > result = new HashMap<>();
        Map<String ,String > map = new HashMap<>();
        try {
            if (StringUtils.isNotBlank(request.getParameter("startDate"))) {
                map.put("startDate", request.getParameter("startDate"));
            }
            if (StringUtils.isNotBlank(request.getParameter("endDate"))) {
                map.put("endDate", request.getParameter("endDate"));
            }
            if (StringUtils.isNotBlank(request.getParameter("companyname"))) {
                map.put("companyname", request.getParameter("companyname"));
            }
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
            result.put("rows", productDetailService.getPage(map));
            result.put("total", productDetailService.getCount(map));
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code",500);
            result.put("msg","请重试或联系系统管理员");
        }
        return result;
    }
}
