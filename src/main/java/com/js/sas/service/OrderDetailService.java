package com.js.sas.service;

import com.js.sas.utils.DateTimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

/**
 * @author daniel
 */
@Service
public class OrderDetailService {

    @Autowired
    @Qualifier(value = "secodJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    /**
     * 店铺详情
     * @param params
     * @return
     */
    public List<Map<String, Object>> getPage(Map<String, String> params) {
        if (params != null) {
            if (!params.containsKey("startDate")||!params.containsKey("endDate")){
                return null;
            }
            String startDate = params.get("startDate");
            String endDate = params.get("endDate");
            Date StartDate = DateTimeUtils.convert(startDate);
            Date EndDate = DateTimeUtils.convert(endDate);
            Timestamp lastMonthStartDate = DateTimeUtils.addTime(StartDate, -1, DateTimeUtils.MONTH);
            Timestamp lastMonthEndDate = DateTimeUtils.addTime(EndDate, -1, DateTimeUtils.MONTH);
            Timestamp lastYearStartDate = DateTimeUtils.addTime(StartDate, -1, DateTimeUtils.YEAR);
            Timestamp lastYearEndDate = DateTimeUtils.addTime(EndDate, -1, DateTimeUtils.YEAR);

            List<Object> list = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            sb.append(" select m.username,m.realname,M.creditstate,m.waysalesman,m.id memberid,al.totalprice,al.ordernum,st.standardn,tbmonth.totalprice mtotalprice,tbyear.totalprice ytotalprice");
            sb.append(" from member m LEFT JOIN (");
            sb.append(" SELECT count(DISTINCT(standard)) standard,os.memberid from orders os ");
            sb.append(" left JOIN orderproduct od on od.orderid = os.id where os.orderstatus <> 7 and os.createtime>='"+startDate+"' and os.createtime <='"+endDate+"' ");
            sb.append(" GROUP BY os.memberid) tb on tb.memberid = m.ID ");
            sb.append(" LEFT JOIN ( SELECT COUNT(os.ID) ordernum, SUM ( totalprice ) totalprice, os.memberid ");
            sb.append(" FROM orders os WHERE os.orderstatus <> 7 and os.createtime>='"+startDate+"' and os.createtime <='"+endDate+"' ");
            sb.append(" GROUP BY os.memberid )al ON al.memberid = M.ID");
            sb.append(" LEFT JOIN ( select round(avg(standardn),2) standardn,memberid from ( ");
            sb.append(" select os.orderno,op.standardn,os.memberid from orders os LEFT JOIN (");
            sb.append(" SELECT COUNT(DISTINCT(standard)) standardn,orderno FROM orderproduct GROUP BY orderno) op on os.orderno =op.orderno");
            sb.append(" where os.orderstatus <> 7 and os.createtime>='"+startDate+"' and os.createtime <='"+endDate+"' ");
            sb.append(" GROUP BY os.orderno,op.standardn,os.memberid )ss ");
            sb.append(" GROUP BY memberid) st on st.memberid = m.ID");
            sb.append(" left join ( SELECT count(id) ordernum,memberid,sum(totalprice) totalprice from orders os");
            sb.append(" where os.orderstatus <> 7 and os.createtime>='"+lastMonthStartDate+"' and os.createtime <='"+lastMonthEndDate+"' ");
            sb.append(" GROUP BY memberid ) tbmonth on tbmonth.memberid = m.ID");
            sb.append(" left join ( SELECT count(id) ordernum,memberid,sum(totalprice) totalprice from orders os");
            sb.append(" where os.orderstatus <> 7 and os.createtime>='"+lastYearStartDate+"' and os.createtime <='"+lastYearEndDate+"' ");
            sb.append(" GROUP BY memberid ) tbyear on tbyear.memberid = m.ID");
            sb.append(" where al.ordernum is not null ");
            if(params.containsKey("username")){
                sb.append( "and m.realname ='"+params.get("username").trim()+"'");
            }
            sb.append(" ORDER BY al.totalprice DESC");

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
            System.out.println(sb.toString());
            List<Map<String, Object>> maps = jdbcTemplate.queryForList(sb.toString(),list.toArray());

            return maps;
        }
        return null;
    }


    /**
     *
     * @param params
     * @return
     */
    public Long getCount(Map<String ,String > params){

        StringBuilder sb = new StringBuilder("SELECT count(1) from  MEMBER M LEFT JOIN ( SELECT COUNT(os.ID) ordernum,");
        sb.append(" COUNT(DISTINCT ( standard )) standard,os.memberid,SUM ( totalprice ) totalprice FROM orders os");
        sb.append(" LEFT JOIN orderproduct od ON od.orderid = os.ID WHERE os.orderstatus <> 7");
        sb.append(" and os.createtime>='"+params.get("startDate")+"' and os.createtime <='"+params.get("endDate")+"'");
        sb.append(" GROUP BY os.memberid ) tb ON tb.memberid = M.ID  WHERE tb.ordernum IS NOT NULL ");
        if(params.containsKey("username")){
            sb.append( " and m.username ='"+params.get("username").trim()+"'");
        }
        return jdbcTemplate.queryForObject(sb.toString(),Long.class);
    }

    public List<Map<String, Object>> getProvinceRateByMemberId(long memberid, Map<String, String> params){
        String startDate = params.get("startDate");
        String endDate = params.get("endDate");
        Date StartDate = DateTimeUtils.convert(startDate);
        Date EndDate = DateTimeUtils.convert(endDate);
        String sql ="select count(1),memberid,sum(totalprice) totalprice,province from orders WHERE  memberid =?" +
                "and createtime >= ? and createtime <= ? and orderstatus <> 7  GROUP BY memberid,province order by sum(totalprice) desc ";
        return jdbcTemplate.queryForList(sql, memberid,StartDate,EndDate);
    }

}