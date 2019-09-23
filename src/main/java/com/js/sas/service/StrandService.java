package com.js.sas.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author daniel
 * @description: 品牌类处理service
 * @create: 2019-09-20 11:34
 */
@Service
public class StrandService {

    @Autowired
    @Qualifier(value = "secodJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    /**
     * 参数：ex: key=year va='2019'年份
     * 品牌参数：ex: key=srand 品牌
     * @param params
     * @return
     */
    public List<Map<String ,Object>> getStrandSalesPage(Map<String ,String > params,String year){
        if(year==null) {
            return null;
        }else{
            int ye = Integer.parseInt(year);
            if(ye<2017||ye>2035){
                return null;
            }
        }
        List<Object> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder(" select brand,");
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
        sb.append(" select sum(op.price*op.num) totalprice,op.brand,to_char(os.createtime, 'yyyy-mm') years");
        sb.append(" from orderproduct op left JOIN orders os on os.orderno = op.orderno ");
        sb.append(" where os.orderstatus <> 7 and to_char(os.createtime, 'yyyy') = ?");
        list.add(year);
        if (params.get("brand")!=null&& StringUtils.isNotBlank(params.get("brand"))){
            sb.append(" and op.brand =?");
            list.add(params.get("brand"));
        }
        sb.append(" GROUP BY brand,years)tb GROUP BY brand ");
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

    public Long getStrandSalesCount(Map<String ,String > params,String year){
        List<Object> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("select count(1) from ( select count(1) from orderproduct op left JOIN orders os on os.orderno = op.orderno " +
                " where os.orderstatus <> 7 and to_char(os.createtime, 'yyyy') =? ");
        list.add(year);
        if (params.get("brand")!=null&& StringUtils.isNotBlank(params.get("brand"))){
            sb.append(" and brand =?");
            list.add(params.get("brand"));
        }
        sb.append(" group by brand) tb");
        return jdbcTemplate.queryForObject(sb.toString(),list.toArray(),Long.class);
    }
}
