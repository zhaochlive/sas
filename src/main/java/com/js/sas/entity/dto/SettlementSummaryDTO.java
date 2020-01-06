package com.js.sas.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @ClassName SettlementSummaryDTO
 * @Description 结算客户财务统计
 * @Author zhaoc
 * @Date 2019/6/14 12:25
 **/
@Data
public class SettlementSummaryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // 结算客户名称
    @ApiModelProperty(value = "结算客户名称", required = true, example = "结算客户")
    private String name;
    // 来源
    @Size(max = 2, message = "来源超长")
    @ApiModelProperty(value = "来源：线上、线下，空字符串为全部来源。", required = true, example = "线上")
    private String channel;
    // 开始时间 yyyy-MM-dd
    @Pattern(regexp = "(([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8]))))|((([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00))-02-29)",
            message = "请填写正确的日期YYYY-MM-DD")
    @ApiModelProperty(value = "开始时间，格式：YYYY-MM-DD", required = true)
    private String startDate;
    // 结束时间 yyyy-MM-dd
    @Pattern(regexp = "(([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8]))))|((([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00))-02-29)",
            message = "请填写正确的日期YYYY-MM-DD")
    @ApiModelProperty(value = "结束时间，格式：YYYY-MM-DD", required = true)
    private String endDate;
    // 分页起始数量（偏移量）
    @Min(value = 0, message = "分页起始数量（偏移量）错误")
    @ApiModelProperty(value = "分页起始数量（偏移量）", required = true, example = "0")
    private int offset;
    // 每页数量
    @Min(value = 1, message = "每页数量错误")
    @ApiModelProperty(value = "每页数量", required = true, example = "20")
    private int limit;
    // 排序字段
    @NotNull(message = "排序字段错误")
    @ApiModelProperty(value = "排序字段", required = true)
    private String sort;
    // 排序方式
    @NotNull(message = "排序方式错误")
    @ApiModelProperty(value = "排序方式", required = true, allowableValues = "asc, desc")
    private String sortOrder;

}
