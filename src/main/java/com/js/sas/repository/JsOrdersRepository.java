package com.js.sas.repository;

import com.js.sas.dto.AreaAmountDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JsOrdersRepository extends JpaRepository<AreaAmountDTO, Integer> {

    /**
     * 本年度各省销售额
     *
     * @return 各省销售额
     */
    @Query(nativeQuery = true, value = "SELECT REPLACE(os.province,'省','') AS 'name', ROUND(SUM(os.amount) / 10000,2) AS 'value' FROM JS_Orders os WHERE os.create_time >= '2019-01-01' AND os.create_time <= '2020-01-01' GROUP BY os.province")
    List<AreaAmountDTO> getProvinceOfSales();

}
