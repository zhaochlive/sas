package com.js.sas.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @ClassName OverdueDTO
 * @Description 现金逾期客户
 * @Author zc
 * @Date 2019/7/12 15:02
 **/
@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "YY_AA_Partner")
public class OverdueDTO extends BaseRowModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private int id;

    @ApiModelProperty(value = "用友往来单位编码", example = "1")
    @ExcelProperty(value = "用友往来单位编码", index = 0)
    private String code;

    @ApiModelProperty(value = "结算客户名称", example = "结算客户")
    @ExcelProperty(value = "往来单位名称", index = 0)
    private String name;

    @ApiModelProperty(value = "逾期金额", example = "0")
    private String receivables;

    @ApiModelProperty(value = "状态 0-有效 1-无效", example = "0")
    private String status;

    @ApiModelProperty(value = "关联编码", example = "1")
    @Column(name = "parent_code", nullable = false)
    private String parentCode;

    @ApiModelProperty(value = "结算类型：1-现金客户 2-账期客户", example = "1")
    @Column(name = "settlement_type", nullable = false)
    private String settlementType;

    @ApiModelProperty(value = "截止昨日逾期金额", example = "0")
    @Column(name = "receivables_before_today", nullable = false)
    @ExcelProperty(value = "逾期金额", index = 0)
    private float receivablesBeforeToday;

    // 分页起始数量（偏移量）
    @Min(value = 0, message = "分页起始数量（偏移量）错误")
    @ApiModelProperty(value = "分页起始数量（偏移量）", required = true, example = "0")
    @Transient
    private int offset;

    // 每页数量
    @Min(value = 1, message = "每页数量错误")
    @ApiModelProperty(value = "每页数量", required = true, example = "20")
    @Transient
    private int limit;

    // 排序字段
    @NotNull(message = "排序字段错误")
    @ApiModelProperty(value = "排序字段", required = true)
    @Transient
    private String sort;

    // 排序方式
    @NotNull(message = "排序方式错误")
    @ApiModelProperty(value = "排序方式", required = true, allowableValues = "asc, desc")
    @Transient
    private String sortOrder;

}
