package com.js.sas.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BackOrderOfSellerOrStoreService {

    @Autowired
    @Qualifier(value = "secodJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    /**
     * 仓库退单信息
     * @param params
     * @return
     */
    public List<Map<String, Object>> getBackOfStore(Map<String, String> params) {
            ArrayList<Object> list = new ArrayList<>();
            StringBuilder sb = new StringBuilder("select st.name,op.storeid,count(1) cut from orderproduct op");
            sb.append(" LEFT JOIN store st on st.id = op.storeid");
            sb.append(" where op.backstate in (1,2,3) and st.name !=''");
            sb.append(" GROUP BY st.name,op.storeid  order by count(1) desc");
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

    /**
     *  仓库退货pageCount
     * @param requestMap
     * @return
     */
    public Long getBackOfStoreCount(Map<String, String> requestMap) {
        ArrayList<Object> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder("select count(1) from ( select st.name,op.storeid from orderproduct op");
        sb.append(" LEFT JOIN store st on st.id = op.storeid");
        sb.append(" where op.backstate in (1,2,3)");
        sb.append(" GROUP BY st.name,op.storeid ) td;");
        return jdbcTemplate.queryForObject(sb.toString(),Long.class);
    }

    /**
     * 商家退单信息
     * @param params
     * @return
     */
    public List<Map<String, Object>> getBackOfSeller(Map<String, String> params) {
        ArrayList<Object> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder("select sc.companyname,op.sellerid, count(1) cut from orderproduct op");
        sb.append(" LEFT JOIN sellercompanyinfo sc on sc.memberid = op.sellerid");
        sb.append(" where op.backstate in (1,2,3)");
        sb.append(" GROUP BY sc.companyname,op.sellerid ORDER BY count(1) desc");
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
    /**
     *  店铺退货pageCount
     * @param requestMap
     * @return
     */
    public Long getBackOfSellerCount(Map<String, String> requestMap) {
        ArrayList<Object> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder("select count(1) from ( select sc.companyname,op.sellerid from orderproduct op");
        sb.append(" LEFT JOIN sellercompanyinfo sc on sc.memberid = op.sellerid");
        sb.append(" where op.backstate in (1,2,3)");
        sb.append(" GROUP BY sc.companyname,op.sellerid) td;");
        return jdbcTemplate.queryForObject(sb.toString(),Long.class);
    }




}
