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
import org.springframework.web.bind.annotation.*;

import javax.persistence.Tuple;
import javax.servlet.http.HttpServletRequest;
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
    @CrossOrigin(origins = "http://localhost:9527", maxAge = 3600)
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
     * @param regionalSalesDTO    区域销售额参数
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

    /**
     * 订单区域统计
     *
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @param offset 偏移量
     * @param limit  数量
     * @return Object
     */
    @ApiOperation(value = "订单区域统计", notes = "数据来源：用友；数据截止日期：昨天")
    @PostMapping(value = "/findOrderAreas")
    public Object findOrderAreas(String startDate, String endDate, String province, String city, int offset, int limit) {
        return salesService.findOrderAreas(startDate, endDate, province, city, offset, limit);
    }
    @PostMapping(value = "/getAmount")
    public Object getAmount(HttpServletRequest request){
        Map<String, String> params = new HashMap<String, String>();
        if(request.getParameter("productName")!=null&&StringUtils.isNotBlank(request.getParameter("productName"))){
            params.put("productName",request.getParameter("productName"));
        }
        if(request.getParameter("classOne")!=null&&StringUtils.isNotBlank(request.getParameter("classOne"))){
            params.put("classOne",request.getParameter("classOne"));
        }
        if(request.getParameter("classTwo")!=null&&StringUtils.isNotBlank(request.getParameter("classTwo"))){
            params.put("classTwo",request.getParameter("classTwo"));
        }
        if(request.getParameter("classify")!=null&&StringUtils.isNotBlank(request.getParameter("classify"))){
            params.put("classify",request.getParameter("classify"));
        }
        if(request.getParameter("standard")!=null&&StringUtils.isNotBlank(request.getParameter("standard"))){
            params.put("standard",request.getParameter("standard"));
        }
        if(request.getParameter("brand")!=null&&StringUtils.isNotBlank(request.getParameter("brand"))){
            params.put("brand",request.getParameter("brand"));
        }
        if(request.getParameter("mark")!=null&&StringUtils.isNotBlank(request.getParameter("mark"))){
            params.put("mark",request.getParameter("mark"));
        }
        if(request.getParameter("material")!=null&&StringUtils.isNotBlank(request.getParameter("material"))){
            params.put("material",request.getParameter("material"));
        }
        if(request.getParameter("grade")!=null&&StringUtils.isNotBlank(request.getParameter("grade"))){
            params.put("grade",request.getParameter("grade"));
        }
        if(request.getParameter("surface")!=null&&StringUtils.isNotBlank(request.getParameter("surface"))){
            params.put("surface",request.getParameter("surface"));
        }
        if(request.getParameter("nominalDiameter")!=null&&StringUtils.isNotBlank(request.getParameter("nominalDiameter"))){
            params.put("nominalDiameter",request.getParameter("nominalDiameter"));
        }
        if(request.getParameter("pitch")!=null&&StringUtils.isNotBlank(request.getParameter("pitch"))){
            params.put("pitch",request.getParameter("pitch"));
        }
        if(request.getParameter("extent")!=null&&StringUtils.isNotBlank(request.getParameter("extent"))){
            params.put("extent",request.getParameter("extent"));
        }
        if(request.getParameter("extent")!=null&&StringUtils.isNotBlank(request.getParameter("extent"))){
            params.put("extent",request.getParameter("extent"));
        }
        if(request.getParameter("outerDiameter")!=null&&StringUtils.isNotBlank(request.getParameter("outerDiameter"))){
            params.put("outerDiameter",request.getParameter("outerDiameter"));
        }
        if(request.getParameter("thickness")!=null&&StringUtils.isNotBlank(request.getParameter("thickness"))){
            params.put("thickness",request.getParameter("thickness"));
        }
        if(request.getParameter("store")!=null&&StringUtils.isNotBlank(request.getParameter("store"))){
            params.put("store",request.getParameter("store"));
        }
        if(request.getParameter("startCreateTime")!=null&&StringUtils.isNotBlank(request.getParameter("startCreateTime"))){
            params.put("startCreateTime",request.getParameter("startCreateTime"));
        }
        if(request.getParameter("endCreateTime")!=null&&StringUtils.isNotBlank(request.getParameter("endCreateTime"))){
            params.put("endCreateTime",request.getParameter("endCreateTime"));
        }
        List<Map<String, Object>> list = salesService.getSaleAmount(params);

//        request.getParameter("");
//        request.getParameter("standard");
//        request.getParameter("brand");
//        request.getParameter("mark");
//        request.getParameter("material");
//        request.getParameter("grade");
//        request.getParameter("surface");
//        request.getParameter("nominalDiameter");
//        request.getParameter("pitch");
//        request.getParameter("extent");
//        request.getParameter("outerDiameter");
//        request.getParameter("thickness");
//        request.getParameter("store");

        return list;
    }
}
