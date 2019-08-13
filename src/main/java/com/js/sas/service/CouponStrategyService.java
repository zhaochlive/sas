package com.js.sas.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CouponStrategyService {

    @Autowired
    @Qualifier(value = "secodJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> getData(Map<String, String> params) throws ParseException {
        List<Object> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder("select yhset.id, yhset.name,count(tb.id) 发放量,");
        sb.append(" sum(case when tb.status = 99 then 1 else 0 end) 使用量,");
        sb.append(" sum(case when tb.status = 2 then 1 else 0 end) 领取未使用量,");
        sb.append(" sum(totalprice) 订单总额,sum(actualpayment) 付款总额,sum(discountprice) 优惠总额,");
        sb.append(" cut 首次下单用户数量 from yhq_ticketset yhset ");
        sb.append(" left join (select yh.id, sum(os.totalprice) totalprice,sum(os.actualpayment) actualpayment,sum(os.discountprice) discountprice,yh.status,yh.ticketsetid,");
        sb.append(" sum(CASE WHEN os.isticket =1 THEN 1 ELSE 0 END) isticket from yhq_ticket yh left JOIN orders os on os.ticketno = yh.no ");
        sb.append(" and os.orderstatus <> 7 where 1=1" );
        if (params.get("startDate") != null && StringUtils.isNotBlank(params.get("startDate"))) {
            sb.append(" and yh.createtime >= ?");
            list.add(DateUtils.parseDate(params.get("startDate") + " 00:00:00", "YYYY-MM-dd HH:mm:ss"));
        }
        if (params.get("endDate") != null && StringUtils.isNotBlank(params.get("endDate"))) {
            sb.append(" and yh.createtime <= ?");
            list.add(DateUtils.parseDate(params.get("endDate") + " 23:59:59", "YYYY-MM-dd HH:mm:ss"));
        }
        sb.append(" GROUP BY yh.no,yh.name,yh.status,yh.id,yh.ticketsetid )tb on tb.ticketsetid = yhset.id");
        sb.append(" LEFT JOIN (select sum(case when customers =1 then 1 else 0 end)  cut,ticketsetid from (");
        sb.append(" select yh.id,yh.name,yh.status,yh.ticketsetid,count(os.id) customers from yhq_ticket yh ");
        sb.append(" left JOIN (SELECT ID, totalprice, actualpayment, shopname, discountprice , orderstatus ,ticketno");
        sb.append(" FROM orders WHERE ID IN ( SELECT MIN (ID) ID FROM orders where orderstatus <> 7 GROUP BY memberid )  and ticketno !=''");
        sb.append(" ) os on os.ticketno = yh.no GROUP BY yh.id)ss GROUP BY ticketsetid ");
        sb.append(" )firstorder on firstorder.ticketsetid = yhset.id where 1=1");
        if (params.get("ticket") != null && StringUtils.isNotBlank(params.get("ticket"))) {
            sb.append(" and yhset.id = ?");
            list.add(Long.parseLong(params.get("ticket")));
        }
        sb.append(" ");
        sb.append(" GROUP BY yhset.id,yhset.name ,firstorder.cut");
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
        String sql = "select count(1) from yhq_ticketset ";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public List<Map<String, Object>> getTickets() {

        String sql = "select id,name from yhq_ticketset ";
        return jdbcTemplate.queryForList(sql);
    }
}
