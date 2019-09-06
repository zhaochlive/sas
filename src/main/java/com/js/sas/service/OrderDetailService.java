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
     * 店铺详情列表
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

            return jdbcTemplate.queryForList(sb.toString(),list.toArray());
        }
        return null;
    }


    /**
     *订单详情统计count
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

    /**
     * 获取省份列表
     * @param memberid
     * @param params
     * @return
     */
    public List<Map<String, Object>> getProvinceRateByMemberId(long memberid, Map<String, String> params){
        String startDate = params.get("startDate");
        String endDate = params.get("endDate");
        Date StartDate = DateTimeUtils.convert(startDate);
        Date EndDate = DateTimeUtils.convert(endDate);
        String sql ="select count(1),memberid,sum(totalprice) totalprice,province from orders WHERE  memberid =?" +
                "and createtime >= ? and createtime <= ? and orderstatus <> 7  GROUP BY memberid,province order by sum(totalprice) desc ";
        return jdbcTemplate.queryForList(sql, memberid,StartDate,EndDate);
    }

    /**
     * 订单统计主表
     * @param params
     * @return
     */
    public List<Map<String, Object>> getOrderReport(Map<String, String> params){
        List<Object> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append(" SELECT to_char( createtime , 'YYYY-MM-DD' ) AS d,");
        sb.append(" sum(case when orderstatus BETWEEN 3 and 5 then 1 else 0 end ) sentnum,");
        sb.append(" sum(case when orderstatus = 1 or orderstatus = 10 then 1 else 0 end ) notsentnum,");
        sb.append(" sum(case when orderstatus = 7 and reason = '订单超时取消' then 1 else 0 end ) cancelnum, ");
        sb.append(" sum(case when orderstatus = 7 then 1 else 0 end ) backnum, ");
        sb.append(" sum(case when totalprice < 500 then 1 else 0 end ) underfive, ");
        sb.append(" sum(case when totalprice >= 500 and totalprice < 800 then 1 else 0 end ) inFiveEight, ");
        sb.append(" sum(case when totalprice >= 800 and totalprice < 1200 then 1 else 0 end ) inEightTwelve, ");
        sb.append(" sum(case when totalprice >= 1200 and totalprice < 1500 then 1 else 0 end ) inTwelveFifteen, ");
        sb.append(" sum(case when totalprice >= 1500 and totalprice < 2000 then 1 else 0 end ) inFifteenTwenty, ");
        sb.append(" sum(case when totalprice >= 2000 and totalprice < 5000 then 1 else 0 end ) inTwentyFifty, ");
        sb.append(" sum(case when totalprice >= 5000 then 1 else 0 end ) moreFive, ");
        sb.append(" COUNT ( os.id ) AS ordernum,");
        sb.append(" sum(case when sp.cut > 1 then 1 else 0 end ) splitnum");
        sb.append(" FROM orders os LEFT JOIN ( ");
        sb.append(" select count(1) cut,os.orderno,to_char( createtime , 'YYYY-MM-DD' ) dd from orders os ");
        sb.append(" LEFT JOIN orderproduct od on os.id = od.orderid GROUP BY os.orderno,dd");
        sb.append(" ) sp on os.orderno = sp.orderno WHERE 1=1");
        if (params.containsKey("startDate")) {
            sb.append(" and os.createtime >='"+ params.get("startDate")+"'");
        }
        if (params.containsKey("endDate")) {
            sb.append(" and os.createtime <='"+ params.get("endDate")+"'");
        }
        sb.append(" GROUP BY d order by d desc");
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
        return jdbcTemplate.queryForList(sb.toString(),list.toArray());
    }

    public Long getReportCount(Map<String ,String > params){
        List<Object> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder("select count(1) from( select to_char( createtime , 'YYYY-MM-DD' ) d  from orders ");
        sb.append("WHERE 1 = 1 ");
        if (params.containsKey("startDate")) {
            sb.append(" and createtime >=?");
            Timestamp alarmStartTime = Timestamp.valueOf(params.get("startDate") );
            list.add(alarmStartTime);
        }
        if (params.containsKey("endDate")) {
            sb.append(" and createtime <=?");
            Timestamp alarmStartTime = Timestamp.valueOf(params.get("endDate") );
            list.add(alarmStartTime);
        }
        sb.append(" GROUP BY d) d");

        return jdbcTemplate.queryForObject(sb.toString(),list.toArray(),Long.class);
    }


    /**
     * 客单价订单列表
     * @param params
     * @return
     */
    public List<Map<String, Object>> getUnitPrice(Map<String, String> params) {
        List<Object> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder(" select count(1) cut, memberid,m.realname,m.waysalesman," +
                " sum(case when totalprice < 500 then 1 else 0 end ) underfive," +
                " sum(case when totalprice >= 500 and totalprice < 800 then 1 else 0 end ) inFiveEight, " +
                " sum(case when totalprice >= 800 and totalprice < 1200 then 1 else 0 end ) inEightTwelve," +
                " sum(case when totalprice >= 1200 and totalprice < 1500 then 1 else 0 end ) inTwelveFifteen," +
                " sum(case when totalprice >= 1500 and totalprice < 2000 then 1 else 0 end ) inFifteenTwenty," +
                " sum(case when totalprice >= 2000 and totalprice < 5000 then 1 else 0 end ) inTwentyFifty," +
                " sum(case when totalprice >= 5000 then 1 else 0 end ) moreFive" +
                " from (  ");
        sb.append(" SELECT os.memberid,os.orderno,sum(od.num*od.price)/count(1) totalprice");
        sb.append(" from orderproduct od ");
        sb.append(" LEFT JOIN orders os on os.orderno = od.orderno ");
        sb.append(" where os.orderstatus <>7 ");
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
        sb.append(" GROUP BY os.memberid,os.orderno  ) ss");
        sb.append(" LEFT JOIN member m on m.id =ss.memberid ");
        sb.append(" where 1=1 ");
        if (params.get("username") != null && StringUtils.isNotBlank(params.get("username"))) {
            sb.append(" and m.realname = ?");
            list.add(params.get("username"));
        }
        sb.append(" GROUP BY ss.memberid,m.realname,m.waysalesman");
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
        return jdbcTemplate.queryForList(sb.toString(),list.toArray());
    }

    /**
     * 客单价订单数量count
     * @param params
     * @return
     */
    public Long getUnitPriceCount(Map<String, String> params) {
        List<Object> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder(" select count(1) from (  ");
        sb.append(" select count(1) from ( ");
        sb.append(" SELECT os.memberid,os.orderno,sum(od.num*od.price)/count(1) unit");
        sb.append(" from orderproduct od ");
        sb.append(" LEFT JOIN orders os on os.orderno = od.orderno ");
        sb.append(" where os.orderstatus <>7 ");
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
        sb.append(" GROUP BY os.memberid,os.orderno  ) ss");
        sb.append(" LEFT JOIN member m on m.id =ss.memberid ");
        sb.append(" where 1=1 ");
        if (params.get("username") != null && StringUtils.isNotBlank(params.get("username"))) {
            sb.append(" and m.realname = ?");
            list.add(params.get("username"));
        }
        sb.append(" GROUP BY ss.memberid ) tb");

        return jdbcTemplate.queryForObject(sb.toString(),list.toArray(),Long.class);
    }

    /**
     * 订单信息
     * @param params
     * @return
     */
    public List<Map<String, Object>> orderInfo(Map<String, String> params) {
        List<Object> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder("SELECT o.createtime,o.orderno,mm.realname,bc.companyname ,o.totalprice, ");
        sb.append(" (CASE WHEN br.invoiceheadup IS NULL THEN");
        sb.append(" CASE WHEN bc.companyname IS NULL THEN mm.realname ELSE bc.companyname END ELSE br.invoiceheadup END");
        sb.append(" ) AS invoiceheadup,o.membercompany,o.waysalesman,o.clerkname,o.isonline,o.orderstatus,o.shipto,o.phone,o.province||o.city||o.area||o.receivingaddress  address");
        sb.append(" from orders o LEFT JOIN member m on o.saleid=m.id");
        sb.append(" LEFT JOIN member mm on o.memberid=mm.id");
        sb.append(" LEFT JOIN billingrecord br ON br.orderno = o.id :: VARCHAR");
        sb.append(" LEFT JOIN buyercompanyinfo bc on o.memberid=bc.memberid where 1=1");
        if (params.containsKey("startDate")) {
            sb.append(" and o.createtime >=?");
            Timestamp alarmStartTime = Timestamp.valueOf(params.get("startDate") + " 00:00:00");
            list.add(alarmStartTime);
        }
        if (params.containsKey("endDate")) {
            sb.append(" and o.createtime <=?");
            Timestamp alarmStartTime = Timestamp.valueOf(params.get("endDate") + " 23:59:59");
            list.add(alarmStartTime);
        }
        if (params.containsKey("username")&&StringUtils.isNotBlank(params.get("username"))) {
            sb.append(" and mm.realname =?");
            list.add(params.get("username"));
        }
        if (params.containsKey("orderno")&&StringUtils.isNotBlank(params.get("orderno"))) {
            sb.append(" and o.orderno =?");
            list.add(params.get("orderno"));
        }
        if (params.containsKey("buyCompany")&&StringUtils.isNotBlank(params.get("buyCompany"))) {
            sb.append(" and bc.companyname =?");
            list.add(params.get("buyCompany"));
        }
        if (params.containsKey("sellCompany")&&StringUtils.isNotBlank(params.get("sellCompany"))) {
            sb.append(" and o.membercompany =?");
            list.add(params.get("sellCompany"));
        }
        sb.append(" order by  o.createtime desc");
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


    public Long getOrderInfoCount(Map<String, String> params) {
        List<Object> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder("SELECT  count(1)");
        sb.append(" from orders o LEFT JOIN member m on o.saleid=m.id");
        sb.append(" LEFT JOIN member mm on o.memberid=mm.id");
        sb.append(" LEFT JOIN billingrecord br ON br.orderno = o.id :: VARCHAR");
        sb.append(" LEFT JOIN buyercompanyinfo bc on o.memberid=bc.memberid where 1=1");
        if (params.containsKey("username")&&StringUtils.isNotBlank(params.get("username"))) {
            sb.append(" and mm.realname =?");
            list.add(params.get("username"));
        }
        if (params.containsKey("orderno")&&StringUtils.isNotBlank(params.get("orderno"))) {
            sb.append(" and o.orderno =?");
            list.add(params.get("orderno"));
        }
        if (params.containsKey("buyCompany")&&StringUtils.isNotBlank(params.get("buyCompany"))) {
            sb.append(" and bc.companyname =?");
            list.add(params.get("buyCompany"));
        }
        if (params.containsKey("sellCompany")&&StringUtils.isNotBlank(params.get("sellCompany"))) {
            sb.append(" and o.membercompany =?");
            list.add(params.get("sellCompany"));
        }
        if (params.containsKey("startDate")) {
            sb.append(" and o.createtime >=?");
            Timestamp alarmStartTime = Timestamp.valueOf(params.get("startDate") + " 00:00:00");
            list.add(alarmStartTime);
        }
        if (params.containsKey("endDate")) {
            sb.append(" and o.createtime <=?");
            Timestamp alarmStartTime = Timestamp.valueOf(params.get("endDate") + " 23:59:59");
            list.add(alarmStartTime);
        }
        return jdbcTemplate.queryForObject(sb.toString(),list.toArray(),Long.class);
    }
}