package com.js.sas.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName OrdersEntity
 * @Description 紧商订单
 * @Author zc
 * @Date 2019/7/24 11:45
 **/
@Entity
@Table(name = "JS_Orders")
@Data
public class OrdersEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private int id;

    @Column(name = "order_no", nullable = false)
    private String orderNo;

    @Column(name = "create_time", nullable = false)
    private String createTime;

    private String province;

    private String city;

    private String county;

    private String address;

    private BigDecimal amount;

    private String status;

    @Column(name = "member_id", nullable = false)
    private int memberId;

    private String username;

    private String realname;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "visit_result", nullable = false)
    private String visitResult;

}
