package com.js.sas.repository;

import com.js.sas.entity.dto.RegionalSalesDTO;
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
    @Query(nativeQuery = true, value = "SELECT 'province' AS 'level', '' AS 'province', '' AS 'city', REPLACE(os.province,'省','') AS 'name', SUM(os.amount) / 10000 AS 'value',CONCAT(SUM( os.amount ) / ( SELECT SUM( amount ) from JS_Orders WHERE create_time >= :startCreateTime AND create_time <= :endCreateTime ) * 100, \"%\") AS 'percent'" +
            " FROM JS_Orders os WHERE os.create_time >= :startCreateTime AND os.create_time <= :endCreateTime GROUP BY os.province ORDER BY value DESC")
    List<RegionalSalesDTO> getRegionalSales(@Param(value = "startCreateTime") String startCreateTime, @Param(value = "endCreateTime") String endCreateTime);

    /**
     * 市级区域销售额
     *
     * @return 区域销售额
     */
    @Query(nativeQuery = true, value = "SELECT 'city' AS 'level', :province AS 'province', '' AS 'city', REPLACE( os.city, '市', '' ) AS 'name', SUM(os.amount) / 10000 AS 'value',CONCAT(SUM( os.amount ) / ( SELECT SUM( amount ) from JS_Orders WHERE create_time >= :startCreateTime AND create_time <= :endCreateTime AND province like CONCAT('%',:province,'%') ) * 100, \"%\") AS 'percent' " +
            "FROM JS_Orders os WHERE os.create_time >= :startCreateTime AND os.create_time <= :endCreateTime AND os.province like CONCAT('%',:province,'%') GROUP BY os.city, province ORDER BY value DESC")
    List<RegionalSalesDTO> getRegionalSalesCity(@Param(value = "province") String province, @Param(value = "startCreateTime") String startCreateTime, @Param(value = "endCreateTime") String endCreateTime);

    /**
     * 区县级区域销售额
     *
     * @return 区域销售额
     */
    @Query(nativeQuery = true, value = "SELECT 'county' AS 'level', :province AS 'province', :city AS 'city', os.county AS 'name', SUM(os.amount) / 10000 AS 'value',CONCAT(SUM( os.amount ) / ( SELECT SUM( amount ) from JS_Orders WHERE create_time >= :startCreateTime AND create_time <= :endCreateTime AND province like CONCAT('%',:province,'%') AND city like CONCAT('%',:city ,'%') ) * 100, \"%\") AS 'percent' " +
            "FROM JS_Orders os WHERE os.create_time >= :startCreateTime AND os.create_time <= :endCreateTime AND os.province like CONCAT('%',:province,'%') AND os.city like CONCAT('%',:city ,'%') GROUP BY os.county, province, city ORDER BY value DESC")
    List<RegionalSalesDTO> getRegionalSalesCounty(@Param(value = "province") String province, @Param(value = "city") String city, @Param(value = "startCreateTime") String startCreateTime, @Param(value = "endCreateTime") String endCreateTime);

}
