package com.js.sas.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RepurchaseRateService {

    @Autowired
    @Qualifier(value = "secodJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    /**
     * 返货查询日期内的所有月份集合
     *
     * @param params
     * @return
     */
    public List<String> getColums(Map<String, String> params) {
        List<Object> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT TO_CHAR(createtime,'YYYYMM') as month from orders  where 1=1 ");
        if (params != null) {
            if (params.containsKey("startDate")) {
                sql.append(" and createtime >=?");
                Timestamp alarmStartTime = Timestamp.valueOf(params.get("startDate") + " 00:00:00" );
                list.add(alarmStartTime);
            }
            if (params.containsKey("endDate")) {
                sql.append(" and createtime <=?");
                Timestamp alarmStartTime = Timestamp.valueOf(params.get("endDate") + " 23:59:59");
                list.add(alarmStartTime);
            }
        }
        sql.append(" GROUP BY TO_CHAR(createtime,'YYYYMM')  order By month desc;");
        return jdbcTemplate.queryForList(sql.toString(), list.toArray(), String.class);
    }

    /**
     * 按月查询用户购买商品次数和金额
     * @param param
     * @return
     */
    public List<Map<String, Object>> getRepurchaseRate(Map<String, String> param) {
        if (param == null){
            return null;
        }
        StringBuilder groupBy = new StringBuilder();
        StringBuilder sb = new StringBuilder("SELECT mb.realname,mb.mobile,bc.companyname ,count(1) 总下单量,min(createtime) firstTime,tb.* from orders od left join ( select ");
        List<String> colums = getColums(param);
        List<Object> list = new ArrayList<>();
        for (String colum : colums) {

            sb.append("sum(case when cha.MONTH ='" + colum + "' then cha.cut else 0 end) as 下单次数_" + colum + ",");
            sb.append("sum(case when cha.MONTH ='" + colum + "' then cha.totalprice else 0 end) as 下单金额_" + colum + ",");
            groupBy.append("下单次数_" + colum + ",下单金额_" + colum + ",");
        }
        sb.append("  cha.memberid  FROM ( SELECT TO_CHAR( os.createtime, 'YYYYMM' ) AS MONTH,os.memberid,COUNT (1) cut,SUM ( os.totalprice ) totalprice ");
        sb.append(" FROM orders os GROUP BY TO_CHAR( os.createtime, 'YYYYMM' ), os.memberid ORDER BY memberid )cha  GROUP BY cha.memberid ) tb on tb.memberid = od.memberid ");
        sb.append(" LEFT JOIN member mb on  tb.memberid = mb.id LEFT JOIN buyercompanyinfo bc ON mb. ID = bc.memberid ");
        sb.append("  where 1=1 ");
        try {
            if (param.get("startDate") != null && StringUtils.isNotBlank(param.get("startDate"))) {
                sb.append(" and od.createtime >= ?");
                list.add(DateUtils.parseDate(param.get("startDate") + " 00:00:00","YYYY-MM-dd HH:mm:ss"));
            }
            if (param.get("endDate") != null && StringUtils.isNotBlank(param.get("endDate"))) {
                sb.append(" and od.createtime <= ?");
                list.add(DateUtils.parseDate(param.get("endDate") + " 23:59:59","YYYY-MM-dd HH:mm:ss"));
            }
            if (param.get("username") != null && StringUtils.isNotBlank(param.get("username"))) {
                sb.append(" and mb.realname = ?");
                list.add(param.get("username"));
            }
            if (param.get("companyname") != null && StringUtils.isNotBlank(param.get("companyname"))) {
                sb.append(" and bc.companyname = ?");
                list.add(param.get("companyname"));
            }
            if (param.get("mobile") != null && StringUtils.isNotBlank(param.get("mobile"))) {
                sb.append(" and mb.mobile = ?");
                list.add(param.get("mobile"));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        sb.append("  GROUP BY ");
        sb.append(groupBy);
        sb.append(" tb.memberid,mb.realname,bc.companyname,mb.mobile  ");
        if (StringUtils.isNotBlank(param.get("sort"))) {
            if (StringUtils.isNotBlank(param.get("sortOrder"))&&"desc".equalsIgnoreCase(param.get("sortOrder"))) {
                sb.append(" order by " + param.get("sort") + "  desc");
            }else{
                sb.append(" order by "+ param.get("sort") +" asc");
            }
        }else {
            sb.append(" order by count(1) desc ");
        }


        if (StringUtils.isNotBlank(param.get("limit"))) {
            long limit = Long.parseLong(param.get("limit").trim());
            sb.append(" limit ? ");
            list.add(limit);
        } else {
            sb.append(" limit 10 ");
        }
        if (StringUtils.isNotBlank(param.get("offset"))) {
            long offset = Long.parseLong(param.get("offset").trim());
            sb.append(" offset ? ;");
            list.add(offset);
        } else {
            sb.append(" offset 0 ;");
        }
//        System.out.println(sb.toString()+"\r\n" +list.toString());
        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(sb.toString(),list.toArray());
        return mapList;
    }

    /**
     * @param param
     * @return
     */
    public Long getCount(Map<String, String> param) {

        try {
            StringBuilder builder = new StringBuilder("select count(1) cut from ( select count(1) ,os.memberid from orders os ");
            builder.append(" LEFT JOIN member mb on  os.memberid = mb.id LEFT JOIN buyercompanyinfo bc ON mb.ID = bc.memberid ");
            builder.append("  where 1=1 ");
            List<Object> list = new ArrayList<>();
            if (param != null) {
                if (param.get("startDate") != null && StringUtils.isNotBlank(param.get("startDate"))) {
                    builder.append(" and os.createtime >= ?");
                    list.add(DateUtils.parseDate(param.get("startDate") + " 00:00:00","YYYY-MM-dd HH:mm:ss"));
                }
                if (param.get("endDate") != null && StringUtils.isNotBlank(param.get("endDate"))) {
                    builder.append(" and os.createtime <= ?");
                    list.add(DateUtils.parseDate(param.get("endDate") + " 23:59:59","YYYY-MM-dd HH:mm:ss"));
                }
                if (param.get("username") != null && StringUtils.isNotBlank(param.get("username"))) {
                    builder.append(" and mb.realname = ?");
                    list.add(param.get("username"));
                }
                if (param.get("companyname") != null && StringUtils.isNotBlank(param.get("companyname"))) {
                    builder.append(" and bc.companyname = ?");
                    list.add(param.get("companyname"));
                }
                if (param.get("mobile") != null && StringUtils.isNotBlank(param.get("mobile"))) {
                    builder.append(" and mb.mobile = ?");
                    list.add(param.get("mobile"));
                }
            }
            builder.append(" group by os.memberid  ) tb");

            return jdbcTemplate.query(builder.toString(), list.toArray(), new ResultSetExtractor<Long>() {
                @Override
                public Long extractData(ResultSet rs) throws SQLException, DataAccessException {
                    if (rs.next()) {
                        return rs.getLong("cut");
                    }
                    return 0L;
                }
            });
        } catch (ParseException e) {
            e.printStackTrace();
            return 0L;
        }
    }
}
