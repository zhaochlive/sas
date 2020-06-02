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
        List<Facilitator> facilitatorCompany = facilitatorGoldsService.getFacilitatorCompany();
        for (Facilitator facilitator : facilitatorGoldsService.getFacilitatorCompany()) {
            if (StringUtils.isNotBlank(request.getParameter("startDate"))) {
                Date startDate = DateTimeUtils.convert(request.getParameter("startDate"));
//                if (startDate.getTime() > facilitator.getStartTime().getTime()){
                    facilitator.setStartTime(startDate);
//                }
            }
            if (StringUtils.isNotBlank(request.getParameter("endDate"))) {
                Date endDate = DateTimeUtils.convert(request.getParameter("endDate"));
//                if (endDate.getTime() < facilitator.getEndTime().getTime()){
                    facilitator.setEndTime(endDate);
//                }
            }
            Map map = facilitatorGoldsService.getFacilitatorGoldsInfoTotal(facilitator);
            list.add(map);
        }
        result.put("rows",list);
        result.put("total",facilitatorCompany.size());
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "getFacilitatorGoldInfo",method = RequestMethod.POST)
    public Object getFacilitatorGoldInfo(HttpServletRequest request){

        Integer limit = 20;
        Integer offset = 0;
        Map<String, Object> result = new HashMap<>();
        Facilitator facilitator = null;
        List<Facilitator> facilitatorCompany = facilitatorGoldsService.getFacilitatorCompany();
        if (StringUtils.isNotBlank(request.getParameter("facilitator"))) {
            for (Facilitator facilitator1 : facilitatorCompany) {
                if (request.getParameter("facilitator").equals(facilitator1.getName())){
                    facilitator = facilitator1;
                };
            }
        }
        if (facilitator==null){
            facilitator = facilitatorCompany.get(0);
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
//            if (startDate.getTime() > facilitator.getStartTime().getTime()){
                facilitator.setStartTime(startDate);
//            }
        }
        if (StringUtils.isNotBlank(request.getParameter("endDate"))) {
            Date endDate = DateTimeUtils.convert(request.getParameter("endDate"));
//            if (endDate.getTime() < facilitator.getEndTime().getTime()){
                facilitator.setEndTime(endDate);
//            }
        }
        System.out.println(facilitator);
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
        List<Map<String, Object>> mapList =new ArrayList<>();
        List<Facilitator> facilitatorCompany = facilitatorGoldsService.getFacilitatorCompany();
        for (Facilitator facilitator : facilitatorCompany) {
            if (StringUtils.isNotBlank(request.getParameter("startDate"))) {
                Date startDate = DateTimeUtils.convert(request.getParameter("startDate"));
//                if (startDate.getTime() > facilitator.getStartTime().getTime()){
                    facilitator.setStartTime(startDate);
//                }
            }
            if (StringUtils.isNotBlank(request.getParameter("endDate"))) {
                Date endDate = DateTimeUtils.convert(request.getParameter("endDate"));
//                if (endDate.getTime() < facilitator.getEndTime().getTime()){
                    facilitator.setEndTime(endDate);
//                }
            }
            Map total = facilitatorGoldsService.getFacilitatorGoldsInfoTotal(facilitator);
            mapList.add(total);
        }

        List<String > columnNameList = new ArrayList<>();
        columnNameList.add("服务商单位");
        columnNameList.add("奥展币");
        columnNameList.add("紧商币");
        columnNameList.add("订单总金额");
        columnNameList.add("返利");

        try {
            List<List<Object>> result = new ArrayList<>();
            List<Object> objects ;
            for (Map<String ,Object> item : mapList) {
                objects = new ArrayList<>();
                objects.add(item.get("name"));
                objects.add(item.get("奥展币"));
                objects.add(item.get("紧商币"));
                objects.add(item.get("订单总金额"));
                objects.add(item.get("返利"));
                result.add(objects);
            }
            CommonUtils.exportByList(response, columnNameList, result, "线下服务商返利汇总");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostMapping(value = "/excel/facilitatorInfo")
    public void facilitatorInfo(HttpServletResponse response, HttpServletRequest request) {

        Facilitator facilitator = null;
        List<Facilitator> facilitatorCompany = facilitatorGoldsService.getFacilitatorCompany();
        if (StringUtils.isNotBlank(request.getParameter("facilitator"))) {
            for (Facilitator facilitator1 : facilitatorCompany) {
                if (request.getParameter("facilitator").equals(facilitator1.getName())){
                    facilitator = facilitator1;
                };
            }
            if (StringUtils.isNotBlank(request.getParameter("startDate"))) {
                Date startDate = DateTimeUtils.convert(request.getParameter("startDate"));
//                if (startDate.getTime() > facilitator.getStartTime().getTime()){
                facilitator.setStartTime(startDate);
//                }
            }
            if (StringUtils.isNotBlank(request.getParameter("endDate"))) {
                Date endDate = DateTimeUtils.convert(request.getParameter("endDate"));
//                if (endDate.getTime() < facilitator.getEndTime().getTime()){
                facilitator.setEndTime(endDate);
//                }
            }
        }
        if (facilitator==null){
            return;
        }
        List<Map<String, Object>> facilitatorGoldInfo = facilitatorGoldsService.getFacilitatorGoldInfo(facilitator, 0, 999999999);

        List<String > columnNameList = new ArrayList<>();
        columnNameList.add("订单编号");
        columnNameList.add("商品名称");
        columnNameList.add("规格");
        columnNameList.add("材质");
        columnNameList.add("表面处理");
        columnNameList.add("标准");
        columnNameList.add("订单创建日期");
        columnNameList.add("返利");
        columnNameList.add("紧商币");
        columnNameList.add("牌号");
        columnNameList.add("奥展产品比例");
        columnNameList.add("奥展币");
        columnNameList.add("订单金额");
        try {
            List<List<Object>> result = new ArrayList<>();
            List<Object> objects ;
            for (Map<String ,Object> item : facilitatorGoldInfo) {
                objects = new ArrayList<>();
                objects.add(item.get("code"));
                objects.add(item.get("name"));
                objects.add(item.get("specification"));
                objects.add(item.get("priuserdefnvc1"));
                objects.add(item.get("priuserdefnvc3"));
                objects.add(item.get("priuserdefnvc10"));
                objects.add(item.get("createdtime"));
                objects.add(item.get("返利"));
                objects.add(item.get("紧商币"));
                objects.add(item.get("priuserdefnvc2"));
                objects.add(item.get("rate"));
                objects.add(item.get("奥展币"));
                objects.add(item.get("taxAmount"));
                result.add(objects);
            }
            CommonUtils.exportByList(response, columnNameList, result, facilitator.getName()+"线下返利明细");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
