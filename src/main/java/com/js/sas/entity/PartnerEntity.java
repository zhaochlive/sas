package com.js.sas.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName Partner
 * @Description 往来单位
 * @Author zc
 * @Date 2019/6/13 09:44
 **/
@Entity
@Table(name = "YY_AA_Partner")
@Data
@EqualsAndHashCode(callSuper = true)
public class PartnerEntity extends BaseRowModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", nullable = false)
    private int id;

    // 序号
    @Column(name = "index", nullable = false)
    private int index;

    // 用友往来单位编码
    @ExcelProperty(value = "用友往来单位编码", index = 0)
    @Column(name = "code", nullable = false)
    private String code;

    // 账期来源单位编码
    @Column(name = "parent_code", nullable = false)
    private String parentCode;

    // 往来单位名称
    @ExcelProperty(value = "往来单位名称", index = 1)
    @Column(name = "name", nullable = false)
    private String name;

    // 应收款
    @Column(name = "receivables", nullable = false)
    private BigDecimal receivables;

    // 状态 0-有效 1-无效
    @Column(name = "status", nullable = false)
    private char status;

    // 账期日
    @Column(name = "payment_date", nullable = false)
    private int paymentDate;

    // 账期月
    @Column(name = "payment_month", nullable = false)
    private int paymentMonth;

    // 账期类型 1-现金客户 2-账期客户
    @Column(name = "settlement_type", nullable = false)
    private char settlementType;

    // 期初应收
    @Column(name = "opening_balance", nullable = false)
    private BigDecimal openingBalance;

    // 已收款总金额
    @Column(name = "amount_collected", nullable = false)
    private BigDecimal amountCollected;

    // 总发货金额
    @Column(name = "amount_delivery", nullable = false)
    private BigDecimal amountDelivery;

    // 客服，导出是业务员
    @ExcelProperty(value = "业务员", index = 2)
    @Column(name = "customer_service_staff", nullable = false)
    private String customerServiceStaff;

    // 业务员
    @Column(name = "salesman", nullable = false)
    private String salesman;

    @Column(name = "amount_today", nullable = false)
    private BigDecimal amountToday;

    @ExcelProperty(value = "逾期金额", index = 3)
    @Column(name = "receivables_before_today", nullable = false)
    private BigDecimal receivablesBeforeToday;

}
