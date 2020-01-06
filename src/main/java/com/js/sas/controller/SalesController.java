package com.js.sas.controller;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.js.sas.entity.dto.OrderProductDTO;
import com.js.sas.entity.dto.RegionalSalesDTO;
import com.js.sas.entity.dto.SaleAmountDTO;
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
import javax.servlet.http.HttpServletRequest;
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
        // 格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 开始日期
        Calendar beginDate = Calendar.getInstance();
        // 设置年份
        beginDate.set(Calendar.YEAR, beginDate.get(Calendar.YEAR));
        // 1月
        beginDate.set(Calendar.MONTH, 0);
        // 1日
        beginDate.set(Calendar.DAY_OF_MONTH, 1);
        // 0点
        beginDate.set(Calendar.HOUR_OF_DAY, 0);
        // 0分
        beginDate.set(Calendar.MINUTE, 0);
        // 0秒
        beginDate.set(Calendar.SECOND, 0);
        // 结束日期
        Calendar endDate = Calendar.getInstance();
        // 设置年份
        endDate.set(Calendar.YEAR, endDate.get(Calendar.YEAR));
        // 12月
        endDate.set(Calendar.MONTH, 11);
        // 31日
        endDate.set(Calendar.DAY_OF_MONTH, 31);
        // 23点
        endDate.set(Calendar.HOUR_OF_DAY, 23);
        // 59分
        endDate.set(Calendar.MINUTE, 59);
        // 59秒
        endDate.set(Calendar.SECOND, 59);
        List<RegionalSalesDTO> provinceOfSalesList = salesService.getRegionalSales(sdf.format(beginDate.getTime()), sdf.format(endDate.getTime()));
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
    public Object findMonthlySalesAmount(String startMonth, String endMonth, String username, String staff, String address, String limit, String offset, String sort, String sortOrder) {
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
            return salesService.findMonthlySalesAmount(startMonth, endMonth, username, staff, address, limitInt, offsetInt, sort, sortOrder);
        } else {
            return ResultUtils.getResult(ResultCode.参数错误);
        }
    }

    /**
     * @param response
     * @param startMonth
     * @param endMonth
     * @param username
     * @param staff
     */
    @ApiIgnore
    @PostMapping("/exportMonthlySalesAmount")
    private void exportMonthlySalesAmount(HttpServletResponse response, String startMonth, String endMonth, String username, String staff, String address) {
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
            result = salesService.findMonthlySalesAmountAll(startMonth, endMonth, username, staff, address);
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
        if (out != null) {
            try {
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @PostMapping(value = "/getAmount")
    public Object getAmount(HttpServletRequest request) {
        Map<String, String> params = new HashMap<String, String>();
        if (request.getParameter("productName") != null && StringUtils.isNotBlank(request.getParameter("productName"))) {
            params.put("productName", request.getParameter("productName"));
        }
        if (request.getParameter("classOne") != null && StringUtils.isNotBlank(request.getParameter("classOne"))) {
            params.put("classOne", request.getParameter("classOne"));
        }
        if (request.getParameter("classTwo") != null && StringUtils.isNotBlank(request.getParameter("classTwo"))) {
            params.put("classTwo", request.getParameter("classTwo"));
        }
        if (request.getParameter("classify") != null && StringUtils.isNotBlank(request.getParameter("classify"))) {
            params.put("classify", request.getParameter("classify"));
        }
        if (request.getParameter("standard") != null && StringUtils.isNotBlank(request.getParameter("standard"))) {
            params.put("standard", request.getParameter("standard"));
        }
        if (request.getParameter("brand") != null && StringUtils.isNotBlank(request.getParameter("brand"))) {
            params.put("brand", request.getParameter("brand"));
        }
        if (request.getParameter("mark") != null && StringUtils.isNotBlank(request.getParameter("mark"))) {
            params.put("mark", request.getParameter("mark"));
        }
        if (request.getParameter("material") != null && StringUtils.isNotBlank(request.getParameter("material"))) {
            params.put("material", request.getParameter("material"));
        }
        if (request.getParameter("grade") != null && StringUtils.isNotBlank(request.getParameter("grade"))) {
            params.put("grade", request.getParameter("grade"));
        }
        if (request.getParameter("surface") != null && StringUtils.isNotBlank(request.getParameter("surface"))) {
            params.put("surface", request.getParameter("surface"));
        }
        if (request.getParameter("nominalDiameter") != null && StringUtils.isNotBlank(request.getParameter("nominalDiameter"))) {
            params.put("nominalDiameter", request.getParameter("nominalDiameter"));
        }
        if (request.getParameter("pitch") != null && StringUtils.isNotBlank(request.getParameter("pitch"))) {
            params.put("pitch", request.getParameter("pitch"));
        }
        if (request.getParameter("extent") != null && StringUtils.isNotBlank(request.getParameter("extent"))) {
            params.put("extent", request.getParameter("extent"));
        }
        if (request.getParameter("extent") != null && StringUtils.isNotBlank(request.getParameter("extent"))) {
            params.put("extent", request.getParameter("extent"));
        }
        if (request.getParameter("outerDiameter") != null && StringUtils.isNotBlank(request.getParameter("outerDiameter"))) {
            params.put("outerDiameter", request.getParameter("outerDiameter"));
        }
        if (request.getParameter("thickness") != null && StringUtils.isNotBlank(request.getParameter("thickness"))) {
            params.put("thickness", request.getParameter("thickness"));
        }
        if (request.getParameter("store") != null && StringUtils.isNotBlank(request.getParameter("store"))) {
            params.put("store", request.getParameter("store"));
        }
        if (request.getParameter("startCreateTime") != null && StringUtils.isNotBlank(request.getParameter("startCreateTime"))) {
            params.put("startCreateTime", request.getParameter("startCreateTime"));
        }
        if (request.getParameter("endCreateTime") != null && StringUtils.isNotBlank(request.getParameter("endCreateTime"))) {
            params.put("endCreateTime", request.getParameter("endCreateTime"));
        }
        if (StringUtils.isNotBlank(request.getParameter("limit"))) {
            params.put("limit", request.getParameter("limit"));
        } else {
            params.put("limit", "10");
        }
        if (StringUtils.isNotBlank(request.getParameter("offset"))) {
            params.put("offset", request.getParameter("offset"));
        } else {
            params.put("offset", "0");
        }
        List<Map<String, Object>> list = salesService.getSaleAmount(params);
        return list;
    }

    /**
     * 商品类别销售情况
     *
     * @param request
     * @return getCategorySalesPage
     */
    @PostMapping(value = "/productCategory")
    public Object productCategory(HttpServletRequest request) {
        Map<String, String> params = new HashMap<String, String>();
        Map<String, Object> result = new HashMap<>();
        String year = null;
        if (request.getParameter("year") != null || StringUtils.isNotBlank(request.getParameter("year"))) {
            year = request.getParameter("year");
        } else {
            result.put("304", "缺少年份参数year，例如：year = 2018");
            return result;
        }
        if (request.getParameter("brand") != null && StringUtils.isNotBlank(request.getParameter("brand"))) {
            params.put("brand", request.getParameter("brand"));
        }
        if (request.getParameter("show") != null && StringUtils.isNotBlank(request.getParameter("show"))) {
            params.put("show", request.getParameter("show"));
        }
        if (request.getParameter("level") != null && StringUtils.isNotBlank(request.getParameter("level"))) {
            params.put("level", request.getParameter("level"));
        }
        if (StringUtils.isNotBlank(request.getParameter("limit"))) {
            params.put("limit", request.getParameter("limit"));
        } else {
            params.put("limit", "100");
        }
        if (StringUtils.isNotBlank(request.getParameter("offset"))) {
            params.put("offset", request.getParameter("offset"));
        } else {
            params.put("offset", "0");
        }
        List<Map<String, Object>> list = salesService.getCategorySalesPage(params, year);
        for (Map<String, Object> map : list) {
            map.put("sss", map.get("sss") + "");
        }
        result.put("rows", list);
        result.put("total", salesService.getCategorySalesCount(params, year));
        return result;
    }

    /**
     * 紧商网商家数量
     *
     * @return Object
     */
    @ApiOperation(value = "紧商网商家数量", notes = "数据来源：紧商网；数据截止日期：实时")
    @PostMapping(value = "/getShopNum")
    public Object getShopNum() {
        return salesService.getShopNum();
    }

    @RequestMapping(value = "/download/productCategory", method = {RequestMethod.GET, RequestMethod.POST})
    public void productCategory(HttpServletResponse response, HttpServletRequest request) {
        Map<String, String> params = new HashMap<String, String>();
        String year = null;
        if (request.getParameter("year") != null || StringUtils.isNotBlank(request.getParameter("year"))) {
            year = request.getParameter("year");
        } else {
            return;
        }
        if (request.getParameter("brand2") != null && StringUtils.isNotBlank(request.getParameter("brand2"))) {
            params.put("brand", request.getParameter("brand2"));
        }
        if (request.getParameter("show2") != null && StringUtils.isNotBlank(request.getParameter("show2"))) {
            params.put("show", request.getParameter("show2"));
        }
        if (request.getParameter("level2") != null && StringUtils.isNotBlank(request.getParameter("level2"))) {
            params.put("level", request.getParameter("level2"));
        }
        params.put("limit", "99999999");
        params.put("offset", "0");
        List<List<Object>> result = new ArrayList<>();
        List<String> columnNameList = new ArrayList<>();
        columnNameList.add("分类");
        columnNameList.add("合计");
        columnNameList.add("品牌");
        columnNameList.add("合计");
        columnNameList.add("一月份");
        columnNameList.add("二月份");
        columnNameList.add("三月份");
        columnNameList.add("四月份");
        columnNameList.add("五月份");
        columnNameList.add("六月份");
        columnNameList.add("七月份");
        columnNameList.add("八月份");
        columnNameList.add("九月份");
        columnNameList.add("十月份");
        columnNameList.add("十一月份");
        columnNameList.add("十二月份");
        try {
            List<Map<String, Object>> list = salesService.getCategorySalesPage(params, year);
            List<Object> objects;
            for (Map<String, Object> order : list) {
                objects = new ArrayList<>();
                objects.add(order.get("name"));
                objects.add(order.get("sss"));
                objects.add(order.get("brand"));
                objects.add(order.get("totalpr"));
                objects.add(order.get("一月"));
                objects.add(order.get("二月"));
                objects.add(order.get("三月"));
                objects.add(order.get("四月"));
                objects.add(order.get("五月"));
                objects.add(order.get("六月"));
                objects.add(order.get("七月"));
                objects.add(order.get("八月"));
                objects.add(order.get("九月"));
                objects.add(order.get("十月"));
                objects.add(order.get("十一月"));
                objects.add(order.get("十二月"));
                result.add(objects);
            }
            CommonUtils.exportByList(response, columnNameList, result, "商品分类销售额统计");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
