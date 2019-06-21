package com.js.sas.controller;

import com.js.sas.dto.SettlementSummaryDTO;
import com.js.sas.entity.SettlementSummaryEntity;
import com.js.sas.service.FinanceService;
import com.js.sas.utils.*;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

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

    @Autowired
    private FinanceService financeService;

    /**
     * 目前调用存储过程实现，存储过程很难维护，
     *
     * @param settlementSummasryDTO
     * @param result
     * @return
     */
    @ApiOperation(value = "结算客户对账单汇总（线上、线下）", notes = "数据来源：用友；数据截止日期：昨天")
    @PostMapping(value = "/settlementSummary")
    public Object settlementSummary(@Validated SettlementSummaryDTO settlementSummasryDTO, BindingResult result) {
        // 参数格式校验
        if (result.hasErrors()) {
            Map<String, String> errorMap = new LinkedHashMap();
            for (FieldError fieldError : result.getFieldErrors()) {
                errorMap.put(fieldError.getCode(), fieldError.getDefaultMessage());
            }
            return ResultUtils.getResult(ResultCode.参数错误, errorMap);
        }

        if ("desc".equals(settlementSummasryDTO.getSortOrder())) {
            settlementSummasryDTO.setSortOrder("asc");
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
    public void download(String name, String channel, String startDate, String endDate, String limit, HttpServletResponse httpServletResponse) {
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
            log.info("下载结算客户汇总（线上、线下）异常。");
            e.printStackTrace();
        }
    }

}
