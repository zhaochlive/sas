package com.js.sas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import springfox.documentation.annotations.ApiIgnore;

/**
 * @ClassName SystemController
 * @Description 系统Controller
 * @Author zc
 * @Date 2019/6/18 15:00
 **/
@Controller
@ApiIgnore
public class SystemController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/settlementCustomerSummary")
    public String settlementCustomerSummary() {
        return "pages/finance/settlementSummary";
    }

    @GetMapping("/customerStatement")
    public String customerStatement() {
        return "pages/finance/customerStatement.html";
    }

    @GetMapping("/overdue")
    public String overdue() {
        return "pages/finance/overdue.html";
    }

    @GetMapping("/salesPerformance")
    public String salesPerformance() {
        return "pages/finance/salesPerformance.html";
    }

    @GetMapping("/productValueOfSales")
    public String productValueOfSales() {
        return "pages/sales/productValueOfSales.html";
    }

    @GetMapping("/customerOfNewOrders")
    public String customerOfNewOrders() {
        return "pages/finance/customerOfNewOrders.html";
    }

    // 区域销售额
    @GetMapping("/regionalSales")
    public String regionalSales() {
        return "pages/operations/regionalSales.html";
    }
}
