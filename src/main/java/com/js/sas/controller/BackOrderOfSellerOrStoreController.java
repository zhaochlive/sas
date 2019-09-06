package com.js.sas.controller;

import com.js.sas.service.BackOrderOfSellerOrStoreService;
import com.js.sas.utils.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

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
            requestMap.put("limit", "10");
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
    /**
     * 仓库退单信息报表导出
     * @param request
     * @return
     */
    @PostMapping(value = "/download/backOfStore")
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

        List<String > columnNameList = new ArrayList<>();
        columnNameList.add("仓库");
        columnNameList.add("数量");
        try {
            List<List<Object>> result = new ArrayList<>();
            List<Map<String, Object>> customerOfOrder = back.getBackOfStore(map);
            List<Object> objects ;
            for (Map<String, Object> order : customerOfOrder) {
                objects = new ArrayList<>();
                objects.add(order.get("name"));
                objects.add(order.get("cut"));
                result.add(objects);
            }
            CommonUtils.exportByList(response, columnNameList, result, "退货仓库统计");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 店铺退单信息
     * @param request
     * @return
     */
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
            requestMap.put("limit", "10");
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
    /**
     * 店铺退单信息报表导出
     * @param request
     * @return
     */
    @PostMapping(value = "/download/backOfSeller")
    public void downloadOfSeller(HttpServletResponse response, HttpServletRequest request) {
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
        List<String > columnNameList = new ArrayList<>();
        columnNameList.add("店铺");
        columnNameList.add("数量");
        try {
            List<List<Object>> result = new ArrayList<>();
            List<Map<String, Object>> customerOfOrder = back.getBackOfSeller(map);
            List<Object> objects ;
            for (Map<String, Object> order : customerOfOrder) {
                objects = new ArrayList<>();
                objects.add(order.get("companyname"));
                objects.add(order.get("cut"));
                result.add(objects);
            }
            CommonUtils.exportByList(response, columnNameList, result, "退货店铺统计");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
