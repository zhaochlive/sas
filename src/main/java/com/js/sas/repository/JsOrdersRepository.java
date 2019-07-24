package com.js.sas.repository;

import com.js.sas.dto.RegionalSalesDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JsOrdersRepository extends JpaRepository<RegionalSalesDTO, Integer> {

    /**
     * 区域销售额
     *
     * @return 区域销售额
     */
    @Query(nativeQuery = true, value = "SELECT REPLACE(os.province,'省','') AS 'name', SUM(os.amount) / 10000 AS 'value',CONCAT(SUM( os.amount ) / ( SELECT SUM( amount ) from JS_Orders WHERE create_time >= '2019-01-01' AND create_time <= '2020-01-01' ) * 100, \"%\") AS 'percent'" +
            " FROM JS_Orders os WHERE os.create_time >= :startCreateTime AND os.create_time <= :endCreateTime GROUP BY os.province ORDER BY value DESC")
    List<RegionalSalesDTO> getRegionalSales(@Param(value = "startCreateTime") String startCreateTime, @Param(value = "endCreateTime") String endCreateTime);

}
