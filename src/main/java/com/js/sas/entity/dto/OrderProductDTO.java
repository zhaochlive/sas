package com.js.sas.entity.dto;

import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * @ClassName OrderProductDTO
 * @Description 订单商品
 * @Author zc
 * @Date 2019/7/16 17:52
 **/
@Entity
@Data
public class OrderProductDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private int id;

    // 订单编号
    private String orderNo;

    // 开始时间
    private String startCreateTime;

    // 结束时间
    private String endCreateTime;

    // 商品名称
    private String productName;

    // 一级分类
    private String classOne;

    // 二级分类
    private String classTwo;

    // 标准（三级分类）
    private String standard;

    // 规格
    private String measure;

    // 品牌
    private String brand;

    // 印记
    private String mark;

    // 材质
    private String material;

    // 牌号
    private String grade;

    // 表面处理
    private String surface;

    // 性能等级
    private String performanceLevel;

}
