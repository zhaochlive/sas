package com.js.sas.controller;

import com.js.sas.service.CouponStrategyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;

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
        return "pages/operations/salesPerformance.html";
    }

    @GetMapping("/salesPerformanceCollect")
    public String salesPerformanceCollect() {
        return "pages/operations/salesPerformanceCollect.html";
    }

    @GetMapping("/productValueOfSales")
    public String productValueOfSales() {
        return "pages/sales/productValueOfSales.html";
    }

    @GetMapping("/repurchaseRate")
    public String repurchaseRate() {
        return "pages/operations/repurchaseRate.html";
    }

    @GetMapping("/customerOfNewOrders")
    public String customerOfNewOrders() {
        return "pages/finance/customerOfNewOrders.html";
    }

    @GetMapping("/customerCountGroup")
    public String customerCountGroup() {
        return "pages/operations/customerCountGroup.html";
    }
    @GetMapping("/orderByCustomerService")
    public String orderByCustomerService() {
        return "pages/operations/orderByCustomerService.html";
    }
    @GetMapping("/storeDetail")
    public String storeDetail() {
        return "pages/operations/storeDetail.html";
    }
    @GetMapping("/storeInfo")
    public String storeInfo() {
        return "pages/operations/storeInfo.html";
    }

    @Autowired
    private CouponStrategyService couponStrategyService;
    //优惠策略
    @GetMapping("/couponStrategy")
    public ModelAndView couponStrategy() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("tickets",couponStrategyService.getTickets());
        modelAndView.setViewName("pages/operations/couponStrategy.html");
        return modelAndView;
    }
    //产品详情
    @GetMapping("/productDetail")
    public String productDetail(HttpServletRequest request, Model model) {
        model.addAttribute("startDate",request.getParameter("startDate"));
        model.addAttribute("endDate",request.getParameter("endDate"));
        return "pages/operations/productDetail.html";
    }

    // 区域销售额
    @GetMapping("/regionalSales")
    public String regionalSales(HttpServletRequest request, Model model) {
        model.addAttribute("startDate",request.getParameter("startDate"));
        model.addAttribute("endDate",request.getParameter("endDate"));
        return "pages/operations/regionalSales.html";
    }
    //订单主表
    @GetMapping("/orders")
    public String orders(HttpServletRequest request, Model model) {
        return "pages/operations/orders.html";
    }
    //订单详情
    @GetMapping("/orderDetail")
    public String orderDetail(HttpServletRequest request, Model model) {
        model.addAttribute("startDate",request.getParameter("startDate"));
        model.addAttribute("endDate",request.getParameter("endDate"));
        return "pages/operations/orderDetail.html";
    }
    //订单产品
    @GetMapping("/orderProduct")
    public String orderProduct(HttpServletRequest request, Model model) {
        model.addAttribute("startDate",request.getParameter("startDate"));
        model.addAttribute("endDate",request.getParameter("endDate"));
        return "pages/operations/orderProduct.html";
    }
    //订单拆分详情
    @GetMapping("/orderSplitDetail")
    public String orderSplitDetail(HttpServletRequest request, Model model) {
        model.addAttribute("startDate",request.getParameter("startDate"));
        model.addAttribute("endDate",request.getParameter("endDate"));
        return "pages/operations/orderSplitDetail.html";
    }

    //订单退单详情
    @GetMapping("/orderBackDetail")
    public String  orderBackDetail(HttpServletRequest request, Model model) {
        model.addAttribute("startDate",request.getParameter("startDate"));
        model.addAttribute("endDate",request.getParameter("endDate"));
        return "pages/operations/orderBackDetail.html";
    }
    //未发货订单详情
    @GetMapping("/unsentOrderDetail")
    public String unsentOrderDetail(HttpServletRequest request, Model model) {
        model.addAttribute("startDate",request.getParameter("startDate"));
        model.addAttribute("endDate",request.getParameter("endDate"));
        return "pages/operations/unsentOrderDetail.html";
    }
    //客单价订单
    @GetMapping("/unitPriceOrder")
    public String unitPriceOrder(HttpServletRequest request, Model model) {
        model.addAttribute("startDate",request.getParameter("startDate"));
        model.addAttribute("endDate",request.getParameter("endDate"));
        return "pages/operations/unitPriceOrder.html";
    }
    //退货店铺及仓库
    @GetMapping("/backOrderOfStore")
    public String backOrderOfStore(HttpServletRequest request, Model model) {
        model.addAttribute("startDate",request.getParameter("startDate"));
        model.addAttribute("endDate",request.getParameter("endDate"));
        return "pages/operations/backOrderOfStore.html";
    }
    //退货店铺及仓库
    @GetMapping("/backOrderOfSeller")
    public String backOrderOfSeller(HttpServletRequest request, Model model) {
        model.addAttribute("startDate",request.getParameter("startDate"));
        model.addAttribute("endDate",request.getParameter("endDate"));
        return "pages/operations/backOrderOfSeller.html";
    }

    // 区域销售额
    @GetMapping("/orderAreas")
    public String orderAreas() {
        return "pages/operations/orderAreas.html";
    }

    // 用友对账单
    @GetMapping("/yongyouStatement")
    public String yongyouStatement() {
        return "pages/finance/yongyouStatement.html";
    }

    // 紧商订单信息
    @GetMapping("/ordersInfo")
    public String ordersInfo() {
        return "pages/sales/orderInfo.html";
    }

    // 紧商订单详情列表
    @GetMapping("/ordersList")
    public String ordersList() {
        return "pages/sales/orderList.html";
    }

    // 用友订单信息
    @GetMapping("/yyOrdersInfo")
    public String yyOrdersInfo() {
        return "pages/sales/yyOrdersInfo.html";
    }

    // 品牌销售情况
    @GetMapping("/brandSales")
    public String brandSales() {
        return "pages/sales/brandSales.html";
    }

    // 客服销售统计
    @GetMapping("/clerkManSales")
    public String clerkManSales() {
        return "pages/sales/clerkManSales.html";
    }
    // 客户结算统计
    @GetMapping("/settlementCustomer")
    public String settlementCustomer() {
        return "pages/sales/settlementCustomer.html";
    }
}
