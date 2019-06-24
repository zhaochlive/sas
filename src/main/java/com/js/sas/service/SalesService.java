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
     * 日销售额列表
     *
     * @param limit 天数
     * @return 日销售列表
     */
    public List<SaleAmountEntity> getSaleAmountByDay(int limit) {
       return saleAmountRepository.getSaleAmountByDay(limit);
    }

    /**
     * 月销售额列表
     *
     * @param limit 月数
     * @return 月销售列表
     */
    public List<SaleAmountEntity> getSaleAmountByMonth(int limit) {
        return saleAmountRepository.getSaleAmountByMonth(limit);
    }

    /**
     * 年销售额列表
     *
     * @param limit 年数
     * @return 年销售列表
     */
    public List<SaleAmountEntity> getSaleAmountByYear(int limit) {
        return saleAmountRepository.getSaleAmountByYear(limit);
    }
}
