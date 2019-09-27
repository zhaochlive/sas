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
public class ClerkManService {

    @Autowired
    @Qualifier(value = "secodJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    /**
     * 参数：ex: key=year va='2019'年份
     * @param params
     * @return
     */
    public List<Map<String ,Object>> getClerkManPage(Map<String ,String > params,String year){
        if(year==null) {
            Calendar now = Calendar.getInstance();
            now.setTime(new Date());
            year = now.getWeekYear()+"";
        }
        List<Object> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder(" select clerkname,");
        sb.append(" round(sum(case when tb.years = '"+year+"-12' then totalprice else 0 end), 2)  十二月,");
        sb.append(" round(sum(case when tb.years = '"+year+"-11' then totalprice else 0 end), 2)  十一月,");
        sb.append(" round(sum(case when tb.years = '"+year+"-10' then totalprice else 0 end), 2)  十月,");
        sb.append(" round(sum(case when tb.years = '"+year+"-09' then totalprice else 0 end), 2)  九月,");
        sb.append(" round(sum(case when tb.years = '"+year+"-08' then totalprice else 0 end), 2) 八月,");
        sb.append(" round(sum(case when tb.years = '"+year+"-07' then totalprice else 0 end), 2)  七月,");
        sb.append(" round(sum(case when tb.years = '"+year+"-06' then totalprice else 0 end), 2)  六月,");
        sb.append(" round(sum(case when tb.years = '"+year+"-05' then totalprice else 0 end), 2)  五月,");
        sb.append(" round(sum(case when tb.years = '"+year+"-04' then totalprice else 0 end), 2)  四月,");
        sb.append(" round(sum(case when tb.years = '"+year+"-03' then totalprice else 0 end), 2)  三月,");
        sb.append(" round(sum(case when tb.years = '"+year+"-02' then totalprice else 0 end), 2)  二月,");
        sb.append(" round(sum(case when tb.years = '"+year+"-01' then totalprice else 0 end), 2)  一月,");
        sb.append(" round(sum(totalprice), 2)   total");
        sb.append(" from (");
        sb.append(" select count(1),sum(totalprice) totalprice,clerkname,to_char(createtime, 'yyyy-mm') years");
        sb.append(" from orders where orderstatus <> 7 and clerkname !=''and to_char(createtime, 'yyyy') =  ?");
        list.add(year);
        if (params.get("clerkname")!=null&& StringUtils.isNotBlank(params.get("clerkname"))){
            sb.append(" and clerkname =?");
            list.add(params.get("clerkname"));
        }
        sb.append(" GROUP BY clerkname,years )tb GROUP BY clerkname ");
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
        return jdbcTemplate.queryForList(sb.toString(), list.toArray());
    }

    public Long getClerkManCount(Map<String ,String > params,String year){
        List<Object> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("select count(1) from ( SELECT clerkname from orders where orderstatus <> 7 and clerkname !='' ");
        if (params.get("clerkname")!=null&& StringUtils.isNotBlank(params.get("clerkname"))){
            sb.append(" and clerkname =?");
            list.add(params.get("clerkname"));
        }
        sb.append(" GROUP BY clerkname) tb");
        return jdbcTemplate.queryForObject(sb.toString(),list.toArray(),Long.class);
    }
}
