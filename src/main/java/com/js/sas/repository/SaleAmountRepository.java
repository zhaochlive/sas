package com.js.sas.repository;

import com.js.sas.entity.SaleAmountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @ClassName SaleAmountRepository
 * @Description 销货数据
 * @Author zc
 * @Date 2019/6/21 12:48
 **/
public interface SaleAmountRepository extends JpaRepository<SaleAmountEntity, Integer> {
    @Query(nativeQuery = true, value = "SELECT DATE_FORMAT( sasd.createTime, '%Y%m%d' )  AS 'id', 0 AS 'months', 0 AS 'years'," +
            " DATE_FORMAT( sasd.createTime, '%Y/%m/%d' ) AS 'days'," +
            " COUNT( sasd.id ) AS 'counts'," +
            " SUM( sasd.amount ) AS 'amount'" +
            " FROM" +
            " YY_SA_SaleDelivery sasd" +
            " GROUP BY" +
            " days, sasd.createTime" +
            " ORDER BY" +
            " days DESC" +
            " LIMIT :limit")
    List<SaleAmountEntity> getSaleAmountByDay(@Param(value = "limit") int limit);

}
