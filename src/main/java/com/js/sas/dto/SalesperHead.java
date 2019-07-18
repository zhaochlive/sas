package com.js.sas.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;

import java.io.Serializable;

public class SalesperHead extends BaseRowModel implements Serializable {
    @ExcelProperty(value = "业务员")
    private String waysalesman;
    @ExcelProperty(value = "订单号")
    private String orderno;
    @ExcelProperty(value = "下单时间")
    private String createtime;
    @ExcelProperty(value = "买家账号")
    private String membername;
    @ExcelProperty(value = "公司名称")
    private String companyname;
    @ExcelProperty(value = "订单总金额")
    private String totalprice;
    @ExcelProperty(value = "下单月份")
    private String month;
    @ExcelProperty(value = "比例")
    private String ratio;

    public String getWaysalesman() {
        return waysalesman;
    }

    public void setWaysalesman(String waysalesman) {
        this.waysalesman = waysalesman;
    }

    public String getOrderno() {
        return orderno;
    }

    public void setOrderno(String orderno) {
        this.orderno = orderno;
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    public String getMembername() {
        return membername;
    }

    public void setMembername(String membername) {
        this.membername = membername;
    }

    public String getCompanyname() {
        return companyname;
    }

    public void setCompanyname(String companyname) {
        this.companyname = companyname;
    }

    public String getTotalprice() {
        return totalprice;
    }

    public void setTotalprice(String totalprice) {
        this.totalprice = totalprice;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getRatio() {
        return ratio;
    }

    public void setRatio(String ratio) {
        this.ratio = ratio;
    }
}
