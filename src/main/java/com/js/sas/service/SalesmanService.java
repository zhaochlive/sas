package com.js.sas.service;

import com.js.sas.entity.dto.SalesmanDTO;
import com.js.sas.repository.SalesmanRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

/**
 * @ClassName PartnerService
 * @Description 业务员
 **/
@Service
@Slf4j
public class SalesmanService {

    @Autowired
    private SalesmanRepository salesmanRepository;

    public Page<SalesmanDTO> findNameList(SalesmanDTO salesman) {

        // 排序规则
        Sort sort = new Sort(Sort.Direction.ASC, "name");
        PageRequest pageRequest = PageRequest.of(0, salesman.getLimit(), sort);

        // 查询条件
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("name", ExampleMatcher.GenericPropertyMatchers.contains()) // 全模糊匹配
                .withIgnorePaths("id")
                .withIgnorePaths("limit");

        Example example = Example.of(salesman, matcher);

        return salesmanRepository.findAll(example, pageRequest);
    }

}
