package com.js.sas.service;

import com.js.sas.dto.OrderProductDTO;
import com.js.sas.dto.RegionalSalesDTO;
import com.js.sas.dto.SaleAmountDTO;
import com.js.sas.entity.OrderProductEntity;
import com.js.sas.repository.JsOrdersRepository;
import com.js.sas.repository.SaleAmountRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.Tuple;
import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private final EntityManager entityManager;

    public SalesService(SaleAmountRepository saleAmountRepository, JsOrdersRepository jsOrdersRepository, EntityManager entityManager) {
        this.saleAmountRepository = saleAmountRepository;
        this.jsOrdersRepository = jsOrdersRepository;
        this.entityManager = entityManager;
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
     * 区域销售额
     *
     * @return 区域销售额
     */
    public List<RegionalSalesDTO> getRegionalSales(String startCreateTime, String endCreateTime) {
        return jsOrdersRepository.getRegionalSales(startCreateTime, endCreateTime);
    }

    /**
     * 商品销售额统计
     *
     * @return 商品销售额统计
     */
    public List<Tuple> getProductValueOfSales(OrderProductDTO orderProductDTO) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = criteriaBuilder.createTupleQuery();
        Root<OrderProductEntity> root = cq.from(OrderProductEntity.class);

        Predicate predicate = criteriaBuilder.conjunction();
        // 开始时间
        if (StringUtils.isNotBlank(orderProductDTO.getStartCreateTime())) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("createTime"), orderProductDTO.getStartCreateTime()));
        }
        // 结束时间
        if (StringUtils.isNotBlank(orderProductDTO.getEndCreateTime())) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("createTime"), orderProductDTO.getEndCreateTime() + " 23:59:59"));
        }
        // 商品名称
        if (StringUtils.isNotBlank(orderProductDTO.getProductName())) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("productName").as(String.class), orderProductDTO.getProductName()));
        }
        // 一级分类
        if (StringUtils.isNotBlank(orderProductDTO.getClassOne())) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("classOne").as(String.class), orderProductDTO.getClassOne()));
        }
        // 二级分类
        if (StringUtils.isNotBlank(orderProductDTO.getClassTwo())) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("classTwo").as(String.class), orderProductDTO.getClassTwo()));
        }
        // 标准（三级分类）
        if (StringUtils.isNotBlank(orderProductDTO.getStandard())) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("standard").as(String.class), orderProductDTO.getStandard()));
        }
        // 规格
        if (StringUtils.isNotBlank(orderProductDTO.getMeasure())) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("measure").as(String.class), orderProductDTO.getMeasure()));
        }
        // 品牌
        if (StringUtils.isNotBlank(orderProductDTO.getBrand())) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("brand").as(String.class), orderProductDTO.getBrand()));
        }
        // 印记
        if (StringUtils.isNotBlank(orderProductDTO.getMark())) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("mark").as(String.class), orderProductDTO.getMark()));
        }
        // 材质
        if (StringUtils.isNotBlank(orderProductDTO.getMaterial())) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("material").as(String.class), orderProductDTO.getMaterial()));
        }
        // 牌号
        if (StringUtils.isNotBlank(orderProductDTO.getGrade())) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("grade").as(String.class), orderProductDTO.getGrade()));
        }
        // 表面处理
        if (StringUtils.isNotBlank(orderProductDTO.getSurface())) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("surface").as(String.class), orderProductDTO.getSurface()));
        }
        // 性能等级
        if (StringUtils.isNotBlank(orderProductDTO.getPerformanceLevel())) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("performanceLevel").as(String.class), orderProductDTO.getPerformanceLevel()));
        }

        Expression<BigDecimal> sum = criteriaBuilder.sum(root.get("amount").as(BigDecimal.class));

        Expression<Long> count = criteriaBuilder.countDistinct(root.get("orderNo"));

        cq.select(criteriaBuilder.tuple(count, sum)).where(predicate);

        return entityManager.createQuery(cq).getResultList();
    }

    /**
     * 销售区域统计
     *
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @param offset    偏移量
     * @param limit     数量
     * @return Map<String, Object>
     */
    public Map<String, Object> findOrderAreas(String startDate, String endDate, String province, String city, int offset, int limit) {
        HashMap<String, Object> result = new HashMap<>();

        StoredProcedureQuery store = this.entityManager.createNamedStoredProcedureQuery("findOrderAreas");

        store.setParameter("startDate", startDate);
        store.setParameter("endDate", endDate);
        store.setParameter("province", province);
        store.setParameter("city", city);
        store.setParameter("offsetNum", offset);
        store.setParameter("limitNum", limit);

        List orderAreasList = store.getResultList();

        result.put("rows", orderAreasList);
        result.put("total", store.getOutputParameterValue("totalNum"));

        return result;
    }


}
