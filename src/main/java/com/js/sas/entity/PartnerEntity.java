package com.js.sas.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @ClassName Partner
 * @Description 往来单位
 * @Author zc
 * @Date 2019/6/13 09:44
 **/
@Entity
@Table(name = "YY_AA_Partner")
@Data
public class PartnerEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", nullable = false)
    private int id;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "parent_code", nullable = false)
    private String parentCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "receivables", nullable = false)
    private float receivables;

    @Column(name = "status", nullable = false)
    private char status;

    @Column(name = "payment_date", nullable = false)
    private int paymentDate;

    @Column(name = "payment_month", nullable = false)
    private int paymentMonth;

    @Column(name = "settlement_type", nullable = false)
    private char settlementType;

}
