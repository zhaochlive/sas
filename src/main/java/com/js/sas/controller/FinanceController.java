package com.js.sas.controller;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.event.WriteHandler;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.metadata.Table;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson.JSONArray;
import com.js.sas.dto.OverdueDTO;
import com.js.sas.dto.SettlementSummaryDTO;
import com.js.sas.entity.DeptStaff;
import com.js.sas.entity.Dictionary;
import com.js.sas.entity.PartnerEntity;
import com.js.sas.entity.SettlementSummaryEntity;
import com.js.sas.repository.DeptStaffRepository;
import com.js.sas.service.DictionaryService;
import com.js.sas.service.FinanceService;
import com.js.sas.service.PartnerService;
import com.js.sas.utils.*;
import com.js.sas.utils.upload.ExcelListener;
import com.js.sas.utils.upload.UploadData;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.*;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.sql.*;
import java.text.ParseException;
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

    @Autowired
    DeptStaffRepository deptStaffRepository;

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

    /**
     * 下载结算客户对账单
     *
     * @param name                结算客户名称
     * @param channel             渠道
     * @param startDate           开始时间
     * @param endDate             结束时间
     * @param limit               数量
     * @param httpServletResponse httpServletResponse
     */
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
     * @param limit               数量
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
            CommonUtils.export(httpServletResponse, overdueList, "现金客户逾期统计", new PartnerEntity());
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
            BigDecimal totalReceivables = BigDecimal.ZERO;

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
                            BigDecimal tempOverdue = BigDecimal.ZERO;

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
                    if (overdue.compareTo(BigDecimal.ZERO) < 1) {  // 逾期金额小于等于0，所有账期逾期金额都是0
                        dataList.set(index, 0);
                    } else {  // 逾期金额大于0，从最后一个开始分摊逾期金额
                        if (overdue.compareTo(new BigDecimal(dataList.get(index).toString())) > -1) {
                            overdue = overdue.subtract(new BigDecimal(dataList.get(index).toString()));
                            dataList.set(index, dataList.get(index));
                        } else {
                            dataList.set(index, overdue);
                            overdue = BigDecimal.ZERO;
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
                        // 此处修改的目的是防止单元格合并之后，数值求和计算错误。
                        if (!totalIndexList.isEmpty()) {
                            rowsList.get(totalIndexList.get(0) - 1).set(5, totalReceivables);
                        }
                        // 置零
                        totalReceivables = BigDecimal.ZERO;
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
                    totalReceivables = BigDecimal.ZERO;
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
        String result = new RemoteShellExecutor("192.168.8.164", 22, "root", "root", "sudo /usr/local/pentaho/cronjobs/001.sh").exec();
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
     * @param name   对账单位名称
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

    /**
     * 导出用友对账单
     *
     * @param response HttpServletResponse
     * @param period   账期
     * @param name     对账单位名称
     */
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

        WriteHandler handler = (WriteHandler) enumMap.get(ExcelPropertyEnum.HANDLER);
        // 写入数据
        ExcelWriter writer = new ExcelWriter(null, out, ExcelTypeEnum.XLSX, true, handler);
        writer.write1((List) enumMap.get(ExcelPropertyEnum.ROWLIST), (Sheet) enumMap.get(ExcelPropertyEnum.SHEET));
        // 合并单元格
        writer.merge(1, 1, 0, 2);
        writer.merge(1, 1, 3, 4);
        writer.merge(1, 1, 5, 6);
        writer.merge(2, 2, 0, 2);
        writer.merge(2, 2, 3, 4);
        writer.merge(2, 2, 5, 6);
        writer.merge(4, 4, 0, 7);
        List<Integer> mergeRowNumList = (List<Integer>) enumMap.get(ExcelPropertyEnum.MERGE);
        for (int index : mergeRowNumList) {
            writer.merge(index, index, 0, 2);
        }
        if (!mergeRowNumList.isEmpty()) {
            int index = mergeRowNumList.get(mergeRowNumList.size() - 1);
            writer.merge(index + 4, index + 4, 0, 2);
            writer.merge(index + 5, index + 5, 0, 2);
            writer.merge(index + 6, index + 6, 0, 2);
            writer.merge(index + 7, index + 7, 0, 2);
            writer.merge(index + 3, index + 3, 0, 7);

            int first = mergeRowNumList.get(0);
            writer.merge(first + 3, first + 3, 0, 7);
        }
        // 关闭
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
                out = new FileOutputStream(sourceFile.getPath() + "/" + enumMap.get(ExcelPropertyEnum.FILENAME).toString() + ".xlsx");
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 写入数据
            ExcelWriter writer = new ExcelWriter(null, out, ExcelTypeEnum.XLSX, true, (WriteHandler) enumMap.get(ExcelPropertyEnum.HANDLER));
            writer.write1((List) enumMap.get(ExcelPropertyEnum.ROWLIST), (Sheet) enumMap.get(ExcelPropertyEnum.SHEET));
            // 合并单元格
            writer.merge(1, 1, 0, 2);
            writer.merge(1, 1, 3, 4);
            writer.merge(1, 1, 5, 6);
            writer.merge(2, 2, 0, 2);
            writer.merge(2, 2, 3, 4);
            writer.merge(2, 2, 5, 6);
            writer.merge(4, 4, 0, 7);
            List<Integer> mergeRowNumList = (List<Integer>) enumMap.get(ExcelPropertyEnum.MERGE);
            for (int index : mergeRowNumList) {
                writer.merge(index, index, 0, 2);
            }
            if (!mergeRowNumList.isEmpty()) {
                int index = mergeRowNumList.get(mergeRowNumList.size() - 1);
                writer.merge(index + 4, index + 4, 0, 2);
                writer.merge(index + 5, index + 5, 0, 2);
                writer.merge(index + 6, index + 6, 0, 2);
                writer.merge(index + 7, index + 7, 0, 2);
                writer.merge(index + 3, index + 3, 0, 7);

                int first = mergeRowNumList.get(0);
                writer.merge(first + 3, first + 3, 0, 7);
            }
            // 关闭
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
        response.setHeader("Content-Disposition", "attachment; filename=" + zipFileName + ".zip");

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

        // 比较结束日期，如果大于今天，显示今天。
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = sdf.parse(endDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (date.after(new Date())) {
            endDate = sdf.format(new Date());
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
        // 背景色行
        List<Integer> backgroundColorList = new ArrayList<>();
        // 居中行
        List<Integer> centerList = new ArrayList<>();
        // 明细居中行
        List<Integer> centerDetailList = new ArrayList<>();
        // 需要合并的行
        List<Integer> mergeRowNumList = new ArrayList<>();
        // 处理数据
        if (dataJSONArray.size() > 0) {
            BigDecimal deliverTotal = BigDecimal.ZERO;
            BigDecimal collectTotal = BigDecimal.ZERO;
            BigDecimal receivableTotal = BigDecimal.ZERO;
            BigDecimal invoiceTotal = BigDecimal.ZERO;
            BigDecimal invoiceBalanceTotal = BigDecimal.ZERO;
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
                dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getString("settleCustomer") + " - " + dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getString("explan"));
                rowList.add(dataList);
                boldList.add(rowList.size());
                centerList.add(rowList.size());
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
                backgroundColorList.add(rowList.size());
                centerList.add(rowList.size());
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
                centerDetailList.add(rowList.size());
                // 明细
                for (int innerIndex = 0; innerIndex < dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getJSONArray("arrDetail").size(); innerIndex++) {
                    dataList = new ArrayList<>();
                    dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getJSONArray("arrDetail").getJSONObject(innerIndex).getString("bookedDate"));
                    dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getJSONArray("arrDetail").getJSONObject(innerIndex).getString("summary"));
                    dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getJSONArray("arrDetail").getJSONObject(innerIndex).getString("category"));
                    dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getJSONArray("arrDetail").getJSONObject(innerIndex).getBigDecimal("deliverAmount").compareTo(BigDecimal.ZERO) == 0 ? "" : dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getJSONArray("arrDetail").getJSONObject(innerIndex).getBigDecimal("deliverAmount"));
                    dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getJSONArray("arrDetail").getJSONObject(innerIndex).getBigDecimal("collectAmount").compareTo(BigDecimal.ZERO) == 0 ? "" : dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getJSONArray("arrDetail").getJSONObject(innerIndex).getBigDecimal("collectAmount"));
                    dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getJSONArray("arrDetail").getJSONObject(innerIndex).getBigDecimal("receivableAmount").compareTo(BigDecimal.ZERO) == 0 ? "" : dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getJSONArray("arrDetail").getJSONObject(innerIndex).getBigDecimal("receivableAmount"));
                    dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getJSONArray("arrDetail").getJSONObject(innerIndex).getBigDecimal("invoiceAmount").compareTo(BigDecimal.ZERO) == 0 ? "" : dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getJSONArray("arrDetail").getJSONObject(innerIndex).getBigDecimal("invoiceAmount"));
                    dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getJSONArray("arrDetail").getJSONObject(innerIndex).getBigDecimal("invoiceBalanceAmount"));
                    rowList.add(dataList);
                    borderList.add(rowList.size());
                    centerDetailList.add(rowList.size());
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
                boldList.add(rowList.size());
                mergeRowNumList.add(rowList.size());
                centerList.add(rowList.size());
                backgroundColorList.add(rowList.size());
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
            dataList.add("综上所述，本月汇总如下");
            rowList.add(dataList);
            boldList.add(rowList.size());
            centerList.add(rowList.size());
            // 月汇总标题行
            dataList = new ArrayList<>();
            dataList.add("合计");
            dataList.add("");
            dataList.add("");
            dataList.add("发货金额");
            dataList.add("收款金额");
            dataList.add("应收款");
            dataList.add("开票金额");
            dataList.add("发票结余");
            rowList.add(dataList);
            boldList.add(rowList.size());
            borderList.add(rowList.size());
            centerList.add(rowList.size());
            backgroundColorList.add(rowList.size());
            // 线上、线下汇总
            rowList.addAll(totalRowList);
            boldList.add(rowList.size());
            borderList.add(rowList.size());
            boldList.add(rowList.size() - 1);
            borderList.add(rowList.size() - 1);
            centerList.add(rowList.size());
            centerList.add(rowList.size() - 1);
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
            centerList.add(rowList.size());
            backgroundColorList.add(rowList.size());
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
        // 名称
        String fileName = name;
        // sheet页
        Sheet sheet1 = new Sheet(1, 0);
        sheet1.setSheetName(fileName);
        sheet1.setAutoWidth(Boolean.TRUE);
        // 样式
        List<Integer> spacialBackgroundColorList = new ArrayList<>();
        if (!backgroundColorList.isEmpty()) {
            spacialBackgroundColorList.add(mergeRowNumList.get(mergeRowNumList.size() - 1) + 5);
            spacialBackgroundColorList.add(mergeRowNumList.get(mergeRowNumList.size() - 1) + 6);
        }
        StyleExcelHandler handler = new StyleExcelHandler(boldList, borderList, backgroundColorList, centerList, spacialBackgroundColorList, centerDetailList);
        // 返回值
        EnumMap<ExcelPropertyEnum, Object> reusltEnumMap = new EnumMap<>(ExcelPropertyEnum.class);
        reusltEnumMap.put(ExcelPropertyEnum.HANDLER, handler);
        reusltEnumMap.put(ExcelPropertyEnum.ROWLIST, rowList);
        reusltEnumMap.put(ExcelPropertyEnum.SHEET, sheet1);
        reusltEnumMap.put(ExcelPropertyEnum.FILENAME, fileName + "(" + startDate + "_" + endDate + ")");
        reusltEnumMap.put(ExcelPropertyEnum.MERGE, mergeRowNumList);

        return reusltEnumMap;
    }

    @ApiIgnore
    @PostMapping("/overdueAllColumns")
    public Object overdueAllColumns() {
        return financeService.findOverdueAllColumns();
    }

    @ApiOperation(value = "客户逾期统计", notes = "数据来源：用友；数据截止日期：昨天")
    @PostMapping("/overdueAll")
    public Object overdueAll(@Validated OverdueDTO partner, BindingResult bindingResult) {
        // 参数格式校验
        Result checkResult = CommonUtils.checkParameter(bindingResult);
        if (checkResult != null) {
            return checkResult;
        }
        // 列名
        List<String> columnsList = financeService.findOverdueAllColumns().get("columns");
        // 数据List
        List<Object[]> resultDataList = financeService.findOverdueAll(partner);
        // 数据
        ArrayList<Map<String, Object>> rowsList = new ArrayList<>();

        for (Object[] dataRow : resultDataList) {
            Map<String, Object> dataMap = new HashMap<>();
            ArrayList<Object> dataList = new ArrayList<>();
            // 账期月, 目前rs第7列
            int month = Integer.parseInt(dataRow[7].toString());
            // 账期日，目前rs第8列
            int day = Integer.parseInt(dataRow[8].toString());
            // 应减去的结算周期数
            int overdueMonths = CommonUtils.overdueMonth(month, day);
            // 当前逾期金额
            BigDecimal overdue = new BigDecimal(dataRow[11].toString());
            // 设置数据行，移除前3列（关联id列、总发货、总收款）
            for (int i = 3; i < dataRow.length; i++) {
                if (i > 12) {  // 计算每个周期的发货和应收
                    if (i >= dataRow.length - overdueMonths * 2) {
                        // 有关联的账期客户逾期总金额不计算未到账期的退货金额，无关联关系的账期客户预期总金额计算所有退货金额
                        // 分月统计全部计算所有退货金额
                        // 发货金额，未到账期均不计算
                        overdue = overdue.subtract(new BigDecimal(dataRow[i].toString()));
                        // 只计算逾期账期数据，如果是未逾期账期数据，需要将逾期款减去相应的发货金额
                        BigDecimal tempOverdue;
                        if ("0".equals(dataRow[0])) {
                            tempOverdue = new BigDecimal(dataList.get(8).toString()).subtract(new BigDecimal(dataRow[i++].toString()));
                            dataList.set(8, tempOverdue);
                        } else {
                            // 有关联账户不计算未到期的退货
                            tempOverdue = new BigDecimal(dataList.get(8).toString()).subtract(new BigDecimal(dataRow[i++].toString()));
                            tempOverdue = tempOverdue.subtract(new BigDecimal(dataRow[i].toString()));
                            dataList.set(8, tempOverdue);
                        }
                        dataList.add(0);
                    } else {
                        dataList.add(new BigDecimal(dataRow[i++].toString()));
                    }
                } else if (i > 9 && i <= 12) {
                    dataList.add(new BigDecimal(dataRow[i].toString()));
                } else {
                    dataList.add(dataRow[i].toString());
                }
            }

            // 根据逾期款，设置excel数据。从后向前，到期初为止。
            for (int index = dataList.size() - 1; index > 8; index--) {
                if (overdue.compareTo(BigDecimal.ZERO) < 1) {  // 逾期金额小于等于0，所有账期逾期金额都是0
                    dataList.set(index, 0);
                } else {  // 逾期金额大于0，从最后一个开始分摊逾期金额
                    if (overdue.compareTo(new BigDecimal(dataList.get(index).toString())) > -1) {
                        overdue = overdue.subtract(new BigDecimal(dataList.get(index).toString()));
                        dataList.set(index, dataList.get(index));
                    } else {
                        dataList.set(index, overdue);
                        overdue = BigDecimal.ZERO;
                    }
                }
            }

            // 补零数量
            // int overdueZero = CommonUtils.overdueZero(month, day);
            // 导出的Excel显示逾期金额，不是发货金额。需要按照账期周期，向后推迟逾期金额，在期初之后补0实现。
            for (int overdueIndex = 0; overdueIndex < month; overdueIndex++) {
                // 插入0
                dataList.add(9, 0);
                // 删除最后一位
                dataList.remove(dataList.size() - 1);
            }

            // 设置数据列
            for (int index = 0; index < columnsList.size(); index++) {
                dataMap.put(columnsList.get(index), dataList.get(index));
            }

            rowsList.add(dataMap);

        }

        HashMap<String, Object> result = new HashMap<>();
        result.put("rows", rowsList);
        result.put("total", financeService.findOverdueAllCount(partner));
        return result;
    }

    @ApiIgnore
    @PostMapping("/exportOverdueAll")
    public void exportOverdueAll(HttpServletResponse httpServletResponse) throws IOException {
        // 列名
        List<String> columnsList = financeService.findOverdueAllColumns().get("columns");
        // 数据List
        List<Object[]> resultDataList = financeService.findOverdueAll(null);
        // 数据
        ArrayList<List<Object>> rowsList = new ArrayList<>();

        for (Object[] dataRow : resultDataList) {
            Map<String, Object> dataMap = new HashMap<>();
            ArrayList<Object> dataList = new ArrayList<>();
            // 账期月, 目前rs第7列
            int month = Integer.parseInt(dataRow[7].toString());
            // 账期日，目前rs第8列
            int day = 0;
            if (StringUtils.isNumeric(dataRow[8].toString())) {
                day = Integer.parseInt(dataRow[8].toString());
            }
            // 应减去的结算周期数
            int overdueMonths = CommonUtils.overdueMonth(month, day);
            // 当前逾期金额
            BigDecimal overdue = new BigDecimal(dataRow[11].toString());
            // 设置数据行，移除前3列（关联id列、总发货、总收款）
            for (int i = 3; i < dataRow.length; i++) {
                if (i > 12) {  // 计算每个周期的发货和应收
                    if (i >= dataRow.length - overdueMonths * 2) {
                        // 有关联的账期客户逾期总金额不计算未到账期的退货金额，无关联关系的账期客户预期总金额计算所有退货金额
                        // 分月统计全部计算所有退货金额
                        // 发货金额，未到账期均不计算
                        overdue = overdue.subtract(new BigDecimal(dataRow[i].toString()));
                        // 只计算逾期账期数据，如果是未逾期账期数据，需要将逾期款减去相应的发货金额
                        BigDecimal tempOverdue = BigDecimal.ZERO;

                        if ("0".equals(dataRow[0])) {
                            tempOverdue = new BigDecimal(dataList.get(8).toString()).subtract(new BigDecimal(dataRow[i++].toString()));
                            dataList.set(8, tempOverdue);
                        } else {
                            // 有关联账户不计算未到期的退货
                            tempOverdue = new BigDecimal(dataList.get(8).toString()).subtract(new BigDecimal(dataRow[i++].toString()));
                            tempOverdue = tempOverdue.subtract(new BigDecimal(dataRow[i].toString()));
                            dataList.set(8, tempOverdue);
                        }
                        dataList.add(0);
                    } else {
                        dataList.add(new BigDecimal(dataRow[i++].toString()));
                    }
                } else if (i > 9 && i <= 12) {
                    dataList.add(new BigDecimal(dataRow[i].toString()));
                } else {
                    dataList.add(dataRow[i].toString());
                }
            }

            // 根据逾期款，设置excel数据。从后向前，到期初为止。
            for (int index = dataList.size() - 1; index > 8; index--) {
                if (overdue.compareTo(BigDecimal.ZERO) < 1) {  // 逾期金额小于等于0，所有账期逾期金额都是0
                    dataList.set(index, 0);
                } else {  // 逾期金额大于0，从最后一个开始分摊逾期金额
                    if (overdue.compareTo(new BigDecimal(dataList.get(index).toString())) > -1) {
                        overdue = overdue.subtract(new BigDecimal(dataList.get(index).toString()));
                        dataList.set(index, dataList.get(index));
                    } else {
                        dataList.set(index, overdue);
                        overdue = BigDecimal.ZERO;
                    }
                }
            }

            // 补零数量
            // int overdueZero = CommonUtils.overdueZero(month, day);
            // 导出的Excel显示逾期金额，不是发货金额。需要按照账期周期，向后推迟逾期金额，在期初之后补0实现。
            for (int overdueIndex = 0; overdueIndex < month; overdueIndex++) {
                // 插入0
                dataList.add(9, 0);
                // 删除最后一位
                dataList.remove(dataList.size() - 1);
            }

            // 设置数据列
            for (int index = 0; index < columnsList.size(); index++) {
                dataMap.put(columnsList.get(index), dataList.get(index));
            }

            rowsList.add(dataList);

        }

        String fileName = "逾期统计表";
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");

        Sheet sheet1 = new Sheet(1, 0);
        sheet1.setSheetName(fileName);
        sheet1.setAutoWidth(Boolean.TRUE);

        fileName = fileName + df.format(new Date());
        ServletOutputStream out = httpServletResponse.getOutputStream();
        httpServletResponse.setContentType("multipart/form-data");
        httpServletResponse.setCharacterEncoding("utf-8");
        httpServletResponse.setHeader("Content-Disposition", "attachment;filename*= UTF-8''" + URLEncoder.encode(fileName, "UTF-8") + ".xlsx");
        // 设置列名
        if (columnsList != null) {
            List<List<String>> list = new ArrayList<>();
            columnsList.forEach(c -> list.add(Collections.singletonList(c)));
            sheet1.setHead(list);
        }
        // 写入数据
        ExcelWriter writer = new ExcelWriter(out, ExcelTypeEnum.XLSX, true);
        writer.write1(rowsList, sheet1);

        writer.finish();
        out.flush();
        out.close();

    }

    @ApiIgnore
    @PostMapping("/overdueSalesColumns")
    public Object overdueSalesColumns() {
        return financeService.findOverdueSalesColumns();
    }

    @ApiOperation(value = "客户逾期统计（销售版本）", notes = "数据来源：用友；数据截止日期：昨天")
    @PostMapping("/overdueSales")
    public Object overdueSales(@Validated OverdueDTO partner, BindingResult bindingResult) {
        // 参数格式校验
        Result checkResult = CommonUtils.checkParameter(bindingResult);
        if (checkResult != null) {
            return checkResult;
        }
        // 列名
        List<String> columnsList = financeService.findOverdueSalesColumns().get("columns");
        // 数据List
        List<Object[]> resultDataList = financeService.findOverdueSales(partner);
        // 数据
        ArrayList<Map<String, Object>> rowsList = new ArrayList<>();

        for (Object[] dataRow : resultDataList) {
            Map<String, Object> dataMap = new HashMap<>();
            ArrayList<Object> dataList = new ArrayList<>();
            // 账期月, 目前rs第7列
            int month = Integer.parseInt(dataRow[7].toString());
            // 账期日，目前rs第8列
            int day = 0;
            if (StringUtils.isNumeric(dataRow[8].toString())) {
                day = Integer.parseInt(dataRow[8].toString());
            }
            // 应减去的结算周期数
            int overdueMonths = CommonUtils.overdueMonth(month, day);
            // 当前逾期金额
            BigDecimal overdue = new BigDecimal(dataRow[11].toString());
            // 设置数据行，移除前3列（关联id列、总发货、总收款）
            for (int i = 3; i < dataRow.length; i++) {
                if (i > 12) {  // 计算每个周期的发货和应收
                    if (i >= dataRow.length - overdueMonths * 2) {
                        // 有关联的账期客户逾期总金额不计算未到账期的退货金额，无关联关系的账期客户预期总金额计算所有退货金额
                        // 分月统计全部计算所有退货金额
                        // 发货金额，未到账期均不计算
                        overdue = overdue.subtract(new BigDecimal(dataRow[i].toString()));
                        // 只计算逾期账期数据，如果是未逾期账期数据，需要将逾期款减去相应的发货金额
                        BigDecimal tempOverdue;
                        if ("0".equals(dataRow[0])) {
                            tempOverdue = new BigDecimal(dataList.get(8).toString()).subtract(new BigDecimal(dataRow[i++].toString()));
                            dataList.set(8, tempOverdue);
                        } else {
                            // 有关联账户不计算未到期的退货
                            tempOverdue = new BigDecimal(dataList.get(8).toString()).subtract(new BigDecimal(dataRow[i++].toString()));
                            tempOverdue = tempOverdue.subtract(new BigDecimal(dataRow[i].toString()));
                            dataList.set(8, tempOverdue);
                        }
                        dataList.add(0);
                    } else {
                        dataList.add(new BigDecimal(dataRow[i++].toString()));
                    }
                } else if (i > 9 && i <= 12) {
                    dataList.add(new BigDecimal(dataRow[i].toString()));
                } else {
                    dataList.add(dataRow[i].toString());
                }
            }

            // 根据逾期款，设置excel数据。从后向前，不计算期初。
            /**
             * 功能只需要显示3个月的，但是涉及账期问题，如果账期一个月，需要多计算1个月，也就是4个月。目前按多算3个月，也就是6个月的数据。
             * 因为是6个月的数据，所以逾期的分摊只需要算三个月，从第11列开始。
             */
            for (int index = dataList.size() - 1; index > 12; index--) {
                if (overdue.compareTo(BigDecimal.ZERO) < 1) {  // 逾期金额小于等于0，所有账期逾期金额都是0
                    dataList.set(index, 0);
                } else {  // 逾期金额大于0，从最后一个开始分摊逾期金额
                    if (overdue.compareTo(new BigDecimal(dataList.get(index).toString())) > -1) {
                        overdue = overdue.subtract(new BigDecimal(dataList.get(index).toString()));
                        dataList.set(index, dataList.get(index));
                    } else {
                        dataList.set(index, overdue);
                        overdue = BigDecimal.ZERO;
                    }
                }
            }

            // 导出的Excel显示逾期金额，不是发货金额。需要按照账期周期，向后推迟逾期金额，在期初之后补0实现。
            for (int overdueIndex = 0; overdueIndex < month; overdueIndex++) {
                // 插入0
                dataList.add(10, 0);
                // 删除最后一位
                dataList.remove(dataList.size() - 1);
            }

            // 期初等于减去显示的月份的逾期款，小于0期初显示0
            if (overdue.compareTo(BigDecimal.ZERO) < 0) {
                dataList.set(9, 0);
            } else {
                dataList.set(9, overdue);
            }

            // 设置数据列
            // 前面固定部分和后面月份动态部门分别处理
            for (int index = 0; index < 10; index++) {
                dataMap.put(columnsList.get(index), dataList.get(index));
            }
            // 动态部分
            for (int index = 1; index <= columnsList.size() - 10; index++) {
                if (index > 3) {
                    dataMap.put(columnsList.get(columnsList.size() - index), dataList.get(dataList.size() - index));
                } else {
                    // 按账期日，每5天划分一列，向后合并。例如：账期日7显示在10日列。
                    // 为了对应bootstrap的列名，看不懂可以重新写，符合规则就行。
                    int date = 0;
                    if (StringUtils.isNumeric(dataMap.get("账期日").toString())) {
                        date = Integer.parseInt(dataMap.get("账期日").toString());
                    }
                    if (date <= 5) {
                        dataMap.put((4 - index) + "05", dataList.get(dataList.size() - index));
                    } else if (date <= 10) {
                        dataMap.put((4 - index) + "10", dataList.get(dataList.size() - index));
                    } else if (date <= 15) {
                        dataMap.put((4 - index) + "15", dataList.get(dataList.size() - index));
                    } else if (date <= 20) {
                        dataMap.put((4 - index) + "20", dataList.get(dataList.size() - index));
                    } else if (date <= 25) {
                        dataMap.put((4 - index) + "25", dataList.get(dataList.size() - index));
                    } else {
                        dataMap.put((4 - index) + "30", dataList.get(dataList.size() - index));
                    }
                }
            }

            rowsList.add(dataMap);

        }

        HashMap<String, Object> result = new HashMap<>();
        result.put("rows", rowsList);
        result.put("total", financeService.findOverdueAllCount(partner));
        return result;
    }

    @ApiIgnore
    @PostMapping("/exportOverdueSales")
    public void exportOverdueSales(HttpServletResponse httpServletResponse) throws IOException {
        String fileName = "逾期统计表";
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
        List<String> columnsList = financeService.findOverdueSalesColumns().get("columns");
        // 表单
        Sheet sheet = new Sheet(1, 0);
        sheet.setSheetName(fileName);
        // 创建一个表格
        Table table = new Table(1);
        // 表头List
        List<List<String>> headList = new ArrayList<List<String>>();
        // 拼接表头
        for (int index = 0; index < columnsList.size(); index++) {
            if (index < 10) {
                List<String> headTitle = new ArrayList<>();
                headTitle.add(columnsList.get(index));
                headTitle.add(columnsList.get(index));
                headList.add(headTitle);
            } else {
                List<String> headTitle = new ArrayList<>();
                headTitle.add(columnsList.get(index));
                headTitle.add("05");
                headList.add(headTitle);
                headTitle = new ArrayList<>();
                headTitle.add(columnsList.get(index));
                headTitle.add("10");
                headList.add(headTitle);
                headTitle = new ArrayList<>();
                headTitle.add(columnsList.get(index));
                headTitle.add("15");
                headList.add(headTitle);
                headTitle = new ArrayList<>();
                headTitle.add(columnsList.get(index));
                headTitle.add("20");
                headList.add(headTitle);
                headTitle = new ArrayList<>();
                headTitle.add(columnsList.get(index));
                headTitle.add("25");
                headList.add(headTitle);
                headTitle = new ArrayList<>();
                headTitle.add(columnsList.get(index));
                headTitle.add("30");
                headList.add(headTitle);
            }
        }

        table.setHead(headList);

        Sheet sheet1 = new Sheet(1, 0);
        sheet1.setSheetName(fileName);
        sheet1.setAutoWidth(Boolean.TRUE);

        fileName = fileName + df.format(new Date());
        ServletOutputStream out = httpServletResponse.getOutputStream();
        httpServletResponse.setContentType("multipart/form-data");
        httpServletResponse.setCharacterEncoding("utf-8");
        httpServletResponse.setHeader("Content-Disposition", "attachment;filename*= UTF-8''" + URLEncoder.encode(fileName, "UTF-8") + ".xlsx");
        // 设置列名
        if (columnsList != null) {
            List<List<String>> list = new ArrayList<>();
            columnsList.forEach(c -> list.add(Collections.singletonList(c)));
            sheet1.setHead(list);
        }

        /**
         * 以下处理数据
         */
        // 数据List
        List<Object[]> resultDataList = financeService.findOverdueSales(null);
        // 数据
        List<List<Object>> rowsList = new ArrayList<>();

        for (Object[] dataRow : resultDataList) {
            ArrayList<Object> dataList = new ArrayList<>();
            // 账期月, 目前rs第7列
            int month = Integer.parseInt(dataRow[7].toString());
            // 账期日，目前rs第8列
            int day = 0;
            if (StringUtils.isNumeric(dataRow[8].toString())) {
                day = Integer.parseInt(dataRow[8].toString());
            }
            // 应减去的结算周期数
            int overdueMonths = CommonUtils.overdueMonth(month, day);
            // 当前逾期金额
            BigDecimal overdue = new BigDecimal(dataRow[11].toString());
            // 设置数据行，移除前3列（关联id列、总发货、总收款）
            for (int i = 3; i < dataRow.length; i++) {
                if (i > 12) {  // 计算每个周期的发货和应收
                    if (i >= dataRow.length - overdueMonths * 2) {
                        // 有关联的账期客户逾期总金额不计算未到账期的退货金额，无关联关系的账期客户预期总金额计算所有退货金额
                        // 分月统计全部计算所有退货金额
                        // 发货金额，未到账期均不计算
                        overdue = overdue.subtract(new BigDecimal(dataRow[i].toString()));
                        // 只计算逾期账期数据，如果是未逾期账期数据，需要将逾期款减去相应的发货金额
                        BigDecimal tempOverdue;
                        if ("0".equals(dataRow[0])) {
                            tempOverdue = new BigDecimal(dataList.get(8).toString()).subtract(new BigDecimal(dataRow[i++].toString()));
                            dataList.set(8, tempOverdue);
                        } else {
                            // 有关联账户不计算未到期的退货
                            tempOverdue = new BigDecimal(dataList.get(8).toString()).subtract(new BigDecimal(dataRow[i++].toString()));
                            tempOverdue = tempOverdue.subtract(new BigDecimal(dataRow[i].toString()));
                            dataList.set(8, tempOverdue);
                        }
                        dataList.add(0);
                    } else {
                        dataList.add(new BigDecimal(dataRow[i++].toString()));
                    }
                } else if (i > 9 && i <= 12) {
                    dataList.add(new BigDecimal(dataRow[i].toString()));
                } else {
                    dataList.add(dataRow[i].toString());
                }
            }

            // 根据逾期款，设置excel数据。从后向前，不计算期初。
            /**
             * 功能只需要显示3个月的，但是涉及账期问题，如果账期一个月，需要多计算1个月，也就是4个月。目前按多算3个月，也就是6个月的数据。
             * 因为是6个月的数据，所以逾期的分摊只需要算三个月，从第11列开始。
             */
            for (int index = dataList.size() - 1; index > 12; index--) {
                if (overdue.compareTo(BigDecimal.ZERO) < 1) {  // 逾期金额小于等于0，所有账期逾期金额都是0
                    dataList.set(index, 0);
                } else {  // 逾期金额大于0，从最后一个开始分摊逾期金额
                    if (overdue.compareTo(new BigDecimal(dataList.get(index).toString())) > -1) {
                        overdue = overdue.subtract(new BigDecimal(dataList.get(index).toString()));
                        dataList.set(index, dataList.get(index));
                    } else {
                        dataList.set(index, overdue);
                        overdue = BigDecimal.ZERO;
                    }
                }
            }

            // 导出的Excel显示逾期金额，不是发货金额。需要按照账期周期，向后推迟逾期金额，在期初之后补0实现。
            for (int overdueIndex = 0; overdueIndex < month; overdueIndex++) {
                // 插入0
                dataList.add(10, 0);
                // 删除最后一位
                dataList.remove(dataList.size() - 1);
            }

            // 期初等于减去显示的月份的逾期款，小于0期初显示0
            if (overdue.compareTo(BigDecimal.ZERO) < 0) {
                dataList.set(9, 0);
            } else {
                dataList.set(9, overdue);
            }

            // 设置数据列
            List<Object> resultList = new ArrayList<>();
            // 前面固定部分和后面月份动态部门分别处理
            for (int index = 0; index < 10; index++) {
                resultList.add(dataList.get(index));
            }
            // 动态部分
            for (int index = 3; index > 0; index--) {
//                if (index > 3) {
//                    resultList.add(dataList.get(dataList.size() - index));
//                } else {
                    // 按账期日补-
                    int date = 0;
                    if (StringUtils.isNumeric(dataList.get(5).toString())) {
                        date = Integer.parseInt(dataList.get(5).toString());
                    }
                    List<Object> zeroList = new ArrayList<>();
                    if (date <= 5) {
                        zeroList.add(dataList.get(dataList.size() - index));
                        zeroList.add("-");
                        zeroList.add("-");
                        zeroList.add("-");
                        zeroList.add("-");
                        zeroList.add("-");
                    } else if (date <= 10) {
                        zeroList.add("-");
                        zeroList.add(dataList.get(dataList.size() - index));
                        zeroList.add("-");
                        zeroList.add("-");
                        zeroList.add("-");
                        zeroList.add("-");
                    } else if (date <= 15) {
                        zeroList.add("-");
                        zeroList.add("-");
                        zeroList.add(dataList.get(dataList.size() - index));
                        zeroList.add("-");
                        zeroList.add("-");
                        zeroList.add("-");
                    } else if (date <= 20) {
                        zeroList.add("-");
                        zeroList.add("-");
                        zeroList.add("-");
                        zeroList.add(dataList.get(dataList.size() - index));
                        zeroList.add("-");
                        zeroList.add("-");
                    } else if (date <= 25) {
                        zeroList.add("-");
                        zeroList.add("-");
                        zeroList.add("-");
                        zeroList.add("-");
                        zeroList.add(dataList.get(dataList.size() - index));
                        zeroList.add("-");
                    } else {
                        zeroList.add("-");
                        zeroList.add("-");
                        zeroList.add("-");
                        zeroList.add("-");
                        zeroList.add("-");
                        zeroList.add(dataList.get(dataList.size() - index));
                    }
                    resultList.addAll(zeroList);
//                }
            }

            rowsList.add(resultList);

        }

        // 写入数据
        ExcelWriter writer = new ExcelWriter(out, ExcelTypeEnum.XLSX, true);
        writer.write1(rowsList, sheet, table);

        writer.finish();
        out.flush();
        out.close();
    }

    enum ExcelPropertyEnum {
        HANDLER, ROWLIST, SHEET, FILENAME, MERGE
    }

    /**
     * 查询用友线上供应商应付对账单
     *
     * @param request
     * @return
     */
    @PostMapping(value = "getSupplier")
    public Object getSupplier(HttpServletRequest request) {
        Map<String, String> requestMap = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        try {
            String isOnline = null;
            if (StringUtils.isNotBlank(request.getParameter("startDate"))) {
                requestMap.put("startDate", request.getParameter("startDate"));
            }
            if (StringUtils.isNotBlank(request.getParameter("endDate"))) {
                requestMap.put("endDate", request.getParameter("endDate"));
            }
            if (StringUtils.isNotBlank(request.getParameter("isOnline"))) {
                isOnline = request.getParameter("isOnline");
            } else {
                return null;
            }
            if (StringUtils.isNotBlank(request.getParameter("name"))) {
                requestMap.put("name", request.getParameter("name"));
            } else {
                return new Result("400", "供应商名称为空", null);
            }
            if (StringUtils.isNotBlank(request.getParameter("limit"))) {
                requestMap.put("limit", request.getParameter("limit"));
            } else {
                requestMap.put("limit", "100");
            }
            if (StringUtils.isNotBlank(request.getParameter("offset"))) {
                requestMap.put("offset", request.getParameter("offset"));
            } else {
                requestMap.put("offset", "0");
            }

            List<Map<String, Object>> supplier = this.getSupplier(requestMap, isOnline);
            result.put("rows", supplier);
            result.put("total", financeService.getSupplierCount(requestMap, isOnline));
            return result;
        } catch (ParseException e) {
            return new Result("400", "异常", null);
        }
    }

    @PostMapping(value = "/download/SupplierExcel")
    public void downSupplierExcel(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> requestMap = new HashMap<>();
        List<List<Object>> result = new ArrayList<>();
        if (StringUtils.isNotBlank(request.getParameter("startDate"))) {
            requestMap.put("startDate", request.getParameter("startDate"));
        }
        if (StringUtils.isNotBlank(request.getParameter("endDate"))) {
            requestMap.put("endDate", request.getParameter("endDate"));
        }
        if (StringUtils.isNotBlank(request.getParameter("name"))) {
            requestMap.put("name", request.getParameter("name"));
        } else {
            return;
        }
        requestMap.put("limit", "10000000000");
        requestMap.put("offset", "0");

        List<Map<String, Object>> online = this.getSupplier(requestMap, "线上");
        List<Map<String, Object>> offline = this.getSupplier(requestMap, "线下");
//        financeService.getCountSupplier
        List<String> columnNameList = new ArrayList<>();
        columnNameList.add("日期");
        columnNameList.add("合同编号");
        columnNameList.add("单据类别");
        columnNameList.add("收货金额");
        columnNameList.add("付款金额");
        columnNameList.add("应付账款");
        columnNameList.add("开票金额");
        columnNameList.add("发票结余/开票余额");
        List<Object> objects;
        objects = new ArrayList<>();
        objects.add("线上供应商对账单:");
        result.add(objects);
        for (Map<String, Object> order : online) {
            objects = new ArrayList<>();
            objects.add(order.get("voucherdate"));
            objects.add(order.get("code"));
            objects.add(order.get("type"));
            objects.add(order.get("receivingAmount"));
            objects.add(order.get("paymentAmount"));
            objects.add(order.get("balancePayableAmount"));
            objects.add(order.get("invoiceAmount"));
            objects.add(order.get("balanceInvoiceAmount"));
            result.add(objects);
        }
        result.add(new ArrayList<>());
        objects = new ArrayList<>();
        objects.add("线下供应商对账单:");
        result.add(objects);
        for (Map<String, Object> order : offline) {
            objects = new ArrayList<>();
            objects.add(order.get("voucherdate"));
            objects.add(order.get("code"));
            objects.add(order.get("type"));
            objects.add(order.get("receivingAmount"));
            objects.add(order.get("paymentAmount"));
            objects.add(order.get("balancePayableAmount"));
            objects.add(order.get("invoiceAmount"));
            objects.add(order.get("balanceInvoiceAmount"));
            result.add(objects);
        }
        List<Map<String, Object>> mapList = financeService.getSupplierCount(requestMap);
        if (mapList != null && mapList.size() > 0) {
            HashMap<String, Object> map = new HashMap<>();
            BigDecimal balancePayableAmount = BigDecimal.ZERO;
            BigDecimal balanceInvoiceAmount = BigDecimal.ZERO;
            BigDecimal receivingAmount = BigDecimal.ZERO;
            BigDecimal paymentAmount = BigDecimal.ZERO;
            BigDecimal invoiceAmount = BigDecimal.ZERO;
            for (Map<String, Object> stringObjectMap : mapList) {
                balancePayableAmount = balancePayableAmount.add(new BigDecimal(stringObjectMap.get("balancePayableAmount").toString()));
                balanceInvoiceAmount = balanceInvoiceAmount.add(new BigDecimal(stringObjectMap.get("balanceInvoiceAmount").toString()));
                receivingAmount = receivingAmount.add(new BigDecimal(stringObjectMap.get("receivingAmount").toString()));
                paymentAmount = paymentAmount.add(new BigDecimal(stringObjectMap.get("paymentAmount").toString()));
                invoiceAmount = invoiceAmount.add(new BigDecimal(stringObjectMap.get("invoiceAmount").toString()));
            }
            map.put("balancePayableAmount", balancePayableAmount);
            map.put("balanceInvoiceAmount", balanceInvoiceAmount);
            map.put("receivingAmount", receivingAmount);
            map.put("paymentAmount", paymentAmount);
            map.put("invoiceAmount", invoiceAmount);
            map.put("type", "汇总");
            mapList.add(map);
        }
        result.add(new ArrayList<>());
        objects = new ArrayList<>();
        objects.add("线下供应商对账单汇总:");
        result.add(objects);
        objects = new ArrayList<>();
        objects.add("单据类别");
        objects.add("收货金额");
        objects.add("付款金额");
        objects.add("应付账款");
        objects.add("开票金额");
        objects.add("发票结余/开票余额");
        result.add(objects);
        for (Map<String, Object> order : mapList) {
            objects = new ArrayList<>();
            objects.add(order.get("type"));
            objects.add(order.get("receivingAmount"));
            objects.add(order.get("paymentAmount"));
            objects.add(order.get("balancePayableAmount"));
            objects.add(order.get("invoiceAmount"));
            objects.add(order.get("balanceInvoiceAmount"));
            result.add(objects);
        }
        CommonUtils.exportByList(response, columnNameList, result, "供应商对账单");


    }

    /**
     * @param requestMap
     * @param isOnline   "线上“：”线下“
     * @return
     * @throws ParseException
     */
    private List<Map<String, Object>> getSupplier(Map<String, String> requestMap, String isOnline) throws ParseException {
        //当前页
        List<Map<String, Object>> page = financeService.getSupplier(requestMap, isOnline);
        //分页前数据
        List<Map<String, Object>> beforePage = null;
        //前期结余
        Map<String, Object> initial = financeService.getOrigAmount(requestMap, isOnline);
        //收货金额
        BigDecimal thisReceivingAmount = null;
        //付款金额
        BigDecimal thisPaymentAmount = null;
        //开票金额
        BigDecimal thisInvoiceAmount = null;
        //初期应付余额
        BigDecimal initPayableAmount = BigDecimal.ZERO;
        //初期开票余额
        BigDecimal initInvoiceAmount = BigDecimal.ZERO;
        if (initial != null && initial.size() > 0) {
            System.out.println(initial.toString());
            initPayableAmount = new BigDecimal(initial.get("payable") == null ? "0" : initial.get("payable").toString());
            initInvoiceAmount = new BigDecimal(initial.get("invoice") == null ? "0" : initial.get("invoice").toString());
        }

        //首页数据
        if ("0".equals(requestMap.get("offset"))) {
            Map<String, Object> index0 = new HashMap<>();
            index0.put("balancePayableAmount", initPayableAmount);
            index0.put("balanceInvoiceAmount", initInvoiceAmount);
            index0.put("Amount", " ");
            index0.put("type", "期初余额");
            page.add(0, index0);
            for (int i = 1; i < page.size(); i++) {
                thisReceivingAmount = BigDecimal.ZERO;
                thisPaymentAmount = BigDecimal.ZERO;
                thisInvoiceAmount = BigDecimal.ZERO;
                String plus = page.get(i).get("plus").toString();
                switch (plus) {
                    case "1":
                        thisReceivingAmount = new BigDecimal(page.get(i).get("Amount").toString());
                        page.get(i).put("receivingAmount", thisReceivingAmount);
                        break;
                    case "-1":
                        thisPaymentAmount = new BigDecimal(page.get(i).get("Amount").toString());
                        page.get(i).put("paymentAmount", thisPaymentAmount);
                        break;
                    case "-2":
                        thisInvoiceAmount = new BigDecimal(page.get(i).get("Amount").toString());
                        page.get(i).put("invoiceAmount", thisInvoiceAmount);
                        break;
                    default:
                        break;
                }

                BigDecimal lastPayableAmount = new BigDecimal(page.get(i - 1).get("balancePayableAmount").toString());
                BigDecimal lastInvoiceAmount = new BigDecimal(page.get(i - 1).get("balanceInvoiceAmount").toString());
                page.get(i).put("balancePayableAmount", lastPayableAmount.add(thisReceivingAmount).subtract(thisPaymentAmount).doubleValue());
                page.get(i).put("balanceInvoiceAmount", lastInvoiceAmount.add(thisReceivingAmount).subtract(thisInvoiceAmount).doubleValue());
            }
        } else {
            beforePage = financeService.getTopOrigAmount(requestMap, isOnline);
            BigDecimal payable = new BigDecimal(beforePage.get(0).get("payable").toString());
            BigDecimal invoice = new BigDecimal(beforePage.get(0).get("invoice").toString());
            for (int i = 0; i < page.size(); i++) {
                thisReceivingAmount = BigDecimal.ZERO;
                thisPaymentAmount = BigDecimal.ZERO;
                thisInvoiceAmount = BigDecimal.ZERO;
                String plus = page.get(i).get("plus").toString();
                BigDecimal amount = new BigDecimal(page.get(i).get("Amount").toString());
                switch (plus) {
                    case "1":
                        thisReceivingAmount = amount;
                        page.get(i).put("receivingAmount", thisReceivingAmount);
                        break;
                    case "-1":
                        thisPaymentAmount = amount;
                        page.get(i).put("paymentAmount", thisPaymentAmount);
                        break;
                    case "-2":
                        thisInvoiceAmount = amount;
                        page.get(i).put("invoiceAmount", thisInvoiceAmount);
                        break;
                    default:
                        break;
                }
                if (i == 0) {
                    page.get(i).put("balancePayableAmount", initPayableAmount.add(payable).add(thisReceivingAmount).subtract(thisPaymentAmount).doubleValue());
                    page.get(i).put("balanceInvoiceAmount", initInvoiceAmount.add(invoice).add(thisReceivingAmount).subtract(thisInvoiceAmount).doubleValue());
                } else {
                    BigDecimal lastPayableAmount = new BigDecimal(page.get(i - 1).get("balancePayableAmount").toString());
                    BigDecimal lastInvoiceAmount = new BigDecimal(page.get(i - 1).get("balanceInvoiceAmount").toString());
                    page.get(i).put("balancePayableAmount", lastPayableAmount.add(thisReceivingAmount).subtract(thisPaymentAmount).doubleValue());
                    page.get(i).put("balanceInvoiceAmount", lastInvoiceAmount.add(thisReceivingAmount).subtract(thisInvoiceAmount).doubleValue());
                }
            }
        }

        return page;
    }

    @PostMapping(value = "getCountSupplier")
    public Object getCountSupplier(HttpServletRequest request) {
        Map<String, String> requestMap = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        try {
            if (StringUtils.isNotBlank(request.getParameter("startDate"))) {
                requestMap.put("startDate", request.getParameter("startDate"));
            }
            if (StringUtils.isNotBlank(request.getParameter("endDate"))) {
                requestMap.put("endDate", request.getParameter("endDate"));
            }
            if (StringUtils.isNotBlank(request.getParameter("name"))) {
                requestMap.put("name", request.getParameter("name"));
            } else {
                return null;
            }
            List<Map<String, Object>> mapList = financeService.getSupplierCount(requestMap);
            if (mapList != null && mapList.size() > 0) {
                HashMap<String, Object> map = new HashMap<>();
                BigDecimal balancePayableAmount = BigDecimal.ZERO;
                BigDecimal balanceInvoiceAmount = BigDecimal.ZERO;
                BigDecimal receivingAmount = BigDecimal.ZERO;
                BigDecimal paymentAmount = BigDecimal.ZERO;
                BigDecimal invoiceAmount = BigDecimal.ZERO;
                for (Map<String, Object> stringObjectMap : mapList) {
                    balancePayableAmount = balancePayableAmount.add(new BigDecimal(stringObjectMap.get("balancePayableAmount").toString()));
                    balanceInvoiceAmount = balanceInvoiceAmount.add(new BigDecimal(stringObjectMap.get("balanceInvoiceAmount").toString()));
                    receivingAmount = receivingAmount.add(new BigDecimal(stringObjectMap.get("receivingAmount").toString()));
                    paymentAmount = paymentAmount.add(new BigDecimal(stringObjectMap.get("paymentAmount").toString()));
                    invoiceAmount = invoiceAmount.add(new BigDecimal(stringObjectMap.get("invoiceAmount").toString()));
                }
                map.put("balancePayableAmount", balancePayableAmount);
                map.put("balanceInvoiceAmount", balanceInvoiceAmount);
                map.put("receivingAmount", receivingAmount);
                map.put("paymentAmount", paymentAmount);
                map.put("invoiceAmount", invoiceAmount);
                map.put("type", "汇总");
                mapList.add(map);
            }
            result.put("rows", mapList);
            result.put("total", 3);
            return result;
        } catch (ParseException e) {
            return new Result("400", "异常", null);
        }
    }

    @PostMapping(value = "/download/excel")
    public void download(HttpServletResponse response, HttpServletRequest request) {
        Map<String, String> requestMap = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isNotBlank(request.getParameter("startDate"))) {
            requestMap.put("startDate", request.getParameter("startDate"));
        }
        if (StringUtils.isNotBlank(request.getParameter("endDate"))) {
            requestMap.put("endDate", request.getParameter("endDate"));
        }
        if (StringUtils.isNotBlank(request.getParameter("name"))) {
            requestMap.put("name", request.getParameter("name"));
        } else {
            return;
        }
        requestMap.put("limit", "9999999");
        requestMap.put("offset", "0");
        List<Map<String, Object>> page = financeService.getSupplier(requestMap, "线上");

        List<String> columnNameList = new ArrayList<>();
        columnNameList.add("日期");
        columnNameList.add("合同编号");
        columnNameList.add("单据类别");
        columnNameList.add("收货金额");
        columnNameList.add("付款金额");
        columnNameList.add("应付账款");
        columnNameList.add("开票金额");
        columnNameList.add("发票结余");

        List<Object> objects;
        for (Map<String, Object> objectMap : page) {
            objects = new ArrayList<>();
            objects.add(objectMap.get("voucherdate"));
            objects.add(objectMap.get("code"));
            objects.add(objectMap.get("type"));
            objects.add("1".equals(objectMap.get("plus")) ? objectMap.get("Amount") : "");
            objects.add("-1".equals(objectMap.get("plus")) ? objectMap.get("Amount") : "");
            objects.add("0".equals(objectMap.get("plus")) ? objectMap.get("Amount") : "");
        }
    }

    /**
     * 文件上传
     * <p>
     * 1. 创建excel对应的实体对象
     * <p>
     * 2. 由于默认异步读取excel，所以需要创建excel一行一行的回调监听器
     * <p>
     * 3. 直接读即可
     */
    @PostMapping("upload")
    @ResponseBody
    public String upload(MultipartFile file) throws IOException {
        // 新版
        // EasyExcel.read(file.getInputStream(), UploadData.class, new UploadDataListener(deptStaffRepository)).sheet().doRead();

        // 旧版
        InputStream inputStream = file.getInputStream();
        ExcelListener listener = new ExcelListener();
        ExcelReader excelReader = new ExcelReader(inputStream, ExcelTypeEnum.XLSX, null, listener);
        excelReader.read(new Sheet(1, 1, UploadData.class));
        List<Object> list = listener.getDatas();
        List<DeptStaff> deptStaffList = new ArrayList<>();
        deptStaffRepository.deleteAll();
        deptStaffRepository.flush();
        for (int i = 0; i < list.size(); i++) {
            DeptStaff deptStaff = new DeptStaff();
            BeanUtils.copyProperties((UploadData) list.get(i), deptStaff);
            deptStaffList.add(deptStaff);
        }
        deptStaffRepository.saveAll(deptStaffList);
        return "success";
    }
}
