package com.js.sas.controller;

import com.js.sas.dto.OrderProductDTO;
import com.js.sas.dto.RegionalSalesDTO;
import com.js.sas.dto.SaleAmountDTO;
import com.js.sas.service.SalesService;
import com.js.sas.utils.CommonUtils;
import com.js.sas.utils.Result;
import com.js.sas.utils.ResultCode;
import com.js.sas.utils.ResultUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Tuple;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName SalesController
 * @Description 销售
 * @Author zc
 * @Date 2019/6/21 17:14
 **/
@Slf4j
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
        List<SaleAmountDTO> saleDeliveryList = salesService.getSaleAmountByDay(limit);
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
        List<SaleAmountDTO> saleDeliveryList = salesService.getSaleAmountByMonth(limit);
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
        List<SaleAmountDTO> saleDeliveryList = salesService.getSaleAmountByYear(limit);
        return ResultUtils.getResult(ResultCode.成功, saleDeliveryList);
    }

    /**
     * 本年度各省销售额
     *
     * @return Result，本年度各省销售额
     */
    @PostMapping("/getProvinceOfSales")
    public Object getProvinceOfSales() {
        List<RegionalSalesDTO> provinceOfSalesList = salesService.getRegionalSales("2019-01-01", "2019-12-31 23:59:59");
        return ResultUtils.getResult(ResultCode.成功, provinceOfSalesList);
    }

    /**
     * 商品销售额
     *
     * @param orderProductDTO 订单商品
     * @return Result，商品销售总额
     */
    @PostMapping("/getProductValueOfSales")
    public Object getProductValueOfSales(OrderProductDTO orderProductDTO) {
        List<Tuple> resultList = salesService.getProductValueOfSales(orderProductDTO);

        HashMap[] rowsArray = new HashMap[1];
        HashMap<String, Object> data = new HashMap<>();

        data.put("count", resultList.get(0).get(0));
        data.put("amount", resultList.get(0).get(1));

        rowsArray[0] = data;

        HashMap<String, Object> result = new HashMap<>();
        result.put("rows", rowsArray);
        result.put("total", 1);

        return result;
    }

    /**
     * 区域销售额
     *
     * @return Result，区域销售额
     */
    @PostMapping("/getRegionalSales")
    public Object getRegionalSales(@Validated RegionalSalesDTO regionalSalesDTO, BindingResult result) {
        // 参数格式校验
        if (result.hasErrors()) {
            LinkedHashMap<String, String> errorMap = new LinkedHashMap<>();
            for (FieldError fieldError : result.getFieldErrors()) {
                errorMap.put(fieldError.getCode(), fieldError.getDefaultMessage());
            }
            return ResultUtils.getResult(ResultCode.参数错误, errorMap);
        }

        List<RegionalSalesDTO> regionalSales = salesService.getRegionalSales(regionalSalesDTO.getStartCreateTime(), regionalSalesDTO.getEndCreateTime() + " 23:59:59");

        HashMap<String, Object> resultHashMap = new HashMap<>();
        resultHashMap.put("rows", regionalSales);
        resultHashMap.put("total", 1);

        return resultHashMap;
    }

    /**
     * 区域销售额导出
     *
     * @param regionalSalesDTO 区域销售额参数
     * @param httpServletResponse httpServletResponse
     */
    @PostMapping("/exportRegionalSales")
    public void exportRegionalSales(RegionalSalesDTO regionalSalesDTO, HttpServletResponse httpServletResponse) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        if (StringUtils.isBlank(regionalSalesDTO.getStartCreateTime())) {
            regionalSalesDTO.setStartCreateTime("2018-01-01");
        }
        if (StringUtils.isBlank(regionalSalesDTO.getEndCreateTime())) {
            regionalSalesDTO.setEndCreateTime(sdf.format(new Date()));
        }

        List<RegionalSalesDTO> regionalSalesList = salesService.getRegionalSales(regionalSalesDTO.getStartCreateTime(), regionalSalesDTO.getEndCreateTime() + " 23:59:59");

        try {
            CommonUtils.export(httpServletResponse, regionalSalesList, "区域销售额", new RegionalSalesDTO());
        } catch (IOException e) {
            log.error("下载区域销售额异常：{}", e);
            e.printStackTrace();
        }
    }
}
