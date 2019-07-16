package com.js.sas.service;

import com.js.sas.dto.PartnerNameDTO;
import com.js.sas.repository.PartnerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

/**
 * @ClassName PartnerService
 * @Description 往来单位Service
 * @Author zc
 * @Date 2019/7/12 17:31
 **/
@Service
@Slf4j
public class PartnerService {

    private final PartnerRepository partnerRepository;

    public PartnerService(PartnerRepository partnerRepository) {
        this.partnerRepository = partnerRepository;
    }

    public Page<PartnerNameDTO> findNameList(PartnerNameDTO partner) {

        // 排序规则
        Sort sort = new Sort(Sort.Direction.ASC, "name");
        PageRequest pageRequest = PageRequest.of(0, partner.getLimit(), sort);

        // 查询条件
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("name", ExampleMatcher.GenericPropertyMatchers.contains()) // 全模糊匹配
                .withIgnorePaths("id")
                .withIgnorePaths("limit");

        Example example = Example.of(partner, matcher);

        return partnerRepository.findAll(example, pageRequest);
    }

}
