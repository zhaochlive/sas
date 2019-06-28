package com.js.sas.controller;

import com.js.sas.entity.AccountsPayable;
import com.js.sas.service.BuyerCapitalService;
import com.js.sas.utils.CommonUtils;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;

@Api(value = "查询对账单信息")
@Controller
@RequestMapping("/buyerCapital")
@Slf4j
public class BuyerCapitalController {

    @Autowired
    private BuyerCapitalService buyerCapitalService;

    @ApiOperation(value = "买家对账单所用的资金列表查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = false, paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "limit", value = "每页数量", required = false, paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "offset", value = "数据起始位", required = false, paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "invoiceName", value = "开票名称", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "companyname", value = "公司名称", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "userNo", value = "会员编号", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "userName", value = "会员全称", required = false, paramType = "query", dataType = "String"),
            //@ApiImplicitParam(name = "seller", value = "卖家id", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "startDate", value = "开始日期", required = false, paramType = "query", dataType = "date"),
            @ApiImplicitParam(name = "endDate", value = "结束日期", required = false, paramType = "query", dataType = "date"),
           /* @ApiImplicitParam(name = "sort", value = "排序字段", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "sortOrder", value = "排序规则", required = false, paramType = "query", dataType = "string")*/
    })
    @PostMapping("/customerStatement")
    @ResponseBody
    public Object listForAccount(@ApiParam @RequestBody Map<String, String> params) {
        for (String str : params.keySet()) {
            log.info("参数key : {} ,value :{}", str, params.get(str));
        }
        Boolean b = false;
        if (params.containsKey("userName") && StringUtils.isNoneBlank(params.get("userName"))) {
            b = true;
        }
        if (params.containsKey("userNo") && StringUtils.isNoneBlank(params.get("userNo"))) {
            b = true;
        }
        if (params.containsKey("invoiceName") && StringUtils.isNoneBlank(params.get("invoiceName"))) {
            b = true;
        }
        if (params.containsKey("companyname") && StringUtils.isNoneBlank(params.get("companyname"))) {
            b = true;
        }
        if (params.containsKey("startDate") && StringUtils.isNoneBlank(params.get("startDate"))) {
            b = true;
        }
        if (params.containsKey("endDate") && StringUtils.isNoneBlank(params.get("endDate"))) {
            b = true;
        }
        if (b) {
            return buyerCapitalService.getAccountsPayable(params);
        } else {
            Map<String, Object> objectHashMap = new HashMap<>();
            objectHashMap.put("total", 0);
            objectHashMap.put("rows", null);
            objectHashMap.put("DeliveryAmount", 0);
            objectHashMap.put("ReceiptAmount", 0);
            objectHashMap.put("OtherAmount", 0);
            objectHashMap.put("Invoice", 0);
            objectHashMap.put("Receivable", 0);
            return objectHashMap;
        }
    }

    @ApiIgnore
    @GetMapping(value = "/download/excel")
    public void download(HttpServletResponse response, HttpServletRequest request) {
        String userName = request.getParameter("userName");
        String userNo = request.getParameter("userNo");
        String invoiceName = request.getParameter("invoiceName");
        String companyname = request.getParameter("companyname");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        Map<String, String> params = new HashMap<>();
        if (StringUtils.isNotBlank(userName)) {
            params.put("userName", userName);
        }
        if (StringUtils.isNotBlank(userNo)) {
            params.put("userNo", userNo);
        }
        if (StringUtils.isNotBlank(invoiceName)) {
            params.put("invoiceName", invoiceName);
        }
        if (StringUtils.isNotBlank(companyname)) {
            params.put("companyname", companyname);
        }
        if (StringUtils.isNotBlank(startDate)) {
            params.put("startDate", startDate);
        }
        if (StringUtils.isNotBlank(endDate)) {
            params.put("endDate", endDate);
        }
        params.put("offset", "0");
        params.put("limit", "10000000");
        log.info("参数userName:{},userNo:{},invoiceName:{},companyname:{},startDate:{},endDate{}:", userName, userNo, invoiceName, companyname, startDate, endDate);
        List<AccountsPayable> accountsPayables = null;
        try {
            Map<String, Object> map = buyerCapitalService.getAccountsPayable(params);
            if (map.get("rows") != null) {
                accountsPayables = (List<AccountsPayable>) map.get("rows");
                AccountsPayable first = new AccountsPayable();
                first.setOrderno("当期结算");
                first.setReceivingAmount((BigDecimal) map.get("ReceiptAmount"));
                first.setDeliveryAmount((BigDecimal) map.get("DeliveryAmount"));
                first.setOtherAmount((BigDecimal) map.get("OtherAmount"));
                first.setReceivableAccount((BigDecimal) map.get("Receivable"));
                first.setInvoicebalance((BigDecimal) map.get("Invoice"));
                accountsPayables.add(0, first);

                CommonUtils.export(response, accountsPayables, "结算客户对账单", new AccountsPayable());
            } else {
                accountsPayables = new ArrayList<>();
                AccountsPayable first = new AccountsPayable();
                first.setOrderno("当期结算");
                first.setReceivingAmount((BigDecimal) map.get("ReceiptAmount"));
                first.setDeliveryAmount((BigDecimal) map.get("DeliveryAmount"));
                first.setOtherAmount((BigDecimal) map.get("OtherAmount"));
                first.setReceivableAccount((BigDecimal) map.get("Receivable"));
                first.setInvoicebalance((BigDecimal) map.get("Invoice"));
                accountsPayables.add(0, first);
                CommonUtils.export(response, accountsPayables, "结算客户对账单", new AccountsPayable());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
