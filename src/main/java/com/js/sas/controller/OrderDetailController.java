package com.js.sas.controller;

import com.js.sas.service.OrderDetailService;
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
import java.text.DecimalFormat;
import java.util.*;

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
            requestMap.put("username",request.getParameter("username").trim());
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
            requestMap.put("username",request.getParameter("username").trim());
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

    @PostMapping(value = "/download/unitPrice")
    public void downloadUnitPrice(HttpServletResponse response, HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        map.put("limit", "10000000000");
        map.put("offset", "0");
        if (StringUtils.isNotBlank(request.getParameter("startDate"))) {
            map.put("startDate", request.getParameter("startDate"));
        }
        if (StringUtils.isNotBlank(request.getParameter("endDate"))) {
            map.put("endDate", request.getParameter("endDate"));
        }
        if (StringUtils.isNotBlank(request.getParameter("username"))){
            map.put("username",request.getParameter("username").trim());
        }
        List<String > columnNameList = new ArrayList<>();
        columnNameList.add("买家名称");
        columnNameList.add("低于500元订单");
        columnNameList.add("500-800元订单");
        columnNameList.add("800-1200订单");
        columnNameList.add("1200-1500元订单");
        columnNameList.add("1500-2000元订单");
        columnNameList.add("2000-5000元订单");
        columnNameList.add("5000元以上订单");
        columnNameList.add("客服人员");

        try {
            List<List<Object>> result = new ArrayList<>();
            List<Map<String,Object>> data = orderDetailService.getUnitPrice(map);
            List<Object> objects ;
            for (Map<String,Object> order : data) {
                objects = new ArrayList<>();
                objects.add(order.get("realname"));
                objects.add(order.get("underfive"));
                objects.add(order.get("inFiveEight"));
                objects.add(order.get("inEightTwelve"));
                objects.add(order.get("inTwelveFifteen"));
                objects.add(order.get("inFifteenTwenty"));
                objects.add(order.get("inTwentyFifty"));
                objects.add(order.get("moreFive"));
                objects.add(order.get("waysalesman"));
                result.add(objects);
            }
            CommonUtils.exportByList(response, columnNameList, result, "客单价订单");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @PostMapping(value = "/download/excel")
    public void download(HttpServletResponse response, HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        map.put("limit", "10000000000");
        map.put("offset", "0");
        if (request.getParameter("startDate")==null|| StringUtils.isBlank(request.getParameter("startDate"))){
            String firstDayOfMonth = DateTimeUtils.firstDayOfMonth(new Date());
            map.put("startDate",firstDayOfMonth);
        }else{
            String startDate = request.getParameter("startDate");
            DateTimeUtils.convert(startDate,DateTimeUtils.DATE_FORMAT);
            map.put("startDate", DateTimeUtils.convert(DateTimeUtils.convert(startDate,DateTimeUtils.DATE_FORMAT),DateTimeUtils.DATE_FORMAT)+" 00:00:00" );
        }
        if (request.getParameter("endDate")==null|| StringUtils.isBlank(request.getParameter("endDate"))){
            String firstDayOfMonth = DateTimeUtils.lastDayOfMonth(new Date());
            map.put("endDate",firstDayOfMonth);
        }else{
            String startDate = request.getParameter("endDate");
            DateTimeUtils.convert(startDate,DateTimeUtils.DATE_FORMAT);
            map.put("endDate", DateTimeUtils.convert(DateTimeUtils.convert(startDate,DateTimeUtils.DATE_FORMAT),DateTimeUtils.DATE_FORMAT)+" 23:59:59" );
        }
        if (StringUtils.isNotBlank(request.getParameter("username"))){
            map.put("username",request.getParameter("username").trim());
        }
        List<String > columnNameList = new ArrayList<>();
        columnNameList.add("客户全称");
        columnNameList.add("客户订单金额");
        columnNameList.add("客户订单数");
        columnNameList.add("客服人员");
        columnNameList.add("是否开通授信");
        columnNameList.add("单个订单平均规格数");
        columnNameList.add("环比（销售额）");
        columnNameList.add("同比（销售额）");
        columnNameList.add("省份|比例");

        try {
            List<List<Object>> result = new ArrayList<>();
            List<Map<String,Object>> data = orderDetailService.getPage(map);
            DecimalFormat df = new DecimalFormat("#.00");
            List<Object> objects ;
            for (Map<String,Object> order : data) {

                Double mtotalprice = order.get("mtotalprice") == null ? 0 : Double.parseDouble(order.get("mtotalprice").toString());
                Double totalprice = order.get("totalprice") == null ? 0 : Double.parseDouble(order.get("totalprice").toString());
                Double ytotalprice = order.get("ytotalprice") == null ? 0 : Double.parseDouble(order.get("ytotalprice").toString());
                objects = new ArrayList<>();
                objects.add(order.get("realname"));
                objects.add(order.get("totalprice"));
                objects.add(order.get("ordernum"));
                objects.add(order.get("waysalesman"));
                if (order.get("creditstate")!=null &&StringUtils.isNotBlank(order.get("creditstate").toString())){
                    int anInt = Integer.parseInt(order.get("creditstate").toString());
                    switch (anInt){
                        case 0: objects.add("未开通"); break;
                        case 2: objects.add("禁用授信"); break;
                        default:objects.add("已开通"); break;
                    }
                }
                objects.add(order.get("standardn"));
                objects.add(df.format((mtotalprice/totalprice)*100)  +"%");
                objects.add(df.format((ytotalprice/totalprice)*100)  +"%");
                long memberid = Long.parseLong(order.get("memberid").toString());
                List<Map<String, Object>> provinceRate = orderDetailService.getProvinceRateByMemberId(memberid,map);
                if (provinceRate!=null){
                    for (Map<String, Object> provinces : provinceRate) {
                        Double price = provinces.get("totalprice") == null ? 0 : Double.parseDouble(provinces.get("totalprice").toString());
                        objects.add(provinces.get("province")+"|"+ df.format((price/totalprice)*100)  +"%");
                    }
                }
                result.add(objects);
            }
            CommonUtils.exportByList(response, columnNameList, result, "产品详情");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostMapping(value = "/download/getOrderReport")
    public void getOrderReport(HttpServletResponse response, HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        map.put("limit", "10000000000");
        map.put("offset", "0");
        if (StringUtils.isNotBlank(request.getParameter("startDate"))) {
            map.put("startDate", request.getParameter("startDate"));
        }
        if (StringUtils.isNotBlank(request.getParameter("endDate"))) {
            map.put("endDate", request.getParameter("endDate"));
        }
        List<String > columnNameList = new ArrayList<>();
        columnNameList.add("日期");
        columnNameList.add("总订单量");
        columnNameList.add("已发出");
        columnNameList.add("未发出");
        columnNameList.add("订单拆分比例");
        columnNameList.add("未付款超时取消订单比例");
        columnNameList.add("退单比例");
        columnNameList.add("店铺退货率");
        columnNameList.add("仓库退货率");
        columnNameList.add("区域消费金额比例地图");
        columnNameList.add("低于500元订单");
        columnNameList.add("500-800元订单");
        columnNameList.add("800-1200订单");
        columnNameList.add("1200-1500元订单");
        columnNameList.add("1500-2000元订单");
        columnNameList.add("2000-5000元订单");
        columnNameList.add("5000元以上订单");

        try {
            List<List<Object>> result = new ArrayList<>();
            List<Map<String,Object>> data = orderDetailService.getOrderReport(map);
            DecimalFormat df = new DecimalFormat("#.00");
            List<Object> objects ;
            for (Map<String,Object> order : data) {
                objects = new ArrayList<>();
                Double ordernum = order.get("ordernum") == null ? 0 : Double.parseDouble(order.get("ordernum").toString());
                Double sentnum = order.get("sentnum") == null ? 0 : Double.parseDouble(order.get("sentnum").toString());
                Double notsentnum = order.get("notsentnum") == null ? 0 : Double.parseDouble(order.get("notsentnum").toString());
                Double splitnum = order.get("splitnum") == null ? 0 : Double.parseDouble(order.get("splitnum").toString());
                Double cancelnum = order.get("cancelnum") == null ? 0 : Double.parseDouble(order.get("cancelnum").toString());
                Double backnum = order.get("backnum") == null ? 0 : Double.parseDouble(order.get("backnum").toString());
                Double underfive = order.get("underfive") == null ? 0 : Double.parseDouble(order.get("underfive").toString());
                Double inFiveEight = order.get("inFiveEight") == null ? 0 : Double.parseDouble(order.get("inFiveEight").toString());
                Double inEightTwelve = order.get("inEightTwelve") == null ? 0 : Double.parseDouble(order.get("inEightTwelve").toString());
                Double inTwelveFifteen = order.get("inTwelveFifteen") == null ? 0 : Double.parseDouble(order.get("inTwelveFifteen").toString());
                Double inFifteenTwenty = order.get("inFifteenTwenty") == null ? 0 : Double.parseDouble(order.get("inFifteenTwenty").toString());
                Double inTwentyFifty = order.get("inTwentyFifty") == null ? 0 : Double.parseDouble(order.get("inTwentyFifty").toString());
                Double moreFive = order.get("moreFive") == null ? 0 : Double.parseDouble(order.get("moreFive").toString());
                objects.add(order.get("d"));
                objects.add(order.get("ordernum"));
                objects.add(df.format(sentnum/ordernum*100)+"%");
                objects.add(df.format(notsentnum/ordernum*100)+"%");
                objects.add(df.format(splitnum/ordernum*100)+"%");
                objects.add(df.format(cancelnum/ordernum*100)+"%");
                objects.add(df.format(backnum/ordernum*100)+"%");
                objects.add("查看");
                objects.add("查看");
                objects.add("查看");
                objects.add(df.format(underfive/ordernum*100)+"%");
                objects.add(df.format(inFiveEight/ordernum*100)+"%");
                objects.add(df.format(inEightTwelve/ordernum*100)+"%");
                objects.add(df.format(inTwelveFifteen/ordernum*100)+"%");
                objects.add(df.format(inFifteenTwenty/ordernum*100)+"%");
                objects.add(df.format(inTwentyFifty/ordernum*100)+"%");
                objects.add(df.format(moreFive/ordernum*100)+"%");
                result.add(objects);
            }
            CommonUtils.exportByList(response, columnNameList, result, "客单价订单");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "orderInfo",method = RequestMethod.POST)
    @ResponseBody
    public Object orderInfo(HttpServletRequest request) {
        Map<String, String> requestMap = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isNotBlank(request.getParameter("startDate"))) {
            requestMap.put("startDate", request.getParameter("startDate"));
        }
        if (StringUtils.isNotBlank(request.getParameter("endDate"))) {
            requestMap.put("endDate", request.getParameter("endDate"));
        }
        if (StringUtils.isNotBlank(request.getParameter("username"))){
            requestMap.put("username",request.getParameter("username").trim());
        }
        if (StringUtils.isNotBlank(request.getParameter("orderno"))){
            requestMap.put("orderno",request.getParameter("orderno").trim());
        }
        if (StringUtils.isNotBlank(request.getParameter("sellCompany"))){
            requestMap.put("sellCompany",request.getParameter("sellCompany").trim());
        }
        if (StringUtils.isNotBlank(request.getParameter("buyCompany"))){
            requestMap.put("buyCompany",request.getParameter("buyCompany").trim());
        }
        if (StringUtils.isNotBlank(request.getParameter("limit"))) {
            requestMap.put("limit", request.getParameter("limit"));
        } else {
            requestMap.put("limit", "10");
        }
            if (StringUtils.isNotBlank(request.getParameter("offset"))){
                requestMap.put("offset", request.getParameter("offset"));
            }else{
                requestMap.put("offset", "0");
            }
        List<Map<String, Object>> page = orderDetailService.orderInfo(requestMap);
        result.put("rows", page);
        result.put("total", orderDetailService.getOrderInfoCount(requestMap));
        return result;
    }

    @PostMapping(value = "/download/orderInfo")
    public void downloadOrderInfo(HttpServletResponse response, HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        map.put("limit", "10000000000");
        map.put("offset", "0");
        if (StringUtils.isNotBlank(request.getParameter("startDate"))) {
            map.put("startDate", request.getParameter("startDate"));
        }
        if (StringUtils.isNotBlank(request.getParameter("endDate"))) {
            map.put("endDate", request.getParameter("endDate"));
        }
        if (StringUtils.isNotBlank(request.getParameter("username"))){
            map.put("username",request.getParameter("username").trim());
        }
        if (StringUtils.isNotBlank(request.getParameter("orderno"))){
            map.put("orderno",request.getParameter("orderno").trim());
        }
        if (StringUtils.isNotBlank(request.getParameter("sellCompany"))){
            map.put("sellCompany",request.getParameter("sellCompany").trim());
        }
        if (StringUtils.isNotBlank(request.getParameter("buyCompany"))){
            map.put("buyCompany",request.getParameter("buyCompany").trim());
        }
        List<String > columnNameList = new ArrayList<>();
        columnNameList.add("下单时间");
        columnNameList.add("订单编号");
        columnNameList.add("紧商网用户名称");
        columnNameList.add("买方");
        columnNameList.add("结算单位名称");
        columnNameList.add("卖方");
        columnNameList.add("客服");
        columnNameList.add("业务员");
        columnNameList.add("来源");
        columnNameList.add("状态");
        columnNameList.add("收货人");
        columnNameList.add("收货电话");
        columnNameList.add("收货地址");
        columnNameList.add("订单金额");

        try {
            List<List<Object>> result = new ArrayList<>();
            List<Map<String, Object>> page = orderDetailService.orderInfo(map);
            List<Object> objects ;
            for (Map<String,Object> order : page) {
                objects = new ArrayList<>();
                objects.add(order.get("createtime"));
                objects.add(order.get("orderno"));
                objects.add(order.get("realname"));
                objects.add(order.get("companyname"));
                objects.add(order.get("invoiceheadup"));
                objects.add(order.get("membercompany"));
                objects.add(order.get("clerkname"));
                objects.add(order.get("waysalesman"));
                objects.add("0".equals(order.get("isonline"))?"线上":"线下");
                Integer orderstatus = Integer.valueOf( order.get("orderstatus").toString());
                String back;
                if (orderstatus!=null) {
                    switch (orderstatus) {
                        case 0: back = "待付款";break;
                        case 1: back = "待发货"; break;
                        case 3: back = "待收货"; break;
                        case 4: back = "待验货"; break;
                        case 5: back = "已完成"; break;
                        case 7: back = "已关闭"; break;
                        case 8: back = "备货中"; break;
                        case 9: back = "备货完成"; break;
                        case 10: back = "部分发货"; break;
                        default : back = "未知状态";
                    }
                }else {
                    back = "未知状态";
                }
                objects.add(back);
                objects.add(order.get("shipto"));
                objects.add(order.get("phone"));
                objects.add(order.get("address"));
                objects.add(order.get("totalprice"));
                result.add(objects);
            }
            CommonUtils.exportByList(response, columnNameList, result, "订单信息");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
