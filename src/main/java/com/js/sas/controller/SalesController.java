package com.js.sas.controller;

import com.js.sas.entity.SaleAmountEntity;
import com.js.sas.service.SalesService;
import com.js.sas.utils.Result;
import com.js.sas.utils.ResultCode;
import com.js.sas.utils.ResultUtils;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @ClassName SalesController
 * @Description 销售
 * @Author zc
 * @Date 2019/6/21 17:14
 **/
@RestController
@RequestMapping("/sales")
public class SalesController {
    private final SalesService salesService;

    public SalesController(SalesService salesService) {
        this.salesService = salesService;
    }

    /**
     * 日销售额
     *
     * @param limit 天数
     * @return Result，日销售额列表
     */
    @ApiOperation(value = "日销售额", notes = "数据来源：用友；数据截止日期：昨天")
    @PostMapping("/getSaleAmountByDay")
    public Result getSaleAmountByDay(int limit) {
        List<SaleAmountEntity> saleDeliveryList = salesService.getSaleAmountByDay(limit);
        return ResultUtils.getResult(ResultCode.成功, saleDeliveryList);
    }

    /**
     * 月销售额
     *
     * @param limit 月数
     * @return Result，月销售额列表
     */
    @ApiOperation(value = "月销售额", notes = "数据来源：用友；数据截止日期：昨天")
    @PostMapping("/getSaleAmountByMonth")
    public Result getSaleAmountByMonth(int limit) {
        List<SaleAmountEntity> saleDeliveryList = salesService.getSaleAmountByMonth(limit);
        return ResultUtils.getResult(ResultCode.成功, saleDeliveryList);
    }

    /**
     * 年销售额
     *
     * @param limit 年数
     * @return Result，年销售额列表
     */
    @ApiOperation(value = "年销售额", notes = "数据来源：用友；数据截止日期：昨天")
    @PostMapping("/getSaleAmountByYear")
    public Result getSaleAmountByYear(int limit) {
        List<SaleAmountEntity> saleDeliveryList = salesService.getSaleAmountByYear(limit);
        return ResultUtils.getResult(ResultCode.成功, saleDeliveryList);
    }

}
