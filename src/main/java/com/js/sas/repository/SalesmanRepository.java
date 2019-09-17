package com.js.sas.repository;

import com.js.sas.entity.SalesmanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesmanRepository extends JpaRepository<SalesmanEntity,Integer> {
}
