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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCountCallbackHandler;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.Tuple;
import javax.persistence.criteria.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

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


    public Map<String, Object> findMonthlySalesAmount(String startMonth, String endMonth, String username, String staff, String address, int limit, int offset, String sort, String sortOrder) {
        // 格式化月份
        String[] startArray = startMonth.split("-");
        String[] endArray = endMonth.split("-");

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(Integer.parseInt(startArray[0]), Integer.parseInt(startArray[1]) - 1, 1);

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.set(Integer.parseInt(endArray[0]), Integer.parseInt(endArray[1]) - 1, 1);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");

        // 参数List
        List paras = new ArrayList<>();
        // 拼接sql
        StringBuilder sb = new StringBuilder("SELECT tm.username, MAX(tm.invoice_head) AS invoice_head, tm.customer_service_staff, MAX(tm.address) AS address,");
        Calendar calendar = Calendar.getInstance();
        calendar.set(2017, 12, 31, 0, 0, 0);
        while (!endCalendar.before(startCalendar)) {
            sb.append(" SUM ( CASE WHEN tm.months = '" + simpleDateFormat.format(endCalendar.getTime()) + "' THEN tm.monthly_amount ELSE 0 END ) AS \"" + simpleDateFormat.format(endCalendar.getTime()) + "\",");
            if (endCalendar.before(calendar)) {
                break;
            }
            // 减一个月
            endCalendar.add(Calendar.MONTH, -1);
        }
        if (sb.toString().endsWith(",")) {
            // 去掉最后一位的逗号
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append(" FROM(SELECT TO_CHAR( os.createtime, 'YYYY-MM' ) AS months,mb.username, MAX(bc.invoiceheadup) as invoice_head, mb.clerkname AS customer_service_staff, MAX(bc.address) AS address, SUM ( os.totalprice ) AS monthly_amount FROM orders os ");
        sb.append(" LEFT JOIN MEMBER mb ON os.memberid = mb.ID LEFT JOIN billingrecord bc ON cast(os.id as varchar) = bc.orderno WHERE os.orderstatus <> 7 AND os.createtime >= ? ");

        paras.add(Timestamp.valueOf(simpleDateFormat.format(startCalendar.getTime()) + "-01 00:00:00"));

        if (StringUtils.isNotBlank(username)) {
            sb.append(" AND mb.username = ? ");
            paras.add(username);
        }
        if (StringUtils.isNotBlank(staff)) {
            sb.append(" AND mb.clerkname = ? ");
            paras.add(staff);
        }
        if (StringUtils.isNotBlank(address)) {
            sb.append(" AND bc.address ilike ? ");
            paras.add("%" + address + "%");
        }
        sb.append(" GROUP BY months, mb.username, customer_service_staff) tm GROUP BY tm.username, tm.customer_service_staff ORDER BY tm.username limit ? offset ? ");

        paras.add(limit);
        paras.add(offset);

        List dataList = jdbcTemplate.queryForList(sb.toString(), paras.toArray());

        // 总数
        paras = new ArrayList();
        StringBuilder countSql = new StringBuilder("SELECT COUNT(1) FROM ( SELECT tm.username FROM ( SELECT TO_CHAR( os.createtime, 'YYYY-MM' ) AS months, mb.username, MAX ( bc.invoiceheadup ) AS invoice_head, mb.clerkname AS customer_service_staff, MAX ( bc.address ) AS address, SUM ( os.totalprice ) AS monthly_amount FROM orders os LEFT JOIN MEMBER mb ON os.memberid = mb.ID LEFT JOIN billingrecord bc ON CAST ( os.ID AS VARCHAR ) = bc.orderno WHERE os.orderstatus <> 7 AND os.createtime >= ? ");
        paras.add(Timestamp.valueOf(simpleDateFormat.format(startCalendar.getTime()) + "-01 00:00:00"));
        if (StringUtils.isNotBlank(username)) {
            countSql.append(" AND mb.username = ? ");
            paras.add(username);
        }
        if (StringUtils.isNotBlank(staff)) {
            countSql.append(" AND mb.clerkname = ? ");
            paras.add(staff);
        }
        if (StringUtils.isNotBlank(address)) {
            countSql.append(" AND bc.address like ? ");
            paras.add("%" + address + "%");
        }
        countSql.append("GROUP BY months, mb.username, customer_service_staff ) tm GROUP BY tm.username, tm.customer_service_staff ) t");

        int total = jdbcTemplate.queryForObject(countSql.toString(), Integer.class, paras.toArray());

        HashMap<String, Object> result = new HashMap<>();

        result.put("rows", dataList);
        result.put("total", total);

        return result;
    }

    public Map<String, Object> findMonthlySalesAmountAll(String startMonth, String endMonth, String username, String staff, String address) {
        // 格式化月份
        String[] startArray = startMonth.split("-");
        String[] endArray = endMonth.split("-");

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(Integer.parseInt(startArray[0]), Integer.parseInt(startArray[1]) - 1, 1);

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.set(Integer.parseInt(endArray[0]), Integer.parseInt(endArray[1]) - 1, 1);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");

        // 参数List
        List paras = new ArrayList<>();
        // 拼接sql
        StringBuilder sb = new StringBuilder("SELECT tm.username AS 客户名称, MAX(tm.invoice_head) AS 开票名称, tm.customer_service_staff AS 客服, MAX(tm.address) AS 地址,");
        Calendar calendar = Calendar.getInstance();
        calendar.set(2017, 12, 31, 0, 0, 0);
        while (!endCalendar.before(startCalendar)) {
            sb.append(" SUM ( CASE WHEN tm.months = '" + simpleDateFormat.format(endCalendar.getTime()) + "' THEN tm.monthly_amount ELSE 0 END ) AS \"" + simpleDateFormat.format(endCalendar.getTime()) + "\",");
            if (endCalendar.before(calendar)) {
                break;
            }
            // 减一个月
            endCalendar.add(Calendar.MONTH, -1);
        }
        if (sb.toString().endsWith(",")) {
            // 去掉最后一位的逗号
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append(" FROM(SELECT TO_CHAR( os.createtime, 'YYYY-MM' ) AS months,mb.username, MAX(bc.invoiceheadup) as invoice_head, mb.clerkname AS customer_service_staff, MAX(bc.address) AS address, SUM ( os.totalprice ) AS monthly_amount FROM orders os ");
        sb.append(" LEFT JOIN MEMBER mb ON os.memberid = mb.ID LEFT JOIN billingrecord bc ON cast(os.id as varchar) = bc.orderno WHERE os.orderstatus <> 7 AND os.createtime >= ? ");

        paras.add(Timestamp.valueOf(simpleDateFormat.format(startCalendar.getTime()) + "-01 00:00:00"));

        if (StringUtils.isNotBlank(username)) {
            sb.append(" AND mb.username = ? ");
            paras.add(username);
        }
        if (StringUtils.isNotBlank(staff)) {
            sb.append(" AND mb.clerkname = ? ");
            paras.add(staff);
        }
        if (StringUtils.isNotBlank(address)) {
            sb.append(" AND bc.address like ? ");
            paras.add("%" + address + "%");
        }
        sb.append(" GROUP BY months, mb.username, customer_service_staff) tm GROUP BY tm.username, tm.customer_service_staff ORDER BY tm.username ");

        // 列名List
        List<String> columnNameList = new ArrayList<>();
        List<List<Object>> dataList = new ArrayList<>();

        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sb.toString(), paras.toArray());
        SqlRowSetMetaData metaData = sqlRowSet.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            columnNameList.add(metaData.getColumnName(i));
        }
        while (sqlRowSet.next()) {
            List<Object> rowList = new ArrayList<>();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                rowList.add(sqlRowSet.getObject(i));
            }
            dataList.add(rowList);
        }
        // 返回Map
        HashMap<String, Object> result = new HashMap<>();
        result.put("columnNameList", columnNameList);
        result.put("dataList", dataList);

        return result;
    }

    public List<Map<String, Object>> getSaleAmount(Map<String, String> params) {

        if (params != null) {
            List<Object> list = new ArrayList<>();
            StringBuilder sb = new StringBuilder("select count(DISTINCT(os.orderno)) cut,sum(price*num) totalprice from orderproduct op left join orders os on os.orderno = op.orderno");
            sb.append(" left join (SELECT pi.*,a.attribute 牙距,a.VALUE,b.attribute 公称直径,b.VALUE,c.attribute 长度,c.VALUE,d.attribute 外径,d.VALUE");
            sb.append(" ,e.attribute 厚度,e.VALUE from productinfo pi ");
            sb.append(" left join (select * from productattr  where attribute ='牙距' " + (params.containsKey("pitch") ? "and value='" + params.get("pitch") + "'" : "") + ") a on pi.id = a.productid ");
            sb.append(" left join (select * from productattr  where attribute ='公称直径' " + (params.containsKey("nominalDiameter") ? "and value='" + params.get("nominalDiameter") + "'" : "") + ") b on pi.id = b.productid ");
            sb.append(" left join (select * from productattr  where attribute ='长度' " + (params.containsKey("extent") ? "and value='" + params.get("extent") + "'" : "") + ") c on pi.id = c.productid ");
            sb.append(" left join (select * from productattr  where attribute ='外径' " + (params.containsKey("outerDiameter") ? "and value='" + params.get("outerDiameter") + "'" : "") + ") d on pi.id = d.productid ");
            sb.append(" left join (select * from productattr  where attribute ='厚度' " + (params.containsKey("thickness") ? "and value='" + params.get("thickness") + "'" : "") + ") e on pi.id = e.productid ");
            sb.append(" ) pp on pp.id = op.pdid where os.orderstatus <>7 ");
            if (params.containsKey("startCreateTime") && StringUtils.isNotBlank(params.get("startCreateTime"))) {
                sb.append("and os.createTime >= ?");
                Timestamp alarmStartTime = Timestamp.valueOf(params.get("startCreateTime") + " 00:00:00");
                list.add(alarmStartTime);
            }
            if (params.containsKey("endCreateTime") && StringUtils.isNotBlank(params.get("endCreateTime"))) {
                sb.append("and os.createTime <= ?");
                Timestamp alarmStartTime = Timestamp.valueOf(params.get("endCreateTime") + " 23:59:59");
                list.add(alarmStartTime);
            }
            if (params.containsKey("productName") && StringUtils.isNotBlank(params.get("productName"))) {
                sb.append("and op.pdname = ?");
                list.add(params.get("productName"));
            }
            if (params.containsKey("classOne") && StringUtils.isNotBlank(params.get("classOne"))) {
                sb.append("and pi.level1 = ?");
                list.add(params.get("classOne"));
            }
            if (params.containsKey("classTwo") && StringUtils.isNotBlank(params.get("classTwo"))) {
                sb.append("and pi.level2 = ?");
                list.add(params.get("classTwo"));
            }
            if (params.containsKey("classify") && StringUtils.isNotBlank(params.get("classify"))) {
                sb.append("and op.classify = ?");
                list.add(params.get("classify"));
            }
            if (params.containsKey("standard") && StringUtils.isNotBlank(params.get("standard"))) {
                sb.append("and op.standard = ?");
                list.add(params.get("standard"));
            }
            if (params.containsKey("brand") && StringUtils.isNotBlank(params.get("brand"))) {
                sb.append("and op.brand = ?");
                list.add(params.get("brand"));
            }
            if (params.containsKey("mark") && StringUtils.isNotBlank(params.get("mark"))) {
                sb.append("and op.mark = ?");
                list.add(params.get("mark"));
            }
            if (params.containsKey("material") && StringUtils.isNotBlank(params.get("material"))) {
                sb.append("and op.material = ?");
                list.add(params.get("material"));
            }
            if (params.containsKey("grade") && StringUtils.isNotBlank(params.get("grade"))) {
                sb.append("and op.gradeno = ?");
                list.add(params.get("grade"));
            }
            if (params.containsKey("surface") && StringUtils.isNotBlank(params.get("surface"))) {
                sb.append("and pi.surfacetreatment = ?");
                list.add(params.get("surface"));
            }
            if (params.containsKey("store") && StringUtils.isNotBlank(params.get("store"))) {
                sb.append("and op.storename = ?");
                list.add(params.get("store"));
            }
            if (params.containsKey("nominalDiameter") && StringUtils.isNotBlank(params.get("nominalDiameter"))) {
                sb.append("and pp.公称直径 != ''");
            }
            if (params.containsKey("pitch") && StringUtils.isNotBlank(params.get("pitch"))) {
                sb.append("and pp.牙距 != ''");
            }
            if (params.containsKey("extent") && StringUtils.isNotBlank(params.get("extent"))) {
                sb.append("and pp.长度 != ''");
            }
            if (params.containsKey("outerDiameter") && StringUtils.isNotBlank(params.get("outerDiameter"))) {
                sb.append("and pp.外径 != ''");
            }
            if (params.containsKey("thickness") && StringUtils.isNotBlank(params.get("thickness"))) {
                sb.append("and pp.厚度 != ''");
            }
            System.out.println(sb.toString());
            return jdbcTemplate.queryForList(sb.toString(), list.toArray());

        }

        return null;
    }

    /**
     * 商品分类销售统计
     * 参数：ex: key=year va='2019'年份
     * 品牌参数：ex: key=srand 品牌
     *
     * @param params
     * @return
     */
    public List<Map<String, Object>> getCategorySalesPage(Map<String, String> params, @NotNull String year) {
//        if(year==null) {
////            Calendar now = Calendar.getInstance();
////            now.setTime(new Date());
////            year = now.getWeekYear()+"";
////        }
        String leveid = "level1id";
        int parentid = 0;
        if (params.containsKey("parentid") && StringUtils.isNotBlank(params.get("parentid"))) {
            if (!"0".equals(params.get("parentid"))) {
                leveid = "level2id";
            }
            parentid = Integer.parseInt(params.get("parentid"));
        }
        List<Object> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder("select tb.name,tb.id,tb.brand,sum(totalpr) totalpr,round(avg(cut),2) sss,");
        sb.append(" round(sum(case when tb.years = '" + year + "-12' then totalpr else 0 end), 2)  十二月,");
        sb.append(" round(sum(case when tb.years = '" + year + "-11' then totalpr else 0 end), 2)  十一月,");
        sb.append(" round(sum(case when tb.years = '" + year + "-10' then totalpr else 0 end), 2)  十月,");
        sb.append(" round(sum(case when tb.years = '" + year + "-09' then totalpr else 0 end), 2)  九月,");
        sb.append(" round(sum(case when tb.years = '" + year + "-08' then totalpr else 0 end), 2) 八月,");
        sb.append(" round(sum(case when tb.years = '" + year + "-07' then totalpr else 0 end), 2)  七月,");
        sb.append(" round(sum(case when tb.years = '" + year + "-06' then totalpr else 0 end), 2)  六月,");
        sb.append(" round(sum(case when tb.years = '" + year + "-05' then totalpr else 0 end), 2)  五月,");
        sb.append(" round(sum(case when tb.years = '" + year + "-04' then totalpr else 0 end), 2)  四月,");
        sb.append(" round(sum(case when tb.years = '" + year + "-03' then totalpr else 0 end), 2)  三月,");
        sb.append(" round(sum(case when tb.years = '" + year + "-02' then totalpr else 0 end), 2)  二月,");
        sb.append(" round(sum(case when tb.years = '" + year + "-01' then totalpr else 0 end), 2)  一月");
        sb.append(" from (");
        sb.append(" select ca.name,ca.sort,ca.id,pi.brand,count(1),sum(op.price*op.num) totalpr,to_char(os.createtime, 'yyyy-mm') years ");
        sb.append(" from productinfo pi LEFT JOIN categories ca on ca.id = pi." + leveid + " left join orderproduct op on op.pdid = pi.id left join orders os on os.orderno = op.orderno  ");
        sb.append(" where os.orderstatus <> 7 and ca.parentid = ? and to_char(os.createtime, 'yyyy') = ?");
        list.add(parentid);
        list.add(year);
        if (params.get("level") != null && StringUtils.isNotBlank(params.get("level"))) {
            sb.append(" and ca.name =?");
            list.add(params.get("level"));
        }
        sb.append(" GROUP BY ca.id ,ca.name,ca.sort,to_char(os.createtime, 'yyyy-mm'),pi.brand )tb");

        sb.append(" LEFT JOIN( select ca.id ,ca.name,sum(op.price*op.num) cut from productinfo pi");
        sb.append(" left join orderproduct op on op.pdid = pi.id LEFT JOIN categories ca on ca.id = pi." + leveid + " left join orders os on os.orderno = op.orderno ");
        sb.append(" where os.orderstatus <> 7 and to_char(os.createtime, 'yyyy') = ?");
        list.add(year);
        sb.append(" GROUP BY ca.id ,ca.name ) tcc on tb.id = tcc.id ");
        sb.append(" GROUP BY tb.name,tb.sort,tb.id,brand order by tb.sort");
        if (StringUtils.isNotBlank(params.get("limit"))) {
            long limit = Long.parseLong(params.get("limit").trim());
            sb.append(" limit ? ");
            list.add(limit);
        } else {
            sb.append(" limit 10 ");
        }
        if (StringUtils.isNotBlank(params.get("offset"))) {
            long offset = Long.parseLong(params.get("offset").trim());
            sb.append(" offset ? ;");
            list.add(offset);
        } else {
            sb.append(" offset 0 ;");
        }
        return jdbcTemplate.queryForList(sb.toString(), list.toArray());
    }

    public Long getCategorySalesCount(Map<String, String> params, String year) {
        List<Object> list = new ArrayList<>();
        String leveid = "level1id";
        int parentid = 0;
        if (params.containsKey("parentid") && StringUtils.isNotBlank(params.get("parentid"))) {
            if (params.containsKey("parentid") && StringUtils.isNotBlank(params.get("parentid"))) {
                if (!"0".equals(params.get("parentid"))) {
                    leveid = "level2id";
                }
                parentid = Integer.parseInt(params.get("parentid"));
            }
        }

        StringBuilder sb = new StringBuilder("select count(1) from ( select ca.name,ca.id,pi.brand,count(1),sum(op.price*op.num) totalpr");
        sb.append(" from productinfo pi LEFT JOIN categories ca on ca.id =  pi." + leveid + " left join orderproduct op on op.pdid = pi.id ");
        sb.append(" left join orders os on os.orderno = op.orderno where os.orderstatus <> 7 and to_char(os.createtime, 'yyyy') = ? ");
        list.add(year);
        sb.append(" and ca.parentid = ? GROUP BY ca.id ,ca.name,pi.brand ) tb ");
        list.add(parentid);
        return jdbcTemplate.queryForObject(sb.toString(), list.toArray(), Long.class);
    }

    /**
     * 紧商网商家数量
     *
     * @return 有效商家数量
     */
    public int getShopNum() {
        return jdbcTemplate.queryForObject("SELECT COUNT(1) FROM sellercompanyinfo sci WHERE sci.validate = 1", Integer.class);
    }
}
