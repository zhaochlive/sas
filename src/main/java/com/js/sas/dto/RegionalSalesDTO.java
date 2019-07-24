package com.js.sas.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * @ClassName RegionalSalesDTO
 * @Description 区域销售额
 * @Author zc
 * @Date 2019/7/24 11:29
 **/
@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class RegionalSalesDTO extends BaseRowModel implements  Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    // 地区
    @ExcelProperty(value = "地区", index = 0)
    private String name;

    // 销售额
    @ExcelProperty(value = "销售额（元）", index = 0)
    private double value;

    // 销售额占比
    @ExcelProperty(value = "占比（%）", index = 0)
    private String percent;

    // 开始时间 yyyy-MM-dd
    @NotNull(message = "日期参数不能为空")
    @Pattern(regexp = "(([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8]))))|((([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00))-02-29)",
            message = "请填写正确的日期YYYY-MM-DD")
    @Transient
    private String startCreateTime;

    // 结束时间 yyyy-MM-dd
    @NotNull(message = "日期参数不能为空")
    @Pattern(regexp = "(([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8]))))|((([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00))-02-29)",
            message = "请填写正确的日期YYYY-MM-DD")
    @Transient
    private String endCreateTime;

}
