package com.js.sas.entity;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name ="buyer_capital")
public class BuyerCapital {
    //主键id
    @Id
    @Column(name="id")
    private Integer id;
    //订单号
    @Column(name="orderno")
    private String orderNo;
    //交易编号
    @Column(name="tradeno")
    private String tradeNo;
    //类别0=消费1=充值2=退款3=提现4=授信5=授信还款6=违约金7=远期定金8=远期余款9=远期全款10=卖家违约金11=授信未出账单还款12=订单返利13=返利消费14=返利退款15拆零费16拆零费退款
    @Column(name="capitaltype")
    private Integer capitalType;
    //金额
    @Column(name="capital")
    private BigDecimal capital;
    //支付方式0=支付宝1=微信2=银行卡3=余额4=授信5-返利券
    @Column(name="paytype")
    private Integer payType;
    //会员id
    @Column(name="memberid")
    private Integer memberId;
    //公司名称
    @Column(name="companyname")
    private String companyName;
    //真实姓名
    @Column(name="member_realname")
    private String memberRealName;
    //会员名称
    @Column(name="member_username")
    private String memberUserName;
    //会员手机号
    @Column(name="member_mobile")
    private String memberMobile;
    //交易时间
    @Column(name="tradetime")
    private Date tradeTime;
    //发票抬头
    @Column(name="invoiceheadup")
    private String invoiceHeadUp;
    //备注
    @Column(name="remark")
    private String remark;
    //充值单号
    @Column(name="rechargenumber")
    private String rechargeNumber;
    //充值平台0=微信1=支付宝2=线下平台3=银行卡
    @Column(name="rechargeperform")
    private Integer rechargePerform;
    //提现单号
    @Column(name="presentationnumber")
    private String presentationNumber;
    //状态0=待处理1=成功2=失败3=待审核4=审核通过5=审核不通过
    @Column(name="rechargestate")
    private Integer rechargeState;
    //审核人员
    @Column(name="verify")
    private String verify;
    //审核人员
    @Column(name="operation")
    private String operation;
    //提现方式1=微信2=支付宝3=银行卡
    @Column(name="withdrawtype")
    private Integer withDrawType;
    //已出账单标记 0=未出 1=已出
    @Column(name="outbillstate")
    private Integer outBillState;
    //交易成功时间
    @Column(name="successtime")
    private Date successTime;
    //授信是否已还款 0=未还，1=已还
    @Column(name="isbackcredit")
    private Integer isBackCredit;
    //合同金额
    @Column(name = "allpay")
    private BigDecimal allPay;
    //会员备注
    @Column(name="membermark")
    private String memberMark;
    //审核备注
    @Column(name="validatemark")
    private String validateMark;
    //处理时间
    @Column(name="operatetime")
    private Date operateTime;
    //第三方支付交易号，比如像支付宝支付返回来的交易号
    @Column(name="transactionid")
    private String transactionId;
    //交易类型:0=消费(0,6,7,8,9,13,15)1=充值(1,5,10,12)2=退款(2,14,16)3=提现(3)
    @Column(name="tradetype")
    private Integer tradeType;
    //是否拥有拆零费 1 被拆零
    @Column(name="scattered")
    private Integer scattered;
    //拆零费金额
    @Column(name="scatteredcapital")
    private BigDecimal scatteredcapital;

    public Integer getScattered() {
        return scattered;
    }

    public void setScattered(Integer scattered) {
        this.scattered = scattered;
    }

    public BigDecimal getScatteredcapital() {
        return scatteredcapital;
    }

    public void setScatteredcapital(BigDecimal scatteredcapital) {
        this.scatteredcapital = scatteredcapital;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public Integer getCapitalType() {
        return capitalType;
    }

    public void setCapitalType(Integer capitalType) {
        this.capitalType = capitalType;
    }

    public BigDecimal getCapital() {
        return capital;
    }

    public void setCapital(BigDecimal capital) {
        this.capital = capital;
    }

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getMemberRealName() {
        return memberRealName;
    }

    public void setMemberRealName(String memberRealName) {
        this.memberRealName = memberRealName;
    }

    public String getMemberUserName() {
        return memberUserName;
    }

    public void setMemberUserName(String memberUserName) {
        this.memberUserName = memberUserName;
    }

    public String getMemberMobile() {
        return memberMobile;
    }

    public void setMemberMobile(String memberMobile) {
        this.memberMobile = memberMobile;
    }

    public Date getTradeTime() {
        return tradeTime;
    }

    public void setTradeTime(Date tradeTime) {
        this.tradeTime = tradeTime;
    }

    public String getInvoiceHeadUp() {
        return invoiceHeadUp;
    }

    public void setInvoiceHeadUp(String invoiceHeadUp) {
        this.invoiceHeadUp = invoiceHeadUp;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getRechargeNumber() {
        return rechargeNumber;
    }

    public void setRechargeNumber(String rechargeNumber) {
        this.rechargeNumber = rechargeNumber;
    }

    public Integer getRechargePerform() {
        return rechargePerform;
    }

    public void setRechargePerform(Integer rechargePerform) {
        this.rechargePerform = rechargePerform;
    }

    public String getPresentationNumber() {
        return presentationNumber;
    }

    public void setPresentationNumber(String presentationNumber) {
        this.presentationNumber = presentationNumber;
    }

    public Integer getRechargeState() {
        return rechargeState;
    }

    public void setRechargeState(Integer rechargeState) {
        this.rechargeState = rechargeState;
    }

    public String getVerify() {
        return verify;
    }

    public void setVerify(String verify) {
        this.verify = verify;
    }

    public Integer getWithDrawType() {
        return withDrawType;
    }

    public void setWithDrawType(Integer withDrawType) {
        this.withDrawType = withDrawType;
    }

    public Integer getOutBillState() {
        return outBillState;
    }

    public void setOutBillState(Integer outBillState) {
        this.outBillState = outBillState;
    }

    public Date getSuccessTime() {
        return successTime;
    }

    public void setSuccessTime(Date successTime) {
        this.successTime = successTime;
    }

    public Integer getIsBackCredit() {
        return isBackCredit;
    }

    public void setIsBackCredit(Integer isBackCredit) {
        this.isBackCredit = isBackCredit;
    }

    public BigDecimal getAllPay() {
        return allPay;
    }

    public void setAllPay(BigDecimal allPay) {
        this.allPay = allPay;
    }

    public String getMemberMark() {
        return memberMark;
    }

    public void setMemberMark(String memberMark) {
        this.memberMark = memberMark;
    }

    public String getValidateMark() {
        return validateMark;
    }

    public void setValidateMark(String validateMark) {
        this.validateMark = validateMark;
    }

    public Date getOperateTime() {
        return operateTime;
    }

    public void setOperateTime(Date operateTime) {
        this.operateTime = operateTime;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Integer getTradeType() {
        return tradeType;
    }

    public void setTradeType(Integer tradeType) {
        this.tradeType = tradeType;
    }

    @Override
    public String toString() {
        return "BuyerCapital{" +
                "id=" + id +
                ", orderNo='" + orderNo + '\'' +
                ", tradeNo='" + tradeNo + '\'' +
                ", capitalType=" + capitalType +
                ", capital=" + capital +
                ", payType=" + payType +
                ", memberId=" + memberId +
                ", companyName='" + companyName + '\'' +
                ", memberRealName='" + memberRealName + '\'' +
                ", memberUserName='" + memberUserName + '\'' +
                ", memberMobile='" + memberMobile + '\'' +
                ", tradeTime=" + tradeTime +
                ", invoiceHeadUp='" + invoiceHeadUp + '\'' +
                ", remark='" + remark + '\'' +
                ", rechargeNumber='" + rechargeNumber + '\'' +
                ", rechargePerform=" + rechargePerform +
                ", presentationNumber='" + presentationNumber + '\'' +
                ", rechargeState=" + rechargeState +
                ", verify='" + verify + '\'' +
                ", operation='" + operation + '\'' +
                ", withDrawType=" + withDrawType +
                ", outBillState=" + outBillState +
                ", successTime=" + successTime +
                ", isBackCredit=" + isBackCredit +
                ", allPay=" + allPay +
                ", memberMark='" + memberMark + '\'' +
                ", validateMark='" + validateMark + '\'' +
                ", operateTime=" + operateTime +
                ", transactionId='" + transactionId + '\'' +
                ", tradeType=" + tradeType +
                ", scattered=" + scattered +
                ", scatteredcapital=" + scatteredcapital +
                '}';
    }
}
