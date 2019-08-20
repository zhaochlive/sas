package com.js.sas.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OrderProductService {

    @Autowired
    @Qualifier(value = "secodJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    /**
     * 订单产品
     * @param params
     * @return
     */
    public List<Map<String, Object>> getPage(Map<String, String> params) {
        List<Object> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder("select op.pdname,op.pdid,op.standard,level1||'-'||level2||'-'||level3 classify,pi.material," +
                "op.gradeno,pi.brand,pi.mark,pi.surfacetreatment,pi.packagetype,op.unit,op.price,op.num,round(op.price*op.num,2) amount,os.orderno,os.isonline ");
        sb.append(" from orderproduct op ");
        sb.append(" left join ProductInfo pi on op.pdid = pi.id ");
        sb.append(" left join orders os on os.id = op.orderid");
        sb.append(" where 1=1 and os.orderstatus <>7");
        if (params.containsKey("orderno")&&StringUtils.isNotBlank(params.get("orderno"))) {
            sb.append(" and os.orderno = ?");
            list.add(params.get("orderno").trim());
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
    public Long getCount(Map<String ,String > params){
        List<Object> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder("select count(1) from orderproduct op");
        sb.append(" left join orders os on os.id = op.orderid where os.orderstatus <>7 ");
        if (params.containsKey("orderno")&&StringUtils.isNotBlank(params.get("orderno"))) {
            sb.append(" and os.orderno = ?");
            list.add(params.get("orderno").trim());
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
        return jdbcTemplate.queryForObject(sb.toString(),list.toArray(),Long.class);
    }

}
