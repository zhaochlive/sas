package com.js.sas.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @ClassName OrderProductEntity
 * @Description 订单商品
 * @Author zc
 * @Date 2019/7/16 09:33
 **/
@Entity
@Table(name = "JS_Order_Product")
@Data
public class OrderProductEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", nullable = false)
    private int id;

    // 订单编号
    @Column(name = "order_no", nullable = false)
    private String orderNo;

    // 订单时间
    @Column(name = "create_time", nullable = false)
    private String createTime;

    // 商品信息id
    @Column(name = "product_info_id", nullable = false)
    private int productInfoId;

    // 商品名称
    @Column(name = "product_name", nullable = false)
    private String productName;

    // 一级分类
    @Column(name = "class_one", nullable = false)
    private String classOne;

    // 二级分类
    @Column(name = "class_two", nullable = false)
    private String classTwo;

    // 标准（三级分类）
    @Column(name = "standard", nullable = false)
    private String standard;

    // 规格
    @Column(name = "measure", nullable = false)
    private String measure;

    // 品牌
    @Column(name = "brand", nullable = false)
    private String brand;

    // 印记
    @Column(name = "mark", nullable = false)
    private String mark;

    // 材质
    @Column(name = "material", nullable = false)
    private String material;

    // 牌号
    @Column(name = "grade", nullable = false)
    private String grade;

    // 表面处理
    @Column(name = "surface", nullable = false)
    private String surface;

    // 性能等级
    @Column(name = "performance_level", nullable = false)
    private String performanceLevel;

    // 包装方式
    @Column(name = "packing", nullable = false)
    private String packing;

    // 金额
    @Column(name = "amount", nullable = false)
    private double amount;

    // 订单状态
    @Column(name = "order_status", nullable = false)
    private String orderStatus;

}
