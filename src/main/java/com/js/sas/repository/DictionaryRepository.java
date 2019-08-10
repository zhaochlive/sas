package com.js.sas.repository;

import com.js.sas.entity.Dictionary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DictionaryRepository extends JpaRepository<Dictionary, Integer> {
    public List<Dictionary> findByCode(String code);
}