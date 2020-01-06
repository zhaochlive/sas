package com.js.sas.entity.dto;

import com.js.sas.utils.validate.groups.GetPartnerListByNameLike;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @ClassName PartnerNameDTO
 * @Description 往来单位名称
 * @Author zc
 * @Date 2019/7/12 18:13
 **/
@Entity
@Table(name = "YY_AA_Partner")
@Data
public class PartnerNameDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @ApiModelProperty(value = "往来单位ID", example = "1")
    private int id;

    @Size(groups = {GetPartnerListByNameLike.class}, max = 30, message = "名称超长")
    @ApiModelProperty(value = "名称", required = true, example = "公司名称")
    private String name;

    @Min(groups = {GetPartnerListByNameLike.class}, value = 1, message = "数量限制不能小于1")
    @ApiModelProperty(value = "查询数量，数量限制不能小于1", required = true, example = "10")
    @Transient
    private int limit;
}
