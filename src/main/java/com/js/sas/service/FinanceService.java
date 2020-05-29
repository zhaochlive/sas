package com.js.sas.service;

import com.alibaba.excel.metadata.Sheet;
import com.alibaba.fastjson.JSONArray;
import com.js.sas.entity.dto.OverdueDTO;
import com.js.sas.utils.CommonUtils;
import com.js.sas.utils.DateTimeUtils;
import com.js.sas.utils.excel.StyleExcelHandler;
import com.js.sas.utils.constant.ExcelPropertyEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import java.math.BigDecimal;
import java.math.BigInteger;
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
    @Resource
    @Qualifier(value = "sqlServerJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    /**
     * 结算客户对账单（线上、线下）
     * <p>
     * ！！！此方法调用存储过程，需要优化！！！
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
        result.put("rows", store.getResultList());
        result.put("total", store.getOutputParameterValue("totalNum"));
        return result;
    }

    /**
     * 逾期统计:列名List
     *
     * @param months  统计月数
     * @param oneMore 是否需要多1个月
     * @return 列名List
     */
    public List<String> findOverdueColumns(int months, boolean oneMore) {
        // 因为包含当前月，所以先减1个月
        months--;
        List<String> columnNameList = new ArrayList<>();
        columnNameList.add("部门");
        columnNameList.add("业务员");
        columnNameList.add("往来单位名称");
        columnNameList.add("账期月");
        columnNameList.add("账期日");
        columnNameList.add("订货客户");
        columnNameList.add("应收总计");
        columnNameList.add("逾期款");
        columnNameList.add("期初应收");
        // 当前时间
        Calendar now = Calendar.getInstance();
        // 开始时间 = 当前时间 - 统计的月数
        Calendar origin = Calendar.getInstance();
        // 如果是大于等于28日，则算下一个账期月
        int nowDate = now.get(Calendar.DAY_OF_MONTH);
        if (nowDate >= 28) {
            months = months - 1;
        }
        if (months < 0) {
            months = 0;
        }
        origin.add(Calendar.MONTH, -months);
        while (origin.before(now)) {
            columnNameList.add(origin.get(Calendar.YEAR) + "年" + (origin.get(Calendar.MONTH) + 1) + "月");
            // 加1个月
            origin.add(Calendar.MONTH, 1);
        }
        // 多计算1个月
        columnNameList.add(origin.get(Calendar.YEAR) + "年" + (origin.get(Calendar.MONTH) + 1) + "月");
        // 如果大于等于28日，等于下个账期月
        if (nowDate >= 28) {
            columnNameList.add(origin.get(Calendar.YEAR) + "年" + (origin.get(Calendar.MONTH) + 2) + "月");
        }
        if (oneMore) {
            if (nowDate >= 28) {
                columnNameList.add(origin.get(Calendar.YEAR) + "年" + (origin.get(Calendar.MONTH) + 3) + "月");
            } else {
                columnNameList.add(origin.get(Calendar.YEAR) + "年" + (origin.get(Calendar.MONTH) + 2) + "月");
            }
        }
        return columnNameList;
    }

    /**
     * 逾期统计:数据List
     *
     * @param partner 逾期客户DTO
     * @param months  统计月数
     * @param oneMore 是否多统计一个月
     * @return 数据List
     */
    public List<Object[]> findOverdue(OverdueDTO partner, int months, boolean oneMore) {
        // 因为包含当前月，所以先减1个月
        months--;
        StringBuilder sqlStringBuilder = new StringBuilder("SELECT yap.parent_code, yap.amount_delivery, yap.amount_collected, IFNULL(ds.department,'-') AS 部门, " +
                "IFNULL( yap.customer_service_staff, '-' ) AS 业务员, yap.name AS 往来单位名称, yap.payment_month AS 账期月, " +
                "IF(yap.parent_name IS NULL OR (yap.parent_name = '' AND yap.settlement_type != '2') OR (yap.settlement_type = '1' AND yap.parent_code = '0'), '现款', yap.payment_date) AS 账期日, IF(yap.parent_name IS NULL OR yap.parent_name = '' OR (yap.settlement_type = '1' AND yap.parent_code = '0'), yap.NAME, yap.parent_name) AS 订货客户, yap.receivables AS 应收总计, " +
                "yap.amount_delivery + yap.opening_balance - yap.amount_collected + yap.amount_refund AS 逾期款, yap.opening_balance AS 期初应收 ");
        // 当前时间
        Calendar now = Calendar.getInstance();
        // 开始时间 = 当前时间 - 统计的月数
        Calendar origin = Calendar.getInstance();
        // 如果是大于等于28日，则算下一个账期月
        int nowDate = now.get(Calendar.DAY_OF_MONTH);
        if (nowDate >= 28) {
            months = months - 1;
        }
        if (months < 0) {
            months = 0;
        }
        origin.add(Calendar.MONTH, -months);
        while (origin.before(now)) {
            sqlStringBuilder.append(", MAX(CASE months WHEN '").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + 1).append("月销售' THEN vssm.amount ELSE 0 END) AS ").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + 1).append("月 ");
            sqlStringBuilder.append(", MIN(CASE months WHEN '").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + 1).append("月退货' THEN vssm.amount ELSE 0 END) AS ").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + 1).append("月退货 ");
            sqlStringBuilder.append(", MIN( CASE vsr.months_received WHEN '").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + 1).append("月收款' THEN vsr.amount_received ELSE 0 END ) AS ").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + 1).append("月收款 ");
            // 加1个月
            origin.add(Calendar.MONTH, 1);
        }
        // 多统计1个月
        sqlStringBuilder.append(", MAX(CASE months WHEN '").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + 1).append("月销售' THEN vssm.amount ELSE 0 END) AS ").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + 1).append("月 ");
        sqlStringBuilder.append(", MIN(CASE months WHEN '").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + 1).append("月退货' THEN vssm.amount ELSE 0 END) AS ").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + 1).append("月退货 ");
        sqlStringBuilder.append(", MIN( CASE vsr.months_received WHEN '").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + 1).append("月收款' THEN vsr.amount_received ELSE 0 END ) AS ").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + 1).append("月收款 ");
        // 如果大于等于28日，等于下个账期月
        if (nowDate >= 28) {
            sqlStringBuilder.append(", MAX(CASE months WHEN '").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + 2).append("月销售' THEN vssm.amount ELSE 0 END) AS ").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + 2).append("月 ");
            sqlStringBuilder.append(", MIN(CASE months WHEN '").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + 2).append("月退货' THEN vssm.amount ELSE 0 END) AS ").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + 2).append("月退货 ");
            sqlStringBuilder.append(", MIN( CASE vsr.months_received WHEN '").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + 2).append("月收款' THEN vsr.amount_received ELSE 0 END ) AS ").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + 2).append("月收款 ");
        }
        if (oneMore) {
            int moreNum = 2;
            if (nowDate >= 28) {
                moreNum = 3;
            }
            sqlStringBuilder.append(", MAX(CASE months WHEN '").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + moreNum).append("月销售' THEN vssm.amount ELSE 0 END) AS ").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + moreNum).append("月 ");
            sqlStringBuilder.append(", MIN(CASE months WHEN '").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + moreNum).append("月退货' THEN vssm.amount ELSE 0 END) AS ").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + moreNum).append("月退货 ");
            sqlStringBuilder.append(", MIN( CASE vsr.months_received WHEN '").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + moreNum).append("月收款' THEN vsr.amount_received ELSE 0 END ) AS ").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + moreNum).append("月收款 ");
        }
        sqlStringBuilder.append(" FROM YY_AA_Partner yap ");
        sqlStringBuilder.append(" LEFT JOIN v_settlement_sales_months_v3 vssm ON yap.id = vssm.settlementId ");
        sqlStringBuilder.append(" LEFT JOIN v_settlement_received vsr ON yap.id = vsr.settlementId ");
        sqlStringBuilder.append(" LEFT JOIN YY_AA_Partner yapp ON yapp.`code` = yap.parent_code ");
        sqlStringBuilder.append(" LEFT JOIN dept_staff ds ON yap.customer_service_staff = ds.NAME ");
        sqlStringBuilder.append(" WHERE yap.status = 0 ");
        if (partner != null && StringUtils.isNotBlank(partner.getCode())) {
            sqlStringBuilder.append(" AND yap.code = '").append(partner.getCode()).append("' ");
        }
        if (partner != null && StringUtils.isNotBlank(partner.getName())) {
            sqlStringBuilder.append(" AND yap.name = '").append(partner.getName()).append("' ");
        }
        if (partner != null && StringUtils.isNotBlank(partner.getOnlyOverdue()) && partner.getOnlyOverdue().equals("true")) {
            sqlStringBuilder.append(" AND yap.receivables > 0 ");
        }
        sqlStringBuilder.append(" GROUP BY ");
        sqlStringBuilder.append(" yap.id, ds.department, yapp.customer_service_staff, yap.code, yap.parent_code, yap.payment_month, yap.payment_date, yap.name, yap.amount_delivery, yap.amount_collected, yap.opening_balance ");
        if (partner != null) {
            sqlStringBuilder.append(" ORDER BY yap.parent_code DESC, yap.name ASC LIMIT ").append(partner.getOffset()).append(", ").append(partner.getLimit());
        } else {
            sqlStringBuilder.append(" ORDER BY yap.parent_code DESC, 账期日 ASC ");
        }
        Query query = entityManager.createNativeQuery(sqlStringBuilder.toString());
        return query.getResultList();
    }

    /**
     * 逾期统计:数据List，客服版本
     *
     * @param partner 逾期客户DTO
     * @param months  统计月数
     * @param oneMore 是否多统计一个月
     * @return 数据List
     */
    public List<Object[]> findOverdueStaff(OverdueDTO partner, int months, boolean oneMore) {
        // 因为包含当前月，所以先减1个月
        months--;
        StringBuilder sqlStringBuilder = new StringBuilder("SELECT yap.parent_code, yap.amount_delivery, yap.amount_collected, IFNULL( ds.department, '-' ) AS 部门, " +
                "yap.customer_service_staff AS 业务员, yap.NAME AS 往来单位名称, yap.payment_month AS 账期月, " +
                "IF(yap.parent_name IS NULL OR ( yap.parent_name = '' AND yap.settlement_type != '2' ) OR ( yap.settlement_type = '1' AND yap.parent_code = '0' ), '现款', yap.payment_date) AS 账期日, IF ( yap.parent_name IS NULL OR yap.parent_name = '' OR ( yap.settlement_type = '1' AND yap.parent_code = '0' ), yap.NAME, yap.parent_name) AS 订货客户, " +
                "yap.amount_delivery + yap.opening_balance - yap.amount_collected + yap.amount_refund AS 应收总计, yap.amount_delivery + yap.opening_balance - yap.amount_collected + yap.amount_refund AS 逾期款, yap.opening_balance AS 期初应收 ");
        // 当前时间
        Calendar now = Calendar.getInstance();
        // 开始时间 = 当前时间 - 统计的月数
        Calendar origin = Calendar.getInstance();
        // 如果是大于等于28日，则算下一个账期月
        int nowDate = now.get(Calendar.DAY_OF_MONTH);
        if (nowDate >= 28) {
            months = months - 1;
        }
        if (months < 0) {
            months = 0;
        }
        origin.add(Calendar.MONTH, -months);
        while (origin.before(now)) {
            sqlStringBuilder.append(", MAX(CASE months WHEN '").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + 1).append("月销售' THEN vssm.amount ELSE 0 END) AS ").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + 1).append("月 ");
            sqlStringBuilder.append(", MIN(CASE months WHEN '").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + 1).append("月退货' THEN vssm.amount ELSE 0 END) AS ").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + 1).append("月退货 ");
            sqlStringBuilder.append(", MIN( CASE vsr.months_received WHEN '").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + 1).append("月收款' THEN vsr.amount_received ELSE 0 END ) AS ").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + 1).append("月收款 ");
            // 加1个月
            origin.add(Calendar.MONTH, 1);
        }
        // 多统计1个月
        sqlStringBuilder.append(", MAX(CASE months WHEN '").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + 1).append("月销售' THEN vssm.amount ELSE 0 END) AS ").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + 1).append("月 ");
        sqlStringBuilder.append(", MIN(CASE months WHEN '").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + 1).append("月退货' THEN vssm.amount ELSE 0 END) AS ").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + 1).append("月退货 ");
        sqlStringBuilder.append(", MIN( CASE vsr.months_received WHEN '").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + 1).append("月收款' THEN vsr.amount_received ELSE 0 END ) AS ").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + 1).append("月收款 ");
        // 如果大于等于28日，等于下个账期月
        if (nowDate >= 28) {
            sqlStringBuilder.append(", MAX(CASE months WHEN '").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + 2).append("月销售' THEN vssm.amount ELSE 0 END) AS ").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + 2).append("月 ");
            sqlStringBuilder.append(", MIN(CASE months WHEN '").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + 2).append("月退货' THEN vssm.amount ELSE 0 END) AS ").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + 2).append("月退货 ");
            sqlStringBuilder.append(", MIN( CASE vsr.months_received WHEN '").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + 2).append("月收款' THEN vsr.amount_received ELSE 0 END ) AS ").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + 2).append("月收款 ");
        }
        if (oneMore) {
            int moreNum = 2;
            if (nowDate >= 28) {
                moreNum = 3;
            }
            sqlStringBuilder.append(", MAX(CASE months WHEN '").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + moreNum).append("月销售' THEN vssm.amount ELSE 0 END) AS ").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + moreNum).append("月 ");
            sqlStringBuilder.append(", MIN(CASE months WHEN '").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + moreNum).append("月退货' THEN vssm.amount ELSE 0 END) AS ").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + moreNum).append("月退货 ");
            sqlStringBuilder.append(", MIN( CASE vsr.months_received WHEN '").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + moreNum).append("月收款' THEN vsr.amount_received ELSE 0 END ) AS ").append(origin.get(Calendar.YEAR)).append("年").append(origin.get(Calendar.MONTH) + moreNum).append("月收款 ");
        }
        sqlStringBuilder.append(" FROM yy_aa_partner_staff yap ");
        sqlStringBuilder.append(" LEFT JOIN v_settlement_sales_months_v4 vssm ON yap.settlement_id = vssm.settlementId AND vssm.customer_service_staff = yap.customer_service_staff AND yap.opening_data = 0 ");
        sqlStringBuilder.append(" LEFT JOIN dept_staff ds ON ds.name = yap.customer_service_staff ");
        sqlStringBuilder.append(" LEFT JOIN v_settlement_received_v2 vsr ON yap.id = vsr.settlementId AND vsr.customer_service_staff = vssm.customer_service_staff AND yap.opening_data = 0 ");
        sqlStringBuilder.append(" LEFT JOIN YY_AA_Partner yapp ON yapp.`code` = yap.parent_code ");
        sqlStringBuilder.append(" WHERE yap.status = 0 ");
        if (partner != null && StringUtils.isNotBlank(partner.getCode())) {
            sqlStringBuilder.append(" AND yap.code = '").append(partner.getCode()).append("' ");
        }
        if (partner != null && StringUtils.isNotBlank(partner.getName())) {
            sqlStringBuilder.append(" AND yap.name = '").append(partner.getName()).append("' ");
        }
        if (partner != null && StringUtils.isNotBlank(partner.getOnlyOverdue()) && partner.getOnlyOverdue().equals("true")) {
            sqlStringBuilder.append(" AND yap.receivables > 0 ");
        }
        sqlStringBuilder.append(" GROUP BY ");
        sqlStringBuilder.append("yap.id, ds.department, yap.customer_service_staff, yap.CODE, yap.parent_code, yap.payment_month, yap.payment_date, yap.NAME, yap.amount_delivery, yap.amount_collected, yap.opening_balance, yap.parent_name, yap.settlement_type, yap.receivables, yap.amount_refund");
        if (partner != null) {
            sqlStringBuilder.append(" ORDER BY yap.parent_code DESC, yap.name ASC LIMIT ").append(partner.getOffset()).append(", ").append(partner.getLimit());
        } else {
            sqlStringBuilder.append(" ORDER BY yap.parent_code DESC, 账期日, yap.name ASC ");
        }
        Query query = entityManager.createNativeQuery(sqlStringBuilder.toString());
        return query.getResultList();
    }

    /**
     * 逾期统计:数据总数量
     *
     * @param partner 逾期客户DTO
     * @return 数量
     */
    public BigInteger findOverdueCount(OverdueDTO partner) {
        StringBuilder sqlCountStringBuilder = new StringBuilder("SELECT COUNT(1) FROM ( SELECT 1 ");
        sqlCountStringBuilder.append(" FROM YY_AA_Partner yap ");
        sqlCountStringBuilder.append(" LEFT JOIN v_settlement_sales_months_v3 vssm ON yap.id = vssm.settlementId ");
        sqlCountStringBuilder.append(" LEFT JOIN dept_staff ds ON yap.customer_service_staff = ds.name ");
        sqlCountStringBuilder.append(" WHERE yap.status = 0 ");
        if (partner != null && StringUtils.isNotBlank(partner.getCode())) {
            sqlCountStringBuilder.append(" AND yap.code = '").append(partner.getCode()).append("' ");
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
     * 逾期统计获取数据列表
     *
     * @param partner OverdueDTO
     * @param months  统计的月份
     * @param isZero  true - 未到账期显示0
     * @param oneMore 是否多统计1个月
     * @param all     true - 导出全部，不进行删减
     * @param isStaff 是否是客服版，true - 是
     * @return 逾期列表
     */
    public List<List<Object>> getOverdueList(OverdueDTO partner, int months, boolean isZero, boolean oneMore, boolean all, boolean isStaff) {
        // 数据List
        List<Object[]> overdueSalesList;
        if (isStaff) {  // 客服版
            overdueSalesList = findOverdueStaff(partner, months, oneMore);
        } else { // 非客服版
            overdueSalesList = findOverdue(partner, months, oneMore);
        }
        // 数据结果列表
        List<List<Object>> rowsList = new ArrayList<>();
        for (Object[] dataRow : overdueSalesList) {
            // 数据行List
            ArrayList<Object> dataList = new ArrayList<>();
            // 账期月, 目前rs第6列
            int month = Integer.parseInt(dataRow[6].toString());
            // 账期日，目前rs第7列
            int day = 0;
            // 现款客户标记
            boolean cash = false;
            if (StringUtils.isNumeric(dataRow[7].toString())) {
                day = Integer.parseInt(dataRow[7].toString());
            } else {
                cash = true;
            }
            // 应减去的结算周期数
            int overdueMonths = CommonUtils.overdueMonth(month, day);
            // 仓库标记
            boolean warehouse = false;
            // 关联code不是零是仓库特殊客户
            if (!dataRow[0].toString().equals("0")) {
                warehouse = true;
            }
            // 应收总计
            BigDecimal receivables = new BigDecimal(dataRow[9].toString());
            // 当前逾期金额
            BigDecimal overdue = new BigDecimal(dataRow[10].toString());
            // 设置数据行，移除前3列（关联id列、总发货、总收款）
            for (int i = 3; i < dataRow.length; i++) {
                if (i > 11) {  // 计算每个账期的应收款
                    if (i >= dataRow.length - overdueMonths * 3) { // 未逾期月份
                        /*
                         * 20191226修改：
                         * 1. 预期总额等于各月逾期金额之和。
                         * 2. 仓库特殊用户，退款金额不抵扣之前的欠款，只计算当月。
                         *
                         * 之前的规则：
                         * 有关联的账期客户逾期总金额不计算未到账期的退货金额，无关联关系的账期客户预期总金额计算所有退货金额
                         * 分月统计全部计算所有退货金额
                         *
                         * 20200103：仓库客户，逾期款不需要减每个月收款，只减退货金额，之前的逻辑是错的。
                         */
                        // 减发货金额
                        overdue = overdue.subtract(new BigDecimal(dataRow[i++].toString()));
                        if (warehouse) { // 标记仓库
                            // 逾期款扣除退货金额
                            overdue = overdue.subtract(new BigDecimal(dataRow[i++].toString()));
                            // 当月发货+退货=实际发货金额
                            BigDecimal monthReceivables = new BigDecimal(dataRow[i - 2].toString()).add(new BigDecimal(dataRow[i - 1].toString()));
                            dataList.add(monthReceivables);
                        } else { // 不是仓库客户，逾期款不需要扣除退货金额
                            // 设置当月发货金额
                            dataList.add(new BigDecimal(dataRow[i - 1].toString()));
                            i++;
                        }
                    } else { // 逾期月份
                        // 发货+退货 = 实际发货金额
                        BigDecimal monthReceivables = new BigDecimal(dataRow[i++].toString()).add(new BigDecimal(dataRow[i++].toString()));
                        dataList.add(monthReceivables);
                    }
                } else if (i > 10) {
                    dataList.add(new BigDecimal(dataRow[i].toString()));
                } else {
                    dataList.add(dataRow[i].toString());
                }
            }
            // 应收总计设置为数字格式
            dataList.set(6, receivables);
            // 现款客户，逾期款等于应收总计
            if (cash) {
                overdue = new BigDecimal(dataList.get(6).toString());
                dataList.set(7, overdue);
            }
            // 导出数据，如果不是需要全部数据，按规则删除不需要的数据
            if (!all && partner == null) {
                // 去掉应收总计、逾期款都为0的
                if (overdue.compareTo(BigDecimal.ZERO) == 0 && receivables.compareTo(BigDecimal.ZERO) == 0) {
                    continue;
                }
                // 不是仓库的，也不是客服版，去掉应收总计小于等于0的
                if (!warehouse && !isStaff && receivables.compareTo(BigDecimal.ZERO) < 1) {
                    continue;
                }
            }
            if (cash) { // 现款客户
                for (int index = dataList.size() - 1; index > 8; index--) {
                    if (overdue.compareTo(BigDecimal.ZERO) == 0) { // 逾期款等于0，所有账期逾期金额都是0
                        dataList.set(index, 0);
                    } else if (overdue.compareTo(BigDecimal.ZERO) < 0) {  // 逾期金额小于0
                        // dataList.size()-index ：倒数第几个，从倒数第一个开始。
                        int dataIndex = dataList.size() - index;
                        // dataRow从最后一个向前，每3列一个收款金额. receiveIndex表示dataRow的倒数第几个，比如倒数第三个，倒数第六个。
                        int deliveryIndex = ((dataIndex - 1) * 3) + 3;
                        // dataRow从倒数第二个向前，每3列一个退款金额. refundIndex表示dataRow的倒数第几个，比如倒数第二个，倒数第五个。
                        int refundIndex = ((dataIndex - 1) * 3) + 2;
                        // 当月发货金额
                        BigDecimal monthDelivery = new BigDecimal(dataRow[dataRow.length - deliveryIndex].toString());
                        // 当月退货金额
                        BigDecimal monthRefund = new BigDecimal(dataRow[dataRow.length - refundIndex].toString());

                        if (monthDelivery.compareTo(BigDecimal.ZERO) == 0 && monthRefund.compareTo(BigDecimal.ZERO) == 0) { // 如果发货和退货都是0，跳过，本月就是0
                            dataList.set(index, 0);
                        } else if (overdue.compareTo(new BigDecimal(dataList.get(index).toString())) > -1) {
                            // 对比收款，如果逾期金额大于等于当月发货金额+退货金额，那么显示逾期的数量（因为是负数，所以和正数的逻辑是反的）
                            dataList.set(index, overdue);
                            overdue = BigDecimal.ZERO;
                        } else {
                            // 如果逾期金额小于当月收款金额+退款金额，那么显示收款金额+退货金额，相减得到新的逾期金额
                            dataList.set(index, dataList.get(index).toString());
                            overdue = overdue.subtract((new BigDecimal(dataList.get(index).toString())));
                        }
                    } else {  // 逾期金额大于0，从最后一个开始分摊逾期金额
                        // 如果当月总计金额小于等于0，说明当月有预收款，设置当月为0，然后计算下一个月。
                        if (new BigDecimal(dataList.get(index).toString()).compareTo(BigDecimal.ZERO) < 1) {
                            dataList.set(index, BigDecimal.ZERO);
                            continue;
                        }
                        // 大于等于当月发货，设置当月发货金额
                        if (overdue.compareTo(new BigDecimal(dataList.get(index).toString())) > -1) {
                            overdue = overdue.subtract(new BigDecimal(dataList.get(index).toString()));
                            dataList.set(index, new BigDecimal(dataList.get(index).toString()));
                        } else {
                            // 小于当月发货，设置逾期金额，并置零
                            dataList.set(index, overdue);
                            overdue = BigDecimal.ZERO;
                        }
                    }
                }
            } else { // 账期客户
                /*
                 * 20191227:
                 * 未逾期，应显示应收金额，每月应收 + 每月逾期 + 期初应收 = 总应收
                 *
                 * 未逾期的应收款 = 应收金额 - 逾期款
                 *
                 * 20200114:
                 * 单结算客户：
                 * 逾期款小于0：未逾期额应收款 = 应收金额
                 * 逾期款大于0：未逾期的应收款 = 应收金额 - 逾期款，并设置逾期金额为0
                 */
                BigDecimal tempReceivables = receivables.subtract(overdue);
                if (!warehouse) { // 非仓库客户，也就是单结算客户
                    if (overdue.compareTo(BigDecimal.ZERO) < 0) {
                        tempReceivables = receivables;
                        overdue = BigDecimal.ZERO;
                    }
                }

                for (int index = dataList.size() - 1; index > dataList.size() - 1 - overdueMonths; index--) {
                    if (tempReceivables.compareTo(BigDecimal.ZERO) == 0 || isZero) { // 等于0，或者未到账期显示0标记为true
                        dataList.set(index, 0);
                    } else if (tempReceivables.compareTo(BigDecimal.ZERO) > 0) { // 大于0
                        if (tempReceivables.compareTo(new BigDecimal(dataList.get(index).toString())) > -1) {
                            tempReceivables = tempReceivables.subtract(new BigDecimal(dataList.get(index).toString()));
                            dataList.set(index, dataList.get(index));
                        } else {
                            dataList.set(index, tempReceivables);
                            tempReceivables = BigDecimal.ZERO;
                        }
                    } else { // 小于0
                        if (new BigDecimal(dataList.get(index).toString()).compareTo(BigDecimal.ZERO) == 0) { // 发货+退货为0
                            dataList.set(index, 0);
                        } else if (tempReceivables.compareTo(new BigDecimal(dataList.get(index).toString())) > -1) { // 应收大于等于发货+退货
                            dataList.set(index, tempReceivables);
                            tempReceivables = BigDecimal.ZERO;
                        } else { // 应收小于发货
                            dataList.set(index, new BigDecimal(dataList.get(index).toString()));
                            tempReceivables = tempReceivables.subtract(new BigDecimal(dataList.get(index).toString()));
                        }
                    }
                }
                // 设置逾期款
                dataList.set(7, overdue);
                /*
                 * 20200104：如果逾期金额小于等于0：
                 * 1.逾期月份的金额先设置为0；
                 * 2.讲逾期金额（预付款）放在最后一个账期，！！！注意:如果最后一个账期，所有相关联的用户都没有正数的应收款，则后移，按此规则一直移到统计当月的下个月
                 */
                for (int index = dataList.size() - 1 - overdueMonths; index > 8; index--) {
                    // 逾期款小于等于0，所有账期月、期初应收都是0
                    if (overdue.compareTo(BigDecimal.ZERO) < 1) {
                        /*
                         * 20200103：
                         * 1.逾期款为负数，那么显示在最后一个账期。
                         */
                        if (index == dataList.size() - 1 - overdueMonths) {
                            dataList.set(index, overdue);
                        } else {
                            dataList.set(index, BigDecimal.ZERO);
                        }
                        overdue = BigDecimal.ZERO;
                    } else {  // 逾期金额大于0，从最后一个开始分摊逾期金额
                        // 大于等于当月发货，设置当月发货金额
                        if (overdue.compareTo(new BigDecimal(dataList.get(index).toString())) > -1) {
                            overdue = overdue.subtract(new BigDecimal(dataList.get(index).toString()));
                            // 发货金额大于等于0
                            dataList.set(index, new BigDecimal(dataList.get(index).toString()));
                        } else {
                            // 小于当月发货，设置逾期金额，并置零
                            dataList.set(index, overdue);
                            overdue = BigDecimal.ZERO;
                        }
                    }
                }
            }
            // 设置期初应收
            dataList.set(8, overdue);
            // 账期客户进行账期月设置
            if (!cash) {
                for (int overdueIndex = 0; overdueIndex < month; overdueIndex++) {
                    // 插入0
                    dataList.add(9, 0);
                    // 删除最后一位
                    dataList.remove(dataList.size() - 1);
                }
            }
            rowsList.add(dataList);
        }
        return rowsList;
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

    public Object getAccountspayable(Map<String, String> requestMap, String explan) {
        Map<String, Object> result = new HashMap<>();
        result.put("customerName", requestMap.get("customerName"));
        result.put("queryStartDate", requestMap.get("startDate"));
        result.put("queryEndDate", requestMap.get("endDate"));
        result.put("explan", explan);
        //应付列表
        List<Map<String, Object>> mapList = this.getAccountspayableList(requestMap, explan);
        //应付前期结转
        Map<String, Object> accountspayableCount = this.getAccountspayableCount(requestMap, explan);
        BigDecimal initInvoiceBalance = new BigDecimal(accountspayableCount.get("initPaymentBalance") == null ?
                "0" : accountspayableCount.get("initPaymentBalance").toString());//还应付款
        BigDecimal initPaymentBalance = new BigDecimal(accountspayableCount.get("initInvoiceBalance") == null ?
                "0" : accountspayableCount.get("initInvoiceBalance").toString());//还应开票

        result.put("initInvoiceBalance", initInvoiceBalance);
        result.put("initPaymentBalance", initPaymentBalance);
        BigDecimal deliverTotalAmount = new BigDecimal(0);
        BigDecimal invoiceBalanceTotalAmount = new BigDecimal(0);
        BigDecimal collectTotalAmount = new BigDecimal(0);
        BigDecimal receivableTotalAmount = new BigDecimal(0);
        BigDecimal invoiceTotalAmount = new BigDecimal(0);
        if (mapList != null && mapList.size() > 0) {
            for (Map<String, Object> map : mapList) {
                String type = map.get("type").toString();
                BigDecimal amount = new BigDecimal(map.get("Amount").toString());
                //
                map.put("voucherdate", map.get("voucherdate").toString().substring(0, 10));
                switch (type) {
                    case "1":
                        initPaymentBalance = initPaymentBalance.add(amount);
                        initInvoiceBalance = initInvoiceBalance.add(amount);
                        collectTotalAmount = collectTotalAmount.add(amount);
                        break;
                    case "2":
                        initPaymentBalance = initPaymentBalance.subtract(amount);
                        receivableTotalAmount = receivableTotalAmount.add(amount);
                        break;
                    case "3":
                        initInvoiceBalance = initInvoiceBalance.subtract(amount);
                        invoiceTotalAmount = invoiceTotalAmount.add(amount);
                        break;
                    default:
                        break;
                }
                map.put("invoiceBalanceAmount", initInvoiceBalance);
                map.put("receivableBalance", initPaymentBalance);
            }

            deliverTotalAmount = (BigDecimal) mapList.get(mapList.size() - 1).get("receivableBalance");
            invoiceBalanceTotalAmount = (BigDecimal) mapList.get(mapList.size() - 1).get("invoiceBalanceAmount");
        } else {
            deliverTotalAmount = initPaymentBalance;
            invoiceBalanceTotalAmount = initInvoiceBalance;
        }
        result.put("deliverTotalAmount", deliverTotalAmount);
        result.put("invoiceBalanceTotalAmount", invoiceBalanceTotalAmount);
        result.put("collectTotalAmount", collectTotalAmount);
        result.put("receivableTotalAmount", receivableTotalAmount);
        result.put("invoiceTotalAmount", invoiceTotalAmount);
        result.put("arrDetail", mapList);
        return result;
    }

    private List<Map<String, Object>> getAccountspayableList(Map<String, String> requestMap, String explan) {
        StringBuilder sb = new StringBuilder();
        ArrayList<Object> list = new ArrayList<>();
        sb.append(" select ss.* from (");
        sb.append(" select code,voucherdate,OrigTotalTaxAmount Amount,'1' type,IdPartner,");
        sb.append(" '线上' pubuserdefnvc1 from PU_PurchaseArrival where voucherState =189 and voucherdate >='2018/12/28 00:00:00'");
        sb.append(" union all select ppi.purchaseInvoiceNo code,ppi.voucherdate,ppi.totalTaxAmount Amount,'2' type,ppi.IdPartner,");
        sb.append(" '线上' pubuserdefnvc1 from pu_PurchaseInvoice ppi");
        sb.append(" left join PU_PurchaseArrival ppa on ppi.sourceVoucherCode = ppa.code where ppi.voucherState =189");
        sb.append(" union all select code,voucherdate,Amount,'3' type,IdPartner,'线上' pubuserdefnvc1 from ARAP_ReceivePayment where idbusitype =80");
        sb.append(" ) ss left join AA_Partner AA on ss.IdPartner = AA.ID where 1=1 and AA.name =? and pubuserdefnvc1 = ?");
        list.add(requestMap.get("customerName"));
        list.add(explan);
        sb.append(" and voucherdate >='" + requestMap.get("startDate") + "'");
        sb.append(" and voucherdate <='" + requestMap.get("endDate") + "'");
        sb.append(" order by voucherdate");
        return jdbcTemplate.queryForList(sb.toString(), list.toArray());
    }

    private Map<String, Object> getAccountspayableCount(Map<String, String> requestMap, String explan) {
        StringBuilder sb = new StringBuilder();
        ArrayList<Object> list = new ArrayList<>();
        sb.append(" select sum(case type when '0' then Amount when '1' then Amount when '3' then -Amount end) initPaymentBalance,");
        sb.append(" sum(case type when '0' then Amount when '1' then Amount when '2' then -Amount end) initInvoiceBalance from (");
        sb.append(" select code,origDate voucherdate,Amount,'0' type,IdPartner,'线上' pubuserdefnvc1 from ARAP_OriginalAmount_ApDetail UNION ALL");
        sb.append(" select code,voucherdate,OrigTotalTaxAmount Amount,'1' type,IdPartner,");
        sb.append(" '线上' pubuserdefnvc1 from PU_PurchaseArrival where voucherState =189 and voucherdate >='2018/12/28 00:00:00'");
        sb.append(" union all select ppi.purchaseInvoiceNo code,ppi.voucherdate,ppi.totalTaxAmount Amount,'2' type,ppi.IdPartner,");
        sb.append(" '线上' pubuserdefnvc1 from pu_PurchaseInvoice ppi");
        sb.append(" left join PU_PurchaseArrival ppa on ppi.sourceVoucherCode = ppa.code where ppi.voucherState =189");
        sb.append(" union all select code,voucherdate,Amount,'3' type,IdPartner,'线上' pubuserdefnvc1 from ARAP_ReceivePayment where idbusitype =80");
        sb.append(" ) ss left join AA_Partner AA on ss.IdPartner = AA.ID where 1=1 and AA.name =? and pubuserdefnvc1 = ?");
        list.add(requestMap.get("customerName"));
        list.add(explan);
        sb.append(" and voucherdate <'" + requestMap.get("startDate") + "'");
        return jdbcTemplate.queryForMap(sb.toString(), list.toArray());
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
        Date date = new Date();
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
        // sheet页
        Sheet sheet1 = new Sheet(1, 0);
        sheet1.setSheetName(name);
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
        reusltEnumMap.put(ExcelPropertyEnum.FILENAME, name + "(" + startDate + "_" + endDate + ")");
        reusltEnumMap.put(ExcelPropertyEnum.MERGE, mergeRowNumList);

        return reusltEnumMap;
    }

}
