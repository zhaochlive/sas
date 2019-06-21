package com.js.sas.repository;

import com.js.sas.entity.SettlementSummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @ClassName ISettlementSummaryRepository
 * @Description 结算客户财务统计
 * @Author zc
 * @Date 2019/6/10 08:00
 **/
public interface ISettlementSummaryRepository extends JpaRepository<SettlementSummaryEntity, Integer> {

}
