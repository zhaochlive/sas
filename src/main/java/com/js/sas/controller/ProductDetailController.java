package com.js.sas.controller;

import com.js.sas.service.ProductDetailService;
import com.js.sas.utils.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("productDetail")
public class ProductDetailController {

    @Autowired
    private ProductDetailService productDetailService;

    @PostMapping("getPage")
    @ResponseBody
    public Object getPage(HttpServletRequest request){
        Map<String ,Object > result = new HashMap<>();
        Map<String ,String > map = new HashMap<>();
        try {
            if (StringUtils.isNotBlank(request.getParameter("startDate"))) {
                map.put("startDate", request.getParameter("startDate"));
            }
            if (StringUtils.isNotBlank(request.getParameter("endDate"))) {
                map.put("endDate", request.getParameter("endDate"));
            }
            if (StringUtils.isNotBlank(request.getParameter("companyname"))) {
                map.put("companyname", request.getParameter("companyname").trim());
            }
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
            result.put("rows", productDetailService.getPage(map));
            result.put("total", productDetailService.getCount(map));
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code",500);
            result.put("msg","请重试或联系系统管理员");
        }
        return result;
    }
    @PostMapping(value = "/download/excel")
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
        List<String > columnNameList = new ArrayList<>();
        columnNameList.add("产品ID");
        columnNameList.add("第三级分类名");
        columnNameList.add("商品名称");
        columnNameList.add("订购量");
        columnNameList.add("销售金额");
        columnNameList.add("卖家公司名称");
        columnNameList.add("材质");
        columnNameList.add("牌号");
        columnNameList.add("规格");
        columnNameList.add("品牌");
        columnNameList.add("印记");
        columnNameList.add("表面处理");
        columnNameList.add("包装方式");
        columnNameList.add("单位");
        columnNameList.add("重量(KG)");
        try {
            List<List<Object>> result = new ArrayList<>();
            List<Map<String,Object>> data = productDetailService.getPage(map);
            List<Object> objects ;
            for (Map<String,Object> order : data) {
                objects = new ArrayList<>();
                objects.add(order.get("id"));
                objects.add(order.get("level3"));
                objects.add(order.get("productname"));
                objects.add(order.get("num"));
                objects.add(order.get("sumprice"));
                objects.add(order.get("companyname"));
                objects.add(order.get("material"));
                objects.add(order.get("cardnum"));
                objects.add(order.get("stand"));
                objects.add(order.get("brand"));
                objects.add(order.get("mark"));
                objects.add(order.get("surfacetreatment"));
                objects.add(order.get("packagetype"));
                objects.add(order.get("unit"));
                objects.add(order.get("weight"));
                result.add(objects);
            }
            CommonUtils.exportByList(response, columnNameList, result, "产品详情");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
