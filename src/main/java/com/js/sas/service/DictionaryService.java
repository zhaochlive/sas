package com.js.sas.service;

import com.js.sas.entity.Dictionary;
import com.js.sas.repository.DictionaryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName DictionaryService
 * @Description 字典表Service
 * @Author zc
 * @Date 2019/8/3 15:46
 **/
@Service
@Slf4j
public class DictionaryService {
    private final DictionaryRepository dictionaryRepository;

    public DictionaryService(DictionaryRepository dictionaryRepository) {
        this.dictionaryRepository = dictionaryRepository;
    }

    public List<Dictionary> findByCode(String code) {
        return dictionaryRepository.findByCode(code);
    }

}
