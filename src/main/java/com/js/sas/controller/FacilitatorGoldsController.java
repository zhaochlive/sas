package com.js.sas.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.js.sas.entity.Facilitator;
import com.js.sas.entity.dto.CustomerOfOrder;
import com.js.sas.service.FacilitatorGoldsService;
import com.js.sas.utils.CommonUtils;
import com.js.sas.utils.DateTimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @Author: daniel
 * @date: 2020/5/13 0013 12:30
 * @Description:
 */
@Controller
@RequestMapping("/facilitator")
public class FacilitatorGoldsController {

    @Autowired
    private FacilitatorGoldsService facilitatorGoldsService;

    @ResponseBody
    @RequestMapping(value = "getFacilitatorGolds",method = RequestMethod.POST)
    public Object getFacilitatorGolds(HttpServletRequest request){
        Map<String, Object> result = new HashMap<>();
        List<Map> list = new ArrayList<>();
        for (Facilitator facilitator : FacilitatorGoldsService.facilitator) {
            if (StringUtils.isNotBlank(request.getParameter("startDate"))) {
                Date startDate = DateTimeUtils.convert(request.getParameter("startDate"));
                if (startDate.getTime() > facilitator.getStartTime().getTime()){
                    facilitator.setStartTime(startDate);
                }
            }
            if (StringUtils.isNotBlank(request.getParameter("endDate"))) {
                Date endDate = DateTimeUtils.convert(request.getParameter("endDate"));
                if (endDate.getTime() < facilitator.getEndTime().getTime()){
                    facilitator.setEndTime(endDate);
                }
            }
            Map map = facilitatorGoldsService.getFacilitatorGoldsInfoTotal(facilitator);
            list.add(map);
        }
        result.put("rows",list);
        result.put("total",FacilitatorGoldsService.facilitator.size());
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "getFacilitatorGoldInfo",method = RequestMethod.POST)
    public Object getFacilitatorGoldInfo(HttpServletRequest request){

        Integer limit = 20;
        Integer offset = 0;
        Map<String, Object> result = new HashMap<>();
        Facilitator facilitator = null;
        if (StringUtils.isNotBlank(request.getParameter("facilitator"))) {
            for (Facilitator facilitator1 : FacilitatorGoldsService.facilitator) {
                if (request.getParameter("facilitator").equals(facilitator1.getName())){
                    facilitator = facilitator1;
                };
            }
        }
        if (facilitator==null){
            facilitator = FacilitatorGoldsService.facilitator.get(0);
        }
        if (StringUtils.isNotBlank(request.getParameter("limit"))) {
            limit = Integer.parseInt(request.getParameter("limit"));
        }
        if (StringUtils.isNotBlank(request.getParameter("offset"))){
            offset = Integer.parseInt(request.getParameter("offset"));
        }
        //保证在服务期内
        if (StringUtils.isNotBlank(request.getParameter("startDate"))) {
            Date startDate = DateTimeUtils.convert(request.getParameter("startDate"));
            if (startDate.getTime() > facilitator.getStartTime().getTime()){
                facilitator.setStartTime(startDate);
            }
        }
        if (StringUtils.isNotBlank(request.getParameter("endDate"))) {
            Date endDate = DateTimeUtils.convert(request.getParameter("endDate"));
            if (endDate.getTime() < facilitator.getEndTime().getTime()){
                facilitator.setEndTime(endDate);
            }
        }

        result.put("total",facilitatorGoldsService.getFacilitatorGoldsInfoCount(facilitator));
        result.put("rows",facilitatorGoldsService.getFacilitatorGoldInfo(facilitator, offset, limit));
        return result;
    }

    /**
     * 查看奥展详情
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "facilitatorOrderInfo",method = RequestMethod.POST)
    public Object facilitatorOrderInfo(HttpServletRequest request){
        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isBlank(request.getParameter("orderNo"))){
            return null;
        }
        result.put("rows",facilitatorGoldsService.facilitatorOrderInfo(request.getParameter("orderNo")));
        result.put("total",100);
        return result;
    }
    @PostMapping(value = "/excel/facilitator")
    public void download(HttpServletResponse response, HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        map.put("limit", "9999999999");
        map.put("offset", "0");
        if (StringUtils.isNotBlank(request.getParameter("goldTypes"))) {
            map.put("goldType", request.getParameter("goldTypes"));
        }
        if (StringUtils.isNotBlank(request.getParameter("year"))) {
            map.put("year", request.getParameter("year"));
        }
        List<Map<String, Object>> mapList =null;
        String fileName=null;
        if ("jinshang".equals(request.getParameter("goldTypes").trim())){
            mapList = facilitatorGoldsService.getFacilitatorGoldsForJinShang(map);
            fileName="紧商金币汇总";
        }else if ("aozhan".equals(request.getParameter("goldTypes").trim())){
            fileName="奥展金币汇总";
            mapList = facilitatorGoldsService.getFacilitatorGoldsForAoZhan(map);
        }
        List<String > columnNameList = new ArrayList<>();
        columnNameList.add("服务商单位");
        columnNameList.add("合计数量");
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
            List<List<Object>> result = new ArrayList<>();
            List<Object> objects ;
            for (Map<String ,Object> item : mapList) {
                objects = new ArrayList<>();
                objects.add(item.get("companyName"));
                objects.add(item.get("total"));
                objects.add(item.get("一月"));
                objects.add(item.get("二月"));
                objects.add(item.get("三月"));
                objects.add(item.get("四月"));
                objects.add(item.get("五月"));
                objects.add(item.get("六月"));
                objects.add(item.get("七月"));
                objects.add(item.get("八月"));
                objects.add(item.get("九月"));
                objects.add(item.get("十月"));
                objects.add(item.get("十一月"));
                objects.add(item.get("十二月"));
                result.add(objects);
            }
            CommonUtils.exportByList(response, columnNameList, result, fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostMapping(value = "/excel/facilitatorInfo")
    public void facilitatorInfo(HttpServletResponse response, HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        map.put("limit", "9999999999");
        map.put("offset", "0");
        if (StringUtils.isNotBlank(request.getParameter("facilitator"))) {
            map.put("facilitator", request.getParameter("facilitator"));
        }
        if (StringUtils.isNotBlank(request.getParameter("goldType"))) {
            map.put("goldType", request.getParameter("goldType"));
        }
        List<Map<String, Object>> mapList =null;
        String fileName=null;
        if ("jinshang".equals(request.getParameter("goldType").trim())){
            mapList = facilitatorGoldsService.getFacilitatorGoldsInfoForJinShang(map);
            fileName="紧商金币汇总";
        }else if ("aozhan".equals(request.getParameter("goldType").trim())){
            fileName="奥展金币汇总";
            mapList = facilitatorGoldsService.getFacilitatorGoldsInfoForAoZhan(map);
        }

        List<String > columnNameList = new ArrayList<>();
        columnNameList.add("订单编号");
        columnNameList.add("订单创建日期");
        columnNameList.add("返还金币");
        columnNameList.add("订单金额");
        try {
            List<List<Object>> result = new ArrayList<>();
            List<Object> objects ;
            for (Map<String ,Object> item : mapList) {
                objects = new ArrayList<>();
                objects.add(item.get("orderno"));
                objects.add(item.get("createtime"));
                objects.add(item.get("golds"));
                objects.add(item.get("totalprice"));
                result.add(objects);
            }
            CommonUtils.exportByList(response, columnNameList, result, fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
