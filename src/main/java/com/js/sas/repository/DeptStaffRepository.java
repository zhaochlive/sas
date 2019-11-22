package com.js.sas.repository;

import com.js.sas.entity.DeptStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * @author ：zc
 * @date ：2019/11/22 10:09
 */
public interface DeptStaffRepository extends JpaRepository<DeptStaff, Integer> {

    /**
     * 清空表
     */
    @Modifying(clearAutomatically = true)
    @Query(nativeQuery = true, value = "TRUNCATE dept_staff")
    void deleteAll();

}
