package com.js.sas.controller;

import com.js.sas.service.CouponStrategyService;
import com.js.sas.utils.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("couponStrategy")
public class CouponStrategyController {

    @Autowired
    private CouponStrategyService couponStrategyService;

    @PostMapping("page")
    @ResponseBody
    public Object getPage(HttpServletRequest request){
        Map<String ,Object > result = new HashMap<>();
        try {
            Map<String ,String > map = new HashMap<>();

            if (StringUtils.isNotBlank(request.getParameter("limit"))) {
                map.put("limit", request.getParameter("limit"));
            } else {
                map.put("limit", "10");
            }
            if (StringUtils.isNotBlank(request.getParameter("offset"))) {
                map.put("offset", request.getParameter("offset"));
            } else {
                map.put("offset", "0");
            }
            if (StringUtils.isNotBlank(request.getParameter("startDate"))) {
                map.put("startDate", request.getParameter("startDate"));
            }
            if (StringUtils.isNotBlank(request.getParameter("endDate"))) {
                map.put("endDate", request.getParameter("endDate"));
            }
            if (StringUtils.isNotBlank(request.getParameter("ticket"))) {
                map.put("ticket", request.getParameter("ticket"));
            }
            result.put("rows",couponStrategyService.getData(map)) ;
            result.put("total",couponStrategyService.getCount(map)) ;
            result.put("code",200);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code",500);
            result.put("msg","请刷新重试");
        }
        return result;
    }

    @PostMapping(value = "/download/excel")
    public void download(HttpServletResponse response, HttpServletRequest request) {
        Map<String ,String > map = new HashMap<>();
        map.put("limit", "9999999999");
        map.put("offset", "0");
        if (StringUtils.isNotBlank(request.getParameter("startDate")))
        if (StringUtils.isNotBlank(request.getParameter("endDate"))) {
            map.put("endDate", request.getParameter("endDate"));
        }
        if (StringUtils.isNotBlank(request.getParameter("ticket"))) {
            map.put("ticket", request.getParameter("ticket"));
        }
        List<String > columnNameList = new ArrayList<>();
        columnNameList.add("优惠券");
        columnNameList.add("发放量");
        columnNameList.add("优惠券使用率");
        columnNameList.add("用券的订单总额");
        columnNameList.add("用券的优惠客单价");
        columnNameList.add("优惠券操作触发的使用人数");
        columnNameList.add("优惠券使用的新用户/比例");
        columnNameList.add("优惠券订单金额占总订单金额比例");
        columnNameList.add("优惠券总值");

        try {
            List<List<Object>> result = new ArrayList<>();
            List<Map<String,Object>> data =couponStrategyService.getData(map) ;
            List<Object> objects ;
            DecimalFormat df = new DecimalFormat("#.00");
            for (Map<String,Object> order : data) {
                Double useNum = order.get("使用量") == null ? 0 : Double.parseDouble(order.get("使用量").toString());
                Double issueNum = order.get("发放量") == null ? 0 : Double.parseDouble(order.get("发放量").toString());
                Double userNum = order.get("首次下单用户数量") == null ? 0 : Double.parseDouble(order.get("首次下单用户数量").toString());
                Double totalPay = order.get("付款总额") == null ? 0 : Double.parseDouble(order.get("付款总额").toString());
                Double discounts = order.get("优惠总额") == null ? 0 : Double.parseDouble(order.get("优惠总额").toString());
                objects = new ArrayList<>();
                objects.add(order.get("name"));
                objects.add(issueNum);
                String aa;
                if(useNum==0||issueNum==0){
                    aa ="-";
                }else {
                    aa = df.format((useNum/issueNum)*100)  +"%";
                }
                objects.add(aa);//优惠券使用率
                objects.add(order.get("订单总额"));
                objects.add((totalPay==0||useNum==0)?0:totalPay/useNum);
                objects.add(useNum);
                String bb ;
                if(userNum==0||issueNum==0){
                    bb = "-";
                }else{
                    bb = userNum+"/"+(userNum/issueNum*100)+"%";
                }
                objects.add(bb);
                String cc;
                if(discounts==null||totalPay==null||discounts==0||totalPay==0){
                    cc ="-";
                }else {
                    cc = df.format((discounts/totalPay)*100)  +"%";
                }
                objects.add(cc);
                objects.add(discounts);
                result.add(objects);
            }
            CommonUtils.exportByList(response, columnNameList, result, "优惠券定价策略");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
