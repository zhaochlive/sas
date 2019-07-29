package com.js.sas.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OrderByKeFuService {

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
                Timestamp alarmStartTime = Timestamp.valueOf(params.get("startDate") + " 00:00:00");
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
     * 查询数量
     * @param params
     * @return
     */
    public Long getCount(Map<String, String> params){
        ArrayList<Object> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder("select count(1) cut from ( select clerkname from ( SELECT TO_CHAR(os.createtime,'YYYYMM') as 月, " +
                "  os.clerkname , mb.username FROM orders os LEFT JOIN MEMBER mb ON os.memberid = mb.ID " +
                " LEFT JOIN buyercompanyinfo bc ON mb. ID = bc.memberid WHERE os.orderstatus in (1, 3, 4, 5, 10) ");
        if (params != null) {
            if (params.get("username") != null && StringUtils.isNotBlank(params.get("username"))) {
                sb.append(" and mb.realname = ?");
                list.add(params.get("username"));
            }
            if (params.get("companyname") != null && StringUtils.isNotBlank(params.get("companyname"))) {
                sb.append(" and bc.companyname = ?");
                list.add(params.get("companyname"));
            }
            if (params.get("clerkname") != null && StringUtils.isNotBlank(params.get("clerkname"))) {
                sb.append(" and os.clerkname = ?");
                list.add(params.get("clerkname"));
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
        }
        sb.append(" GROUP BY TO_CHAR(os.createtime,'YYYYMM'),os.clerkname, mb.id ) t where t.clerkname is not null  group by username,clerkname )tb");

        return jdbcTemplate.queryForObject(sb.toString(), list.toArray(),Long.class);
    }


    public List<Map<String, Object>> getPage(Map<String ,String > param){
        if (param == null){
            return null;
        }
        StringBuilder sb = new StringBuilder("select clerkname,username,companyname, ");
        List<String> colums = getColums(param);
        List<Object> list = new ArrayList<>();
        for (String colum : colums) {
            sb.append("sum(case when MONTH='"+colum+"' then total else 0 end) as 月份_"+colum+",");
        }
        sb.append(" SUM(siz) siz from ( SELECT TO_CHAR(os.createtime,'YYYYMM') as MONTH, os.clerkname , mb.username ,bc.companyname ,COUNT(realname) siz ,SUM(os.totalprice) total");
        sb.append(" FROM orders os LEFT JOIN MEMBER mb ON os.memberid = mb.ID LEFT JOIN buyercompanyinfo bc ON mb. ID = bc.memberid ");
        sb.append(" WHERE os.orderstatus in (1, 3, 4, 5, 10)");
        try {
            if (param.get("startDate") != null && StringUtils.isNotBlank(param.get("startDate"))) {
                sb.append(" and os.createtime >= ?");
                list.add(DateUtils.parseDate(param.get("startDate") + " 00:00:00","YYYY-MM-dd HH:mm:ss"));
            }
            if (param.get("endDate") != null && StringUtils.isNotBlank(param.get("endDate"))) {
                sb.append(" and os.createtime <= ?");
                list.add(DateUtils.parseDate(param.get("endDate") + " 23:59:59","YYYY-MM-dd HH:mm:ss"));
            }
            if (param.get("username") != null && StringUtils.isNotBlank(param.get("username"))) {
                sb.append(" and mb.username = ?");
                list.add(param.get("username"));
            }
            if (param.get("companyname") != null && StringUtils.isNotBlank(param.get("companyname"))) {
                sb.append(" and bc.companyname = ?");
                list.add(param.get("companyname"));
            }
            if (param.get("clerkname") != null && StringUtils.isNotBlank(param.get("clerkname"))) {
                sb.append(" and os.clerkname = ?");
                list.add(param.get("clerkname"));
            }
        }catch (ParseException e){
            e.printStackTrace();
            log.info("输入参数格式不正确导致日期转换异常");
        }
        sb.append(" GROUP BY TO_CHAR(os.createtime,'YYYYMM'),os.clerkname, mb.username,bc.companyname ) t");
        sb.append(" where clerkname is not null and clerkname!='' group by clerkname,companyname,username ");

        if (StringUtils.isNotBlank(param.get("sort"))) {
            if (StringUtils.isNotBlank(param.get("sortOrder"))&&"desc".equalsIgnoreCase(param.get("sortOrder"))) {
                sb.append(" order by " + param.get("sort") + "  desc");
            }else{
                sb.append(" order by "+ param.get("sort") +" asc");
            }
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
        log.info(sb.toString());
        List<Map<String, Object>> maps = jdbcTemplate.queryForList(sb.toString(), list.toArray());
        return maps;
    }

}
