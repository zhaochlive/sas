package com.js.sas.controller;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson.JSONArray;
import com.js.sas.dto.OverdueDTO;
import com.js.sas.dto.SettlementSummaryDTO;
import com.js.sas.entity.Dictionary;
import com.js.sas.entity.PartnerEntity;
import com.js.sas.entity.SettlementSummaryEntity;
import com.js.sas.service.DictionaryService;
import com.js.sas.service.FinanceService;
import com.js.sas.service.PartnerService;
import com.js.sas.utils.*;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.*;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.zip.ZipOutputStream;

/**
 * @ClassName FinanceController
 * @Description 财务Controller
 * @Author zc
 * @Date 2019/6/11 12:47
 **/
@RestController
@Slf4j
@RequestMapping("/finance")
public class FinanceController {

    private final FinanceService financeService;

    private final DictionaryService dictionaryService;

    private final PartnerService partnerService;

    private final DataSource dataSource;

    public FinanceController(FinanceService financeService, DictionaryService dictionaryService, DataSource dataSource, PartnerService partnerService) {
        this.financeService = financeService;
        this.dictionaryService = dictionaryService;
        this.dataSource = dataSource;
        this.partnerService = partnerService;
    }

    /**
     * 目前调用存储过程实现，后期需要修改实现方法。
     *
     * @param settlementSummasryDTO 结算客户汇总DTO
     * @param bindingResult         校验结果
     * @return Object
     */
    @ApiOperation(value = "结算客户汇总（线上、线下）", notes = "数据来源：用友；数据截止日期：昨天")
    @PostMapping(value = "/settlementSummary")
    public Object settlementSummary(@Validated SettlementSummaryDTO settlementSummasryDTO, BindingResult bindingResult) {
        // 参数格式校验
        Result checkResult = CommonUtils.checkParameter(bindingResult);
        if (checkResult != null) {
            return checkResult;
        }

        return financeService.getSettlementSummary(settlementSummasryDTO.getName(),
                settlementSummasryDTO.getChannel(),
                settlementSummasryDTO.getStartDate(),
                settlementSummasryDTO.getEndDate(),
                settlementSummasryDTO.getOffset(),
                settlementSummasryDTO.getLimit(),
                settlementSummasryDTO.getSort(),
                settlementSummasryDTO.getSortOrder());
    }

    @ApiIgnore
    @PostMapping("/settlementSummary/download/excel")
    public void downloadSettlementSummary(String name, String channel, String startDate, String endDate, String limit, HttpServletResponse httpServletResponse) {
        List<SettlementSummaryEntity> settlementSummaryList = (List<SettlementSummaryEntity>) financeService.getSettlementSummary(
                name,
                channel,
                startDate,
                endDate,
                0,
                Integer.parseInt(limit),
                "name",
                "asc").get("rows");
        try {
            CommonUtils.export(httpServletResponse, settlementSummaryList, "结算客户汇总（线上、线下）", new SettlementSummaryEntity());
        } catch (IOException e) {
            log.error("下载结算客户汇总（线上、线下）异常：{}", e);
            e.printStackTrace();
        }
    }

    @ApiOperation(value = "现金客户逾期统计", notes = "数据来源：用友；数据截止日期：昨天")
    @PostMapping("/overdue")
    public Object overdue(@Validated OverdueDTO partner, BindingResult bindingResult) {
        // 参数格式校验
        Result checkResult = CommonUtils.checkParameter(bindingResult);
        if (checkResult != null) {
            return checkResult;
        }

        HashMap<String, Object> result = new HashMap<>();

        Page page = financeService.findOverdue(partner);

        result.put("rows", page.getContent());
        result.put("total", page.getTotalElements());

        return result;
    }

    /**
     * 逾期现金客户导出
     *
     * @param httpServletResponse httpServletResponse
     */
    @ApiIgnore
    @PostMapping("/overdueCash/download/excel")
    public void downloadOverdueCash(int limit, HttpServletResponse httpServletResponse) {
        OverdueDTO overdueDTO = new OverdueDTO();
        overdueDTO.setLimit(limit);
        overdueDTO.setSort("name");
        overdueDTO.setSortOrder("asc");
        overdueDTO.setStatus("0");
        overdueDTO.setSettlementType("1");
        overdueDTO.setParentCode("0");

        List overdueList = financeService.findOverdue(overdueDTO).getContent();
        try {
            CommonUtils.export(httpServletResponse, overdueList, "现金客户逾期统计", new OverdueDTO());
        } catch (IOException e) {
            log.error("下载现金客户逾期统计异常：{}", e);
            e.printStackTrace();
        }
    }


    /**
     * 账期逾期客户导出
     * 导出表格内容截止到当前月份，动态表格直接调用存储过程实现。
     * 业务逻辑很复杂
     *
     * @param httpServletResponse httpServletResponse
     */
    @ApiIgnore
    @PostMapping("/overdueCredit/download/excel")
    public void downloadOverdueCredit(HttpServletResponse httpServletResponse) {
        Connection con = null;
        ResultSet rs = null;
        try {
            con = dataSource.getConnection();
            CallableStatement c = con.prepareCall("{call PROC_settlement_sales_months}");
            rs = c.executeQuery();

            ResultSetMetaData rsmd = rs.getMetaData();
            // 数据列数
            int count = rsmd.getColumnCount();

            // 列名数据
            ArrayList<String> columnsList = new ArrayList<>();
            // 移除前3列（关联id列、总发货、总收款）
            for (int i = 4; i < count; i++) {
                columnsList.add(rsmd.getColumnLabel(i));
                if (i > 11) {
                    i++;
                }
            }

            // 数据
            ArrayList<List<Object>> rowsList = new ArrayList<>();
            // 需要合并计算的用户序号List，根据code编码判断
            List<Integer> totalIndexList = new ArrayList<>();
            // 需要合并的序号List集合
            List<List<Integer>> totalList = new ArrayList<>();
            // 关联code
            String parentCode = "";
            // 有关联账号标记
            boolean hasParentCode;
            // 关联账户总计应收
            BigDecimal totalReceivables = new BigDecimal(0);

            while (rs.next()) {
                ArrayList<Object> dataList = new ArrayList<>();
                // 账期月, 目前rs第7列
                int month = rs.getInt(7);
                // 账期日，目前rs第8列
                int day = rs.getInt(8);
                // 应减去的结算周期数
                int overdueMonths = CommonUtils.overdueMonth(month, day);
                // 当前逾期金额
                BigDecimal overdue = rs.getBigDecimal(10);

                // 设置数据行，移除前3列（关联id列、总发货、总收款）
                for (int i = 4; i <= count; i++) {
                    if (i > 11) {  // 计算每个周期的发货和应收
                        if (i > count - overdueMonths * 2) {
                            // 有关联的账期客户逾期总金额不计算未到账期的退货金额，无关联关系的账期客户预期总金额计算所有退货金额
                            // 分月统计全部计算所有退货金额
                            // 发货金额，未到账期均不计算
                            overdue = overdue.subtract(rs.getBigDecimal(i));
                            // 只计算逾期账期数据，如果是未逾期账期数据，需要将逾期款减去相应的发货金额
                            BigDecimal tempOverdue = new BigDecimal(0);

                            if ("0".equals(rs.getString("parent_code"))) {
                                tempOverdue = new BigDecimal(dataList.get(6).toString()).subtract(rs.getBigDecimal(i++));
                                dataList.set(6, tempOverdue);
                            } else {
                                // 有关联账户不计算未到期的退货
                                tempOverdue = new BigDecimal(dataList.get(6).toString()).subtract(rs.getBigDecimal(i++));
                                tempOverdue = tempOverdue.subtract(rs.getBigDecimal(i));
                                dataList.set(6, tempOverdue);
                            }
                            dataList.add(0);
                        } else {
                            dataList.add(rs.getBigDecimal(i++));
                        }
                    } else if (i == 11) {
                        dataList.add(rs.getBigDecimal(i));
                    } else {
                        dataList.add(rs.getString(i));
                    }
                }

                // 根据逾期款，设置excel数据。从后向前，到期初为止。
                for (int index = dataList.size() - 1; index > 6; index--) {
                    if (overdue.compareTo(new BigDecimal(0)) < 1) {  // 逾期金额小于等于0，所有账期逾期金额都是0
                        dataList.set(index, 0);
                    } else {  // 逾期金额大于0，从最后一个开始分摊逾期金额
                        if (overdue.compareTo(new BigDecimal(dataList.get(index).toString())) > -1) {
                            overdue = overdue.subtract(new BigDecimal(dataList.get(index).toString()));
                            dataList.set(index, dataList.get(index));
                        } else {
                            dataList.set(index, overdue);
                            overdue = new BigDecimal(0);
                        }
                    }
                }

                // 补零数量
                // int overdueZero = CommonUtils.overdueZero(month, day);
                // 导出的Excel显示逾期金额，不是发货金额。需要按照账期周期，向后推迟逾期金额，在期初之后补0实现。
                for (int overdueIndex = 0; overdueIndex < month; overdueIndex++) {
                    // 插入0
                    dataList.add(8, 0);
                    // 删除最后一位
                    dataList.remove(dataList.size() - 1);
                }

                // 设置数据列
                rowsList.add(dataList);

                // 设置逾期金额总计
                // 如果存在关联code
                if (!"0".equals(rs.getString("parent_code"))) {
                    hasParentCode = true;
                    // 如果两个不相等，说明是新的关联code，重新赋值
                    if (!parentCode.equals(rs.getString("parent_code"))) {
                        parentCode = rs.getString("parent_code");
                        // 计算之前应收之和
//                        for (int index : totalIndexList) {
//                            rowsList.get(index - 1).set(5, totalReceivables);
//                        }
                        // 此处修改的目的是防止单元格合并之后，数值求和计算错误。
                        if (!totalIndexList.isEmpty()) {
                            rowsList.get(totalIndexList.get(0) - 1).set(5, totalReceivables);
                        }
                        // 置零
                        totalReceivables = new BigDecimal(0);
                        // 添加至集合
                        if (!totalIndexList.isEmpty()) {
                            totalList.add(totalIndexList);
                        }
                        // 序列list置空
                        totalIndexList = new ArrayList<>();
                    }
                } else {
                    // 最后一个totalIndexList
                    if (!totalIndexList.isEmpty()) {
                        // 计算之前应收之和
                        for (int index : totalIndexList) {
                            rowsList.get(index - 1).set(5, totalReceivables);
                        }
                        // 添加至集合
                        totalList.add(totalIndexList);
                        totalIndexList = new ArrayList<>();
                    } else {
                        // 如果没有关联客户，将逾期金额赋值到总逾期金额
                        rowsList.get(rs.getRow() - 1).set(5, rowsList.get(rs.getRow() - 1).get(6));
                    }
                    // 置零
                    hasParentCode = false;
                    parentCode = "";
                    totalReceivables = new BigDecimal(0);
                }

                // 设置应收金额合计
                if (hasParentCode) {
                    totalIndexList.add(rs.getRow());
                    totalReceivables = totalReceivables.add(new BigDecimal(dataList.get(6).toString()));
                }

            }

            // 导出excel
            exportOverdue(httpServletResponse, columnsList, rowsList, "账期客户逾期统计", totalList);

        } catch (SQLException | IOException e) {
            log.error("下载账期客户逾期统计异常：{}", e);
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 导出账期逾期客户Excel
     * 牵涉到单元格合并和特殊的表结构，单独一个方法实现。
     *
     * @param response       HttpServletResponse
     * @param columnNameList 导出列名List
     * @param dataList       导出数据List
     * @param fileName       导出文件名，目前sheet页是相同名称
     * @param totalList      需要合并的数据序号List
     * @throws IOException @Description
     */
    private void exportOverdue(HttpServletResponse response, List<String> columnNameList, List<List<Object>> dataList, String fileName, List<List<Integer>> totalList) throws IOException {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");

        Sheet sheet1 = new Sheet(1, 0);
        sheet1.setSheetName(fileName);
        sheet1.setAutoWidth(Boolean.TRUE);

        fileName = fileName + df.format(new Date());
        ServletOutputStream out = response.getOutputStream();
        response.setContentType("multipart/form-data");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment;filename*= UTF-8''" + URLEncoder.encode(fileName, "UTF-8") + ".xlsx");
        // 设置列名
        if (columnNameList != null) {
            List<List<String>> list = new ArrayList<>();
            columnNameList.forEach(c -> list.add(Collections.singletonList(c)));
            sheet1.setHead(list);
        }
        // 写入数据
        ExcelWriter writer = new ExcelWriter(out, ExcelTypeEnum.XLSX, true);
        writer.write1(dataList, sheet1);
        // 合并单元格
        for (List<Integer> totalIndexList : totalList) {
            if (!totalIndexList.isEmpty()) {
                if (!totalIndexList.get(0).equals(totalIndexList.get(totalIndexList.size() - 1))) {
                    writer.merge(totalIndexList.get(0), totalIndexList.get(totalIndexList.size() - 1), 5, 5);
                }
            }
        }

        writer.finish();
        out.flush();
        out.close();

    }

    /**
     * 更新逾期数据
     *
     * @return result
     */
    @PostMapping("/refreshOverdueData")
    public Result refreshOverdueData() {
        String result = new RemoteShellExecutor("192.168.8.65", 22, "root", "xQL=Q)*rV=hV_i@VFhP2", "sudo /usr/local/kettle/data-integration/cronjobs/001.sh").exec();
        return ResultUtils.getResult(ResultCode.成功, result);
    }

    /**
     * 查询逾期数据更新时间
     *
     * @return 逾期数据更新时间
     */
    @PostMapping("/findeOverdueRefreshTime")
    public Result findeOverdueRefreshTime() {
        List<Dictionary> dictionaryList = dictionaryService.findByCode("001");
        if (!dictionaryList.isEmpty()) {
            return ResultUtils.getResult(ResultCode.成功, dictionaryList.get(0).getValue());
        } else {
            return ResultUtils.getResult(ResultCode.系统异常);
        }
    }

    @Value("${yongyou.url}")
    private String url;

    /**
     * 用友对账单
     *
     * @param period 账期
     * @param name 对账单位名称
     * @return Result
     */
    @PostMapping("/findYonyouStatement")
    public Result findYonyouStatement(String period, String name) {

        if (StringUtils.isBlank(period) || StringUtils.isBlank(name)) {
            return ResultUtils.getResult(ResultCode.参数错误);
        }

        String startDate;
        String endDate;

        String[] dateArray = period.split("-");

        if (dateArray.length == 2) {
            if (CommonUtils.isNumber(dateArray[0]) && CommonUtils.isNumber(dateArray[1])) {
                if (Integer.parseInt(dateArray[1]) > 1 && Integer.parseInt(dateArray[1]) <= 12) {
                    startDate = dateArray[0] + "-" + (Integer.parseInt(dateArray[1]) - 1) + "-28";
                    endDate = period + "-27";
                } else if (Integer.parseInt(dateArray[1]) == 1) {
                    startDate = (Integer.parseInt(dateArray[0]) - 1) + "-12-28";
                    endDate = period + "-27";
                } else {
                    return ResultUtils.getResult(ResultCode.参数错误);
                }
            } else {
                return ResultUtils.getResult(ResultCode.参数错误);
            }
        } else {
            return ResultUtils.getResult(ResultCode.参数错误);
        }

        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("startDate", startDate);
        multiValueMap.add("endDate", endDate);
        multiValueMap.add("settleCustomer", name);

        ResponseEntity responseEntity = CommonUtils.sendPostRequest(url, multiValueMap);

        return ResultUtils.getResult(ResultCode.成功, responseEntity.getBody());

    }

    @ApiIgnore
    @PostMapping("/exportYonyouStatement")
    public void exportYonyouStatement(HttpServletResponse response, String period, String name) {
        EnumMap<ExcelPropertyEnum, Object> enumMap = getYonyouStatementExcel(period, name);

        ServletOutputStream out = null;
        try {
            out = response.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        response.setContentType("multipart/form-data");
        response.setCharacterEncoding("utf-8");
        try {
            response.setHeader("Content-Disposition", "attachment;filename*= UTF-8''" + URLEncoder.encode(enumMap.get(ExcelPropertyEnum.FILENAME).toString(), "UTF-8") + ".xlsx");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        StyleExcelHandler handler = (StyleExcelHandler) enumMap.get(ExcelPropertyEnum.HANDLER);
        // 写入数据
        ExcelWriter writer = new ExcelWriter(null, out, ExcelTypeEnum.XLSX, true, handler);
        writer.write1((List) enumMap.get(ExcelPropertyEnum.ROWLIST), (Sheet) enumMap.get(ExcelPropertyEnum.SHEET));

        writer.finish();
        try {
            if (out != null) {
                out.flush();
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 导出全部用友对账单，并打包ZIP文件
     *
     * @param response
     * @param period
     */
    @ApiIgnore
    @PostMapping("/exportAllYonyouStatement")
    public void exportAllYonyouStatement(HttpServletResponse response, String period) {
        // 判断参数
        if (StringUtils.isBlank(period)) {
            return;
        }
        log.info("导出全部用友对账单");
        // 输出流
        OutputStream out = null;
        // 获取往来单位List
        List<PartnerEntity> partnerList = partnerService.findAllNameListOrderByName();
        // 导出时间格式化
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
        // 导出压缩文件名称
        String zipFileName = df.format(new Date());
        // 导出文件夹
        File sourceFile = new File("/usr/local/project/sas/" + zipFileName);
        if (!sourceFile.exists()) {
            sourceFile.mkdirs();
        }

        // 遍历往来单位
        for (PartnerEntity partner : partnerList) {
            EnumMap<ExcelPropertyEnum, Object> enumMap = getYonyouStatementExcel(period, partner.getName());
            if (enumMap == null) {
                //log.info("获取接口数据错误，单位名称：" + partner.getName() + "，账期：" + period);
                continue;
            }
            try {
                out = new FileOutputStream(sourceFile.getPath() + "/"+ enumMap.get(ExcelPropertyEnum.FILENAME).toString() + ".xlsx");
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 写入数据
            ExcelWriter writer = new ExcelWriter(null, out, ExcelTypeEnum.XLSX, true, (StyleExcelHandler) enumMap.get(ExcelPropertyEnum.HANDLER));
            writer.write1((List) enumMap.get(ExcelPropertyEnum.ROWLIST), (Sheet) enumMap.get(ExcelPropertyEnum.SHEET));
            writer.finish();
            try {
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
           // log.info("单位名称：" + partner.getName() + "，账期：" + period);
        }

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename="+zipFileName+".zip");

        // 压缩
        ServletOutputStream servletOutputStream = null;
        try {
            servletOutputStream = response.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(servletOutputStream);
            CommonUtils.compress(sourceFile, zos, zipFileName, true);
            log.info("导出全部用友对账单-压缩完成");
        } catch (Exception e) {
            throw new RuntimeException("用友对账单批量导出压缩异常", e);
        } finally {
            if (zos != null) {
                try {
                    zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    /**
     * 导出用友对账单Excel公用方法
     *
     * @param period 账期，格式：yyyy-MM
     * @param name   对账单位名称
     * @return 对账单信息EnumMap
     */
    @ApiIgnore
    private EnumMap<ExcelPropertyEnum, Object> getYonyouStatementExcel(String period, String name) {
        // 判断参数
        if (StringUtils.isBlank(period) || StringUtils.isBlank(name)) {
            return null;
        }
        // 开始时间
        String startDate;
        // 结束时间
        String endDate;
        // 分割时间
        String[] dateArray = period.split("-");
        // 拼接时间，上个月的28日，到这个月的27日
        if (dateArray.length == 2) {
            if (CommonUtils.isNumber(dateArray[0]) && CommonUtils.isNumber(dateArray[1])) {
                if (Integer.parseInt(dateArray[1]) > 1 && Integer.parseInt(dateArray[1]) <= 12) {
                    startDate = dateArray[0] + "-" + (Integer.parseInt(dateArray[1]) - 1) + "-28";
                    endDate = period + "-27";
                } else if (Integer.parseInt(dateArray[1]) == 1) {
                    startDate = (Integer.parseInt(dateArray[0]) - 1) + "-12-28";
                    endDate = period + "-27";
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
        // 调用接口获取对账单数据
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("startDate", startDate);
        multiValueMap.add("endDate", endDate);
        multiValueMap.add("settleCustomer", name);
        ResponseEntity responseEntity = CommonUtils.sendPostRequest(url, multiValueMap);

        if (responseEntity.getBody() == null) {
            return null;
        }

        // 格式化JSONArray
        JSONArray dataJSONArray = JSONArray.parseArray("[" + responseEntity.getBody() + "]");
        // 每行数据List
        List<Object> dataList = new ArrayList<>();
        // 总行数据List
        List<List<Object>> rowList = new ArrayList<>();
        // 对账单明细行数据List
        List<List<Object>> totalRowList = new ArrayList<>();
        // 需要加粗显示行号List
        List<Integer> boldList = new ArrayList<>();
        // 需要加边框行号List
        List<Integer> borderList = new ArrayList<>();
        // 处理数据
        if (dataJSONArray.size() > 0) {
            BigDecimal deliverTotal = new BigDecimal(0);
            BigDecimal collectTotal = new BigDecimal(0);
            BigDecimal receivableTotal = new BigDecimal(0);
            BigDecimal invoiceTotal = new BigDecimal(0);
            BigDecimal invoiceBalanceTotal = new BigDecimal(0);
            // 第一行，结算客户信息
            dataList.add(dataJSONArray.getJSONObject(0).getString("settleCustomer"));
            dataList.add("");
            dataList.add("");
            dataList.add(dataJSONArray.getJSONObject(0).getString("settleCustomerTel"));
            dataList.add("");
            dataList.add(dataJSONArray.getJSONObject(0).getString("settleCustomerFax"));
            dataList.add("");
            rowList.add(dataList);
            boldList.add(rowList.size());
            // 第二行，公司信息
            dataList = new ArrayList<>();
            dataList.add(dataJSONArray.getJSONObject(0).getString("company"));
            dataList.add("");
            dataList.add("");
            dataList.add(dataJSONArray.getJSONObject(0).getString("companyTel"));
            dataList.add("");
            dataList.add(dataJSONArray.getJSONObject(0).getString("companyFax"));
            dataList.add("");
            rowList.add(dataList);
            boldList.add(rowList.size());
            // 第三行，空白
            dataList = new ArrayList<>();
            rowList.add(dataList);
            // 线上、线下
            for (int index = 0; index < dataJSONArray.getJSONObject(0).getJSONArray("reportContent").size(); index++) {
                // 线上、线下标题
                dataList = new ArrayList<>();
                dataList.add("");
                dataList.add("");
                dataList.add("");
                dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getString("settleCustomer") + " - " + dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getString("explan"));
                rowList.add(dataList);
                boldList.add(rowList.size());
                // 时间行
                dataList = new ArrayList<>();
                dataList.add("日期：" + dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getString("queryStartDate") + " _ " + dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getString("queryEndDate"));
                rowList.add(dataList);
                boldList.add(rowList.size());
                // 明细标题行
                dataList = new ArrayList<>();
                dataList.add("日期");
                dataList.add("合同编号");
                dataList.add("类别");
                dataList.add("发货金额");
                dataList.add("收款金额");
                dataList.add("应收款");
                dataList.add("开票金额");
                dataList.add("发票结余");
                rowList.add(dataList);
                boldList.add(rowList.size());
                borderList.add(rowList.size());
                // 期初数据行
                dataList = new ArrayList<>();
                dataList.add("线上期初数据");
                dataList.add("上期结转：");
                dataList.add("");
                dataList.add("");
                dataList.add("");
                dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getBigDecimal("initReceivableBanlance"));
                dataList.add("");
                dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getBigDecimal("initInvoiceBanlance"));
                rowList.add(dataList);
                borderList.add(rowList.size());
                // 明细
                for (int innerIndex = 0; innerIndex < dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getJSONArray("arrDetail").size(); innerIndex++) {
                    dataList = new ArrayList<>();
                    dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getJSONArray("arrDetail").getJSONObject(innerIndex).getString("bookedDate"));
                    dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getJSONArray("arrDetail").getJSONObject(innerIndex).getString("summary"));
                    dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getJSONArray("arrDetail").getJSONObject(innerIndex).getString("category"));
                    dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getJSONArray("arrDetail").getJSONObject(innerIndex).getBigDecimal("deliverAmount"));
                    dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getJSONArray("arrDetail").getJSONObject(innerIndex).getBigDecimal("collectAmount"));
                    dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getJSONArray("arrDetail").getJSONObject(innerIndex).getBigDecimal("receivableAmount"));
                    dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getJSONArray("arrDetail").getJSONObject(innerIndex).getBigDecimal("invoiceAmount"));
                    dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getJSONArray("arrDetail").getJSONObject(innerIndex).getBigDecimal("invoiceBalanceAmount"));
                    rowList.add(dataList);
                    borderList.add(rowList.size());
                }
                // 汇总信息
                dataList = new ArrayList<>();
                dataList.add("本月" + dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getString("explan") + "结算");
                dataList.add("");
                dataList.add("");
                dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getBigDecimal("deliverTotalAmount"));
                dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getBigDecimal("collectTotalAmount"));
                dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getBigDecimal("receivableTotalAmount"));
                dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getBigDecimal("invoiceTotalAmount"));
                dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getBigDecimal("invoiceBalanceTotalAmount"));
                rowList.add(dataList);
                borderList.add(rowList.size());
                // 备注行
                dataList = new ArrayList<>();
                dataList.add("备注：本月" + dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getString("explan") + "销售、收款、开票如上表所示");
                rowList.add(dataList);
                boldList.add(rowList.size());
                // 空白行
                dataList = new ArrayList<>();
                rowList.add(dataList);
                // 本月信息行
                dataList = new ArrayList<>();
                dataList.add("本月小计 - " + dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getString("explan") + "：");
                dataList.add("");
                dataList.add("");
                dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getBigDecimal("deliverTotalAmount"));
                dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getBigDecimal("collectTotalAmount"));
                dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getBigDecimal("receivableTotalAmount"));
                dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getBigDecimal("invoiceTotalAmount"));
                dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getBigDecimal("invoiceBalanceTotalAmount"));
                totalRowList.add(dataList);

                deliverTotal = deliverTotal.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getBigDecimal("deliverTotalAmount"));
                collectTotal = collectTotal.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getBigDecimal("collectTotalAmount"));
                receivableTotal = receivableTotal.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getBigDecimal("receivableTotalAmount"));
                invoiceTotal = invoiceTotal.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getBigDecimal("invoiceTotalAmount"));
                invoiceBalanceTotal = invoiceBalanceTotal.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getBigDecimal("invoiceBalanceTotalAmount"));
            }
            // 月汇总
            dataList = new ArrayList<>();
            dataList.add("综上所述,本月汇总如下:");
            rowList.add(dataList);
            boldList.add(rowList.size());
            // 月汇总标题行
            dataList = new ArrayList<>();
            dataList.add("日期");
            dataList.add("合同编号");
            dataList.add("");
            dataList.add("发货金额");
            dataList.add("收款金额");
            dataList.add("应收款");
            dataList.add("开票金额");
            dataList.add("发票结余");
            rowList.add(dataList);
            boldList.add(rowList.size());
            borderList.add(rowList.size());
            // 线上、线下汇总
            rowList.addAll(totalRowList);
            boldList.add(rowList.size());
            borderList.add(rowList.size());
            boldList.add(rowList.size() - 1);
            borderList.add(rowList.size() - 1);
            // 月累计行
            dataList = new ArrayList<>();
            dataList.add("本月累计：");
            dataList.add("");
            dataList.add("");
            dataList.add(deliverTotal);
            dataList.add(collectTotal);
            dataList.add(receivableTotal);
            dataList.add(invoiceTotal);
            dataList.add(invoiceBalanceTotal);
            rowList.add(dataList);
            boldList.add(rowList.size());
            borderList.add(rowList.size());
            // 其他信息行
            dataList = new ArrayList<>();
            dataList.add("1、此对账单的截止日期为上述发出日期。");
            rowList.add(dataList);
            boldList.add(rowList.size());

            dataList = new ArrayList<>();
            dataList.add("2、如有错漏，请于发出此对账单后七日內提出，否则视为默认！");
            rowList.add(dataList);
            boldList.add(rowList.size());

            dataList = new ArrayList<>();
            dataList.add("制单：");
            dataList.add("");
            dataList.add("业务员确认：");
            dataList.add("");
            dataList.add("");
            dataList.add("发出日期：");
            dataList.add("");
            dataList.add("");
            rowList.add(dataList);
            boldList.add(rowList.size());

            dataList = new ArrayList<>();
            rowList.add(dataList);

            dataList = new ArrayList<>();
            dataList.add("客户签字：");
            rowList.add(dataList);
            boldList.add(rowList.size());

            dataList = new ArrayList<>();
            dataList.add("客户盖章：");
            rowList.add(dataList);
            boldList.add(rowList.size());
        } else {
            return null;
        }
        // 导出时间格式化
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
        // 名称
        String fileName = name + "-用友对账单";
        // sheet页
        Sheet sheet1 = new Sheet(1, 0);
        sheet1.setSheetName(fileName);
        sheet1.setAutoWidth(Boolean.TRUE);
        // 样式
        StyleExcelHandler handler = new StyleExcelHandler(boldList, borderList);
        // 返回值
        EnumMap<ExcelPropertyEnum, Object> reusltEnumMap = new EnumMap<>(ExcelPropertyEnum.class);
        reusltEnumMap.put(ExcelPropertyEnum.HANDLER, handler);
        reusltEnumMap.put(ExcelPropertyEnum.ROWLIST, rowList);
        reusltEnumMap.put(ExcelPropertyEnum.SHEET, sheet1);
        reusltEnumMap.put(ExcelPropertyEnum.FILENAME, fileName + df.format(new Date()));
        return reusltEnumMap;
    }

    enum ExcelPropertyEnum {
        HANDLER, ROWLIST, SHEET, FILENAME
    }
}
