package com.js.sas.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author daniel
 * @description: 客服类类处理service
 * @create: 2019年9月21日
 */
@Service
public class SettlementCustomerService {

    @Autowired
    @Qualifier(value = "secodJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    /**
     * 参数：ex: key=year va='2019'年份
     * @param params
     * @return
     */
    public List<Map<String ,Object>> getSettlementCustomerPage(Map<String ,String > params,String year){
        if(year==null) {
            Calendar now = Calendar.getInstance();
            now.setTime(new Date());
            year = now.getWeekYear()+"";
        }
        List<Object> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder(" select invoiceheadup,");
        sb.append(" round(sum(case when tb.months = '"+year+"-12' then totalprice else 0 end), 2)  十二月,");
        sb.append(" round(sum(case when tb.months = '"+year+"-11' then totalprice else 0 end), 2)  十一月,");
        sb.append(" round(sum(case when tb.months = '"+year+"-10' then totalprice else 0 end), 2)  十月,");
        sb.append(" round(sum(case when tb.months = '"+year+"-09' then totalprice else 0 end), 2)  九月,");
        sb.append(" round(sum(case when tb.months = '"+year+"-08' then totalprice else 0 end), 2) 八月,");
        sb.append(" round(sum(case when tb.months = '"+year+"-07' then totalprice else 0 end), 2)  七月,");
        sb.append(" round(sum(case when tb.months = '"+year+"-06' then totalprice else 0 end), 2)  六月,");
        sb.append(" round(sum(case when tb.months = '"+year+"-05' then totalprice else 0 end), 2)  五月,");
        sb.append(" round(sum(case when tb.months = '"+year+"-04' then totalprice else 0 end), 2)  四月,");
        sb.append(" round(sum(case when tb.months = '"+year+"-03' then totalprice else 0 end), 2)  三月,");
        sb.append(" round(sum(case when tb.months = '"+year+"-02' then totalprice else 0 end), 2)  二月,");
        sb.append(" round(sum(case when tb.months = '"+year+"-01' then totalprice else 0 end), 2)  一月,");
        sb.append(" round(sum(totalprice), 2) total");
        sb.append(" from (select invoiceheadup,sum(totalprice) totalprice,months from ( ");
        sb.append(" select (CASE WHEN br.invoiceheadup IS NULL THEN CASE WHEN bc.companyname IS NULL THEN mm.realname ELSE bc.companyname END ELSE br.invoiceheadup END) invoiceheadup,");
        sb.append(" to_char(o.createtime, 'yyyy-mm') months,totalprice from orders o LEFT JOIN member m on o.saleid=m.id LEFT JOIN member mm on o.memberid=mm.id");
        sb.append(" LEFT JOIN billingrecord br ON br.orderno = o.id :: VARCHAR LEFT JOIN buyercompanyinfo bc on o.memberid=bc.memberid ");
        sb.append(" where 1=1 and o.orderstatus <> 7 and to_char(o.createtime, 'yyyy')=? ");
        list.add(year);
        if (params.get("invoiceheadup")!=null&& StringUtils.isNotBlank(params.get("invoiceheadup"))){
            sb.append(" and (br.invoiceheadup=? or bc.companyname =? or mm.realname = ?)");
            list.add(params.get("invoiceheadup"));
            list.add(params.get("invoiceheadup"));
            list.add(params.get("invoiceheadup"));
        }
        sb.append(" )ta GROUP BY invoiceheadup,months )tb GROUP BY invoiceheadup ");
        if (StringUtils.isNotBlank(params.get("sort"))) {
            if (StringUtils.isNotBlank(params.get("sortOrder"))&&"desc".equalsIgnoreCase(params.get("sortOrder"))) {
                sb.append(" order by " + params.get("sort") + "  desc");
            }else{
                sb.append(" order by "+ params.get("sort") +" asc");
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
//        System.out.println(sb.toString());
        return jdbcTemplate.queryForList(sb.toString(), list.toArray());
    }

    public Long getSettlementCustomerCount(Map<String ,String > params,String year){
        List<Object> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("select count(1) from (select invoiceheadup,sum(totalprice) totalprice from (");
        sb.append("select (CASE WHEN br.invoiceheadup IS NULL THEN CASE WHEN bc.companyname IS NULL THEN mm.realname ELSE bc.companyname END ELSE br.invoiceheadup END) AS invoiceheadup,to_char(o.createtime, 'yyyy-mm') months,totalprice\n" +
                " from orders o LEFT JOIN member m on o.saleid=m.id  " +
                " LEFT JOIN member mm on o.memberid=mm.id " +
                " LEFT JOIN billingrecord br ON br.orderno = o.id :: VARCHAR " +
                " LEFT JOIN buyercompanyinfo bc on o.memberid=bc.memberid " +
                " where 1=1 and o.orderstatus <> 7 and to_char(o.createtime, 'yyyy')=?");
        list.add(year);
        if (params.get("invoiceheadup")!=null&& StringUtils.isNotBlank(params.get("invoiceheadup"))){
            sb.append(" and (br.invoiceheadup=? or bc.companyname =? or mm.realname = ?)");
            list.add(params.get("invoiceheadup"));
            list.add(params.get("invoiceheadup"));
            list.add(params.get("invoiceheadup"));
        }
        sb.append(" )ta GROUP BY invoiceheadup )tb ");
        return jdbcTemplate.queryForObject(sb.toString(),list.toArray(),Long.class);
    }
}
