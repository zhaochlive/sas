package com.js.sas.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @ClassName SettlementSummaryEntity
 * @Description 结算客户财务统计
 * @Author zc
 * @Date 2019/6/10 08:00
 **/
@Entity
@NamedStoredProcedureQuery(name = "getSettlementSummary", procedureName = "PROC_SettlementSummary_OnLineOffLine",
        resultClasses = {SettlementSummaryEntity.class},
        parameters = {
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "settlementName", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "channel", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "startDate", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "endDate", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "offsetNum", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "limitNum", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "sort", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "sortOrder", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "totalNum", type = Integer.class)
        })
@Data
@EqualsAndHashCode(callSuper = true)
public class SettlementSummaryEntity extends BaseRowModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", nullable = false)
    private int id;
    // 结算客户编码
    @ExcelProperty(value = "编码", index = 0)
    @Column(name = "code", nullable = false)
    private String code;
    // 结算客户名称
    @ExcelProperty(value = "结算客户名称", index = 0)
    @Column(name = "name", nullable = false)
    private String name;
    // 来源
    @ExcelProperty(value = "来源", index = 0)
    @Column(name = "channel", nullable = false)
    private String channel;
    // 期初余额
    @ExcelProperty(value = "期初", index = 0)
    @Column(name = "openingBalance", nullable = false)
    private float openingBalance;
    // 发货金额
    @ExcelProperty(value = "发货金额", index = 0)
    @Column(name = "deliveryAmount", nullable = false)
    private float deliveryAmount;
    // 收款金额
    @ExcelProperty(value = "收款金额", index = 0)
    @Column(name = "receivedAmount", nullable = false)
    private float receivedAmount;
    // 应收款
    @ExcelProperty(value = "应收款余额", index = 0)
    @Column(name = "receivables", nullable = false)
    private float receivables;
    // 已开票金额
    @ExcelProperty(value = "已开票金额", index = 0)
    @Column(name = "invoiceAmount", nullable = false)
    private float invoiceAmount;
    // 发票结余
    @ExcelProperty(value = "发票结余", index = 0)
    @Column(name = "invoiceBalance", nullable = false)
    private float invoiceBalance;
}
