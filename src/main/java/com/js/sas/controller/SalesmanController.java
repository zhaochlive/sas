package com.js.sas.controller;

import com.js.sas.entity.dto.SalesmanDTO;
import com.js.sas.service.SalesmanService;
import com.js.sas.utils.Result;
import com.js.sas.utils.ResultCode;
import com.js.sas.utils.ResultUtils;
import com.js.sas.utils.validate.groups.GetPartnerListByNameLike;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;

/**
 * 业务员
 * @Date 2019-9-11 10:58:05
 **/
@RestController
@Slf4j
@RequestMapping("/salesman")
public class SalesmanController {

    @Autowired
    private SalesmanService salesService;

    @ApiOperation(value = "根据名称模糊查询往来单位，必须设置数量", notes = "数据来源：用友；数据截止日期：昨天")
    @PostMapping("/getSalesmanByNameLikeLimit")
    public Result getPartnerByNameLikeLimit(@Validated(value = GetPartnerListByNameLike.class) SalesmanDTO partner, BindingResult result) {
        if (result.hasErrors()) {
            LinkedHashMap<Integer, String> errorMap = new LinkedHashMap<>();
            for (int index = 0; index < result.getFieldErrors().size(); index++) {
                errorMap.put(index + 1, result.getFieldErrors().get(index).getDefaultMessage());
            }
            return ResultUtils.getResult(ResultCode.参数错误, errorMap);
        }

        Page<SalesmanDTO> markerPage = salesService.findNameList(partner);

        return ResultUtils.getResult(ResultCode.成功, markerPage.getContent());
    }

}
