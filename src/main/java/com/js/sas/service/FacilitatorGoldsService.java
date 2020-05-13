package com.js.sas.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author: daniel
 * @date: 2020/5/13 0013 12:50
 * @Description:
 */
@Service
public class FacilitatorGoldsService {

    @Autowired
    @Qualifier(value = "secodJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    private static final String jinshangRate = "2";

    public List<Map<String, Object>> getFacilitatorGoldsForJinShang(Map<String, String> params) {
        List<Object> list = new ArrayList<>();
        String rate = jinshangRate;
        String year = params.get("year").trim();
        StringBuilder builder = new StringBuilder("SELECT tb.member_company_name companyName,round(sum(totalprice)*0.02,2) total,");
        builder.append(" round(sum(case when tb.years = '"+year+"-12' then totalprice else 0 end)*"+rate+"/100, 2)  十二月,");
        builder.append(" round(sum(case when tb.years = '"+year+"-11' then totalprice else 0 end)*"+rate+"/100, 2)  十一月,");
        builder.append(" round(sum(case when tb.years = '"+year+"-10' then totalprice else 0 end)*"+rate+"/100, 2)  十月,");
        builder.append(" round(sum(case when tb.years = '"+year+"-09' then totalprice else 0 end)*"+rate+"/100, 2)  九月,");
        builder.append(" round(sum(case when tb.years = '"+year+"-08' then totalprice else 0 end)*"+rate+"/100, 2) 八月,");
        builder.append(" round(sum(case when tb.years = '"+year+"-07' then totalprice else 0 end)*"+rate+"/100, 2)  七月,");
        builder.append(" round(sum(case when tb.years = '"+year+"-06' then totalprice else 0 end)*"+rate+"/100, 2)  六月,");
        builder.append(" round(sum(case when tb.years = '"+year+"-05' then totalprice else 0 end)*"+rate+"/100, 2)  五月,");
        builder.append(" round(sum(case when tb.years = '"+year+"-04' then totalprice else 0 end)*"+rate+"/100, 2)  四月,");
        builder.append(" round(sum(case when tb.years = '"+year+"-03' then totalprice else 0 end)*"+rate+"/100, 2)  三月,");
        builder.append(" round(sum(case when tb.years = '"+year+"-02' then totalprice else 0 end)*"+rate+"/100, 2)  二月,");
        builder.append(" round(sum(case when tb.years = '"+year+"-01' then totalprice else 0 end)*"+rate+"/100, 2)  一月");
        builder.append(" from (SELECT fa.id,fa.memberid,fa.member_company_name,fa.sign_starttime,fa.sign_endtime,");
        builder.append(" SUM(O.totalprice) totalprice,to_char(o.createtime, 'yyyy-mm') years");
        builder.append(" from facilitator fa ");
        builder.append(" inner join orders o on o.memberid = fa.memberid and o.createtime BETWEEN fa.sign_starttime and sign_endtime");
        builder.append(" where o.shopname ='紧商科技紧固件自营'");
        builder.append(" GROUP BY fa.id,to_char(o.createtime, 'yyyy-mm'))tb ");
        builder.append(" GROUP BY tb.memberid,tb.member_company_name ");
        if (StringUtils.isNotBlank(params.get("limit"))) {
            long limit = Long.parseLong(params.get("limit").trim());
            builder.append(" limit ? ");
            list.add(limit);
        } else {
            builder.append(" limit 20 ");
        }
        if (StringUtils.isNotBlank(params.get("offset"))) {
            long offset = Long.parseLong(params.get("offset").trim());
            builder.append(" offset ? ;");
            list.add(offset);
        } else {
            builder.append(" offset 0 ;");
        }
        return jdbcTemplate.queryForList(builder.toString(),list.toArray());
    }
    public List<Map<String, Object>> getFacilitatorGoldsForAoZhan(Map<String, String> params) {
        List<Object> list = new ArrayList<>();
        String year = params.get("year").trim();
        StringBuilder builder = new StringBuilder("SELECT tb.member_company_name companyName,round(sum(totalprice),2) total,");
        builder.append(" round(sum(case when tb.years = '"+year+"-12' then totalprice else 0 end), 2)  十二月,");
        builder.append(" round(sum(case when tb.years = '"+year+"-11' then totalprice else 0 end), 2)  十一月,");
        builder.append(" round(sum(case when tb.years = '"+year+"-10' then totalprice else 0 end), 2)  十月,");
        builder.append(" round(sum(case when tb.years = '"+year+"-09' then totalprice else 0 end), 2)  九月,");
        builder.append(" round(sum(case when tb.years = '"+year+"-08' then totalprice else 0 end), 2) 八月,");
        builder.append(" round(sum(case when tb.years = '"+year+"-07' then totalprice else 0 end), 2)  七月,");
        builder.append(" round(sum(case when tb.years = '"+year+"-06' then totalprice else 0 end), 2)  六月,");
        builder.append(" round(sum(case when tb.years = '"+year+"-05' then totalprice else 0 end), 2)  五月,");
        builder.append(" round(sum(case when tb.years = '"+year+"-04' then totalprice else 0 end), 2)  四月,");
        builder.append(" round(sum(case when tb.years = '"+year+"-03' then totalprice else 0 end), 2)  三月,");
        builder.append(" round(sum(case when tb.years = '"+year+"-02' then totalprice else 0 end), 2)  二月,");
        builder.append(" round(sum(case when tb.years = '"+year+"-01' then totalprice else 0 end), 2)  一月");
        builder.append(" from (SELECT fa.id,fa.memberid,fa.member_company_name,fa.sign_starttime,fa.sign_endtime,");
        builder.append(" SUM(O.totalprice) totalprice,to_char(o.createtime, 'yyyy-mm') years");
        builder.append(" from facilitator fa inner join (SELECT o.id,o.createtime,o.memberid,o.shopname,");
        builder.append(" sum(CASE WHEN gradeno='304' THEN op.actualpayment*0.008 WHEN gradeno='316' THEN op.actualpayment*0.008");
        builder.append(" WHEN gradeno='304L' THEN op.actualpayment*0.018 WHEN gradeno='321' THEN op.actualpayment*0.028");
        builder.append(" WHEN gradeno='2205' THEN op.actualpayment*0.028 WHEN gradeno='2520' THEN op.actualpayment*0.028");
        builder.append(" WHEN gradeno='660' THEN op.actualpayment*0.028 END) totalprice");
        builder.append(" from orders o INNER JOIN orderproduct op on o.id = op.orderid");
        builder.append(" where op.gradeno in('304','316','304L','321','2205','2520','660') ");
        builder.append(" GROUP BY o.id) o on o.memberid = fa.memberid and o.createtime BETWEEN fa.sign_starttime and sign_endtime");
        builder.append(" where o.shopname = '奥展实业' GROUP BY fa.id,to_char(o.createtime, 'yyyy-mm'))tb");
        builder.append(" GROUP BY tb.memberid,tb.member_company_name");
        if (StringUtils.isNotBlank(params.get("limit"))) {
            long limit = Long.parseLong(params.get("limit").trim());
            builder.append(" limit ? ");
            list.add(limit);
        } else {
            builder.append(" limit 20 ");
        }
        if (StringUtils.isNotBlank(params.get("offset"))) {
            long offset = Long.parseLong(params.get("offset").trim());
            builder.append(" offset ? ;");
            list.add(offset);
        } else {
            builder.append(" offset 0 ;");
        }
        return jdbcTemplate.queryForList(builder.toString(),list.toArray());
    }


    public Integer getFacilitatorCount(Map<String, String> params){
        String shopname ;
        if ("jinshang".equals(params.get("goldType"))){
            shopname ="紧商科技紧固件自营";
        }else if ("aozhan".equals(params.get("goldType"))){
            shopname ="奥展实业";
        }else {
            return -1;
        }
        String sql ="select count(*) from( SELECT fa.id,fa.memberid,fa.member_company_name,fa.sign_starttime,fa.sign_endtime,SUM(O.totalprice) totalpr" +
            " from facilitator fa " +
            " inner join orders o on o.memberid = fa.memberid and o.createtime BETWEEN fa.sign_starttime and sign_endtime" +
            " where o.shopname = '"+shopname +"'"+
            " GROUP BY fa.id)tb";
        return jdbcTemplate.queryForObject(sql,Integer.class);
    }


}
