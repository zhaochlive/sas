package com.js.sas.service;

import com.js.sas.dto.OverdueDTO;
import com.js.sas.repository.PartnerRepository;
import com.js.sas.utils.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.sql.Timestamp;
import java.text.ParseException;

/**
 * @ClassName FinanceService
 * @Description 财务Service
 * @Author zc
 * @Date 2019/6/19 18:58
 **/
@Service
@Slf4j
public class FinanceService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    @Qualifier(value = "sqlServerJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    private final PartnerRepository partnerRepository;

    public FinanceService(PartnerRepository partnerRepository) {
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

        HashMap<String, List<String>> result = new HashMap<>();
        result.put("columns", columnList);
        return result;
    }

    public List<Object[]> findOverdueAll(OverdueDTO partner) {

        StringBuilder sqlStringBuilder = new StringBuilder("SELECT yap.parent_code, yap.amount_delivery, yap.amount_collected, IFNULL(ds.department,'') AS 部门, " +
                "yap.customer_service_staff AS 业务员, yap.code AS 用友往来单位编码, yap.name AS 往来单位名称, yap.payment_month AS 账期月, " +
                "yap.payment_date AS 账期日, IF(yap.parent_name IS NULL OR yap.parent_name = '' OR (yap.settlement_type = 1 AND yap.parent_code = 0), yap.NAME, yap.parent_name) AS 订货客户, yap.receivables AS 应收总计, " +
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
}
