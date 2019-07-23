package com.js.sas.service;

import com.js.sas.dto.CustomerOfOrder;
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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CustomerService {

    @Autowired
    @Qualifier("secodJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    public List<CustomerOfOrder> getCustomerOfOrder(Map<String,String> param) throws ParseException {

        if (param==null){
            return  null;
        }
        List<Object > list = new ArrayList<>();
        StringBuilder builder = new StringBuilder("SELECT t1.memberid ,mb.username ,t1.firsttime ,bc.companyname ," +
                " mb.province || mb.city || mb.citysmall as city, mb.address,mb.realname,mb.mobile as mobile," +
                " mb.telephone,mb.waysalesman ,sum ( os.totalprice ) as totalprice  from" +
                "(select min( os.createtime ) as firsttime,os.memberid from orders os where 1=1");

        sqlCommon(param, list, builder);

        builder.append("  group by t1.memberid,t1.firsttime,mb.username,bc.companyname,mb.province || mb.city || mb.citysmall," +
                " mb.address,mb.realname,mb.mobile,mb.telephone,mb.waysalesman order by t1.firsttime desc");//limit 10; ");
        if (StringUtils.isNotBlank(param.get("limit"))){
            long limit = Long.parseLong(param.get("limit").trim());
            builder.append(" limit ?");
            list.add(limit);
        }else{
            builder.append(" limit 10");
        }
        if (StringUtils.isNotBlank(param.get("offset"))){
            long limit = Long.parseLong(param.get("offset").trim());
            builder.append(" offset ? ;");
            list.add(limit);
        }else{
            builder.append(" offset 0 ;");
        }
        List<Map<String,Object>> customers = jdbcTemplate.queryForList(builder.toString(),list.toArray());

        List<CustomerOfOrder> customerOfOrders = new ArrayList<>();
        CustomerOfOrder customerOfOrder =null;
        for (Map<String,Object> customer : customers) {
            customerOfOrder = new CustomerOfOrder();
            customerOfOrder.setUsername(customer.get("username")==null?null:customer.get("username").toString());
            customerOfOrder.setMemberid(Long.parseLong(customer.get("memberid").toString()));
            customerOfOrder.setFirsttime((Date)customer.get("firsttime"));
            customerOfOrder.setCompanyname(customer.get("companyname")==null?null:customer.get("companyname").toString());
            customerOfOrder.setCity(customer.get("city")==null?null:customer.get("city").toString());
            customerOfOrder.setAddress(customer.get("address")==null?null:customer.get("address").toString());
            customerOfOrder.setRealname(customer.get("realname")==null?null:customer.get("realname").toString());
            customerOfOrder.setMobile(customer.get("mobile")==null?null:customer.get("mobile").toString());
            customerOfOrder.setTelephone(customer.get("telephone")==null?null:customer.get("telephone").toString());
            customerOfOrder.setWaysalesman(customer.get("waysalesman")==null?null:customer.get("waysalesman").toString());
            customerOfOrder.setTotalprice(customer.get("totalprice")==null?null:customer.get("totalprice").toString());
            customerOfOrders.add(customerOfOrder);
//            System.out.println(customerOfOrder.toString());
        }
        return customerOfOrders;
    }

    public Long getCount(Map<String, String> param) throws ParseException{
        if (param==null){
            return  0L;
        }
        List<Object > list = new ArrayList<>();
        StringBuilder builder = new StringBuilder("SELECT count(1) cut from  (SELECT mb.username from " +
                "(select min( os.createtime ) as firsttime,os.memberid from orders os where 1=1");
        sqlCommon(param, list, builder);
        builder.append("  group by t1.memberid,t1.firsttime,mb.username,bc.companyname,mb.province || mb.city || mb.citysmall," +
                " mb.address,mb.realname,mb.mobile,mb.telephone,mb.waysalesman order by t1.firsttime ) tb;");

        log.info(builder.toString());
        log.info(list.toString());
        return jdbcTemplate.query(builder.toString(), list.toArray(), new ResultSetExtractor<Long>() {
            @Override
            public Long extractData(ResultSet rs) throws SQLException, DataAccessException {
                if (rs.next()) {
                    return rs.getLong("cut");
                }
                return 0L;
            }
        });
    }

    private void sqlCommon(Map<String, String> param, List<Object> list, StringBuilder builder) throws ParseException {
        builder.append(" and os.orderstatus <> 7 group by os.memberid )t1  left join orders os on t1.memberid = os.memberid ");
        if (param.get("startDate") != null && StringUtils.isNotBlank(param.get("startDate"))) {
            builder.append(" and os.createtime >= ?");
            list.add(DateUtils.parseDate(param.get("startDate") + " 00:00:00","YYYY-MM-dd HH:mm:ss"));
        }
        if (param.get("endDate") != null && StringUtils.isNotBlank(param.get("endDate"))) {
            builder.append(" and os.createtime <= ?");
            list.add(DateUtils.parseDate(param.get("endDate") + " 23:59:59","YYYY-MM-dd HH:mm:ss"));
        }
        builder.append(" and os.orderstatus <> 7 left join member mb on t1.memberid = mb.id  left join buyercompanyinfo bc on mb.id = bc.memberid where 1=1 ");
        if (param.get("startDate") != null && StringUtils.isNotBlank(param.get("startDate"))) {
            builder.append(" and os.createtime >= ?");
            list.add(DateUtils.parseDate(param.get("startDate") + " 00:00:00","YYYY-MM-dd HH:mm:ss"));
        }
        if (param.get("endDate") != null && StringUtils.isNotBlank(param.get("endDate"))) {
            builder.append(" and os.createtime <= ?");
            list.add(DateUtils.parseDate(param.get("endDate") + " 23:59:59","YYYY-MM-dd HH:mm:ss"));
        }
        if (param.get("waysalesman") != null && StringUtils.isNotBlank(param.get("waysalesman"))) {
            builder.append(" and mb.waysalesman  =?");
            list.add(param.get("waysalesman").trim());
        }
        if (param.get("companyname") != null && StringUtils.isNotBlank(param.get("companyname"))) {
            builder.append(" and bc.companyname  =?");
            list.add(param.get("companyname").trim());
        }
        if (param.get("mobile") != null && StringUtils.isNotBlank(param.get("mobile"))) {
            builder.append(" and mb.mobile  =?");
            list.add(param.get("mobile").replace(" ",""));
        }
    }
}
