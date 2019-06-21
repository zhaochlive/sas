package com.js.sas.repository;

import com.js.sas.entity.PartnerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @ClassName PartnerRepository
 * @Description 往来单位
 * @Author zc
 * @Date 2019/6/10 08:00
 **/
public interface IPartnerRepository extends JpaRepository<PartnerEntity, Integer> {

    /**
     * 模糊查询往来单位名称，返回限定数量数据。
     *
     * @param name
     * @param limit
     * @return
     */
    @Query(nativeQuery = true, value = "SELECT yap.id, yap.code, yap.parentCode, yap.name, yap.receivables, yap.status, yap.paymentDate, yap.paymentMonth, yap.settlementType " +
            "FROM YY_AA_Partner yap " +
            "WHERE yap.status = 0 " +
            "AND yap.name " +
            "LIKE %:name% LIMIT :limit")
    List<PartnerEntity> findByNameLikeValidLimit(@Param(value = "name") String name, @Param(value = "limit") int limit);

}
