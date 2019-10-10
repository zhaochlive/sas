package com.js.sas.service;

import com.js.sas.dto.OrderProductDTO;
import com.js.sas.dto.RegionalSalesDTO;
import com.js.sas.dto.SaleAmountDTO;
import com.js.sas.entity.OrderProductEntity;
import com.js.sas.repository.JsOrdersRepository;
import com.js.sas.repository.SaleAmountRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.Tuple;
import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
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
    @Autowired
    @Qualifier(value = "secodJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

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


    public List<Map<String, Object>> getSaleAmount(Map<String, String> params) {

        if(params!=null){
            List<Object> list = new ArrayList<>();
            StringBuilder sb = new StringBuilder("select count(DISTINCT(os.orderno)) cut,sum(price*num) totalprice from orderproduct op left join orders os on os.orderno = op.orderno");
            sb.append(" left join (SELECT pi.*,a.attribute 牙距,a.VALUE,b.attribute 公称直径,b.VALUE,c.attribute 长度,c.VALUE,d.attribute 外径,d.VALUE");
            sb.append(" ,e.attribute 厚度,e.VALUE from productinfo pi ");
            sb.append(" left join (select * from productattr  where attribute ='牙距' "+(params.containsKey("pitch")?"and value='"+params.get("pitch")+"'":"")+") a on pi.id = a.productid ");
            sb.append(" left join (select * from productattr  where attribute ='公称直径' "+(params.containsKey("nominalDiameter")?"and value='"+params.get("nominalDiameter")+"'":"")+") b on pi.id = b.productid ");
            sb.append(" left join (select * from productattr  where attribute ='长度' "+(params.containsKey("extent")?"and value='"+params.get("extent")+"'":"")+") c on pi.id = c.productid ");
            sb.append(" left join (select * from productattr  where attribute ='外径' "+(params.containsKey("outerDiameter")?"and value='"+params.get("outerDiameter")+"'":"")+") d on pi.id = d.productid ");
            sb.append(" left join (select * from productattr  where attribute ='厚度' "+(params.containsKey("thickness")?"and value='"+params.get("thickness")+"'":"")+") e on pi.id = e.productid ");
            sb.append(" ) pp on pp.id = op.pdid where os.orderstatus <>7 ");
            if (params.containsKey("startCreateTime")&&StringUtils.isNotBlank(params.get("startCreateTime"))){
                sb.append( "and os.createTime >= ?");
                Timestamp alarmStartTime = Timestamp.valueOf(params.get("startCreateTime") + " 00:00:00");
                list.add(alarmStartTime);
            }
            if (params.containsKey("endCreateTime")&&StringUtils.isNotBlank(params.get("endCreateTime"))){
                sb.append( "and os.createTime <= ?");
                Timestamp alarmStartTime = Timestamp.valueOf(params.get("endCreateTime") + " 23:59:59");
                list.add(alarmStartTime);
            }
            if (params.containsKey("productName")&&StringUtils.isNotBlank(params.get("productName"))){
                sb.append( "and op.pdname = ?");
                list.add(params.get("productName"));
            }
            if (params.containsKey("classOne")&&StringUtils.isNotBlank(params.get("classOne"))){
                sb.append( "and pi.level1 = ?");
                list.add(params.get("classOne"));
            }
            if (params.containsKey("classTwo")&&StringUtils.isNotBlank(params.get("classTwo"))){
                sb.append( "and pi.level2 = ?");
                list.add(params.get("classTwo"));
            }
            if (params.containsKey("classify")&&StringUtils.isNotBlank(params.get("classify"))){
                sb.append( "and op.classify = ?");
                list.add(params.get("classify"));
            }
            if (params.containsKey("standard")&&StringUtils.isNotBlank(params.get("standard"))){
                sb.append( "and op.standard = ?");
                list.add(params.get("standard"));
            }
            if (params.containsKey("brand")&&StringUtils.isNotBlank(params.get("brand"))){
                sb.append( "and op.brand = ?");
                list.add(params.get("brand"));
            }
            if (params.containsKey("mark")&&StringUtils.isNotBlank(params.get("mark"))){
                sb.append( "and op.mark = ?");
                list.add(params.get("mark"));
            }
            if (params.containsKey("material")&&StringUtils.isNotBlank(params.get("material"))){
                sb.append( "and op.material = ?");
                list.add(params.get("material"));
            }
            if (params.containsKey("grade")&&StringUtils.isNotBlank(params.get("grade"))){
                sb.append( "and op.gradeno = ?");
                list.add(params.get("grade"));
            }
            if (params.containsKey("surface")&&StringUtils.isNotBlank(params.get("surface"))){
                sb.append( "and pi.surfacetreatment = ?");
                list.add(params.get("surface"));
            }
            if (params.containsKey("store")&&StringUtils.isNotBlank(params.get("store"))){
                sb.append( "and op.storename = ?");
                list.add(params.get("store"));
            }
            if (params.containsKey("nominalDiameter")&&StringUtils.isNotBlank(params.get("nominalDiameter"))){
                sb.append( "and pp.公称直径 != ''");
            }
            if (params.containsKey("pitch")&&StringUtils.isNotBlank(params.get("pitch"))){
                sb.append( "and pp.牙距 != ''");
            }
            if (params.containsKey("extent")&&StringUtils.isNotBlank(params.get("extent"))){
                sb.append( "and pp.长度 != ''");
            }
            if (params.containsKey("outerDiameter")&&StringUtils.isNotBlank(params.get("outerDiameter"))){
                sb.append( "and pp.外径 != ''");
            }
            if (params.containsKey("thickness")&&StringUtils.isNotBlank(params.get("thickness"))){
                sb.append( "and pp.厚度 != ''");
            }
            System.out.println(sb.toString());
            return jdbcTemplate.queryForList(sb.toString(),list.toArray());

        }

        return null;
    }
}
