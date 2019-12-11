package com.js.sas.api;

import com.js.sas.service.FinanceService;
import com.js.sas.utils.DateTimeUtils;
import com.js.sas.utils.Result;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: daniel
 * @date: 2019/11/27 0027 16:53
 * @Description:
 */
@Api
@Controller
@RequestMapping("api")
public class ApiController {

    @Autowired
    private FinanceService financeService;


    /**
     *
     * @param customerName
     * @param startDate
     * @param endDate
     * @return
     */
    @ApiOperation(value = "订单区域统计",httpMethod = "POST",notes = "数据来源：用友；数据截止日期：昨天")
    @RequestMapping(value = "getPurchaseInvoice",method = RequestMethod.POST)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "customerName",required = true,paramType = "query",dataType = "String"),
            @ApiImplicitParam(name = "startDate",required = true,paramType = "query",dataType = "String"),
            @ApiImplicitParam(name = "endDate",required = true,paramType = "query",dataType = "String")
    })
    @ResponseBody
    public Object getPurchaseInvoice(@RequestParam(value = "customerName") String customerName,
                                     @RequestParam(value = "startDate") String startDate,
                                     @RequestParam(value = "endDate") String endDate){
        if (StringUtils.isBlank(customerName)){
            return new Result("400","用户名为空",null);
        }
        if (startDate.length()>=10){
            startDate = startDate.substring(0,10);
        }
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("customerName", customerName.trim());
        requestMap.put("startDate", startDate+" 00:00:00");
        requestMap.put("endDate", endDate);
        Map<String, Object> result = new HashMap<>();
        result.put("customerName",customerName);
        result.put("company","紧商科技股份有限公司/财务");
        result.put("companyTel","TEL:0571-57173777-7037");
        result.put("companyFax","FAX:0571-83693829");
        Object online = financeService.getAccountspayable(requestMap,"线上");
        Object offline = financeService.getAccountspayable(requestMap, "线下");
        List<Object> list = new ArrayList<>();
        list.add(online);
        list.add(offline);
        result.put("reportContent",list);
        return result;
    }

}
