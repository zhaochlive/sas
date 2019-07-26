package com.js.sas.controller;

import com.js.sas.dto.AreaAmountDTO;
import com.js.sas.dto.OrderProductDTO;
import com.js.sas.dto.SaleAmountDTO;
import com.js.sas.entity.OrderProductEntity;
import com.js.sas.service.SalesService;
import com.js.sas.utils.Result;
import com.js.sas.utils.ResultCode;
import com.js.sas.utils.ResultUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

/**
 * @ClassName SalesController
 * @Description 销售
 * @Author zc
 * @Date 2019/6/21 17:14
 **/
@Slf4j
@RestController
@RequestMapping("/sales")
public class SalesController {

    private final SalesService salesService;

    // @PersistenceContext
    private final EntityManager entityManager;

    public SalesController(SalesService salesService, EntityManager entityManager) {
        this.salesService = salesService;
        this.entityManager = entityManager;
    }

    /**
     * 日销售额
     *
     * @param limit 天数
     * @return Result，日销售额列表
     */
    @ApiOperation(value = "日销售额", notes = "数据来源：用友；数据截止日期：昨天")
    @PostMapping("/getSaleAmountByDay")
    public Result getSaleAmountByDay(int limit) {
        List<SaleAmountDTO> saleDeliveryList = salesService.getSaleAmountByDay(limit);
        return ResultUtils.getResult(ResultCode.成功, saleDeliveryList);
    }

    /**
     * 月销售额
     *
     * @param limit 月数
     * @return Result，月销售额列表
     */
    @ApiOperation(value = "月销售额", notes = "数据来源：用友；数据截止日期：昨天")
    @PostMapping("/getSaleAmountByMonth")
    public Result getSaleAmountByMonth(int limit) {
        List<SaleAmountDTO> saleDeliveryList = salesService.getSaleAmountByMonth(limit);
        return ResultUtils.getResult(ResultCode.成功, saleDeliveryList);
    }

    /**
     * 年销售额
     *
     * @param limit 年数
     * @return Result，年销售额列表
     */
    @ApiOperation(value = "年销售额", notes = "数据来源：用友；数据截止日期：昨天")
    @PostMapping("/getSaleAmountByYear")
    public Result getSaleAmountByYear(int limit) {
        List<SaleAmountDTO> saleDeliveryList = salesService.getSaleAmountByYear(limit);
        return ResultUtils.getResult(ResultCode.成功, saleDeliveryList);
    }

    /**
     * 本年度各省销售额
     *
     * @return Result，本年度各省销售额
     */
    @PostMapping("/getProvinceOfSales")
    public Object getProvinceOfSales() {
        List<AreaAmountDTO> provinceOfSalesList = salesService.getProvinceOfSales("2019-01-01", "2019-12-31 23:59:59");
        return ResultUtils.getResult(ResultCode.成功, provinceOfSalesList);
    }

    /**
     * 商品销售额
     *
     * @param orderProductDTO
     * @return Result，商品销售总额
     */
    @PostMapping("/getProductValueOfSales")
    public Object getProductValueOfSales(OrderProductDTO orderProductDTO) {
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
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("createTime"), orderProductDTO.getEndCreateTime() +" 23:59:59"));
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

        List<Tuple> resultList = entityManager.createQuery(cq).getResultList();

        HashMap[] rowsArray = new HashMap[1];
        HashMap data = new HashMap();

        data.put("count", resultList.get(0).get(0));
        data.put("amount", resultList.get(0).get(1));

        rowsArray[0] = data;

        HashMap<String, Object> result = new HashMap<>();
        result.put("rows", rowsArray);
        result.put("total", 1);

        return result;
    }

}
