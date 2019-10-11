package com.js.sas.controller;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;
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
import springfox.documentation.annotations.ApiIgnore;

import javax.persistence.Tuple;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * @param offset    偏移量
     * @param limit     数量
     * @return Object
     */
    @ApiOperation(value = "订单区域统计", notes = "数据来源：用友；数据截止日期：昨天")
    @PostMapping(value = "/findOrderAreas")
    public Object findOrderAreas(String startDate, String endDate, String province, String city, int offset, int limit) {
        return salesService.findOrderAreas(startDate, endDate, province, city, offset, limit);
    }

    /**
     * 客户、开票名称、客服、地址，月销售统计
     * 其中：开票名称、地址随机取一个
     *
     * @param startMonth 开始时间 yyyy-MM
     * @param endMonth   结束时间 yyyy-MM
     * @return Object
     */
    @ApiOperation(value = "客户、开票名称、客服、地址，月销售统计", notes = "数据来源：紧商网；数据截止日期：实时")
    @PostMapping(value = "/findMonthlySalesAmount")
    public Object findMonthlySalesAmount(String startMonth, String endMonth, String username, String staff, String limit, String offset, String sort, String sortOrder) {
        // 参数不能为空
        if (StringUtils.isBlank(startMonth) || StringUtils.isBlank(endMonth) || StringUtils.isBlank(limit) || StringUtils.isBlank(offset)) {
            return ResultUtils.getResult(ResultCode.参数错误);
        }
        int limitInt = 10;
        int offsetInt = 0;
        // 数字判断
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        if (pattern.matcher(limit).matches() && pattern.matcher(offset).matches()) {
            limitInt = Integer.parseInt(limit);
            offsetInt = Integer.parseInt(offset);
        } else {
            return ResultUtils.getResult(ResultCode.参数错误);
        }
        // 判断月份格式
        String str = "(\\b[1-3]\\d{3})-(0[1-9]|1[0-2])";
        pattern = Pattern.compile(str);
        Matcher matcherStart = pattern.matcher(startMonth);
        Matcher matcherEnd = pattern.matcher(endMonth);
        if (matcherStart.find() && matcherEnd.find()) {
            return salesService.findMonthlySalesAmount(startMonth, endMonth, username, staff, limitInt, offsetInt, sort, sortOrder);
        } else {
            return ResultUtils.getResult(ResultCode.参数错误);
        }
    }

    /**
     *
     * @param response
     * @param startMonth
     * @param endMonth
     * @param username
     * @param staff
     */
    @ApiIgnore
    @PostMapping("/exportMonthlySalesAmount")
    private void exportMonthlySalesAmount(HttpServletResponse response, String startMonth, String endMonth, String username, String staff) {
        // 参数不能为空
        if (StringUtils.isBlank(startMonth) || StringUtils.isBlank(endMonth)) {
            return;
        }
        // 判断月份格式
        String str = "(\\b[1-3]\\d{3})-(0[1-9]|1[0-2])";
        Pattern pattern = Pattern.compile(str);
        Matcher matcherStart = pattern.matcher(startMonth);
        Matcher matcherEnd = pattern.matcher(endMonth);

        Map<String, Object> result = new HashMap<>();
        List dataList = new ArrayList();
        List<String> columnNameList = new ArrayList();

        if (matcherStart.find() && matcherEnd.find()) {
            result = salesService.findMonthlySalesAmountAll(startMonth, endMonth, username, staff);
            columnNameList = (List) result.get("columnNameList");
            dataList = (List) result.get("dataList");
        } else {
            return;
        }

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");

        String fileName = "紧商网客户月下单统计";
        Sheet sheet1 = new Sheet(1, 0);
        sheet1.setSheetName(fileName);
        sheet1.setAutoWidth(Boolean.TRUE);

        fileName = fileName + df.format(new Date());
        ServletOutputStream out = null;
        try {
            out = response.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        response.setContentType("multipart/form-data");
        response.setCharacterEncoding("utf-8");
        try {
            response.setHeader("Content-Disposition", "attachment;filename*= UTF-8''" + URLEncoder.encode(fileName, "UTF-8") + ".xlsx");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // 设置列名
        if (columnNameList != null) {
            List<List<String>> list = new ArrayList<>();
            columnNameList.forEach(c -> list.add(Collections.singletonList(c)));
            sheet1.setHead(list);
        }
        // 写入数据
        ExcelWriter writer = new ExcelWriter(out, ExcelTypeEnum.XLSX, true);
        writer.write1(dataList, sheet1);
        writer.finish();

        if(out != null) {
            try {
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }


}
