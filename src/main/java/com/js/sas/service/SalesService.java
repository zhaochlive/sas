package com.js.sas.service;

import com.js.sas.dto.AreaAmountDTO;
import com.js.sas.dto.SaleAmountDTO;
import com.js.sas.repository.JsOrdersRepository;
import com.js.sas.repository.SaleAmountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final JsOrdersRepository jsOrdersRepository;

    public SalesService(SaleAmountRepository saleAmountRepository, JsOrdersRepository jsOrdersRepository) {
        this.saleAmountRepository = saleAmountRepository;
        this.jsOrdersRepository = jsOrdersRepository;
    }

    /**
     * 日销售额列表
     *
     * @param limit 天数
     * @return 日销售列表
     */
    public List<SaleAmountDTO> getSaleAmountByDay(int limit) {
       return saleAmountRepository.getSaleAmountByDay(limit);
    }

    /**
     * 月销售额列表
     *
     * @param limit 月数
     * @return 月销售列表
     */
    public List<SaleAmountDTO> getSaleAmountByMonth(int limit) {
        return saleAmountRepository.getSaleAmountByMonth(limit);
    }

    /**
     * 年销售额列表
     *
     * @param limit 年数
     * @return 年销售列表
     */
    public List<SaleAmountDTO> getSaleAmountByYear(int limit) {
        return saleAmountRepository.getSaleAmountByYear(limit);
    }

    /**
     * 各省销售额
     *
     * @return 各省销售额
     */
    public List<AreaAmountDTO> getProvinceOfSales(String startDate, String endDate) {
        return jsOrdersRepository.getProvinceOfSales();
    }


}
