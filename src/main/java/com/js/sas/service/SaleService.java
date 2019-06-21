package com.js.sas.service;

import com.js.sas.entity.SaleDeliveryEntity;
import com.js.sas.repository.SaleDeliveryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName SaleService
 * @Description 销售Service
 * @Author zc
 * @Date 2019/6/21 16:51
 **/
@Service
@Slf4j
public class SaleService {

    private final SaleDeliveryRepository saleDeliveryRepository;

    public SaleService(SaleDeliveryRepository saleDeliveryRepository) {
        this.saleDeliveryRepository = saleDeliveryRepository;
    }

    public List<SaleDeliveryEntity> getSaleAmountByDay(int limit) {
       return saleDeliveryRepository.getSaleAmountByDay(limit);
    }
}
