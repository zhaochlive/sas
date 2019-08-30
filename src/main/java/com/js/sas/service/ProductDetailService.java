package com.js.sas.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ProductDetailService {

    @Autowired
    @Qualifier(value = "secodJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    public List<Map<String ,Object>> getPage(Map<String ,String > params)throws ParseException {
        List<Object> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append(" select P.productname,p.level3,p.material,p.cardnum,p.stand,p.brand,p.mark,p.surfacetreatment,p.packagetype,p.unit," +
                "sc.companyname,round(tb.sumprice,2) sumprice,p.id,round(p.weight*tb.num,2) weight,tb.cut,tb.num");
        sb.append(" from ProductInfo p left join sellercompanyinfo sc on p.memberid = sc.memberid ");
        sb.append(" LEFT JOIN (select sum(sumprice) sumprice,pdid,count(pdid) cut,sum(tb.num) num from ( ");
        sb.append(" select op.price*op.num sumprice,op.num,op.id,op.pdid from orderproduct op " );
        sb.append(" left join orders os on os.id = op.orderid where 1=1 and orderstatus <>7");
        if (params.get("startDate") != null && StringUtils.isNotBlank(params.get("startDate"))) {
            sb.append(" and os.createtime >= ?");
            list.add(DateUtils.parseDate(params.get("startDate") + " 00:00:00", "YYYY-MM-dd HH:mm:ss"));
        }
        if (params.get("endDate") != null && StringUtils.isNotBlank(params.get("endDate"))) {
            sb.append(" and os.createtime <= ?");
            list.add(DateUtils.parseDate(params.get("endDate") + " 23:59:59", "YYYY-MM-dd HH:mm:ss"));
        }
        sb.append(" ) tb GROUP BY pdid )tb on tb.pdid = p.id ");
        sb.append(" where tb.sumprice is not null ");
        if (params.containsKey("companyname") && StringUtils.isNotBlank(params.get("companyname"))) {
            sb.append(" and sc.companyname = ?");
            list.add(params.get("companyname"));
        }
        sb.append(" ORDER BY tb.sumprice desc ");
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
        return jdbcTemplate.queryForList(sb.toString(),list.toArray());
    }

    public Long getCount(Map<String, String> map) {
        String sql ="select count(1) from (select count(1) from orderproduct op LEFT JOIN orders os ON os.ID = op.orderid WHERE orderstatus <> 7  GROUP BY pdid) tb";
        return jdbcTemplate.queryForObject(sql,Long.class);
    }
}
