package com.js.sas.repository;

import com.js.sas.entity.DeptStaff;
import com.js.sas.entity.MemberSalesman;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * @author ：dxf
 * @date ：2019/12/26
 */
public interface MemberSalemanRepository extends JpaRepository<MemberSalesman, String > {

    /**
     * 清空表
     */
    @Modifying(clearAutomatically = true)
    @Query(nativeQuery = true, value = "TRUNCATE YY_Member_Saleman")
    void deleteAll();

}
