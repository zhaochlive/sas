package com.js.sas.dto;

import com.js.sas.utils.validate.groups.GetPartnerListByNameLike;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @ClassName PartnerDTO
 * @Description 往来单位
 * @Author zc
 * @Date 2019/6/13 17:57
 **/
@Data
public class PartnerDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Size(groups = {GetPartnerListByNameLike.class}, max = 30, message = "名称超长")
    @ApiModelProperty(value = "名称", required = true, example = "公司名称")
    private String name;

    @Min(groups = {GetPartnerListByNameLike.class}, value = 1, message = "数量限制不能小于1")
    @ApiModelProperty(value = "查询数量，数量限制不能小于1", required = true, example = "10")
    private int limit;
}
