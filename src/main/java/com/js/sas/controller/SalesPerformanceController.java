package com.js.sas.controller;

import com.js.sas.dto.SalesperHead;
import com.js.sas.service.SalesPerformanceService;
import com.js.sas.utils.CommonUtils;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping("performance")
@Api
@Slf4j
public class SalesPerformanceController {

    @Autowired
    @Qualifier(value = "secodJdbcTemplate")
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private SalesPerformanceService salesPerformanceService;

    /**
     * 业务员业绩报表
     * @param request
     * @return
     */
    @PostMapping("/salesPerformance")
    @ResponseBody
    public Object salesPerformance(HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, String> map = new HashMap<>();
        if (StringUtils.isNotBlank(request.getParameter("limit"))) {
            map.put("limit", request.getParameter("limit"));
        } else {
            map.put("limit", "10");
//            return null;
        }
        if (StringUtils.isNotBlank(request.getParameter("offset"))) {
            map.put("offset", request.getParameter("offset"));
        } else {
            map.put("offset", "0");
        }
        if (StringUtils.isNotBlank(request.getParameter("companyname"))) {
            map.put("companyname", request.getParameter("companyname").trim());
        }
        if (StringUtils.isNotBlank(request.getParameter("membername"))) {
            map.put("membername", request.getParameter("membername").trim());
        }
        if (StringUtils.isNotBlank(request.getParameter("waysalesman"))) {
            map.put("waysalesman", request.getParameter("waysalesman").trim());
        }
        if (StringUtils.isNotBlank(request.getParameter("startDate"))) {
            map.put("startDate", request.getParameter("startDate"));
        }
        if (StringUtils.isNotBlank(request.getParameter("endDate"))) {
            map.put("endDate", request.getParameter("endDate"));
        }
        List<Map<String, Object>> page = null;
        try {
            page = salesPerformanceService.getPage(map);
            Double d = 0D;
            if(map.containsKey("waysalesman")){
                d = salesPerformanceService.getPerformanceOfSales(map);
            }
            resultMap.put("total", salesPerformanceService.getCount(map));
            resultMap.put("rows", page);
            resultMap.put("performance", d);
            resultMap.put("waysalesman", request.getParameter("waysalesman"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultMap;
    }

    /**
     * 业务员业绩报表导出
     * @param response
     * @param request
     */
    @ApiIgnore
    @PostMapping(value = "/download/excel")
    public void download(HttpServletResponse response, HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        map.put("limit", "9999999999");
        map.put("offset", "0");
        if (StringUtils.isNotBlank(request.getParameter("waysalesman"))) {
            map.put("waysalesman", request.getParameter("waysalesman"));
        }
        if (StringUtils.isNotBlank(request.getParameter("companyname"))) {
            map.put("companyname", request.getParameter("companyname"));
        }
        if (StringUtils.isNotBlank(request.getParameter("membername"))) {
            map.put("membername", request.getParameter("membername"));
        }
        if (StringUtils.isNotBlank(request.getParameter("startDate"))) {
            map.put("startDate", request.getParameter("startDate"));
        }
        if (StringUtils.isNotBlank(request.getParameter("endDate"))) {
            map.put("endDate", request.getParameter("endDate"));
        }
        List<Map<String, Object>> page = null;
        List<SalesperHead> salesperHanders = new ArrayList<>();

//        log.info("参数userName:{},userNo:{},invoiceName:{},companyname:{},startDate:{},endDate{}:", userName, userNo, invoiceName, companyname, startDate, endDate);
        try {
            page = salesPerformanceService.getPage(map);
            for (Map<String, Object> mp : page) {
                SalesperHead salesperHead = new SalesperHead();
                salesperHead.setCompanyname(mp.get("公司名称") == null ? null : mp.get("公司名称").toString());
                salesperHead.setMonth(mp.get("下单月份") == null ? null : mp.get("下单月份").toString());
                salesperHead.setMembername(mp.get("买家账号") == null ? null : mp.get("买家账号").toString());
                salesperHead.setOrderno(mp.get("订单号") == null ? null : mp.get("订单号").toString());
                salesperHead.setWaysalesman(mp.get("业务员") == null ? null : mp.get("业务员").toString());
                salesperHead.setTotalprice(mp.get("订单总金额") == null ? null : mp.get("订单总金额").toString());
                salesperHead.setRatio(mp.get("比例") == null ? null : mp.get("比例").toString());
                String createTime = mp.get("下单时间").toString();
                createTime = createTime.replace("T" ," ");
                salesperHead.setCreatetime(createTime.substring(0,createTime.indexOf(".")));
                salesperHanders.add(salesperHead);
            }
            CommonUtils.export(response, salesperHanders, "业务员业绩", new SalesperHead());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
