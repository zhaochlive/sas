package com.js.sas.repository;

import com.js.sas.dto.OverdueDTO;
import com.js.sas.entity.PartnerEntity;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.math.BigDecimal;
import java.util.List;

/**
 * @ClassName PartnerRepository
 * @Description 往来单位
 * @Author zc
 * @Date 2019/6/10 08:00
 **/
public interface PartnerRepository extends JpaRepository<PartnerEntity, Integer>, JpaSpecificationExecutor<OverdueDTO> {

}
