package com.js.sas.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName SaleDeliveryEntity
 * @Description 销货单
 * @Author zc
 * @Date 2019/6/21 11:53
 **/
@Entity
@Table(name = "YY_SA_SaleDelivery")
@Data
public class SaleDeliveryEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private int id;

    // 结算客户ID
    @Column(nullable = false)
    private int settlementId;

    // 总金额
    @Column(nullable = false)
    private float amount;

    // 创建时间
    @Column(nullable = false)
    private Date createTime;

    // 销售渠道
    @Column(nullable = false)
    private String channel;

    // 状态，0-无效 1-生效
    @Column(nullable = false)
    private char status;
}
