package com.js.sas.controller;

import com.js.sas.service.OrderProductService;
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
import java.util.*;

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

    @PostMapping(value = "/download/excel")
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
        if (StringUtils.isNotBlank(request.getParameter("orderno"))){
            map.put("orderno",request.getParameter("orderno").trim());
        }
        List<String > columnNameList = new ArrayList<>();
        columnNameList.add("订单类型");
        columnNameList.add("商品名称");
        columnNameList.add("商品分类");
        columnNameList.add("材质");
        columnNameList.add("牌号");
        columnNameList.add("品牌");
        columnNameList.add("印记");
        columnNameList.add("表面处理");
        columnNameList.add("包装方式");
        columnNameList.add("单位");
        columnNameList.add("单价");
        columnNameList.add("订购量");
        columnNameList.add("货款金额");
        try {
            List<List<Object>> result = new ArrayList<>();
            List<Map<String,Object>> data = orderProductService.getPage(map);

            List<Object> objects ;
            for (Map<String,Object> order : data) {
                objects = new ArrayList<>();
                objects.add(order.get("isonline"));
                objects.add(order.get("pdname"));
                objects.add(order.get("classify"));
                objects.add(order.get("material"));
                objects.add(order.get("gradeno"));
                objects.add(order.get("brand"));
                objects.add(order.get("mark"));
                objects.add(order.get("surfacetreatment"));
                objects.add(order.get("packagetype"));
                objects.add(order.get("unit"));
                objects.add(order.get("price"));
                objects.add(order.get("num"));
                objects.add(order.get("amount"));
                result.add(objects);
            }
            CommonUtils.exportByList(response, columnNameList, result, "订单产品");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "receiptList",method = RequestMethod.POST)
    @ResponseBody
    public Object receiptList(HttpServletRequest request) {
        Map<String, String> requestMap = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isNotBlank(request.getParameter("startDate"))) {
            requestMap.put("startDate", request.getParameter("startDate"));
        }
        if (StringUtils.isNotBlank(request.getParameter("endDate"))) {
            requestMap.put("endDate", request.getParameter("endDate"));
        }
        if (StringUtils.isNotBlank(request.getParameter("invoiceName"))){
            requestMap.put("invoiceName",request.getParameter("invoiceName").trim());
        }
        if (StringUtils.isNotBlank(request.getParameter("username"))){
            requestMap.put("username",request.getParameter("username").trim());
        }
        if (StringUtils.isNotBlank(request.getParameter("paytype"))){
            requestMap.put("paytype",request.getParameter("paytype"));
        }
        if (StringUtils.isNotBlank(request.getParameter("salesman"))){
            requestMap.put("salesman",request.getParameter("salesman"));
        }
        if (StringUtils.isNotBlank(request.getParameter("clerkname"))){
            requestMap.put("clerkname",request.getParameter("clerkname"));
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
        List<Map<String, Object>> page = orderProductService.receiptList(requestMap);
        result.put("rows", page);
        result.put("total", orderProductService.getReceiptListCount(requestMap));
        return result;
    }


    @PostMapping(value = "/download/receiptList")
    public void downloadReceiptList(HttpServletResponse response, HttpServletRequest request) {
        Map<String, String> requestMap = new HashMap<>();
        if (StringUtils.isNotBlank(request.getParameter("startDate"))) {
            requestMap.put("startDate", request.getParameter("startDate"));
        }
        if (StringUtils.isNotBlank(request.getParameter("endDate"))) {
            requestMap.put("endDate", request.getParameter("endDate"));
        }
        if (StringUtils.isNotBlank(request.getParameter("invoiceName"))){
            requestMap.put("invoiceName",request.getParameter("invoiceName").trim());
        }
        if (StringUtils.isNotBlank(request.getParameter("username"))){
            requestMap.put("username",request.getParameter("username").trim());
        }
        if (StringUtils.isNotBlank(request.getParameter("paytype"))){
            requestMap.put("paytype",request.getParameter("paytype"));
        }
        if (StringUtils.isNotBlank(request.getParameter("salesman"))){
            requestMap.put("salesman",request.getParameter("salesman"));
        }
        if (StringUtils.isNotBlank(request.getParameter("clerkname"))){
            requestMap.put("clerkname",request.getParameter("clerkname"));
        }
        if (StringUtils.isNotBlank(request.getParameter("sort"))) {
            requestMap.put("sort", request.getParameter("sort").trim());
        }
        if (StringUtils.isNotBlank(request.getParameter("sortOrder"))) {
            requestMap.put("sortOrder", request.getParameter("sortOrder").trim());
        }
        requestMap.put("offset", "0");
        requestMap.put("limit", "1000000000");
        List<String > columnNameList = new ArrayList<>();
        columnNameList.add("订单编号");
        columnNameList.add("开票名称");
        columnNameList.add("客户名称");
        columnNameList.add("业务员");
        columnNameList.add("客服");
        columnNameList.add("支付类型");
        columnNameList.add("交易号");
        columnNameList.add("订单总额");
        columnNameList.add("交易时间");
        try {
            List<List<Object>> result = new ArrayList<>();
            List<Map<String, Object>> data = orderProductService.receiptList(requestMap);

            List<Object> objects ;
            for (Map<String,Object> order : data) {
                objects = new ArrayList<>();
                objects.add(order.get("orderno"));
                objects.add(order.get("invoiceheadup"));
                objects.add(order.get("realname"));
                objects.add(order.get("waysalesman"));
                objects.add(order.get("clerkname"));

                Integer paytype = Integer.valueOf(order.get("paytype").toString());
                String pay ;
                switch (paytype){
                    case 0: pay = "支付宝";break;
                    case 1: pay = "微信";break;
                    case 2: pay = "银行卡";break;
                    case 3: pay = "余额";break;
                    case 4: pay = "授信";break;
                    default: pay = "-";break;
                }
                objects.add(pay);
                objects.add(order.get("transactionid"));
                objects.add(order.get("totalprice"));
                objects.add(order.get("paymenttime"));
                result.add(objects);
            }
            CommonUtils.exportByList(response, columnNameList, result, "收款明细");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
