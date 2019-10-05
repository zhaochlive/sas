package com.js.sas.controller;

import com.js.sas.service.StoreDetailService;
import com.js.sas.utils.CommonUtils;
import com.js.sas.utils.DateTimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;

import static java.math.RoundingMode.HALF_DOWN;

/**
 * 商铺信息
 */
@Controller
@RequestMapping("storeDetail")
public class StoreDetailController {

    @Autowired
    private StoreDetailService storeDetailService;

    /**
     * 商家详情统计  默认当天数据
     * @param request
     * @return
     */

    @RequestMapping(value = "page",method = RequestMethod.POST)
    @ResponseBody
    public Object storeDetail(HttpServletRequest request) {
        Date now = new Date();
        Map<String, String> requestMap = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        if (request.getParameter("startDate")==null|| StringUtils.isBlank(request.getParameter("startDate"))){
            requestMap.put("startDate",DateTimeUtils.firstDayOfMonth(new Date()));
        }else{
            String startDate = request.getParameter("startDate");
            requestMap.put("startDate", DateTimeUtils.convert(DateTimeUtils.convert(startDate,DateTimeUtils.DATE_FORMAT),DateTimeUtils.DATE_FORMAT)+" 00:00:00" );
        }
        if (request.getParameter("endDate")==null|| StringUtils.isBlank(request.getParameter("endDate"))){
            requestMap.put("endDate",DateTimeUtils.convert(now,DateTimeUtils.DATE_FORMAT)+" 23:59:59" );
        }else{
            String startDate = request.getParameter("endDate");
            requestMap.put("endDate", DateTimeUtils.convert(DateTimeUtils.convert(startDate,DateTimeUtils.DATE_FORMAT),DateTimeUtils.DATE_FORMAT)+" 23:59:59" );
        }
        if (StringUtils.isNotBlank(request.getParameter("shopname"))){
            requestMap.put("shopname",request.getParameter("shopname").trim());
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

    @PostMapping(value = "/download/excel")
    public void download(HttpServletResponse response, HttpServletRequest request) {
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("limit", "9999999999");
        requestMap.put("offset", "0");
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
        if (StringUtils.isNotBlank(request.getParameter("shopname"))){
            requestMap.put("shopname",request.getParameter("shopname").trim());
        }
        List<String > columnNameList = new ArrayList<>();
        columnNameList.add("店铺名称");
        columnNameList.add("付款订单数量");
        columnNameList.add("发货订单数量");
        columnNameList.add("下单金额");
        columnNameList.add("下单金额环比 / 同比");
        columnNameList.add("下单人数");
        columnNameList.add("新用户下单金额");
        columnNameList.add("新用户下单人数");
        columnNameList.add("客单价");
        columnNameList.add("客单价环比 / 同比");
        columnNameList.add("订单平均规格数");
        columnNameList.add("店铺部分发货比例");
        columnNameList.add("店铺违约率");
        columnNameList.add("店铺未付款超时取消订单比例");
        columnNameList.add("店铺退货率");
        columnNameList.add("当日发货");
        columnNameList.add("超时一天发货");
        columnNameList.add("超时两天发货");
        columnNameList.add("超时三天");
        columnNameList.add("低于500元订单");
        columnNameList.add("在500_800元订单");
        columnNameList.add("在800_1200订单");
        columnNameList.add("在1200_1500元订单");
        columnNameList.add("在1500_2000元订单");
        columnNameList.add("在2000_5000元订单");
        columnNameList.add("在5000元以上订单");

        try {
            List<List<Object>> result = new ArrayList<>();
            List<Map<String,Object>> data = storeDetailService.getPage(requestMap);
            if(data==null){
                return;
            }
            List<Object> objects =null;
            for (Map<String,Object> order : data) {
                objects = new ArrayList<>();
                objects.add(order.get("shopname"));
                objects.add(order.get("付款订单数量"));
                objects.add(order.get("发货订单数量"));
                objects.add(order.get("下单金额"));
                objects.add(order.get("下单金额环比")+ "%|"+order.get("下单金额同比")+"%");
                objects.add(order.get("下单人数"));
                objects.add(order.get("首次下单金额"));
                objects.add(order.get("首次下单人数"));
                objects.add(order.get("客单价"));
//                objects.add(order.get("客单价环比 / 同比"));
                objects.add(order.get("环比客单价")+ "%|"+order.get("同比客单价")+"%");
                objects.add(order.get("平均规格"));
                objects.add(order.get("店铺发货率"));
                objects.add(order.get("店铺违约率"));
                objects.add(order.get("未付款超时取消订单"));
                objects.add(order.get("店铺退货率"));
                objects.add(order.get("当日发货"));
                objects.add(order.get("超时一天发货"));
                objects.add(order.get("超时两天发货"));
                objects.add(order.get("超时三天"));
                objects.add(order.get("低于500元订单"));
                objects.add(order.get("在500_800元订单"));
                objects.add(order.get("在800_1200订单"));
                objects.add(order.get("在1200_1500元订单"));
                objects.add(order.get("在1500_2000元订单"));
                objects.add(order.get("在2000_5000元订单"));
                objects.add(order.get("在5000元以上订单"));

                result.add(objects);
            }
            CommonUtils.exportByList(response, columnNameList, result, "店铺详情统计");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 商家信息详情/商家详情
     * @param request
     * @return
     */
    @RequestMapping(value = "storeInfo",method = RequestMethod.POST)
    @ResponseBody
    public Object storeInfo(HttpServletRequest request) {
        Date now = new Date();
        Map<String, String> requestMap = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        //当天
        if (request.getParameter("startDate")==null|| StringUtils.isBlank(request.getParameter("startDate"))){
//            requestMap.put("startDate",DateTimeUtils.convert(DateTimeUtils.addTime(now,-1,DateTimeUtils.MONTH) ,DateTimeUtils.DATE_FORMAT)+" 00:00:00" );
        }else{
            String startDate = request.getParameter("startDate");
            requestMap.put("startDate", DateTimeUtils.convert(DateTimeUtils.convert(startDate,DateTimeUtils.DATE_FORMAT),DateTimeUtils.DATE_FORMAT)+" 00:00:00" );
        }
        if (request.getParameter("endDate")==null|| StringUtils.isBlank(request.getParameter("endDate"))){
            requestMap.put("endDate",DateTimeUtils.convert(now,DateTimeUtils.DATE_FORMAT)+" 23:59:59" );
        }else{
            String startDate = request.getParameter("endDate");
            requestMap.put("endDate", DateTimeUtils.convert(DateTimeUtils.convert(startDate,DateTimeUtils.DATE_FORMAT),DateTimeUtils.DATE_FORMAT)+" 23:59:59" );
        }
        if (StringUtils.isNotBlank(request.getParameter("shopname"))){
            requestMap.put("shopname",request.getParameter("shopname").trim());
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
            requestMap.put("limit", "100");
        }
        if (StringUtils.isNotBlank(request.getParameter("offset"))) {
            requestMap.put("offset", request.getParameter("offset"));
        } else {
            requestMap.put("offset", "0");
        }
        List<Map<String, Object>> page = storeDetailService.getStoreInfo(requestMap);
        result.put("rows", page);
        result.put("total", storeDetailService.getStoreInfoCount(requestMap));
        return result;
    }
}
