package com.js.sas.controller;

import com.js.sas.service.RepurchaseRateService;
import com.js.sas.utils.CommonUtils;
import com.js.sas.utils.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.*;

@Controller
@RequestMapping("repurchase")
@Slf4j
public class RepurchaseRateController {

    @Autowired
    private RepurchaseRateService repurchaseRateService;

    @PostMapping(value = "getColums")
    @ResponseBody
    public Object getColums(HttpServletRequest request){
        HashMap<String, String> hashMap = new HashMap<>();
        if(StringUtils.isNotBlank(request.getParameter("startDate"))){
            hashMap.put("startDate",request.getParameter("startDate"));
        }else{
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            hashMap.put("startDate",year+"-01-01");
        }
        if(StringUtils.isNotBlank(request.getParameter("endDate"))){
            hashMap.put("endDate",request.getParameter("endDate"));
        }else {
            hashMap.put("endDate",DateTimeUtils.convert(new Date(),DateTimeUtils.DATE_FORMAT));
        }
        List<String> colums = repurchaseRateService.getColums(hashMap);
        List<String> result = new ArrayList<>();
        for (String colum : colums) {
            result.add("下单次数_"+ colum);
            result.add("下单金额_"+ colum);
        }
        return  result;
    }

    @GetMapping(value = "getRepurchaseRate")
    @ResponseBody
    public Object RepurchaseRate(HttpServletRequest request){

        Map<String, String> map = new HashMap<>();
        if (StringUtils.isNotBlank(request.getParameter("limit"))) {
            map.put("limit", request.getParameter("limit"));
        } else {
            map.put("limit", "100");
        }
        if (StringUtils.isNotBlank(request.getParameter("offset"))) {
            map.put("offset", request.getParameter("offset"));
        } else {
            map.put("offset", "0");
        }
        if(StringUtils.isNotBlank(request.getParameter("startDate"))){
            map.put("startDate",request.getParameter("startDate"));
//        }else{
//            Calendar cal = Calendar.getInstance();
//            map.put("startDate",cal.get(Calendar.YEAR)+"-01-01");
        }
        if(StringUtils.isNotBlank(request.getParameter("endDate"))){
            map.put("endDate",request.getParameter("endDate"));
//        }else {
//            map.put("endDate",DateTimeUtils.convert(new Date(),DateTimeUtils.DATE_FORMAT));
        }
        if (StringUtils.isNotBlank(request.getParameter("companyname"))) {
            map.put("companyname", request.getParameter("companyname").trim());
        }
        if (StringUtils.isNotBlank(request.getParameter("mobile"))) {
            map.put("mobile", request.getParameter("mobile").trim());
        }
        if (StringUtils.isNotBlank(request.getParameter("username"))) {
            map.put("username", request.getParameter("username").trim());
        }
        if (StringUtils.isNotBlank(request.getParameter("sort"))) {
            map.put("sort", request.getParameter("sort").trim());
        }
        if (StringUtils.isNotBlank(request.getParameter("sortOrder"))) {
            map.put("sortOrder", request.getParameter("sortOrder").trim());
        }
        HashMap<String, Object> resultMap = new HashMap<>();
        List<Map<String,Object>> colums = repurchaseRateService.getRepurchaseRate(map);
        resultMap.put("total",repurchaseRateService.getCount(map) );
        resultMap.put("rows", colums);
        return  resultMap;
    }


    /**
     * 查询购买次数与购买次数的人员数量统计表格
     * @return
     */
    @GetMapping("countGroup")
    @ResponseBody
    public Object countGroup(){
        Map<String, Object> hashMap = new HashMap<>();
        List<Map<String, Object>> mapList = repurchaseRateService.countGroup();
        List<Integer> cot = new ArrayList<>();
        List<Integer> cut = new ArrayList<>();
        if (mapList!=null&&mapList.size()>0){
            for (Map<String, Object> map : mapList) {
                Integer cut1 = Integer.parseInt(map.get("cut").toString());
                Integer cot1 = Integer.parseInt(map.get("cot").toString());
                cot.add(cot1);
                cut.add(cut1);
            }
        }
        hashMap.put("cot",cot.toArray());
        hashMap.put("cut",cut.toArray());
        return hashMap;
    }

    @RequestMapping("/download/excel")
    public void downloadExcel(HttpServletResponse response) {
        try {
            List<Map<String, Object>> mapList = repurchaseRateService.countGroup();
            if (mapList != null && mapList.size() > 0) {

                HSSFWorkbook wb = new HSSFWorkbook();
                HSSFSheet sheet = wb.createSheet("学生档案表");
                HSSFRow row0 = sheet.createRow(0);
                HSSFRow row1 = sheet.createRow(1);
                row0.createCell(0).setCellValue("下单次数");
                row1.createCell(0).setCellValue("商家数量");

                for (int i = 0; i < mapList.size(); i++) {
                    Map<String, Object> map = mapList.get(i);
                    row0.createCell(i + 1).setCellValue(map.get("cut").toString());
                    row1.createCell(i + 1).setCellValue(map.get("cot").toString());
                }
                response.setContentType("application/vnd.ms-excel");
                response.setHeader("Content-Disposition", "attachment;filename*= UTF-8''" + URLEncoder.encode("订单次数客户统计", "UTF-8") + ".xlsx");
                OutputStream ouputStream = response.getOutputStream();
                wb.write(ouputStream);
                ouputStream.flush();
                ouputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @PostMapping(value = "/download/page")
    public void download(HttpServletResponse response, HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        map.put("limit", "9999999999");
        map.put("offset", "0");
        if (StringUtils.isNotBlank(request.getParameter("startDate"))) {
            map.put("startDate", request.getParameter("startDate"));
        }
        if (StringUtils.isNotBlank(request.getParameter("endDate"))) {
            map.put("endDate", request.getParameter("endDate"));
        }
        if (StringUtils.isNotBlank(request.getParameter("companyname"))) {
            map.put("companyname", request.getParameter("companyname").trim());
        }
        if (StringUtils.isNotBlank(request.getParameter("mobile"))) {
            map.put("mobile", request.getParameter("mobile").trim());
        }
        if (StringUtils.isNotBlank(request.getParameter("username"))) {
            map.put("username", request.getParameter("username").trim());
        }
        if (StringUtils.isNotBlank(request.getParameter("sort"))) {
            map.put("sort", request.getParameter("sort").trim());
        }
        if (StringUtils.isNotBlank(request.getParameter("sortOrder"))) {
            map.put("sortOrder", request.getParameter("sortOrder").trim());
        }
        List<String > columnNameList = new ArrayList<>();
        columnNameList.add("客户名称");
        columnNameList.add("手机号");
        columnNameList.add("公司名称");
        columnNameList.add("总下单量");
        columnNameList.add("首次下单时间");
        List<String> colums = repurchaseRateService.getColums(map);
        for (String colum : colums) {
            columnNameList.add("下单次数_"+ colum);
            columnNameList.add("下单金额_"+ colum);
        }

        try {
            List<List<Object>> result = new ArrayList<>();
            List<Map<String,Object>> data = repurchaseRateService.getRepurchaseRate(map);
            List<Object> objects =null;
            for (Map<String,Object> order : data) {
                objects = new ArrayList<>();
                objects.add(order.get("realname"));
                objects.add(order.get("mobile"));
                objects.add(order.get("companyname"));
                objects.add(order.get("总下单量"));
                objects.add(order.get("firstTime"));
                for (String s : colums) {
                    objects.add(order.get("下单次数_"+s));
                    objects.add(order.get("下单金额_"+s));
                }
                result.add(objects);
            }
            CommonUtils.exportByList(response, columnNameList, result, "复购率统计");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("repurchaseRateData")
    @ResponseBody
    public Object repurchaseRateData(HttpServletRequest request){
        Map<String, String> params = new HashMap<>();
        if (StringUtils.isNotBlank(request.getParameter("startDate"))) {
            params.put("startDate", request.getParameter("startDate"));
        }
        if (StringUtils.isNotBlank(request.getParameter("endDate"))) {
            params.put("endDate", request.getParameter("endDate"));
        }
        return repurchaseRateService.repurchaseRate(params);
    }
    @PostMapping("repurchaseRate")
    @ResponseBody
    public Object repurchaseRate(HttpServletRequest request){
        Map<String, String> params = new HashMap<>();
        if (StringUtils.isNotBlank(request.getParameter("startDate"))) {
            params.put("startDate", request.getParameter("startDate"));
        }
        if (StringUtils.isNotBlank(request.getParameter("endDate"))) {
            params.put("endDate", request.getParameter("endDate"));
        }
        Double moreThanOne = repurchaseRateService.getRepurchaseNum(">", 1, params);
        Double all = repurchaseRateService.getRepurchaseNum(">=", 1, params);

//        System.out.println(moreThanOne  + ""+all);
        return moreThanOne/all*100;
    }

}
