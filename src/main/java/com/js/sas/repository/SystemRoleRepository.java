package com.js.sas.repository;

import com.js.sas.entity.SystemMenu;
import com.js.sas.entity.SystemRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemRoleRepository extends JpaRepository<SystemRole,Long> {

}
