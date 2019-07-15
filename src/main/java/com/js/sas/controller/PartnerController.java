package com.js.sas.controller;

import com.js.sas.dto.PartnerNameDTO;
import com.js.sas.service.PartnerService;
import com.js.sas.utils.Result;
import com.js.sas.utils.ResultCode;
import com.js.sas.utils.ResultUtils;
import com.js.sas.utils.validate.groups.GetPartnerListByNameLike;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;

/**
 * @ClassName PartnerController
 * @Description 往来单位Controller
 * @Author zc
 * @Date 2019/6/13 11:26
 **/
@RestController
@Slf4j
@RequestMapping("/partner")
public class PartnerController {

    private final PartnerService partnerService;

    public PartnerController(PartnerService partnerService) {
        this.partnerService = partnerService;
    }

    @ApiOperation(value = "根据名称模糊查询往来单位，必须设置数量", notes = "数据来源：用友；数据截止日期：昨天")
    @PostMapping("/getPartnerByNameLikeLimit")
    public Result getPartnerByNameLikeLimit(@Validated(value = GetPartnerListByNameLike.class) PartnerNameDTO partner, BindingResult result) {
        if (result.hasErrors()) {
            LinkedHashMap<Integer, String> errorMap = new LinkedHashMap<>();
            for (int index = 0; index < result.getFieldErrors().size(); index++) {
                errorMap.put(index + 1, result.getFieldErrors().get(index).getDefaultMessage());
            }
            return ResultUtils.getResult(ResultCode.参数错误, errorMap);
        }

        Page<PartnerNameDTO> markerPage = partnerService.findNameList(partner);

        return ResultUtils.getResult(ResultCode.成功, markerPage.getContent());
    }

}
