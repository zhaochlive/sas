package com.js.sas.repository;

import com.js.sas.entity.SystemArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemAreaResprository extends JpaRepository<SystemArea, Integer> {

    public List<SystemArea> findAllByNameLike(String name);
}
