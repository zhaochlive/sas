package com.js.sas.controller;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.js.sas.dto.OverdueDTO;
import com.js.sas.dto.SettlementSummaryDTO;
import com.js.sas.entity.SettlementSummaryEntity;
import com.js.sas.service.FinanceService;
import com.js.sas.service.PartnerService;
import com.js.sas.utils.*;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.*;
import java.util.*;
import java.util.Date;

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

    private final DataSource dataSource;

    private final PartnerService partnerService;

    public FinanceController(FinanceService financeService, DataSource dataSource, PartnerService partnerService) {
        this.financeService = financeService;
        this.dataSource = dataSource;
        this.partnerService = partnerService;
    }

    /**
     * 目前调用存储过程实现，存储过程很难维护，后期需要修改实现方法。
     *
     * @param settlementSummasryDTO 结算客户汇总DTO
     * @param result                校验结果
     * @return Object
     */
    @ApiOperation(value = "结算客户汇总（线上、线下）", notes = "数据来源：用友；数据截止日期：昨天")
    @PostMapping(value = "/settlementSummary")
    public Object settlementSummary(@Validated SettlementSummaryDTO settlementSummasryDTO, BindingResult result) {
        // 参数格式校验
        if (result.hasErrors()) {
            LinkedHashMap<String, String> errorMap = new LinkedHashMap<>();
            for (FieldError fieldError : result.getFieldErrors()) {
                errorMap.put(fieldError.getCode(), fieldError.getDefaultMessage());
            }
            return ResultUtils.getResult(ResultCode.参数错误, errorMap);
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
        if (bindingResult.hasErrors()) {
            LinkedHashMap<Integer, String> errorMap = new LinkedHashMap<>();
            for (int index = 0; index < bindingResult.getFieldErrors().size(); index++) {
                errorMap.put(index + 1, bindingResult.getFieldErrors().get(index).getDefaultMessage());
            }
            return ResultUtils.getResult(ResultCode.参数错误, errorMap);
        }

        HashMap<String, Object> result = new HashMap<>();

        Page<OverdueDTO> page = financeService.findOverdue(partner);

        result.put("rows", page.getContent());
        result.put("total", page.getTotalElements());

        return result;
    }

    /**
     * 逾期现金客户导出
     *
     * @param httpServletResponse
     */
    @ApiIgnore
    @PostMapping("/overdueCash/download/excel")
    public void downloadOverdueCash(int limit, HttpServletResponse httpServletResponse) {
        OverdueDTO overdueDTO = new OverdueDTO();
        overdueDTO.setLimit(limit);
        overdueDTO.setSort("name");
        overdueDTO.setSortOrder("asc");
        List<OverdueDTO> overdueList = financeService.findOverdue(overdueDTO).getContent();
        try {
            CommonUtils.export(httpServletResponse, overdueList, "现金客户逾期统计", new OverdueDTO());
        } catch (IOException e) {
            log.error("下载现金客户逾期统计异常：{}", e);
            e.printStackTrace();
        }
    }


    /**
     * 逾期账期客户导出
     * 导出表格内容截止到当前月份，动态表格直接调用存储过程实现。
     *
     * @param httpServletResponse
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
            for (int i = 4; i <= count; i++) {
                columnsList.add(rsmd.getColumnLabel(i));
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
            double totalReceivables = 0.0;

            while (rs.next()) {
                ArrayList<Object> dataList = new ArrayList<>();
                // 总收款金额，目前rs第2列
                double amount_collected = rs.getDouble(3);
                // 总发货金额，目前rs第3列
                double amount_delivery = rs.getDouble(2);
                // 总应收，目前是rs第9列
                double overdueAmount = rs.getDouble(9);
                // 账期月, 目前rs第4列
                int month = rs.getInt(6);
                // 账期日，目前rs第5列
                int day = rs.getInt(7);
                // 应减去的结算周期数
                int overdueMonths = CommonUtils.overdueMonth(month, day);
                /**
                 * 设置数据行，移除前3列（关联id列、总发货、总收款）
                 */
                for (int i = 4; i <= count; i++) {
                    if (i > 9) {  // 计算每个周期的发货和应收
                        if (i > count - overdueMonths) {  // 只计算逾期账期数据，如果是未逾期账期数据，需要将逾期款减去相应的发货金额
                            dataList.set(5, Double.valueOf(dataList.get(5).toString()) - rs.getDouble(i));
                            dataList.add(0);
                        } else {
                            dataList.add(rs.getString(i));
                        }
                    } else {
                        dataList.add(rs.getString(i));
                    }
                }

                // 当前逾期金额
                double overdue = Double.parseDouble(dataList.get(5).toString());
                // 根据逾期款，设置excel数据。从后向前，到期初为止。
                for (int index = dataList.size() - 1; index > 5; index--) {
                    if (overdue <= 0) {  // 逾期金额小于等于0，所有账期逾期金额都是0
                        dataList.set(index, 0);
                    } else {  // 逾期金额大于0，从最后一个开始分摊逾期金额
                        if (overdue >= Double.parseDouble(dataList.get(index).toString())) {
                            overdue = overdue - Double.parseDouble(dataList.get(index).toString());
                            dataList.set(index, dataList.get(index));
                        } else {
                            dataList.set(index, overdue);
                            overdue = 0;
                        }
                    }
                }

                // 导出的Excel显示逾期金额，不是发货金额。需要按照账期周期，向后推迟逾期金额，在期初之后补0实现。
                for (int overdueIndex = 0; overdueIndex < overdueMonths; overdueIndex++) {
                    // 插入0
                    dataList.add(7, 0);
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
                        for (int index : totalIndexList) {
                            rowsList.get(index - 1).set(4, totalReceivables);
                        }
                        // 置零
                        totalReceivables = 0.0;
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
                            rowsList.get(index - 1).set(4, totalReceivables);
                        }
                        // 添加至集合
                        totalList.add(totalIndexList);
                        totalIndexList = new ArrayList<>();
                    } else {
                        // 如果没有关联客户，将逾期金额赋值到总逾期金额
                        rowsList.get(rs.getRow() - 1).set(4, rowsList.get(rs.getRow() - 1).get(5));
                    }
                    // 置零
                    hasParentCode = false;
                    parentCode = "";
                    totalReceivables = 0.0;
                }

                // 设置应收金额合计
                if (hasParentCode) {
                    totalIndexList.add(rs.getRow());
                    totalReceivables += Double.valueOf(dataList.get(5).toString());
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
     * 导出逾期账期客户Excel
     *
     * @param response       HttpServletResponse
     * @param columnNameList 导出列名List
     * @param dataList       导出数据List
     * @param fileName       导出文件名，目前sheet页是相同名称
     * @throws IOException @Description
     */
    public void exportOverdue(HttpServletResponse response, List<String> columnNameList, List<List<Object>> dataList, String fileName, List<List<Integer>> totalList) throws IOException {
        Sheet sheet1 = new Sheet(1, 0);
        sheet1.setSheetName(fileName);
        sheet1.setAutoWidth(Boolean.TRUE);

        fileName = fileName + new Date().getTime();
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
                if (totalIndexList.get(0) != totalIndexList.get(totalIndexList.size() - 1)) {
                    writer.merge(totalIndexList.get(0), totalIndexList.get(totalIndexList.size() - 1), 4, 4);
                }
            }
        }

        writer.finish();
        out.flush();
        out.close();

    }


}
