package com.js.sas.repository;

import com.js.sas.entity.SaleDeliveryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @ClassName SaleDeliveryRepository
 * @Description 销货数据
 * @Author zc
 * @Date 2019/6/21 12:48
 **/
public interface SaleDeliveryRepository extends JpaRepository<SaleDeliveryEntity, Integer> {
    @Query(nativeQuery = true, value = "SELECT DATE_FORMAT( sasd.createTime, '%Y%m%d' ) days," +
            " COUNT( sasd.id )," +
            " SUM( sasd.amount )" +
            " FROM" +
            " YY_SA_SaleDelivery sasd" +
            " GROUP BY" +
            " days" +
            " ORDER BY" +
            " days DESC" +
            " LIMIT :limit")
    List<SaleDeliveryEntity> getSaleAmountByDay(@Param(value = "limit") int limit);

}
