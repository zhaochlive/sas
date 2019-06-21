package com.js.sas.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

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
    private int id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String parentCode;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private float receivables;

    @Column(nullable = false)
    private char status;

    @Column(nullable = false)
    private int paymentDate;

    @Column(nullable = false)
    private int paymentMonth;

    @Column(nullable = false)
    private char settlementType;

}
