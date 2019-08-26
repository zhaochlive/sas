package com.js.sas.entity;

import com.alibaba.excel.metadata.BaseRowModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @ClassName OrderAreasEntity
 * @Description 销售区域统计表
 * @Author zc
 * @Date 2019/8/2 10:40
 **/
@Entity
@NamedStoredProcedureQuery(name = "findOrderAreas", procedureName = "PROC_order_areas",
        resultClasses = {OrderAreasEntity.class},
        parameters = {
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "startDate", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "endDate", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "province", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "city", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "offsetNum", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "limitNum", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "totalNum", type = Integer.class)
        })
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderAreasEntity extends BaseRowModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private int id;

    private String province;

    private String city;

    private String totalAmount;

    private String overRate;

    private String onRate;

    private String orderCounts;

    private String memberCounts;

    private String newTotalAmount;

    private String overNewRate;

    private String onNewRate;

    private String newOrderCounts;

    private String newMemberCounts;

    private String buyAgainRate;

    private String overBuyAgainRate;

    private String onBuyAgainRate;

    private String amountPerOrder;

    private String overAmountPerOrderRate;

    private String onAmountPerOrderRate;

    private String SCSCounts;

    private String SYSCounts;

    private String MYSCounts;

    private String TBALBBCounts;

    private String otherCounts;
}
