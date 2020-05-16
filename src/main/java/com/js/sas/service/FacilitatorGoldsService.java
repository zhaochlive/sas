package com.js.sas.service;

import com.js.sas.utils.DateTimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

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

    /**
     * 获取所有的服务商
     * @return
     */
    public List<Map<String, Object>>getFacilitator(){
        String sql ="select memberid,member_company_name companyName from facilitator";
        return jdbcTemplate.queryForList(sql);
    }

    private Map<String, Object>getFacilitator(Long memberId){
        String sql ="select * from facilitator where memberid = ? order by sign_starttime desc  limit 1 ";
        return jdbcTemplate.queryForMap(sql,memberId);
    }

    public List<Map<String, Object>> getFacilitatorGoldsForJinShang(Map<String, String> params) {
        List<Object> list = new ArrayList<>();
        String rate = jinshangRate;
        String year;
        if (StringUtils.isNotBlank(params.get("year"))){
            year = params.get("year").trim();
        }else {
            SimpleDateFormat yyyy = new SimpleDateFormat("yyyy");
            year = yyyy.format(new Date());
        }
        StringBuilder builder = new StringBuilder("SELECT tb.member_company_name companyName,tb.memberid,round(sum(totalprice)*0.02,2) total,");
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
        builder.append(" where o.shopname ='紧商科技紧固件自营' and o.orderstatus =5");
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
        String year ;
        if (StringUtils.isNotBlank(params.get("year"))){
            year = params.get("year").trim();
        }else {
            SimpleDateFormat yyyy = new SimpleDateFormat("yyyy");
            year = yyyy.format(new Date());
        }
        StringBuilder builder = new StringBuilder("SELECT tb.member_company_name companyName,tb.memberid,round(sum(totalprice),2) total,");
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
        builder.append(" where op.gradeno in('304','316','304L','321','2205','2520','660') and o.orderstatus =5 ");
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

   public Long getFacilitatorGoldsInfoCount(Map<String, String> params){
       String shopname ;
       if ("jinshang".equals(params.get("goldType"))){
           shopname ="紧商科技紧固件自营";
       }else if ("aozhan".equals(params.get("goldType"))){
           shopname ="奥展实业";
       }else {
           return -1L;
       }
       String facilitator = params.get("facilitator");
       Map<String, Object> maps = getFacilitator(Long.parseLong(facilitator));
       String signStarttime = maps.get("sign_starttime").toString();
       String signEndtime = maps.get("sign_endtime").toString();
       StringBuilder builder = new StringBuilder("select count(1) from ( select o.orderno ");
       builder.append(" from orders o left JOIN orderproduct op on o.id = op.orderid");
       builder.append(" where o.memberid = "+facilitator);
       builder.append(" and o.orderstatus =5 ");
       builder.append(" and o.shopname = '"+shopname +"'");
       builder.append(" and o.createtime >= '"+signStarttime+"'");
       builder.append(" and o.createtime <= '"+signEndtime+"'");
       if (params.get("startDate") != null && StringUtils.isNotBlank(params.get("startDate"))) {
           builder.append(" and o.createtime >= '"+params.get("startDate")+"'");
       }
       if (params.get("endDate") != null && StringUtils.isNotBlank(params.get("endDate"))) {
           builder.append(" and o.createtime <= '"+params.get("endDate")+"'");
       }
       builder.append(" GROUP BY o.id ) tb");
       return jdbcTemplate.queryForObject(builder.toString(),Long.class);
   }

    public List<Map<String ,Object>> getFacilitatorGoldsInfoForJinShang(Map<String, String> params) {
        List<Object> list = new ArrayList<>();
        String facilitator = params.get("facilitator");
        Map<String, Object> maps = getFacilitator(Long.parseLong(facilitator));
        String signStarttime = maps.get("sign_starttime").toString();
        String signEndtime = maps.get("sign_endtime").toString();
        StringBuilder builder = new StringBuilder("select o.orderno,o.createtime,round((o.totalprice*0.02),2) golds,o.totalprice");
        builder.append(" from orders o left JOIN orderproduct op on o.id = op.orderid");
        builder.append(" where o.memberid = "+facilitator);
        builder.append(" and o.orderstatus =5  and o.shopname = '紧商科技紧固件自营' ");
        builder.append(" ");
        builder.append(" and o.createtime >= '"+signStarttime+"'");
        builder.append(" and o.createtime <= '"+signEndtime+"'");
        if (params.get("startDate") != null && StringUtils.isNotBlank(params.get("startDate"))) {
            builder.append(" and o.createtime >= '"+params.get("startDate")+"'");
        }
        if (params.get("endDate") != null && StringUtils.isNotBlank(params.get("endDate"))) {
            builder.append(" and o.createtime <= '"+params.get("endDate")+"'");
        }
        builder.append(" GROUP BY o.id");
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
    public List<Map<String ,Object>> getFacilitatorGoldsInfoForAoZhan(Map<String, String> params) {
        List<Object> list = new ArrayList<>();
        String facilitator = params.get("facilitator");
        Map<String, Object> maps = getFacilitator(Long.parseLong(facilitator));
        String signStarttime = maps.get("sign_starttime").toString();
        String signEndtime = maps.get("sign_endtime").toString();
        StringBuilder builder = new StringBuilder("select o.orderno,o.createtime,round(sum(CASE" +
                " WHEN op.gradeno='304' THEN op.actualpayment*0.008 " +
                " WHEN op.gradeno='316' THEN op.actualpayment*0.008 " +
                " WHEN op.gradeno='304L' THEN op.actualpayment*0.018 " +
                " WHEN op.gradeno='321' THEN op.actualpayment*0.028 " +
                " WHEN op.gradeno='2205' THEN op.actualpayment*0.028 " +
                " WHEN op.gradeno='2520' THEN op.actualpayment*0.028 " +
                " WHEN op.gradeno='660' THEN op.actualpayment*0.028 " +
                "END),4) golds,o.totalprice");
        builder.append(" from orders o left JOIN orderproduct op on o.id = op.orderid");
        builder.append(" where o.memberid = "+facilitator);
        builder.append(" and o.orderstatus =5  and o.shopname = '奥展实业' ");
        builder.append(" and o.createtime >= '"+signStarttime+"'");
        builder.append(" and o.createtime <= '"+signEndtime+"'");
        if (params.get("startDate") != null && StringUtils.isNotBlank(params.get("startDate"))) {
            builder.append(" and o.createtime >= '"+params.get("startDate")+"'");
        }
        if (params.get("endDate") != null && StringUtils.isNotBlank(params.get("endDate"))) {
            builder.append(" and o.createtime <= '"+params.get("endDate")+"'");
        }
        builder.append(" GROUP BY o.id");
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

    public List<Map<String ,Object>> facilitatorOrderInfo(String orderNo) {
        StringBuilder builder = new StringBuilder("select o.orderno,o.createtime,op.gradeno,op.actualpayment,(CASE ");
        builder.append(" WHEN gradeno='304' THEN '0.8%' WHEN gradeno='316' THEN '0.8%' WHEN gradeno='304L' THEN '1.8%'");
        builder.append(" WHEN gradeno='304' THEN '0.8%' WHEN gradeno='316' THEN '0.8%' WHEN gradeno='304L' THEN '1.8%'");
        builder.append(" WHEN gradeno='321' THEN '2.8%' WHEN gradeno='2205' THEN '2.8%' WHEN gradeno='2520' THEN '2.8%'");
        builder.append(" WHEN gradeno='660' THEN '2.8%' END )rate,");
        builder.append(" (CASE WHEN gradeno='304' THEN op.actualpayment*0.008 WHEN gradeno='316' THEN op.actualpayment*0.008");
        builder.append(" WHEN gradeno='304L' THEN op.actualpayment*0.018 WHEN gradeno='321' THEN op.actualpayment*0.028");
        builder.append(" WHEN gradeno='2205' THEN op.actualpayment*0.028 WHEN gradeno='2520' THEN op.actualpayment*0.028");
        builder.append(" WHEN gradeno='660' THEN op.actualpayment*0.028 END) totalprice");
        builder.append(" from orders o INNER JOIN orderproduct op on o.id = op.orderid");
        builder.append(" where op.gradeno in('304','316','304L','321','2205','2520','660')");
        builder.append(" and o.orderstatus =5 and o.shopname = '奥展实业'");
        builder.append(" and o.orderno = '"+orderNo.trim()+"'");
        return jdbcTemplate.queryForList(builder.toString());
    }
}
