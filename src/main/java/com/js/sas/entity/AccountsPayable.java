package com.js.sas.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class AccountsPayable extends BaseRowModel implements Serializable {

    @ApiModelProperty(notes = "日期")
    @ExcelProperty(value = "日期", index = 0)
    private Date tradetime;
    @ApiModelProperty(notes = "交易编号")
    @ExcelProperty(value = "交易编号", index = 0)
    private String orderno;
    @ApiModelProperty(notes = "类别{0=消费1=充值2=退款3=提现4=授信5=授信还款6=违约金7=远期定金8=远期余款9=远期全款10=卖家违约金11=授信未出账单还款}")
    private int capitaltype;
    @ApiModelProperty(notes = "类别名称{0=消费1=充值2=退款3=提现4=授信5=授信还款6=违约金7=远期定金8=远期余款9=远期全款10=卖家违约金11=授信未出账单还款}")
    @ExcelProperty(value = "类别", index = 0)
    private String capitalTypeName;
    @ApiModelProperty(notes = "支付方式{0=支付宝1=微信2=银行卡3=余额4=授信}")
    private int paytype;
    @ApiModelProperty(notes = "支付单号")
    private String transactionid;
    @ApiModelProperty(notes = "发货金额")
    @ExcelProperty(value = "发货金额", index = 0)
    private BigDecimal deliveryAmount;
    @ApiModelProperty(notes = "收款金额")
    @ExcelProperty(value = "收款金额", index = 0)
    private BigDecimal receivingAmount;
    @ApiModelProperty(notes = "其他金额")
    @ExcelProperty(value = "其他金额", index = 0)
    private BigDecimal otherAmount;
    @ApiModelProperty(notes = "应收账款")
    @ExcelProperty(value = "应收款", index = 0)
    private BigDecimal receivableAccount;
    @ApiModelProperty(notes = "开票金额")
    @ExcelProperty(value = "开票金额", index = 0)
    private BigDecimal invoiceamount;
    @ApiModelProperty(notes = "发票结余")
    @ExcelProperty(value = "发票结余", index = 0)
    private BigDecimal invoicebalance;
    @ApiModelProperty(notes = "支付单号")
    @ExcelProperty(value = "支付单号", index = 0)
    private String payno;
    @ApiModelProperty(notes = "备注")
    @ExcelProperty(value = "备注", index = 0)
    private String remark;
    @ApiModelProperty(notes = "会员名称")
    private String  username;
    @ApiModelProperty(notes = "状态0=待处理1=成功2=失败3=待审核4=审核通过5=审核不通过")
    private int rechargestate;
    @ApiModelProperty(notes = "充值平台")
    private int rechargeperform;

    public int getRechargeperform() {
        return rechargeperform;
    }

    public void setRechargeperform(int rechargeperform) {
        this.rechargeperform = rechargeperform;
    }

    public String getPayno() {
        return payno;
    }

    public void setPayno(String payno) {
        this.payno = payno;
    }

    public Date getTradetime() {
        return tradetime;
    }

    public void setTradetime(Date tradetime) {
        this.tradetime = tradetime;
    }

    public String getOrderno() {
        return orderno;
    }

    public void setOrderno(String orderno) {
        this.orderno = orderno;
    }

    public int getCapitaltype() {
        return capitaltype;
    }

    public void setCapitaltype(int capitaltype) {
        this.capitaltype = capitaltype;
    }

    public int getPaytype() {
        return paytype;
    }

    public void setPaytype(int paytype) {
        this.paytype = paytype;
    }

    public String getTransactionid() {
        return transactionid;
    }

    public void setTransactionid(String transactionid) {
        this.transactionid = transactionid;
    }

    public BigDecimal getDeliveryAmount() {
        return deliveryAmount;
    }

    public void setDeliveryAmount(BigDecimal deliveryAmount) {
        this.deliveryAmount = deliveryAmount;
    }

    public BigDecimal getReceivingAmount() {
        return receivingAmount;
    }

    public void setReceivingAmount(BigDecimal receivingAmount) {
        this.receivingAmount = receivingAmount;
    }

    public BigDecimal getOtherAmount() {
        return otherAmount;
    }

    public void setOtherAmount(BigDecimal otherAmount) {
        this.otherAmount = otherAmount;
    }

    public BigDecimal getReceivableAccount() {
        return receivableAccount;
    }

    public void setReceivableAccount(BigDecimal receivableAccount) {
        this.receivableAccount = receivableAccount;
    }

    public BigDecimal getInvoiceamount() {
        return invoiceamount;
    }

    public void setInvoiceamount(BigDecimal invoiceamount) {
        this.invoiceamount = invoiceamount;
    }

    public BigDecimal getInvoicebalance() {
        return invoicebalance;
    }

    public void setInvoicebalance(BigDecimal invoicebalance) {
        this.invoicebalance = invoicebalance;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getRechargestate() {
        return rechargestate;
    }

    public void setRechargestate(int rechargestate) {
        this.rechargestate = rechargestate;
    }

    public String getCapitalTypeName() {
        return capitalTypeName;
    }

    public void setCapitalTypeName(String capitalTypeName) {
        this.capitalTypeName = capitalTypeName;
    }

    @Override
    public String toString() {
        return "AccountsPayable{" +
                "tradetime=" + tradetime +
                ", orderno='" + orderno + '\'' +
                ", capitaltype=" + capitaltype +
                ", capitalTypeName='" + capitalTypeName + '\'' +
                ", paytype=" + paytype +
                ", transactionid='" + transactionid + '\'' +
                ", deliveryAmount=" + deliveryAmount +
                ", receivingAmount=" + receivingAmount +
                ", otherAmount=" + otherAmount +
                ", receivableAccount=" + receivableAccount +
                ", invoiceamount=" + invoiceamount +
                ", invoicebalance=" + invoicebalance +
                ", remark='" + remark + '\'' +
                ", payno='" + payno + '\'' +
                ", username='" + username + '\'' +
                ", rechargestate=" + rechargestate +
                ", rechargeperform=" + rechargeperform +
                '}';
    }
}
