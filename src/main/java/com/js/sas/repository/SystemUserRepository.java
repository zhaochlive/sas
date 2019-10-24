package com.js.sas.repository;

import com.js.sas.entity.SystemUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SystemUserRepository extends JpaRepository<SystemUser,Long> {

    @Transactional
    @Modifying
    @Query("update SystemUser set password=:password where id =:id")
    int upDatePassword(@Param("id")Long id,@Param("password")String password);
}
