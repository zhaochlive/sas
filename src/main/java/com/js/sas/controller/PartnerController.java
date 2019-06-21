package com.js.sas.controller;

import com.js.sas.dto.PartnerDTO;
import com.js.sas.entity.PartnerEntity;
import com.js.sas.repository.IPartnerRepository;
import com.js.sas.utils.Result;
import com.js.sas.utils.ResultCode;
import com.js.sas.utils.ResultUtils;
import com.js.sas.utils.validate.groups.GetPartnerListByNameLike;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    @Autowired
    private IPartnerRepository iPartnerRepository;

    @ApiOperation(value = "根据名称模糊查询往来单位，必须设置数量", notes = "数据来源：用友；数据截止日期：昨天")
    @PostMapping("/getPartnerByNameLikeLimit")
    public Result getPartnerByNameLikeLimit(@Validated(value = GetPartnerListByNameLike.class) PartnerDTO partnerDTO, BindingResult result) {
        if (result.hasErrors()) {
            Map<Integer, String> errorMap = new LinkedHashMap();
            for (int index = 0; index < result.getFieldErrors().size(); index ++) {
                errorMap.put(index+1, result.getFieldErrors().get(index).getDefaultMessage());
            }
            return  ResultUtils.getResult(ResultCode.参数错误,errorMap);
        }

        // 往来单位名称
        partnerDTO.setName(Optional.ofNullable(partnerDTO.getName()).orElse(""));
        // 查询数量
        partnerDTO.setLimit(Optional.ofNullable(partnerDTO.getLimit()).orElse(10));

        List<PartnerEntity> partnerList = iPartnerRepository.findByNameLikeValidLimit(partnerDTO.getName(), partnerDTO.getLimit());

        return ResultUtils.getResult(ResultCode.成功, partnerList);
    }

}
