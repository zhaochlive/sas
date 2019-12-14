package com.js.sas.service;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson.JSONArray;
import com.js.sas.dto.OverdueDTO;
import com.js.sas.repository.PartnerRepository;
import com.js.sas.utils.CommonUtils;
import com.js.sas.utils.DateTimeUtils;
import com.js.sas.utils.StyleExcelHandler;
import com.js.sas.utils.constant.ExcelPropertyEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import springfox.documentation.annotations.ApiIgnore;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.criteria.Predicate;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.text.ParseException;
import java.util.Date;

/**
 * @ClassName FinanceService
 * @Description 财务Service
 * @Author zc
 * @Date 2019/6/19 18:58
 **/
@Service
@Slf4j
public class FinanceService {

    @Value("${yongyou.url}")
    private String url;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    @Qualifier(value = "sqlServerJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    private final DataSource dataSource;

    private final PartnerRepository partnerRepository;

    public FinanceService(DataSource dataSource, PartnerRepository partnerRepository) {
        this.dataSource = dataSource;
        this.partnerRepository = partnerRepository;
    }

    /**
     * 结算客户对账单（线上、线下）
     *
     * @param name      结算客户名称
     * @param channel   来源（线上、线下）
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @param offset    偏移量
     * @param limit     数量
     * @param sort      排序字段
     * @param sortOrder 排序规则
     * @return Map<String, Object>
     */
    public Map<String, Object> getSettlementSummary(String name, String channel, String startDate, String endDate, int offset, int limit, String sort, String sortOrder) {
        HashMap<String, Object> result = new HashMap<>();

        StoredProcedureQuery store = this.entityManager.createNamedStoredProcedureQuery("getSettlementSummary");

        store.setParameter("settlementName", name);
        store.setParameter("channel", channel);
        store.setParameter("startDate", startDate);
        store.setParameter("endDate", endDate);
        store.setParameter("offsetNum", offset);
        store.setParameter("limitNum", limit);
        store.setParameter("sort", sort);
        store.setParameter("sortOrder", sortOrder);

        List settlementSummaryList = store.getResultList();

        result.put("rows", settlementSummaryList);
        result.put("total", store.getOutputParameterValue("totalNum"));

        return result;
    }

    /**
     * 逾期客户
     *
     * @param partner 逾期客户
     * @return 逾期客户列表
     */
    public Page findOverdue(OverdueDTO partner) {
        // 排序规则
        Sort.Direction sortDirection;
        if (partner.getSortOrder() == null || partner.getSortOrder().equals("desc")) {
            sortDirection = Sort.Direction.DESC;
        } else {
            sortDirection = Sort.Direction.ASC;
        }
        // 判断排序字段
        if (!StringUtils.isNotBlank(partner.getSort())) {
            partner.setSort("name");
        }

        if (partner.getLimit() <= 0) {
            partner.setLimit(1);
        }

        Sort sort = new Sort(sortDirection, partner.getSort());
        Pageable pageable = PageRequest.of(partner.getOffset() / partner.getLimit(), partner.getLimit(), sort);

        Specification<OverdueDTO> specification = (Specification<OverdueDTO>) (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.isNotBlank(partner.getCode())) {
                predicates.add(criteriaBuilder.equal(root.<String>get("code"), partner.getCode()));
            }
            if (StringUtils.isNotBlank(partner.getName())) {
                predicates.add(criteriaBuilder.equal(root.<String>get("name"), partner.getName()));
            }
            if (StringUtils.isNotBlank(partner.getOnlyOverdue()) && "true".equals(partner.getOnlyOverdue())) {
                predicates.add(criteriaBuilder.greaterThan(root.<BigDecimal>get("receivablesBeforeToday"), new BigDecimal(0)));
            }
            predicates.add(criteriaBuilder.equal(root.<String>get("status"), '0'));
            predicates.add(criteriaBuilder.equal(root.<String>get("settlementType"), 1));
            predicates.add(criteriaBuilder.equal(root.<String>get("parentCode"), "0"));
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        };

        return partnerRepository.findAll(specification, pageable);
    }

    /**
     * 逾期统计列名
     *
     * @return Map<String, Object>
     */
    public Map<String, List<String>> findOverdueAllColumns() {
        List<String> columnList = new ArrayList<>();

        columnList.add("部门");
        columnList.add("业务员");
        columnList.add("用友往来单位编码");
        columnList.add("往来单位名称");
        columnList.add("账期月");
        columnList.add("账期日");
        columnList.add("订货客户");
        columnList.add("应收总计");
        columnList.add("逾期款");
        columnList.add("期初应收");

        // 当前时间
        Calendar now = Calendar.getInstance();
        // 初始时间
        Calendar origin = Calendar.getInstance();
        // 2019-01-01 00:00:00
        origin.set(2019, 0, 1, 0, 0, 0);

        while (origin.before(now)) {
            columnList.add(origin.get(Calendar.YEAR) + "年" + (origin.get(Calendar.MONTH) + 1) + "月");
            // 加1个月
            origin.add(Calendar.MONTH, 1);
        }
        // 如果日期大于27日，则需要多计算下一个月
        if (now.get(Calendar.DATE) > 27 ) {
            columnList.add(origin.get(Calendar.YEAR) + "年" + (origin.get(Calendar.MONTH) + 1) + "月");
        }

        HashMap<String, List<String>> result = new HashMap<>();
        result.put("columns", columnList);
        return result;
    }

    public List<Object[]> findOverdueAll(OverdueDTO partner) {

        StringBuilder sqlStringBuilder = new StringBuilder("SELECT yap.parent_code, yap.amount_delivery, yap.amount_collected, IFNULL(ds.department,'') AS 部门, " +
                "yap.customer_service_staff AS 业务员, yap.code AS 用友往来单位编码, yap.name AS 往来单位名称, yap.payment_month AS 账期月, " +
                "IF(yap.settlement_type = '1' AND yap.parent_code = '0', '现款', yap.payment_date) AS 账期日, IF(yap.parent_name IS NULL OR yap.parent_name = '' OR (yap.settlement_type = '1' AND yap.parent_code = '0'), yap.NAME, yap.parent_name) AS 订货客户, yap.receivables AS 应收总计, " +
                "yap.amount_delivery + yap.opening_balance - yap.amount_collected + yap.amount_refund AS 逾期款, yap.opening_balance AS 期初应收 ");
        // 当前时间
        Calendar now = Calendar.getInstance();
        // 初始时间
        Calendar origin = Calendar.getInstance();
        // 2019-01-01 00:00:00
        origin.set(2019, 0, 1, 0, 0, 0);

        while (origin.before(now)) {
            sqlStringBuilder.append(", SUM(CASE months WHEN '" + origin.get(Calendar.YEAR) + "年" + (origin.get(Calendar.MONTH) + 1) + "月销售' THEN vssm.amount ELSE 0 END) AS " + origin.get(Calendar.YEAR) + "年" + (origin.get(Calendar.MONTH) + 1) + "月 ");
            sqlStringBuilder.append(", SUM(CASE months WHEN '" + origin.get(Calendar.YEAR) + "年" + (origin.get(Calendar.MONTH) + 1) + "月退货' THEN vssm.amount ELSE 0 END) AS " + origin.get(Calendar.YEAR) + "年" + (origin.get(Calendar.MONTH) + 1) + "月退货 ");
            // 加1个月
            origin.add(Calendar.MONTH, 1);
        }

        // 如果日期大于27日，则需要多计算下一个月
        if (now.get(Calendar.DATE) > 27 ) {
            sqlStringBuilder.append(", SUM(CASE months WHEN '" + origin.get(Calendar.YEAR) + "年" + (origin.get(Calendar.MONTH) + 1) + "月销售' THEN vssm.amount ELSE 0 END) AS " + origin.get(Calendar.YEAR) + "年" + (origin.get(Calendar.MONTH) + 1) + "月 ");
            sqlStringBuilder.append(", SUM(CASE months WHEN '" + origin.get(Calendar.YEAR) + "年" + (origin.get(Calendar.MONTH) + 1) + "月退货' THEN vssm.amount ELSE 0 END) AS " + origin.get(Calendar.YEAR) + "年" + (origin.get(Calendar.MONTH) + 1) + "月退货 ");
        }

        sqlStringBuilder.append(" FROM YY_AA_Partner yap ");
        sqlStringBuilder.append(" LEFT JOIN v_settlement_sales_months vssm ON yap.id = vssm.settlementId ");
        sqlStringBuilder.append(" LEFT JOIN dept_staff ds ON yap.customer_service_staff = ds.name ");
        sqlStringBuilder.append(" WHERE yap.status = 0 ");
        if (partner != null && StringUtils.isNotBlank(partner.getCode())) {
            sqlStringBuilder.append(" AND yap.code = '" + partner.getCode() + "' ");
        }
        if (partner != null && StringUtils.isNotBlank(partner.getName())) {
            sqlStringBuilder.append(" AND yap.name = '" + partner.getName() + "' ");
        }
        if (partner != null && StringUtils.isNotBlank(partner.getOnlyOverdue()) && partner.getOnlyOverdue().equals("true")) {
            sqlStringBuilder.append(" AND yap.receivables > 0 ");
        }
        sqlStringBuilder.append(" GROUP BY ");
        sqlStringBuilder.append(" yap.id, ds.department, yap.code, yap.parent_code, yap.payment_month, yap.payment_date, yap.name, yap.amount_delivery, yap.amount_collected, yap.opening_balance ");

        if (partner != null) {
            sqlStringBuilder.append(" ORDER BY yap.parent_code DESC, yap.name ASC LIMIT " + partner.getOffset() + ", " + partner.getLimit());
        } else {
            sqlStringBuilder.append(" ORDER BY yap.parent_code DESC, yap.name ASC ");
        }

        Query query = entityManager.createNativeQuery(sqlStringBuilder.toString());

        //query.unwrap(NativeQueryImpl.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

        return query.getResultList();
    }

    public BigInteger findOverdueAllCount(OverdueDTO partner) {
        StringBuilder sqlCountStringBuilder = new StringBuilder("SELECT COUNT(1) FROM ( SELECT 1 ");
        sqlCountStringBuilder.append(" FROM YY_AA_Partner yap ");
        sqlCountStringBuilder.append(" LEFT JOIN v_settlement_sales_months vssm ON yap.id = vssm.settlementId ");
        sqlCountStringBuilder.append(" LEFT JOIN dept_staff ds ON yap.customer_service_staff = ds.name ");
        sqlCountStringBuilder.append(" WHERE yap.status = 0 ");
        if (partner != null && StringUtils.isNotBlank(partner.getCode())) {
            sqlCountStringBuilder.append(" AND yap.code = '" + partner.getCode() + "' ");
        }
        if (partner != null && StringUtils.isNotBlank(partner.getName())) {
            sqlCountStringBuilder.append(" AND yap.name = '" + partner.getName() + "' ");
        }
        if (partner != null && StringUtils.isNotBlank(partner.getOnlyOverdue()) && partner.getOnlyOverdue().equals("true")) {
            sqlCountStringBuilder.append(" AND yap.receivables > 0 ");
        }
        sqlCountStringBuilder.append(" GROUP BY ");
        sqlCountStringBuilder.append(" yap.id, ds.department, yap.code, yap.parent_code, yap.payment_month, yap.payment_date, yap.name, yap.amount_delivery, yap.amount_collected, yap.opening_balance ");
        sqlCountStringBuilder.append(" ) t ");
        return (BigInteger) entityManager.createNativeQuery(sqlCountStringBuilder.toString()).getSingleResult();
    }

    /**
     * 逾期统计（销售版）列名
     *
     * @return Map<String, Object>
     */
    public Map<String, List<String>> findOverdueSalesColumns() {
        List<String> columnList = new ArrayList<>();

        columnList.add("部门");
        columnList.add("业务员");
        columnList.add("用友往来单位编码");
        columnList.add("往来单位名称");
        columnList.add("账期月");
        columnList.add("账期日");
        columnList.add("订货客户");
        columnList.add("应收总计");
        columnList.add("逾期款");
        columnList.add("期初应收");

        // 当前时间
        Calendar now = Calendar.getInstance();
        // 如果日期大于27日，则需要多计算下一个月
        if (now.get(Calendar.DATE) > 27 ) {
            // 加1个月
            now.add(Calendar.MONTH, 1);
        }
        // 减2个月
        now.add(Calendar.MONTH, -2);
        columnList.add(now.get(Calendar.YEAR) + "年" + (now.get(Calendar.MONTH) + 1) + "月");
        // 加1个月
        now.add(Calendar.MONTH, 1);
        columnList.add(now.get(Calendar.YEAR) + "年" + (now.get(Calendar.MONTH) + 1) + "月");
        // 加1个月
        now.add(Calendar.MONTH, 1);
        columnList.add(now.get(Calendar.YEAR) + "年" + (now.get(Calendar.MONTH) + 1) + "月");

        HashMap<String, List<String>> result = new HashMap<>();
        result.put("columns", columnList);
        return result;
    }

    /**
     * 逾期统计（销售版）
     *
     * @param partner
     * @return
     */
    public List<Object[]> findOverdueSales(OverdueDTO partner) {

        StringBuilder sqlStringBuilder = new StringBuilder("SELECT yap.parent_code, yap.amount_delivery, yap.amount_collected, IFNULL(ds.department,'') AS 部门, " +
                "IFNULL( yapp.customer_service_staff, '' ) AS 业务员, yap.code AS 用友往来单位编码, yap.name AS 往来单位名称, yap.payment_month AS 账期月, " +
                "IF(yap.parent_name IS NULL OR yap.parent_name = '' OR (yap.settlement_type = '1' AND yap.parent_code = '0'), '现款', yap.payment_date) AS 账期日, IF(yap.parent_name IS NULL OR yap.parent_name = '' OR (yap.settlement_type = '1' AND yap.parent_code = '0'), yap.NAME, yap.parent_name) AS 订货客户, yap.receivables AS 应收总计, " +
                "yap.amount_delivery + yap.opening_balance - yap.amount_collected + yap.amount_refund AS 逾期款, yap.opening_balance AS 期初应收 ");
        // 当前时间
        Calendar now = Calendar.getInstance();
        /**
         * 初始时间
         * 功能只需要显示3个月的，但是涉及账期问题，如果账期一个月，需要多计算1个月，也就是4个月。目前按多算3个月，也就是6个月的数据。
         */
        Calendar origin = Calendar.getInstance();
        origin.add(Calendar.MONTH, -6);
        origin.set(origin.get(Calendar.YEAR), origin.get(Calendar.MONTH), 1, 0, 0, 0);

        while (origin.before(now)) {
            sqlStringBuilder.append(", SUM(CASE months WHEN '" + origin.get(Calendar.YEAR) + "年" + (origin.get(Calendar.MONTH) + 1) + "月销售' THEN vssm.amount ELSE 0 END) AS " + origin.get(Calendar.YEAR) + "年" + (origin.get(Calendar.MONTH) + 1) + "月 ");
            sqlStringBuilder.append(", SUM(CASE months WHEN '" + origin.get(Calendar.YEAR) + "年" + (origin.get(Calendar.MONTH) + 1) + "月退货' THEN vssm.amount ELSE 0 END) AS " + origin.get(Calendar.YEAR) + "年" + (origin.get(Calendar.MONTH) + 1) + "月退货 ");
            // 加1个月
            origin.add(Calendar.MONTH, 1);
        }
        // 如果日期大于27日，则需要多计算下一个月
        if (now.get(Calendar.DATE) > 27 ) {
            sqlStringBuilder.append(", SUM(CASE months WHEN '" + origin.get(Calendar.YEAR) + "年" + (origin.get(Calendar.MONTH) + 1) + "月销售' THEN vssm.amount ELSE 0 END) AS " + origin.get(Calendar.YEAR) + "年" + (origin.get(Calendar.MONTH) + 1) + "月 ");
            sqlStringBuilder.append(", SUM(CASE months WHEN '" + origin.get(Calendar.YEAR) + "年" + (origin.get(Calendar.MONTH) + 1) + "月退货' THEN vssm.amount ELSE 0 END) AS " + origin.get(Calendar.YEAR) + "年" + (origin.get(Calendar.MONTH) + 1) + "月退货 ");
        }

        sqlStringBuilder.append(" FROM YY_AA_Partner yap ");
        sqlStringBuilder.append(" LEFT JOIN v_settlement_sales_months vssm ON yap.id = vssm.settlementId ");
        sqlStringBuilder.append(" LEFT JOIN dept_staff ds ON yap.customer_service_staff = ds.name ");
        sqlStringBuilder.append(" LEFT JOIN YY_AA_Partner yapp ON yapp.`code` = yap.parent_code ");
        sqlStringBuilder.append(" WHERE yap.status = 0 ");
        if (partner != null && StringUtils.isNotBlank(partner.getCode())) {
            sqlStringBuilder.append(" AND yap.code = '" + partner.getCode() + "' ");
        }
        if (partner != null && StringUtils.isNotBlank(partner.getName())) {
            sqlStringBuilder.append(" AND yap.name = '" + partner.getName() + "' ");
        }
        if (partner != null && StringUtils.isNotBlank(partner.getOnlyOverdue()) && partner.getOnlyOverdue().equals("true")) {
            sqlStringBuilder.append(" AND yap.receivables > 0 ");
        }
        sqlStringBuilder.append(" GROUP BY ");
        sqlStringBuilder.append(" yap.id, ds.department, yapp.customer_service_staff, yap.code, yap.parent_code, yap.payment_month, yap.payment_date, yap.name, yap.amount_delivery, yap.amount_collected, yap.opening_balance ");

        if (partner != null) {
            sqlStringBuilder.append(" ORDER BY yap.parent_code DESC, yap.name ASC LIMIT " + partner.getOffset() + ", " + partner.getLimit());
        } else {
            sqlStringBuilder.append(" ORDER BY yap.parent_code DESC, yap.name ASC ");
        }

        Query query = entityManager.createNativeQuery(sqlStringBuilder.toString());

        //query.unwrap(NativeQueryImpl.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

        return query.getResultList();
    }

    public BigInteger findOverdueSalesCount(OverdueDTO partner) {
        StringBuilder sqlCountStringBuilder = new StringBuilder("SELECT COUNT(1) FROM ( SELECT 1 ");
        sqlCountStringBuilder.append(" FROM YY_AA_Partner yap ");
        sqlCountStringBuilder.append(" LEFT JOIN v_settlement_sales_months vssm ON yap.id = vssm.settlementId ");
        sqlCountStringBuilder.append(" LEFT JOIN dept_staff ds ON yap.customer_service_staff = ds.name ");
        sqlCountStringBuilder.append(" WHERE yap.status = 0 ");
        if (partner != null && StringUtils.isNotBlank(partner.getCode())) {
            sqlCountStringBuilder.append(" AND yap.code = '" + partner.getCode() + "' ");
        }
        if (partner != null && StringUtils.isNotBlank(partner.getName())) {
            sqlCountStringBuilder.append(" AND yap.name = '" + partner.getName() + "' ");
        }
        if (partner != null && StringUtils.isNotBlank(partner.getOnlyOverdue()) && partner.getOnlyOverdue().equals("true")) {
            sqlCountStringBuilder.append(" AND yap.receivables > 0 ");
        }
        sqlCountStringBuilder.append(" GROUP BY ");
        sqlCountStringBuilder.append(" yap.id, ds.department, yap.code, yap.parent_code, yap.payment_month, yap.payment_date, yap.name, yap.amount_delivery, yap.amount_collected, yap.opening_balance ");
        sqlCountStringBuilder.append(" ) t ");
        return (BigInteger) entityManager.createNativeQuery(sqlCountStringBuilder.toString()).getSingleResult();
    }


    /**
     * 查询供应商对账单
     *
     * @param hashMap
     * @param isOnline
     * @return
     */
    public List<Map<String, Object>> getSupplier(Map<String, String> hashMap, String isOnline) {
        long pageSize = 0L;
        long offset = 0L;
        try {
            if (hashMap.containsKey("limit") && StringUtils.isNotBlank(hashMap.get("limit"))) {
                pageSize = Long.parseLong(hashMap.get("limit").trim());
            }
            if (hashMap.containsKey("offset") && StringUtils.isNotBlank(hashMap.get("offset"))) {
                offset = Long.parseLong(hashMap.get("offset").trim());
            }

            List list = new ArrayList<String>();
            StringBuilder builder = new StringBuilder();
            builder.append("select * from ( select Row_Number() over (ORDER BY tb.voucherdate,tb.code)as RowNumbes,aa.name ,tb.* from AA_Partner aa ");
            builder.append(" LEFT JOIN( select code ,voucherdate,'1' plus,idpartner,'进货单' type,totalTaxAmount Amount ");
            builder.append(" ,(case pubuserdefnvc1 when '限时购' then '线上' when '线上' then '线上' else '线下' END) isOnline");
            builder.append(" from PU_PurchaseArrival  where voucherState = 189 UNION ");
            builder.append(" select code ,voucherdate,'0' plus,idfirstpartner idpartner,'应付冲应付' type,'0' Amount ,'线上' isOnline");
            builder.append(" from ARAP_StrikeBalance  where idbusitype = 13 UNION");
            builder.append(" select code ,voucherdate,'-2' plus,idpartner,'开票单据' ,sum(OrigTaxAmount) Amount,isOnline from ( ");
            builder.append(" select ppi.code,ppib.OrigTaxAmount,ppi.voucherdate,ppi.idpartner,");
            builder.append(" (case ppa.pubuserdefnvc1 when '限时购' then '线上' when '线上' then '线上' else '线下' END) isOnline");
            builder.append(" from PU_PurchaseInvoice ppi ");
            builder.append(" left join PU_PurchaseInvoice_b ppib on ppib.idPurchaseInvoiceDTO = ppi.id");
            builder.append(" left join PU_PurchaseArrival ppa on ppa.code = ppib.SourceVoucherCode where ppi.voucherState = 189 ) tb ");
            builder.append(" GROUP BY isOnline,code ,voucherdate,idpartner ");
            builder.append(" UNION select  code ,voucherdate,'-1' plus,idpartner,'付款单' type,origAmount Amount");
            builder.append(" ,(case pubuserdefnvc1 when '限时购' then '线上' when '线上' then '线上' else '线下' END) isOnline");
            builder.append(" from ARAP_ReceivePayment   where idbusitype = 80 ");
            builder.append(" ) tb on aa.id = tb.idpartner where isOnline =?");
            list.add(isOnline);
            if (hashMap.containsKey("name") && StringUtils.isNotBlank(hashMap.get("name"))) {
                builder.append(" and aa.name =? ");
                list.add(hashMap.get("name"));
            }
            if (hashMap.containsKey("startDate") && StringUtils.isNotBlank(hashMap.get("startDate"))) {
                builder.append(" and tb.voucherdate >= ?");
                String ss = hashMap.get("startDate") + " 00:00:00";
                list.add(DateTimeUtils.parseDate(ss, DateTimeUtils.DATE_TIME_FORMAT));
            }
            if (hashMap.containsKey("endDate") && StringUtils.isNotBlank(hashMap.get("endDate"))) {
                builder.append(" and tb.voucherdate <= ?");
                String ss = hashMap.get("endDate") + " 23:59:59";
                list.add(DateTimeUtils.parseDate(ss, DateTimeUtils.DATE_TIME_FORMAT));
            }

            builder.append(" )tba where 1=1 ");

            builder.append(" and RowNumbes BETWEEN " + (offset + 1) + " and " + (offset + pageSize));
            return jdbcTemplate.queryForList(builder.toString(), list.toArray());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 数据条数
     *
     * @param hashMap
     * @return
     */
    public long getSupplierCount(Map<String, String> hashMap, String isOnline) {
        try {
            List list = new ArrayList<String>();
            StringBuilder builder = new StringBuilder();
            builder.append(" select count(*) from AA_Partner aa LEFT JOIN( ");
            builder.append(" select code,idpartner,voucherdate,(case pubuserdefnvc1 when '限时购' then '线上' when '线上' then '线上' else '线下' END) isOnline ");
            builder.append(" from PU_PurchaseArrival  where voucherState = 189 ");
            builder.append(" UNION select code ,idfirstpartner idpartner,voucherdate,'线上' isOnline from ARAP_StrikeBalance  where idbusitype = 13  ");
            builder.append(" UNION select code ,idpartner,voucherdate,isOnline from ");
            builder.append(" (select ppi.code,ppi.idpartner,ppi.voucherdate,(case ppa.pubuserdefnvc1 when '限时购' then '线上' when '线上' then '线上' else '线下' END) isOnline  ");
            builder.append(" from PU_PurchaseInvoice ppi  left join PU_PurchaseInvoice_b ppib on ppib.idPurchaseInvoiceDTO = ppi.id ");
            builder.append(" left join PU_PurchaseArrival ppa on ppa.code = ppib.SourceVoucherCode where ppi.voucherState = 189 ) tb  ");
            builder.append(" GROUP BY isOnline,code ,voucherdate,idpartner  ");
            builder.append(" UNION select  code ,idpartner,voucherdate,(case pubuserdefnvc1 when '限时购' then '线上' when '线上' then '线上' else '线下' END) isOnline ");
            builder.append(" from ARAP_ReceivePayment   where idbusitype = 80  ");
            builder.append(" ) tb on aa.id = tb.idpartner where isOnline =? ");
            list.add(isOnline);
            if (hashMap.containsKey("name") && StringUtils.isNotBlank(hashMap.get("name"))) {
                builder.append(" and aa.name =? ");
                list.add(hashMap.get("name"));
            }
            if (hashMap.containsKey("startDate") && StringUtils.isNotBlank(hashMap.get("startDate"))) {
                builder.append(" and tb.voucherdate >= ?");
                String ss = hashMap.get("startDate") + " 00:00:00";
                list.add(DateTimeUtils.parseDate(ss, DateTimeUtils.DATE_TIME_FORMAT));
            }
            if (hashMap.containsKey("endDate") && StringUtils.isNotBlank(hashMap.get("endDate"))) {
                builder.append(" and tb.voucherdate <= ?");
                String ss = hashMap.get("endDate") + " 23:59:59";
                list.add(DateTimeUtils.parseDate(ss, DateTimeUtils.DATE_TIME_FORMAT));
            }
//            System.out.println("【getSupplierCount】："+builder.toString());
            return jdbcTemplate.queryForObject(builder.toString(), list.toArray(), Long.class);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0L;
    }

    /**
     * 获取用户期初数据
     *
     * @param hashMap
     * @return
     */
    public Map<String, Object> getOrigAmount(Map<String, String> hashMap, String isOnline) throws ParseException {
        if (!hashMap.containsKey("name") && StringUtils.isBlank(hashMap.get("name"))) {
            throw new RuntimeException("未获取到用户");
        }
        StringBuilder builder = new StringBuilder();
        List list = new ArrayList<String>();
        builder.append(" select sum(CASE plus WHEN '1' THEN Amount WHEN '-1' THEN -Amount ELSE 0 END) payable,");
        builder.append(" sum(CASE plus WHEN '1' THEN Amount WHEN '-2' THEN -Amount ELSE 0 END) invoice");
        builder.append(" from AA_Partner aa LEFT JOIN(");
        builder.append(" select code,voucherdate,'1' plus,idpartner,totalTaxAmount Amount ,");
        builder.append(" (case pubuserdefnvc1 when '限时购' then '线上' when '线上' then '线上' else '线下' END) isOnline");
        builder.append(" from PU_PurchaseArrival  where voucherState = 189 ");
        builder.append(" UNION select code ,voucherdate,'-2' plus,idpartner,sum(OrigTaxAmount) Amount,isOnline ");
        builder.append(" from (select ppi.code,ppib.OrigTaxAmount,ppi.voucherdate,ppi.idpartner, ");
        builder.append(" (case ppa.pubuserdefnvc1 when '限时购' then '线上' when '线上' then '线上' else '线下' END) isOnline ");
        builder.append(" from PU_PurchaseInvoice ppi  left join PU_PurchaseInvoice_b ppib on ppib.idPurchaseInvoiceDTO = ppi.id ");
        builder.append(" left join PU_PurchaseArrival ppa on ppa.code = ppib.SourceVoucherCode where ppi.voucherState = 189 ) tb  GROUP BY isOnline,code ,voucherdate,idpartner");
        builder.append(" UNION select code ,voucherdate,'-1' plus,idpartner,origAmount Amount");
        builder.append(" ,(case pubuserdefnvc1 when '限时购' then '线上' when '线上' then '线上' else '线下' END) isOnline");
        builder.append(" from ARAP_ReceivePayment   where idbusitype = 80 ");
        builder.append(" UNION select code ,'2018-12-30 01:01:01','1' plus,idpartner,origAmount Amount,'线上'isOnline from ARAP_OriginalAmount_ApDetail ");
        builder.append(" ) tb on aa.id = tb.idpartner where tb.voucherdate >= '2018-12-28 00:00:00' and isonline = ?");
        list.add(isOnline);
        if (hashMap.containsKey("name") && StringUtils.isNotBlank(hashMap.get("name"))) {
            builder.append(" and aa.name =? ");
            list.add(hashMap.get("name"));
        }
        if (hashMap.containsKey("startDate") && StringUtils.isNotBlank(hashMap.get("startDate"))) {
            builder.append(" and tb.voucherdate < ?");
            String ss = hashMap.get("startDate") + " 00:00:00";
            list.add(DateTimeUtils.parseDate(ss, DateTimeUtils.DATE_TIME_FORMAT));
        }
//        System.out.println("【getOrigAmount】:"+builder.toString());
        return jdbcTemplate.queryForMap(builder.toString(), list.toArray());
    }

    /**
     * 获取分页 offsset前 余额
     *
     * @param hashMap
     * @return
     * @throws ParseException
     */
    public List<Map<String, Object>> getTopOrigAmount(Map<String, String> hashMap, String isOnline) throws ParseException {
        Long offset = 0L;
        if (hashMap.containsKey("offset") && StringUtils.isNotBlank(hashMap.get("offset"))) {
            offset = Long.parseLong(hashMap.get("offset").trim());
        }
        if (!hashMap.containsKey("name") && StringUtils.isBlank(hashMap.get("name"))) {
            throw new RuntimeException("未获取到用户");
        }
        StringBuilder builder = new StringBuilder();
        List list = new ArrayList<String>();
        builder.append(" select sum(CASE plus WHEN '1' THEN Amount WHEN '-1' THEN -Amount ELSE 0 END) payable,");
        builder.append(" sum(CASE plus WHEN '1' THEN Amount WHEN '-2' THEN -Amount ELSE 0 END) invoice");
        builder.append(" from ( select top " + offset + " * from (");
        builder.append(" select aa.name ,tb.* from AA_Partner aa LEFT JOIN(");
        builder.append(" select code,voucherdate,'1' plus,idpartner,totalTaxAmount Amount " +
                ",(case pubuserdefnvc1 when '限时购' then '线上' when '线上' then '线上' else '线下' END) isOnline from PU_PurchaseArrival where voucherState = 189");
        builder.append(" UNION (select code,voucherdate,'-2' plus,idpartner, sum(OrigTaxAmount) Amount,isOnline from (");
        builder.append(" select ppi.code,ppib.OrigTaxAmount,ppi.voucherdate,ppi.idpartner,");
        builder.append(" (case ppa.pubuserdefnvc1 when '限时购' then '线上' when '线上' then '线上' else '线下' END) isOnline");
        builder.append(" from PU_PurchaseInvoice ppi left join PU_PurchaseInvoice_b ppib on ppib.idPurchaseInvoiceDTO = ppi.id");
        builder.append(" left join PU_PurchaseArrival ppa on ppa.code = ppib.SourceVoucherCode where ppi.voucherState = 189 ) tb ");
        builder.append(" GROUP BY isOnline,code ,voucherdate,idpartner)");
        builder.append(" UNION select  code ,voucherdate,'-1' plus,idpartner,origAmount Amount" +
                ",(case pubuserdefnvc1 when '限时购' then '线上' when '线上' then '线上' else '线下' END) isOnline from ARAP_ReceivePayment   where idbusitype = 80");
        builder.append(" ) tb on aa.id = tb.idpartner where isOnline =? ");
        list.add(isOnline);
        if (hashMap.containsKey("name") && StringUtils.isNotBlank(hashMap.get("name"))) {
            builder.append(" and aa.name =? ");
            list.add(hashMap.get("name"));
        }
        if (hashMap.containsKey("startDate") && StringUtils.isNotBlank(hashMap.get("startDate"))) {
            builder.append(" and tb.voucherdate >= ?");
            String ss = hashMap.get("startDate") + " 00:00:00";
            list.add(DateTimeUtils.parseDate(ss, DateTimeUtils.DATE_TIME_FORMAT));
        }
        if (hashMap.containsKey("endDate") && StringUtils.isNotBlank(hashMap.get("endDate"))) {
            builder.append(" and tb.voucherdate <= ?");
            String ss = hashMap.get("endDate") + " 23:59:59";
            list.add(DateTimeUtils.parseDate(ss, DateTimeUtils.DATE_TIME_FORMAT));
        }
        builder.append(" )ss ORDER BY voucherdate,code )bb GROUP BY bb.idpartner;");
//        System.out.println("【getTopOrigAmount】："+ builder.toString());
        return jdbcTemplate.queryForList(builder.toString(), list.toArray());
    }

    /**
     * 结算
     *
     * @param hashMap
     * @return
     * @throws ParseException
     */
    public List<Map<String, Object>> getSupplierCount(Map<String, String> hashMap) throws ParseException {
        if (!hashMap.containsKey("name") && StringUtils.isBlank(hashMap.get("name"))) {
            throw new RuntimeException("未获取到用户");
        }
        StringBuilder builder = new StringBuilder();
        List list = new ArrayList<String>();
        builder.append(" select ");
        builder.append(" sum(CASE plus WHEN '1' THEN Amount ELSE 0 END) receivingAmount,");
        builder.append(" sum(CASE plus WHEN '-1' THEN Amount ELSE 0 END) paymentAmount,");
        builder.append(" sum(CASE plus WHEN '-2' THEN Amount ELSE 0 END) invoiceAmount,isOnline type ");
        builder.append(" from AA_Partner aa LEFT JOIN( ");
        builder.append(" select code,voucherdate,'1' plus,idpartner,totalTaxAmount Amount ,");
        builder.append(" (case pubuserdefnvc1 when '限时购' then '线上' when '线上' then '线上' else '线下' END) isOnline ");
        builder.append(" from PU_PurchaseArrival  where voucherState = 189 ");
        builder.append(" UNION  select code ,voucherdate,'0' plus,idfirstpartner idpartner,'0' Amount , ");
        builder.append(" '线上' isOnline from ARAP_StrikeBalance  where idbusitype = 13 ");
        builder.append(" UNION select code ,voucherdate,'-2' plus,idpartner,sum(OrigTaxAmount) Amount,isOnline");
        builder.append(" from (  select ppi.code,ppib.OrigTaxAmount,ppi.voucherdate,ppi.idpartner,");
        builder.append(" (case ppa.pubuserdefnvc1 when '限时购' then '线上' when '线上' then '线上' else '线下' END) isOnline");
        builder.append(" from PU_PurchaseInvoice ppi  left join PU_PurchaseInvoice_b ppib on ppib.idPurchaseInvoiceDTO = ppi.id");
        builder.append(" left join PU_PurchaseArrival ppa on ppa.code = ppib.SourceVoucherCode");
        builder.append(" where ppi.voucherState = 189 ) tb  GROUP BY isOnline,code ,voucherdate,idpartner");
        builder.append(" UNION select  code ,voucherdate,'-1' plus,idpartner,origAmount Amount ,");
        builder.append(" (case pubuserdefnvc1 when '限时购' then '线上' when '线上' then '线上' else '线下' END) isOnline");
        builder.append(" from ARAP_ReceivePayment where idbusitype = 80");
        builder.append(" ) tb on aa.id = tb.idpartner where 1=1");
        if (hashMap.containsKey("name") && StringUtils.isNotBlank(hashMap.get("name"))) {
            builder.append(" and aa.name =? ");
            list.add(hashMap.get("name"));
        }
        if (hashMap.containsKey("startDate") && StringUtils.isNotBlank(hashMap.get("startDate"))) {
            builder.append(" and tb.voucherdate >= ?");
            String ss = hashMap.get("startDate") + " 00:00:00";
            list.add(DateTimeUtils.parseDate(ss, DateTimeUtils.DATE_TIME_FORMAT));
        }
        if (hashMap.containsKey("endDate") && StringUtils.isNotBlank(hashMap.get("endDate"))) {
            builder.append(" and tb.voucherdate <= ?");
            String ss = hashMap.get("endDate") + " 23:59:59";
            list.add(DateTimeUtils.parseDate(ss, DateTimeUtils.DATE_TIME_FORMAT));
        }
        builder.append(" GROUP BY isonline ;");
//        System.out.println("结算【getSupplierCount】："+ builder.toString());
        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(builder.toString(), list.toArray());
        Date date = DateTimeUtils.parseDate(hashMap.get("endDate"), DateTimeUtils.DATE_FORMAT);
        Timestamp time = DateTimeUtils.addTime(date, 1, DateTimeUtils.DAY);
        hashMap.put("startDate", DateTimeUtils.convert(time, DateTimeUtils.DATE_FORMAT));
        Map<String, Object> online = this.getOrigAmount(hashMap, "线上");
        Map<String, Object> offline = this.getOrigAmount(hashMap, "线下");

        for (Map<String, Object> objectMap : mapList) {
            if ("线上".equals(objectMap.get("type"))) {
                objectMap.put("balancePayableAmount", online.get("payable"));
                objectMap.put("balanceInvoiceAmount", online.get("invoice"));
            } else {
                objectMap.put("balancePayableAmount", offline.get("payable"));
                objectMap.put("balanceInvoiceAmount", offline.get("invoice"));
            }
        }
        return mapList;
    }

    public List<Map<String, Object>> getPurchaseArrivalDetail(Map<String, Object> requestMap) {
        StringBuilder builder = new StringBuilder();
        List<Object> list = new ArrayList<>();
        System.out.println(requestMap.toString());

        builder.append("select top 500 PPA.code,PPb.origTaxAmount,ppb.quantity,ppb.origTaxPrice,PPA.voucherdate from PU_PurchaseArrival PPA ");
        builder.append(" left join PU_PurchaseArrival_b PPB on ppa.id = PPB.idPurchaseArrivalDTO");
        builder.append(" left join AA_Partner AA on PPA.IdPartner = AA.ID where AA.name =?");
        list.add(requestMap.get("name"));
        List<Map<String, Object>> maps = jdbcTemplate.queryForList(builder.toString(), list.toArray());
        return maps;
    }

    public Object getAccountspayable(Map<String, String > requestMap, String explan) {
        Map<String, Object> result = new HashMap<>();
        result.put("customerName",requestMap.get("customerName"));
        result.put("queryStartDate",requestMap.get("startDate"));
        result.put("queryEndDate",requestMap.get("endDate"));
        result.put("explan",explan);
        //应付列表
        List<Map<String, Object>> mapList = this.getAccountspayableList(requestMap, explan);
        //应付前期结转
        Map<String, Object> accountspayableCount = this.getAccountspayableCount(requestMap, explan);
        BigDecimal initInvoiceBalance = new BigDecimal(accountspayableCount.get("initPaymentBalance")==null?
                "0":accountspayableCount.get("initPaymentBalance").toString());//还应付款
        BigDecimal  initPaymentBalance = new BigDecimal(accountspayableCount.get("initInvoiceBalance")==null?
                "0":accountspayableCount.get("initInvoiceBalance").toString());//还应开票

        result.put("initInvoiceBalance",initInvoiceBalance);
        result.put("initPaymentBalance",initPaymentBalance);
        BigDecimal deliverTotalAmount = new BigDecimal(0);
        BigDecimal invoiceBalanceTotalAmount = new BigDecimal(0);
        BigDecimal collectTotalAmount = new BigDecimal(0);
        BigDecimal receivableTotalAmount = new BigDecimal(0);
        BigDecimal invoiceTotalAmount = new BigDecimal(0);
        if (mapList!=null&&mapList.size()>0) {
            for (Map<String, Object> map : mapList) {
                String type = map.get("type").toString();
                BigDecimal amount = new BigDecimal(map.get("Amount").toString());
    //
                map.put("voucherdate",map.get("voucherdate").toString().substring(0,10));
                switch (type){
                    case "1" :
                        initPaymentBalance = initPaymentBalance.add(amount);
                        initInvoiceBalance = initInvoiceBalance.add(amount);
                        collectTotalAmount = collectTotalAmount.add(amount);
                        break;
                    case "2" :
                        initPaymentBalance = initPaymentBalance.subtract(amount);
                        receivableTotalAmount = receivableTotalAmount.add(amount);
                        break;
                    case "3" :
                        initInvoiceBalance = initInvoiceBalance.subtract(amount);
                        invoiceTotalAmount = invoiceTotalAmount.add(amount);
                        break;
                    default:break;
                }
                map.put("invoiceBalanceAmount",initInvoiceBalance);
                map.put("receivableBalance",initPaymentBalance);
            }

            deliverTotalAmount = (BigDecimal) mapList.get(mapList.size() -  1).get("receivableBalance");
            invoiceBalanceTotalAmount = (BigDecimal) mapList.get(mapList.size() - 1).get("invoiceBalanceAmount");
        }else{
            deliverTotalAmount = initPaymentBalance;
            invoiceBalanceTotalAmount = initInvoiceBalance;
        }
        result.put("deliverTotalAmount",deliverTotalAmount);
        result.put("invoiceBalanceTotalAmount",invoiceBalanceTotalAmount);
        result.put("collectTotalAmount",collectTotalAmount);
        result.put("receivableTotalAmount",receivableTotalAmount);
        result.put("invoiceTotalAmount",invoiceTotalAmount);
        result.put("arrDetail",mapList);
        return result;
    }

    private List<Map<String, Object>> getAccountspayableList(Map<String, String> requestMap, String explan) {
        StringBuilder sb = new StringBuilder();
        ArrayList<Object> list = new ArrayList<>();
        sb.append(" select ss.* from (");
        sb.append(" select code,voucherdate,OrigTotalTaxAmount Amount,'1' type,IdPartner,");
        sb.append(" (case pubuserdefnvc1 when '限时购' then '线上' when '线上' then '线上' else '线下' END) pubuserdefnvc1 from PU_PurchaseArrival where voucherState =189 and voucherdate >='2018/12/28 00:00:00'");
        sb.append(" union all select ppi.purchaseInvoiceNo code,ppi.voucherdate,ppi.totalTaxAmount Amount,'2' type,ppi.IdPartner,");
        sb.append(" (case ppa.pubuserdefnvc1 when '限时购' then '线上' when '线上' then '线上' else '线下' END) pubuserdefnvc1 from pu_PurchaseInvoice ppi");
        sb.append(" left join PU_PurchaseArrival ppa on ppi.sourceVoucherCode = ppa.code where ppi.voucherState =189");
        sb.append(" union all select code,voucherdate,Amount,'3' type,IdPartner,'线下' pubuserdefnvc1 from ARAP_ReceivePayment where idbusitype =80");
        sb.append(" ) ss left join AA_Partner AA on ss.IdPartner = AA.ID where 1=1 and AA.name =? and pubuserdefnvc1 = ?");
        list.add(requestMap.get("customerName"));
        list.add(explan);
        sb.append(" and voucherdate >='"+requestMap.get("startDate")+"'");
        sb.append(" and voucherdate <='"+requestMap.get("endDate")+"'");
        sb.append( " order by voucherdate");
        return  jdbcTemplate.queryForList(sb.toString(), list.toArray());
    }

    private Map<String, Object> getAccountspayableCount(Map<String, String> requestMap, String explan) {
        StringBuilder sb = new StringBuilder();
        ArrayList<Object> list = new ArrayList<>();
        sb.append(" select sum(case type when '0' then Amount when '1' then Amount when '3' then -Amount end) initPaymentBalance,");
        sb.append(" sum(case type when '0' then Amount when '1' then Amount when '2' then -Amount end) initInvoiceBalance from (");
        sb.append(" select code,origDate voucherdate,Amount,'0' type,IdPartner,'线下' pubuserdefnvc1 from ARAP_OriginalAmount_ApDetail UNION ALL");
        sb.append(" select code,voucherdate,OrigTotalTaxAmount Amount,'1' type,IdPartner,");
        sb.append(" (case pubuserdefnvc1 when '限时购' then '线上' when '线上' then '线上' else '线下' END) pubuserdefnvc1 from PU_PurchaseArrival where voucherState =189 and voucherdate >='2018/12/28 00:00:00'");
        sb.append(" union all select ppi.purchaseInvoiceNo code,ppi.voucherdate,ppi.totalTaxAmount Amount,'2' type,ppi.IdPartner,");
        sb.append(" (case ppa.pubuserdefnvc1 when '限时购' then '线上' when '线上' then '线上' else '线下' END) pubuserdefnvc1 from pu_PurchaseInvoice ppi");
        sb.append(" left join PU_PurchaseArrival ppa on ppi.sourceVoucherCode = ppa.code where ppi.voucherState =189");
        sb.append(" union all select code,voucherdate,Amount,'3' type,IdPartner,'线下' pubuserdefnvc1 from ARAP_ReceivePayment where idbusitype =80");
        sb.append(" ) ss left join AA_Partner AA on ss.IdPartner = AA.ID where 1=1 and AA.name =? and pubuserdefnvc1 = ?");
        list.add(requestMap.get("customerName"));
        list.add(explan);
        sb.append(" and voucherdate <'"+requestMap.get("startDate")+"'");
        System.out.println(sb.toString());
        return  jdbcTemplate.queryForMap(sb.toString(), list.toArray());
    }

    public void downloadOverdueCredit(HttpServletResponse httpServletResponse) {
        Connection con = null;
        ResultSet rs = null;
        try {
            con = dataSource.getConnection();
            CallableStatement c = con.prepareCall("{call PROC_settlement_sales_months}");
            rs = c.executeQuery();

            ResultSetMetaData rsmd = rs.getMetaData();
            // 数据列数
            int count = rsmd.getColumnCount();

            // 列名数据
            ArrayList<String> columnsList = new ArrayList<>();
            // 移除前3列（关联id列、总发货、总收款）
            for (int i = 4; i < count; i++) {
                columnsList.add(rsmd.getColumnLabel(i));
                if (i > 11) {
                    i++;
                }
            }

            // 数据
            ArrayList<List<Object>> rowsList = new ArrayList<>();
            // 需要合并计算的用户序号List，根据code编码判断
            List<Integer> totalIndexList = new ArrayList<>();
            // 需要合并的序号List集合
            List<List<Integer>> totalList = new ArrayList<>();
            // 关联code
            String parentCode = "";
            // 有关联账号标记
            boolean hasParentCode;
            // 关联账户总计应收
            BigDecimal totalReceivables = BigDecimal.ZERO;

            while (rs.next()) {
                ArrayList<Object> dataList = new ArrayList<>();
                // 账期月, 目前rs第7列
                int month = rs.getInt(7);
                // 账期日，目前rs第8列
                int day = rs.getInt(8);
                // 应减去的结算周期数
                int overdueMonths = CommonUtils.overdueMonth(month, day);
                // 当前逾期金额
                BigDecimal overdue = rs.getBigDecimal(10);

                // 设置数据行，移除前3列（关联id列、总发货、总收款）
                for (int i = 4; i <= count; i++) {
                    if (i > 11) {  // 计算每个周期的发货和应收
                        if (i > count - overdueMonths * 2) {
                            // 有关联的账期客户逾期总金额不计算未到账期的退货金额，无关联关系的账期客户预期总金额计算所有退货金额
                            // 分月统计全部计算所有退货金额
                            // 发货金额，未到账期均不计算
                            overdue = overdue.subtract(rs.getBigDecimal(i));
                            // 只计算逾期账期数据，如果是未逾期账期数据，需要将逾期款减去相应的发货金额
                            BigDecimal tempOverdue = BigDecimal.ZERO;

                            if ("0".equals(rs.getString("parent_code"))) {
                                tempOverdue = new BigDecimal(dataList.get(6).toString()).subtract(rs.getBigDecimal(i++));
                                dataList.set(6, tempOverdue);
                            } else {
                                // 有关联账户不计算未到期的退货
                                tempOverdue = new BigDecimal(dataList.get(6).toString()).subtract(rs.getBigDecimal(i++));
                                tempOverdue = tempOverdue.subtract(rs.getBigDecimal(i));
                                dataList.set(6, tempOverdue);
                            }
                            dataList.add(0);
                        } else {
                            dataList.add(rs.getBigDecimal(i++));
                        }
                    } else if (i == 11) {
                        dataList.add(rs.getBigDecimal(i));
                    } else {
                        dataList.add(rs.getString(i));
                    }
                }

                // 根据逾期款，设置excel数据。从后向前，到期初为止。
                for (int index = dataList.size() - 1; index > 6; index--) {
                    if (overdue.compareTo(BigDecimal.ZERO) < 1) {  // 逾期金额小于等于0，所有账期逾期金额都是0
                        dataList.set(index, 0);
                    } else {  // 逾期金额大于0，从最后一个开始分摊逾期金额
                        if (overdue.compareTo(new BigDecimal(dataList.get(index).toString())) > -1) {
                            overdue = overdue.subtract(new BigDecimal(dataList.get(index).toString()));
                            dataList.set(index, dataList.get(index));
                        } else {
                            dataList.set(index, overdue);
                            overdue = BigDecimal.ZERO;
                        }
                    }
                }

                // 补零数量
                // int overdueZero = CommonUtils.overdueZero(month, day);
                // 导出的Excel显示逾期金额，不是发货金额。需要按照账期周期，向后推迟逾期金额，在期初之后补0实现。
                for (int overdueIndex = 0; overdueIndex < month; overdueIndex++) {
                    // 插入0
                    dataList.add(8, 0);
                    // 删除最后一位
                    dataList.remove(dataList.size() - 1);
                }

                // 设置数据列
                rowsList.add(dataList);

                // 设置逾期金额总计
                // 如果存在关联code
                if (!"0".equals(rs.getString("parent_code"))) {
                    hasParentCode = true;
                    // 如果两个不相等，说明是新的关联code，重新赋值
                    if (!parentCode.equals(rs.getString("parent_code"))) {
                        parentCode = rs.getString("parent_code");
                        // 此处修改的目的是防止单元格合并之后，数值求和计算错误。
                        if (!totalIndexList.isEmpty()) {
                            rowsList.get(totalIndexList.get(0) - 1).set(5, totalReceivables);
                        }
                        // 置零
                        totalReceivables = BigDecimal.ZERO;
                        // 添加至集合
                        if (!totalIndexList.isEmpty()) {
                            totalList.add(totalIndexList);
                        }
                        // 序列list置空
                        totalIndexList = new ArrayList<>();
                    }
                } else {
                    // 最后一个totalIndexList
                    if (!totalIndexList.isEmpty()) {
                        // 计算之前应收之和
                        for (int index : totalIndexList) {
                            rowsList.get(index - 1).set(5, totalReceivables);
                        }
                        // 添加至集合
                        totalList.add(totalIndexList);
                        totalIndexList = new ArrayList<>();
                    } else {
                        // 如果没有关联客户，将逾期金额赋值到总逾期金额
                        rowsList.get(rs.getRow() - 1).set(5, rowsList.get(rs.getRow() - 1).get(6));
                    }
                    // 置零
                    hasParentCode = false;
                    parentCode = "";
                    totalReceivables = BigDecimal.ZERO;
                }

                // 设置应收金额合计
                if (hasParentCode) {
                    totalIndexList.add(rs.getRow());
                    totalReceivables = totalReceivables.add(new BigDecimal(dataList.get(6).toString()));
                }

            }

            // 导出excel
            exportOverdue(httpServletResponse, columnsList, rowsList, "账期客户逾期统计", totalList);

        } catch (SQLException | IOException e) {
            log.error("下载账期客户逾期统计异常：{}", e);
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 查询全部逾期数据
     *
     * @param partner
     * @return
     */
    public Map<String, Object> findOverdueAllData(OverdueDTO partner) {
        // 列名
        List<String> columnsList = findOverdueAllColumns().get("columns");
        // 数据List
        List<Object[]> resultDataList = findOverdueAll(partner);
        // 数据
        List<Map<String, Object>> rowsMapList = new ArrayList<>();
        // 数据
        ArrayList<List<Object>> rowsList = new ArrayList<>();

        // 为了通用导出和页面格式，回传两个数据格式，后续可以优化
        Map<String, Object> resultMap = new HashMap<>();

        for (Object[] dataRow : resultDataList) {
            Map<String, Object> dataMap = new HashMap<>();
            ArrayList<Object> dataList = new ArrayList<>();
            // 账期月, 目前rs第7列
            int month = Integer.parseInt(dataRow[7].toString());
            // 账期日，目前rs第8列
            int day = 0;
            if (StringUtils.isNumeric(dataRow[8].toString())) {
                day = Integer.parseInt(dataRow[8].toString());
            }
            // 应减去的结算周期数
            int overdueMonths = CommonUtils.overdueMonth(month, day);
            // 当前逾期金额
            BigDecimal overdue = new BigDecimal(dataRow[11].toString());
            // 设置数据行，移除前3列（关联id列、总发货、总收款）
            for (int i = 3; i < dataRow.length; i++) {
                if (i > 12) {  // 计算每个周期的发货和应收
                    if (i >= dataRow.length - overdueMonths * 2) {
                        // 有关联的账期客户逾期总金额不计算未到账期的退货金额，无关联关系的账期客户预期总金额计算所有退货金额
                        // 分月统计全部计算所有退货金额
                        // 发货金额，未到账期均不计算
                        overdue = overdue.subtract(new BigDecimal(dataRow[i].toString()));
                        // 只计算逾期账期数据，如果是未逾期账期数据，需要将逾期款减去相应的发货金额
                        BigDecimal tempOverdue;
                        if ("0".equals(dataRow[0])) {
                            tempOverdue = new BigDecimal(dataList.get(8).toString()).subtract(new BigDecimal(dataRow[i++].toString()));
                            dataList.set(8, tempOverdue);
                        } else {
                            // 有关联账户不计算未到期的退货
                            tempOverdue = new BigDecimal(dataList.get(8).toString()).subtract(new BigDecimal(dataRow[i++].toString()));
                            tempOverdue = tempOverdue.subtract(new BigDecimal(dataRow[i].toString()));
                            dataList.set(8, tempOverdue);
                        }
                        dataList.add(0);
                    } else {
                        dataList.add(new BigDecimal(dataRow[i++].toString()));
                    }
                } else if (i > 9 && i <= 12) {
                    dataList.add(new BigDecimal(dataRow[i].toString()));
                } else {
                    dataList.add(dataRow[i].toString());
                }
            }

            // 根据逾期款，设置excel数据。从后向前，到期初为止。
            for (int index = dataList.size() - 1; index > 8; index--) {
                if (overdue.compareTo(BigDecimal.ZERO) < 1) {  // 逾期金额小于等于0，所有账期逾期金额都是0
                    dataList.set(index, 0);
                } else {  // 逾期金额大于0，从最后一个开始分摊逾期金额
                    if (overdue.compareTo(new BigDecimal(dataList.get(index).toString())) > -1) {
                        overdue = overdue.subtract(new BigDecimal(dataList.get(index).toString()));
                        dataList.set(index, dataList.get(index));
                    } else {
                        dataList.set(index, overdue);
                        overdue = BigDecimal.ZERO;
                    }
                }
            }

            // 导出的Excel显示逾期金额，不是发货金额。需要按照账期周期，向后推迟逾期金额，在期初之后补0实现。
            for (int overdueIndex = 0; overdueIndex < month; overdueIndex++) {
                // 插入0
                dataList.add(9, 0);
                // 删除最后一位
                dataList.remove(dataList.size() - 1);
            }

            // 设置数据列
            for (int index = 0; index < columnsList.size(); index++) {
                dataMap.put(columnsList.get(index), dataList.get(index));
            }

            rowsMapList.add(dataMap);
            rowsList.add(dataList);
        }

        resultMap.put("map", rowsMapList);
        resultMap.put("list", rowsList);

        return resultMap;
    }

    /**
     * 导出账期逾期客户Excel
     * 牵涉到单元格合并和特殊的表结构，单独一个方法实现。
     *
     * @param response       HttpServletResponse
     * @param columnNameList 导出列名List
     * @param dataList       导出数据List
     * @param fileName       导出文件名，目前sheet页是相同名称
     * @param totalList      需要合并的数据序号List
     * @throws IOException @Description
     */
    private void exportOverdue(HttpServletResponse response, List<String> columnNameList, List<List<Object>> dataList, String fileName, List<List<Integer>> totalList) throws IOException {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");

        Sheet sheet1 = new Sheet(1, 0);
        sheet1.setSheetName(fileName);
        sheet1.setAutoWidth(Boolean.TRUE);

        fileName = fileName + df.format(new Date());
        ServletOutputStream out = response.getOutputStream();
        response.setContentType("multipart/form-data");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment;filename*= UTF-8''" + URLEncoder.encode(fileName, "UTF-8") + ".xlsx");
        // 设置列名
        if (columnNameList != null) {
            List<List<String>> list = new ArrayList<>();
            columnNameList.forEach(c -> list.add(Collections.singletonList(c)));
            sheet1.setHead(list);
        }
        // 写入数据
        ExcelWriter writer = new ExcelWriter(out, ExcelTypeEnum.XLSX, true);
        writer.write1(dataList, sheet1);
        // 合并单元格
        for (List<Integer> totalIndexList : totalList) {
            if (!totalIndexList.isEmpty()) {
                if (!totalIndexList.get(0).equals(totalIndexList.get(totalIndexList.size() - 1))) {
                    writer.merge(totalIndexList.get(0), totalIndexList.get(totalIndexList.size() - 1), 5, 5);
                }
            }
        }

        writer.finish();
        out.flush();
        out.close();

    }

    /**
     * 导出用友对账单Excel公用方法
     *
     * @param period 账期，格式：yyyy-MM
     * @param name   对账单位名称
     * @return 对账单信息EnumMap
     */
    public EnumMap<ExcelPropertyEnum, Object> getYonyouStatementExcel(String period, String name) {
        // 判断参数
        if (StringUtils.isBlank(period) || StringUtils.isBlank(name)) {
            return null;
        }
        // 开始时间
        String startDate;
        // 结束时间
        String endDate;
        // 分割时间
        String[] dateArray = period.split("-");
        // 拼接时间，上个月的28日，到这个月的27日
        if (dateArray.length == 2) {
            if (CommonUtils.isNumber(dateArray[0]) && CommonUtils.isNumber(dateArray[1])) {
                if (Integer.parseInt(dateArray[1]) > 1 && Integer.parseInt(dateArray[1]) <= 12) {
                    startDate = dateArray[0] + "-" + (Integer.parseInt(dateArray[1]) - 1) + "-28";
                    endDate = period + "-27";
                } else if (Integer.parseInt(dateArray[1]) == 1) {
                    startDate = (Integer.parseInt(dateArray[0]) - 1) + "-12-28";
                    endDate = period + "-27";
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }

        // 比较结束日期，如果大于今天，显示今天。
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = sdf.parse(endDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (date.after(new Date())) {
            endDate = sdf.format(new Date());
        }

        // 调用接口获取对账单数据
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("startDate", startDate);
        multiValueMap.add("endDate", endDate);
        multiValueMap.add("settleCustomer", name);
        ResponseEntity responseEntity = CommonUtils.sendPostRequest(url, multiValueMap);
        if (responseEntity.getBody() == null) {
            return null;
        }
        // 格式化JSONArray
        JSONArray dataJSONArray = JSONArray.parseArray("[" + responseEntity.getBody() + "]");
        // 每行数据List
        List<Object> dataList = new ArrayList<>();
        // 总行数据List
        List<List<Object>> rowList = new ArrayList<>();
        // 对账单明细行数据List
        List<List<Object>> totalRowList = new ArrayList<>();
        // 需要加粗显示行号List
        List<Integer> boldList = new ArrayList<>();
        // 需要加边框行号List
        List<Integer> borderList = new ArrayList<>();
        // 背景色行
        List<Integer> backgroundColorList = new ArrayList<>();
        // 居中行
        List<Integer> centerList = new ArrayList<>();
        // 明细居中行
        List<Integer> centerDetailList = new ArrayList<>();
        // 需要合并的行
        List<Integer> mergeRowNumList = new ArrayList<>();
        // 处理数据
        if (dataJSONArray.size() > 0) {
            BigDecimal deliverTotal = BigDecimal.ZERO;
            BigDecimal collectTotal = BigDecimal.ZERO;
            BigDecimal receivableTotal = BigDecimal.ZERO;
            BigDecimal invoiceTotal = BigDecimal.ZERO;
            BigDecimal invoiceBalanceTotal = BigDecimal.ZERO;
            // 第一行，结算客户信息
            dataList.add(dataJSONArray.getJSONObject(0).getString("settleCustomer"));
            dataList.add("");
            dataList.add("");
            dataList.add(dataJSONArray.getJSONObject(0).getString("settleCustomerTel"));
            dataList.add("");
            dataList.add(dataJSONArray.getJSONObject(0).getString("settleCustomerFax"));
            dataList.add("");
            rowList.add(dataList);
            boldList.add(rowList.size());
            // 第二行，公司信息
            dataList = new ArrayList<>();
            dataList.add(dataJSONArray.getJSONObject(0).getString("company"));
            dataList.add("");
            dataList.add("");
            dataList.add(dataJSONArray.getJSONObject(0).getString("companyTel"));
            dataList.add("");
            dataList.add(dataJSONArray.getJSONObject(0).getString("companyFax"));
            dataList.add("");
            rowList.add(dataList);
            boldList.add(rowList.size());
            // 第三行，空白
            dataList = new ArrayList<>();
            rowList.add(dataList);
            // 线上、线下
            for (int index = 0; index < dataJSONArray.getJSONObject(0).getJSONArray("reportContent").size(); index++) {
                // 线上、线下标题
                dataList = new ArrayList<>();
                dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getString("settleCustomer") + " - " + dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getString("explan"));
                rowList.add(dataList);
                boldList.add(rowList.size());
                centerList.add(rowList.size());
                // 时间行
                dataList = new ArrayList<>();
                dataList.add("日期：" + dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getString("queryStartDate") + " _ " + dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getString("queryEndDate"));
                rowList.add(dataList);
                boldList.add(rowList.size());
                // 明细标题行
                dataList = new ArrayList<>();
                dataList.add("日期");
                dataList.add("合同编号");
                dataList.add("类别");
                dataList.add("发货金额");
                dataList.add("收款金额");
                dataList.add("应收款");
                dataList.add("开票金额");
                dataList.add("发票结余");
                rowList.add(dataList);
                boldList.add(rowList.size());
                borderList.add(rowList.size());
                backgroundColorList.add(rowList.size());
                centerList.add(rowList.size());
                // 期初数据行
                dataList = new ArrayList<>();
                dataList.add("线上期初数据");
                dataList.add("上期结转：");
                dataList.add("");
                dataList.add("");
                dataList.add("");
                dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getBigDecimal("initReceivableBanlance"));
                dataList.add("");
                dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getBigDecimal("initInvoiceBanlance"));
                rowList.add(dataList);
                borderList.add(rowList.size());
                centerDetailList.add(rowList.size());
                // 明细
                for (int innerIndex = 0; innerIndex < dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getJSONArray("arrDetail").size(); innerIndex++) {
                    dataList = new ArrayList<>();
                    dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getJSONArray("arrDetail").getJSONObject(innerIndex).getString("bookedDate"));
                    dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getJSONArray("arrDetail").getJSONObject(innerIndex).getString("summary"));
                    dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getJSONArray("arrDetail").getJSONObject(innerIndex).getString("category"));
                    dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getJSONArray("arrDetail").getJSONObject(innerIndex).getBigDecimal("deliverAmount").compareTo(BigDecimal.ZERO) == 0 ? "" : dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getJSONArray("arrDetail").getJSONObject(innerIndex).getBigDecimal("deliverAmount"));
                    dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getJSONArray("arrDetail").getJSONObject(innerIndex).getBigDecimal("collectAmount").compareTo(BigDecimal.ZERO) == 0 ? "" : dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getJSONArray("arrDetail").getJSONObject(innerIndex).getBigDecimal("collectAmount"));
                    dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getJSONArray("arrDetail").getJSONObject(innerIndex).getBigDecimal("receivableAmount").compareTo(BigDecimal.ZERO) == 0 ? "" : dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getJSONArray("arrDetail").getJSONObject(innerIndex).getBigDecimal("receivableAmount"));
                    dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getJSONArray("arrDetail").getJSONObject(innerIndex).getBigDecimal("invoiceAmount").compareTo(BigDecimal.ZERO) == 0 ? "" : dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getJSONArray("arrDetail").getJSONObject(innerIndex).getBigDecimal("invoiceAmount"));
                    dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getJSONArray("arrDetail").getJSONObject(innerIndex).getBigDecimal("invoiceBalanceAmount"));
                    rowList.add(dataList);
                    borderList.add(rowList.size());
                    centerDetailList.add(rowList.size());
                }
                // 汇总信息
                dataList = new ArrayList<>();
                dataList.add("本月" + dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getString("explan") + "结算");
                dataList.add("");
                dataList.add("");
                dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getBigDecimal("deliverTotalAmount"));
                dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getBigDecimal("collectTotalAmount"));
                dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getBigDecimal("receivableTotalAmount"));
                dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getBigDecimal("invoiceTotalAmount"));
                dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getBigDecimal("invoiceBalanceTotalAmount"));
                rowList.add(dataList);
                borderList.add(rowList.size());
                boldList.add(rowList.size());
                mergeRowNumList.add(rowList.size());
                centerList.add(rowList.size());
                backgroundColorList.add(rowList.size());
                // 备注行
                dataList = new ArrayList<>();
                dataList.add("备注：本月" + dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getString("explan") + "销售、收款、开票如上表所示");
                rowList.add(dataList);
                boldList.add(rowList.size());
                // 空白行
                dataList = new ArrayList<>();
                rowList.add(dataList);
                // 本月信息行
                dataList = new ArrayList<>();
                dataList.add("本月小计 - " + dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getString("explan") + "：");
                dataList.add("");
                dataList.add("");
                dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getBigDecimal("deliverTotalAmount"));
                dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getBigDecimal("collectTotalAmount"));
                dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getBigDecimal("receivableTotalAmount"));
                dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getBigDecimal("invoiceTotalAmount"));
                dataList.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getBigDecimal("invoiceBalanceTotalAmount"));
                totalRowList.add(dataList);

                deliverTotal = deliverTotal.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getBigDecimal("deliverTotalAmount"));
                collectTotal = collectTotal.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getBigDecimal("collectTotalAmount"));
                receivableTotal = receivableTotal.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getBigDecimal("receivableTotalAmount"));
                invoiceTotal = invoiceTotal.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getBigDecimal("invoiceTotalAmount"));
                invoiceBalanceTotal = invoiceBalanceTotal.add(dataJSONArray.getJSONObject(0).getJSONArray("reportContent").getJSONObject(index).getBigDecimal("invoiceBalanceTotalAmount"));
            }
            // 月汇总
            dataList = new ArrayList<>();
            dataList.add("综上所述，本月汇总如下");
            rowList.add(dataList);
            boldList.add(rowList.size());
            centerList.add(rowList.size());
            // 月汇总标题行
            dataList = new ArrayList<>();
            dataList.add("合计");
            dataList.add("");
            dataList.add("");
            dataList.add("发货金额");
            dataList.add("收款金额");
            dataList.add("应收款");
            dataList.add("开票金额");
            dataList.add("发票结余");
            rowList.add(dataList);
            boldList.add(rowList.size());
            borderList.add(rowList.size());
            centerList.add(rowList.size());
            backgroundColorList.add(rowList.size());
            // 线上、线下汇总
            rowList.addAll(totalRowList);
            boldList.add(rowList.size());
            borderList.add(rowList.size());
            boldList.add(rowList.size() - 1);
            borderList.add(rowList.size() - 1);
            centerList.add(rowList.size());
            centerList.add(rowList.size() - 1);
            // 月累计行
            dataList = new ArrayList<>();
            dataList.add("本月累计：");
            dataList.add("");
            dataList.add("");
            dataList.add(deliverTotal);
            dataList.add(collectTotal);
            dataList.add(receivableTotal);
            dataList.add(invoiceTotal);
            dataList.add(invoiceBalanceTotal);
            rowList.add(dataList);
            boldList.add(rowList.size());
            borderList.add(rowList.size());
            centerList.add(rowList.size());
            backgroundColorList.add(rowList.size());
            // 其他信息行
            dataList = new ArrayList<>();
            dataList.add("1、此对账单的截止日期为上述发出日期。");
            rowList.add(dataList);
            boldList.add(rowList.size());

            dataList = new ArrayList<>();
            dataList.add("2、如有错漏，请于发出此对账单后七日內提出，否则视为默认！");
            rowList.add(dataList);
            boldList.add(rowList.size());

            dataList = new ArrayList<>();
            dataList.add("制单：");
            dataList.add("");
            dataList.add("业务员确认：");
            dataList.add("");
            dataList.add("");
            dataList.add("发出日期：");
            dataList.add("");
            dataList.add("");
            rowList.add(dataList);
            boldList.add(rowList.size());

            dataList = new ArrayList<>();
            rowList.add(dataList);

            dataList = new ArrayList<>();
            dataList.add("客户签字：");
            rowList.add(dataList);
            boldList.add(rowList.size());

            dataList = new ArrayList<>();
            dataList.add("客户盖章：");
            rowList.add(dataList);
            boldList.add(rowList.size());
        } else {
            return null;
        }
        // 名称
        String fileName = name;
        // sheet页
        Sheet sheet1 = new Sheet(1, 0);
        sheet1.setSheetName(fileName);
        sheet1.setAutoWidth(Boolean.TRUE);
        // 样式
        List<Integer> spacialBackgroundColorList = new ArrayList<>();
        if (!backgroundColorList.isEmpty()) {
            spacialBackgroundColorList.add(mergeRowNumList.get(mergeRowNumList.size() - 1) + 5);
            spacialBackgroundColorList.add(mergeRowNumList.get(mergeRowNumList.size() - 1) + 6);
        }
        StyleExcelHandler handler = new StyleExcelHandler(boldList, borderList, backgroundColorList, centerList, spacialBackgroundColorList, centerDetailList);
        // 返回值
        EnumMap<ExcelPropertyEnum, Object> reusltEnumMap = new EnumMap<>(ExcelPropertyEnum.class);
        reusltEnumMap.put(ExcelPropertyEnum.HANDLER, handler);
        reusltEnumMap.put(ExcelPropertyEnum.ROWLIST, rowList);
        reusltEnumMap.put(ExcelPropertyEnum.SHEET, sheet1);
        reusltEnumMap.put(ExcelPropertyEnum.FILENAME, fileName + "(" + startDate + "_" + endDate + ")");
        reusltEnumMap.put(ExcelPropertyEnum.MERGE, mergeRowNumList);

        return reusltEnumMap;
    }

}
