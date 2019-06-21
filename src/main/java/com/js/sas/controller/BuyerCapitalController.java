package com.js.sas.controller;

import com.js.sas.entity.AccountsPayable;
import com.js.sas.service.BuyerCapitalService;
import com.js.sas.utils.CommonUtils;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(value = "查询对账单信息")
@Controller
@RequestMapping("/buyerCapital")
@Slf4j
public class BuyerCapitalController {

    @Autowired
    private BuyerCapitalService buyerCapitalService;

    @ApiOperation(value = "买家对账单所用的资金列表查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = false, paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "limit", value = "每页数量", required = false, paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "offset", value = "数据起始位", required = false, paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "invoiceName", value = "开票名称", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "companyname", value = "公司名称", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "userNo", value = "会员编号", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "userName", value = "会员全称", required = false, paramType = "query", dataType = "String"),
            //@ApiImplicitParam(name = "seller", value = "卖家id", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "startDate", value = "开始日期", required = false, paramType = "query", dataType = "date"),
            @ApiImplicitParam(name = "endDate", value = "结束日期", required = false, paramType = "query", dataType = "date"),
           /* @ApiImplicitParam(name = "sort", value = "排序字段", required = false, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "sortOrder", value = "排序规则", required = false, paramType = "query", dataType = "string")*/
    })
    @PostMapping("/customerStatement")
    @ResponseBody
    public Object listForAccount(@ApiParam @RequestBody Map<String, String> params) {
        for (String str : params.keySet()) {
            log.info("参数key : {} ,value {}", str, params.get(str));
        }
        return buyerCapitalService.getAccountsPayable(params);
    }

    @ApiIgnore
    @GetMapping(value = "/download/excel")
    public void download( HttpServletResponse response, HttpServletRequest request) {
        String userName = request.getParameter("userName");
        String userNo = request.getParameter("userNo");
        String invoiceName = request.getParameter("invoiceName");
        String companyname = request.getParameter("companyname");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        Map<String, String> params = new HashMap<>();
        if(StringUtils.isNotBlank(userName)){params.put("userName",userName);}
        if(StringUtils.isNotBlank(userNo)){params.put("userNo",userNo);}
        if(StringUtils.isNotBlank(invoiceName)){params.put("invoiceName",invoiceName);}
        if(StringUtils.isNotBlank(companyname)){params.put("companyname",companyname);}
        if(StringUtils.isNotBlank(startDate)){params.put("startDate",startDate);}
        if(StringUtils.isNotBlank(endDate)){params.put("endDate",endDate);}
            params.put("offset","0");
            params.put("limit","10000000");
        log.info("参数userName:{},userNo:{},invoiceName:{},companyname:{},startDate:{},endDate{}:", userName,userNo,invoiceName,companyname,startDate,endDate);
        try {
            Map<String, Object> map = buyerCapitalService.getAccountsPayable(params);
            List<AccountsPayable> accountsPayables = (List<AccountsPayable>) map.get("rows");
       /* Resource resource = new ClassPathResource("templates/filetemplates/buyer_capital_template.xlsx");
        File file = null;
        Workbook workbook = null;
            file = resource.getFile();
            if (file.getName().endsWith("xls")) {     //Excel&nbsp;2003
                workbook = new HSSFWorkbook(new FileInputStream(file));
            } else if (file.getName().endsWith("xlsx")) {    // Excel 2007/2010
                workbook = new XSSFWorkbook(new FileInputStream(file));
            }
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 0; i < accountsPayables.size(); i++) {
                AccountsPayable accountsPayable = accountsPayables.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(accountsPayable.getTradetime());
                row.createCell(2).setCellValue(accountsPayable.getOrderno());
                row.createCell(3).setCellValue(accountsPayable.getCapitalTypeName());
                row.createCell(4).setCellValue(accountsPayable.getDeliveryAmount() == null ? 0f : accountsPayable.getDeliveryAmount().floatValue());
                row.createCell(5).setCellValue(accountsPayable.getReceivingAmount() == null ? 0f : accountsPayable.getReceivingAmount().floatValue());
                row.createCell(6).setCellValue(accountsPayable.getOtherAmount() == null ? 0f : accountsPayable.getOtherAmount().floatValue());
                row.createCell(7).setCellValue(accountsPayable.getReceivableAccount() == null ? 0f : accountsPayable.getReceivableAccount().floatValue());
                row.createCell(8).setCellValue(accountsPayable.getInvoiceamount() == null ? 0f : accountsPayable.getInvoiceamount().floatValue());
                row.createCell(9).setCellValue(accountsPayable.getInvoicebalance() == null ? 0f : accountsPayable.getInvoicebalance().floatValue());
                row.createCell(10).setCellValue(accountsPayable.getPayno());
                row.createCell(11).setCellValue(accountsPayable.getRemark());
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String time = sdf.format(new Date());
            response.setHeader("Content-Disposition", "attachment;filename=File" + time + "结算用户对账单.xlsx");
            response.setContentType("application/vnd.ms-excel;charset=UTF-8");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Cache-Control", "no-cache");
//            response.setDateHeader("Expires", 0);

            OutputStream output;

            output = response.getOutputStream();
            BufferedOutputStream bufferedOutPut = new BufferedOutputStream(output);
            bufferedOutPut.flush();
            workbook.write(bufferedOutPut);
            bufferedOutPut.close();*/
            CommonUtils.export(response, accountsPayables, "结算客户对账单", new AccountsPayable());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
