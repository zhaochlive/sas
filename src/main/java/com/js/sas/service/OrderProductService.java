package com.js.sas.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OrderProductService {

    @Autowired
    @Qualifier(value = "secodJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    /**
     * 订单产品
     *
     * @param params
     * @return
     */
    public List<Map<String, Object>> getPage(Map<String, String> params) {
        List<Object> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder("select op.pdname,op.pdid,op.standard,level1||'-'||level2||'-'||level3 classify,pi.material," +
                "op.gradeno,pi.brand,pi.mark,pi.surfacetreatment,pi.packagetype,op.unit,op.price,op.num,round(op.price*op.num,2) amount,os.orderno,os.isonline ");
        sb.append(" from orderproduct op ");
        sb.append(" left join ProductInfo pi on op.pdid = pi.id ");
        sb.append(" left join orders os on os.id = op.orderid");
        sb.append(" where 1=1 and os.orderstatus <>7");
        if (params.containsKey("orderno") && StringUtils.isNotBlank(params.get("orderno"))) {
            sb.append(" and os.orderno = ?");
            list.add(params.get("orderno").trim());
        }
        if (params.containsKey("startDate")) {
            sb.append(" and os.createtime >=?");
            Timestamp alarmStartTime = Timestamp.valueOf(params.get("startDate") + " 00:00:00");
            list.add(alarmStartTime);
        }
        if (params.containsKey("endDate")) {
            sb.append(" and os.createtime <=?");
            Timestamp alarmStartTime = Timestamp.valueOf(params.get("endDate") + " 23:59:59");
            list.add(alarmStartTime);
        }

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

    public Long getCount(Map<String, String> params) {
        List<Object> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder("select count(1) from orderproduct op");
        sb.append(" left join orders os on os.id = op.orderid where os.orderstatus <>7 ");
        if (params.containsKey("orderno") && StringUtils.isNotBlank(params.get("orderno"))) {
            sb.append(" and os.orderno = ?");
            list.add(params.get("orderno").trim());
        }
        if (params.containsKey("startDate")) {
            sb.append(" and os.createtime >=?");
            Timestamp alarmStartTime = Timestamp.valueOf(params.get("startDate") + " 00:00:00");
            list.add(alarmStartTime);
        }
        if (params.containsKey("endDate")) {
            sb.append(" and os.createtime <=?");
            Timestamp alarmStartTime = Timestamp.valueOf(params.get("endDate") + " 23:59:59");
            list.add(alarmStartTime);
        }
        return jdbcTemplate.queryForObject(sb.toString(), list.toArray(), Long.class);
    }

    public Integer getReceiptListCount(Map<String, String> params) {
        List<Object> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder(" select count(*)");
        sb.append(" from orders o ");
        sb.append(" LEFT JOIN member mm on o.saleid=mm.id  ");
        sb.append(" LEFT JOIN billorder bo on o.id=bo.orderid");
        sb.append(" LEFT JOIN billingrecord blr on bo.billrecordid = blr.id");
        sb.append(" where o.orderstatus not in (0,7) ");
        if (params.containsKey("invoiceName") && StringUtils.isNotBlank(params.get("invoiceName"))) {
            sb.append(" and blr.invoiceheadup = ?");
            list.add(params.get("invoiceName"));
        }
        if (params.containsKey("username") && StringUtils.isNotBlank(params.get("username"))) {
            sb.append(" and mm.realname = ?");
            list.add(params.get("username"));
        }
        if (params.containsKey("paytype") && StringUtils.isNotBlank(params.get("paytype"))) {
            sb.append(" and o.paytype = ?");
            list.add(Integer.valueOf(params.get("paytype")));
        }else {
            sb.append(" and paytype in (0,1,2)");
        }
        if (params.containsKey("clerkname") && StringUtils.isNotBlank(params.get("clerkname"))) {
            sb.append(" and o.clerkname = ?");
            list.add(params.get("clerkname"));
        }
        if (params.containsKey("salesman") && StringUtils.isNotBlank(params.get("salesman"))) {
            sb.append(" and mm.waysalesman = ?");
            list.add(params.get("salesman"));
        }
        if (params.containsKey("startDate")) {
            sb.append(" and o.paymenttime >=?");
            Timestamp alarmStartTime = Timestamp.valueOf(params.get("startDate") + " 00:00:00");
            list.add(alarmStartTime);
        }
        if (params.containsKey("endDate")) {
            sb.append(" and o.paymenttime <=?");
            Timestamp alarmStartTime = Timestamp.valueOf(params.get("endDate") + " 23:59:59");
            list.add(alarmStartTime);
        }
        return jdbcTemplate.queryForObject(sb.toString(), Integer.class,list.toArray());
    }

    public List<Map<String, Object>> receiptList(Map<String, String> params) {
        List<Object> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder("select o.paytype,o.orderno,o.transactionid,mm.realname,blr.invoiceheadup,mm.waysalesman,o.clerkname,paymenttime,o.totalprice");
        sb.append(" from orders o ");
        sb.append(" LEFT JOIN member mm on o.saleid=mm.id  ");
        sb.append(" LEFT JOIN billorder bo on o.id=bo.orderid");
        sb.append(" LEFT JOIN billingrecord blr on bo.billrecordid = blr.id");
        sb.append(" where o.orderstatus not in (0,7)   ");
        if (params.containsKey("invoiceName") && StringUtils.isNotBlank(params.get("invoiceName"))) {
            sb.append(" and blr.invoiceheadup = ?");
            list.add(params.get("invoiceName"));
        }
        if (params.containsKey("username") && StringUtils.isNotBlank(params.get("username"))) {
            sb.append(" and mm.realname = ?");
            list.add(params.get("username"));
        }
        if (params.containsKey("paytype") && StringUtils.isNotBlank(params.get("paytype"))) {
            sb.append(" and o.paytype = ?");
            list.add(Integer.valueOf(params.get("paytype")));
        }else {
            sb.append(" and paytype in (0,1,2)");
        }
        if (params.containsKey("clerkname") && StringUtils.isNotBlank(params.get("clerkname"))) {
            sb.append(" and o.clerkname = ?");
            list.add(params.get("clerkname"));
        }
        if (params.containsKey("salesman") && StringUtils.isNotBlank(params.get("salesman"))) {
            sb.append(" and mm.waysalesman = ?");
            list.add(params.get("salesman"));
        }
        if (params.containsKey("startDate")) {
            sb.append(" and o.paymenttime >=?");
            Timestamp alarmStartTime = Timestamp.valueOf(params.get("startDate") + " 00:00:00");
            list.add(alarmStartTime);
        }
        if (params.containsKey("endDate")) {
            sb.append(" and o.paymenttime <=?");
            Timestamp alarmStartTime = Timestamp.valueOf(params.get("endDate") + " 23:59:59");
            list.add(alarmStartTime);
        }
        if (StringUtils.isNotBlank(params.get("sort"))) {
            if (StringUtils.isNotBlank(params.get("sortOrder"))) {
                sb.append(" order by " + params.get("sort") +" "+ params.get("sortOrder"));
            }else{
                sb.append(" order by "+ params.get("sort") +" desc");
            }
        }
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
}
