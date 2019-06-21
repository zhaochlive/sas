package com.js.sas.service;

import com.js.sas.entity.SaleAmountEntity;
import com.js.sas.repository.SaleAmountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName SalesService
 * @Description 销售Service
 * @Author zc
 * @Date 2019/6/21 16:51
 **/
@Service
@Slf4j
public class SalesService {

    private final SaleAmountRepository saleAmountRepository;

    public SalesService(SaleAmountRepository saleAmountRepository) {
        this.saleAmountRepository = saleAmountRepository;
    }

    /**
     * 获取日销售额列表
     *
     * @param limit 今天之前的天数
     * @return
     */
    public List<SaleAmountEntity> getSaleAmountByDay(int limit) {
       return saleAmountRepository.getSaleAmountByDay(limit);
    }
}
