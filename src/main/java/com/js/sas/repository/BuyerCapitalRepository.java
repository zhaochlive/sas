package com.js.sas.repository;

import com.js.sas.entity.BuyerCapital;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;


@Repository
public interface BuyerCapitalRepository extends JpaRepository<BuyerCapital,Integer>, JpaSpecificationExecutor<BuyerCapital> {

    @Query(value = "select * from ", nativeQuery = true)
    List<BuyerCapital> selectBuyerCapitalByParam(Map<String, Object> params, Pageable pageable);

    @Query(value ="select * from buyer_capital where tradeno=?1 and  orderno =?2  and capitaltype in(15,16) limit 1", nativeQuery = true)
    BuyerCapital getBuyerCapitalByTradenoAndOrderno(String tradeno, String orderno);

}
