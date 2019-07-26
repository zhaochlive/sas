package com.js.sas.dto;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * @ClassName AreaAmountDTO
 * @Description 区域销售额
 * @Author zc
 * @Date 2019/7/23 17:36
 **/
@Entity
@Data
public class AreaAmountDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    // 地区
    private String name;

    // 销售额
    private double value;

}
