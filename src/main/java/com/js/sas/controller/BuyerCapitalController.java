package com.js.sas.controller;

import com.js.sas.service.BuyerCapitalService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

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
            @ApiImplicitParam(name = "seller", value = "卖家id", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "startDate", value = "开始日期", required = false, paramType = "query", dataType = "date"),
            @ApiImplicitParam(name = "endDate", value = "结束日期", required = false, paramType = "query", dataType = "date"),
           /* @ApiImplicitParam(name = "sort", value = "排序字段", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "sortOrder", value = "排序规则", required = false, paramType = "query", dataType = "string")*/
    })
    @PostMapping("/customerStatement")
    @ResponseBody
    public Object listForAccount(@ApiParam @RequestBody Map<String, String> params) {
        for (String str : params.keySet()) {
            log.info("参数key : {} ,value {}", str, params.get("str"));
        }
        return buyerCapitalService.getAccountsPayable(params);
    }

    @ApiIgnore
    @PostMapping("/download/excel")
    public void download(@RequestBody Map<String, String> params, HttpServletResponse httpServletResponse) {

    }

}
