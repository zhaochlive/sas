package com.js.sas.service;

import com.github.pagehelper.PageHelper;
import com.js.sas.entity.Facilitator;
import com.js.sas.utils.DateTimeUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author: daniel
 * @date: 2020/5/13 0013 12:50
 * @Description:
 */
@Service
@Slf4j
public class FacilitatorGoldsService {

    @Autowired
    @Qualifier(value = "secodJdbcTemplate")
    private JdbcTemplate jdbcTemplate;
    @Autowired
    @Qualifier(value = "sqlServerJdbcTemplate")
    private JdbcTemplate sqlServerJdbcTemplate;

    private static final String jinshangRate = "2";

    public List<Facilitator> getFacilitatorCompany(){
        String sql ="select fa.memberid,bc.companyname,sign_starttime,sign_endtime from facilitator fa " +
                "left join buyercompanyinfo bc on fa.memberid = bc.memberid " +
                "where fa.state =0";
        List<Map<String, Object>> maps = jdbcTemplate.queryForList(sql);
        List<Facilitator> facilitators = new ArrayList<>();
        if (maps==null || maps.size()==0){
            throw new RuntimeException("加载服务商失败");
        }
        for (Map<String, Object> map : maps) {
            Facilitator facilitator = new Facilitator();
            facilitator.setMemberid(Long.parseLong(map.get("memberid").toString()));
            facilitator.setName(map.get("companyname").toString());
            facilitator.setStartTime((Date)map.get("sign_starttime"));
            facilitator.setEndTime((Date)map.get("sign_endtime"));
            facilitators.add(facilitator);
        }
        return facilitators;
    }

    /**
     * 返金币计算详情
     * @param facilitator
     * @return
     */
    public List<Map<String, Object>> getFacilitatorGoldInfo(Facilitator facilitator,Integer offset,Integer limit){
        ArrayList<Object> list = new ArrayList<>();
        StringBuilder builder = new StringBuilder("select top "+limit+" aap.name facilitator,sab.id,sa.code,sa.createdtime,aa.productInfo,sab.taxAmount," );
        builder.append(" case when aa.IDInventoryClass in (3,4) then sab.taxAmount*0.003 ");
        builder.append(" when aa.IDInventoryClass in (9,10,11,12,13,14,15,16) then sab.taxAmount*0.03");
        builder.append(" when aa.IDInventoryClass in (5,1,2) then sab.taxAmount*0.01");
        builder.append(" when aa.IDInventoryClass in (23,24,25,26,27,28,29,30,31,32,33,34,35,36) then sab.taxAmount*0.01 end 返利," );
        builder.append(" CASE WHEN aa.productInfo = 70200 THEN sab.taxAmount*0.02 END 紧商币,");
        builder.append(" CASE WHEN aa.productInfo = 70200 THEN '紧商' when aa.productInfo = 70201 then '奥展' END 品牌,");
        builder.append(" aa.name,aa.specification,aa.priuserdefnvc1,aa.priuserdefnvc2,aa.priuserdefnvc3,aa.priuserdefnvc10,");
        builder.append(" sum(CASE when aa.productInfo = 70201 and aa.priuserdefnvc2 = '304' THEN sab.taxAmount*0.008");
        builder.append(" when aa.productInfo = 70201 and aa.priuserdefnvc2 = '316' THEN sab.taxAmount*0.008");
        builder.append(" when aa.productInfo = 70201 and aa.priuserdefnvc2 = '316L' THEN sab.taxAmount*0.008 ");
        builder.append(" when aa.productInfo = 70201 and aa.priuserdefnvc2 = 'A480' THEN sab.taxAmount*0.008 ");
        builder.append(" when aa.productInfo = 70201 and aa.priuserdefnvc2 = '304L' THEN sab.taxAmount*0.018");
        builder.append(" when aa.productInfo = 70201 and aa.priuserdefnvc2 = '321' THEN sab.taxAmount*0.028");
        builder.append(" when aa.productInfo = 70201 and aa.priuserdefnvc2 = '2205' THEN sab.taxAmount*0.028");
        builder.append(" when aa.productInfo = 70201 and aa.priuserdefnvc2 = '2520' THEN sab.taxAmount*0.028");
        builder.append(" when aa.productInfo = 70201 and aa.priuserdefnvc2 = '660' THEN sab.taxAmount*0.028 END ) 奥展币,aa.priuserdefnvc2,");
        builder.append(" (CASE when aa.productInfo = 70201 and aa.priuserdefnvc2 = '304' THEN '0.8%'");
        builder.append(" when aa.productInfo = 70201 and aa.priuserdefnvc2 = '316L' THEN '0.8%'");
        builder.append(" when aa.productInfo = 70201 and aa.priuserdefnvc2 = 'A480' THEN '0.8%'");
        builder.append(" when aa.productInfo = 70201 and aa.priuserdefnvc2 = '316' THEN '0.8%'");
        builder.append(" when aa.productInfo = 70201 and aa.priuserdefnvc2 = '304L' THEN '1.8%'");
        builder.append(" when aa.productInfo = 70201 and aa.priuserdefnvc2 = '321' THEN '2.8%'");
        builder.append(" when aa.productInfo = 70201 and aa.priuserdefnvc2 = '2205' THEN '2.8%'");
        builder.append(" when aa.productInfo = 70201 and aa.priuserdefnvc2 = '2520' THEN '2.8%'");
        builder.append(" when aa.productInfo = 70201 and aa.priuserdefnvc2 = '660' THEN '2.8%' END ) rate");
        builder.append(" from SA_SaleDelivery sa ");
        builder.append(" left join SA_SaleDelivery_b sab on sa.id =sab.idSaleDeliveryDTO ");
        builder.append(" left join AA_Inventory aa on sab.IDInventory = aa.id ");
        builder.append(" left join AA_Partner aap on sa.IDCustomer = aap.id ");
        builder.append(" where sa.pubuserdefnvc1 not in ('线上','限时购') ");
        builder.append(" and sa.createdtime >= ?");
        list.add(facilitator.getStartTime());
        builder.append(" and sa.createdtime <= ?");
        list.add(facilitator.getEndTime());
        builder.append(" and aap.name = ?");
        list.add(facilitator.getName());
        builder.append(" and sab.id not in (select top "+offset+" sab.id from SA_SaleDelivery sa ");
        builder.append(" left join SA_SaleDelivery_b sab on sa.id =sab.idSaleDeliveryDTO ");
        builder.append(" left join AA_Inventory aa on sab.IDInventory = aa.id ");
        builder.append(" left join AA_Partner aap on sa.IDCustomer = aap.id where sa.pubuserdefnvc1 not in ('线上','限时购') ");
        builder.append(" and sa.createdtime >= ?");
        list.add(facilitator.getStartTime());
        builder.append(" and sa.createdtime <= ?");
        list.add(facilitator.getEndTime());
        builder.append(" and aap.name = ?");
        list.add(facilitator.getName());
        builder.append(" GROUP BY sab.taxAmount,sab.id");
        builder.append(" ORDER BY sab.id desc )");
        builder.append(" GROUP BY aap.name,sab.taxAmount,sab.id,sa.code,sa.createdtime,aa.priuserdefnvc2,aa.productInfo," +
                "aa.idinventoryclass,aa.name,aa.specification,aa.priuserdefnvc1,aa.priuserdefnvc2,aa.priuserdefnvc3,aa.priuserdefnvc10");
        builder.append(" ORDER BY sab.id desc ");
        return sqlServerJdbcTemplate.queryForList(builder.toString(),list.toArray());
    }


   public Map getFacilitatorGoldsInfoTotal(Facilitator facilita) throws EmptyResultDataAccessException {
       ArrayList<Object> list = new ArrayList<>();
       StringBuilder builder = new StringBuilder("select top 1 * from(select null 订单总金额,0 紧商币,0 奥展币,null 返利,'"+facilita.getName()+"' name UNION");
       builder.append(" select sum(taxAmount) 订单总金额,sum(紧商币)紧商币,sum(奥展币)奥展币,sum(返利)返利,name from (");
       builder.append(" select aap.name,sab.taxAmount,case when aa.IDInventoryClass in (3,4) then sab.taxAmount*0.003");
       builder.append(" when aa.IDInventoryClass in (9,10,11,12,13,14,15,16) then sab.taxAmount*0.03 ");
       builder.append(" when aa.IDInventoryClass in (5,1,2) then sab.taxAmount*0.01 ");
       builder.append(" when aa.IDInventoryClass in (23,24,25,26,27,28,29,30,31,32,33,34,35,36) then sab.taxAmount*0.01 end 返利,");
       builder.append(" CASE WHEN aa.productInfo = 70200 THEN sab.taxAmount*0.02 END 紧商币,");
       builder.append(" sum(CASE when aa.productInfo = 70201 and aa.priuserdefnvc2 = '304' THEN sab.taxAmount*0.008 ");
       builder.append(" when aa.productInfo = 70201 and aa.priuserdefnvc2 = '316' THEN sab.taxAmount*0.008 ");
       builder.append(" when aa.productInfo = 70201 and aa.priuserdefnvc2 = '316L' THEN sab.taxAmount*0.008 ");
       builder.append(" when aa.productInfo = 70201 and aa.priuserdefnvc2 = 'A480' THEN sab.taxAmount*0.008 ");
       builder.append(" when aa.productInfo = 70201 and aa.priuserdefnvc2 = '304L' THEN sab.taxAmount*0.018");
       builder.append(" when aa.productInfo = 70201 and aa.priuserdefnvc2 = '321' THEN sab.taxAmount*0.028 ");
       builder.append(" when aa.productInfo = 70201 and aa.priuserdefnvc2 = '2205' THEN sab.taxAmount*0.028");
       builder.append(" when aa.productInfo = 70201 and aa.priuserdefnvc2 = '2520' THEN sab.taxAmount*0.028");
       builder.append(" when aa.productInfo = 70201 and aa.priuserdefnvc2 = '660' THEN sab.taxAmount*0.028 END ) 奥展币");
       builder.append(" from SA_SaleDelivery sa ");
       builder.append(" left join SA_SaleDelivery_b sab on sa.id =sab.idSaleDeliveryDTO");
       builder.append(" left join AA_Inventory aa on sab.IDInventory = aa.id");
       builder.append(" left join AA_Partner aap on sa.idcustomer  = aap.id");
       builder.append(" where sa.pubuserdefnvc1 not in ('线上','限时购') and aap.name = ?");
       list.add(facilita.getName());
       builder.append(" and sab.id not in (select top 0 sab.id from SA_SaleDelivery sa  left join SA_SaleDelivery_b sab on sa.id =sab.idSaleDeliveryDTO");
       builder.append(" left join AA_Inventory aa on sab.IDInventory = aa.id  left join AA_Partner aap on sa.IDCustomer = aap.id");
       builder.append(" where sa.pubuserdefnvc1 not in ('线上','限时购') and aap.name = ?");
       list.add(facilita.getName());
       builder.append(" GROUP BY sab.taxAmount,sab.id ORDER BY sab.id desc ) ");
       builder.append(" and sa.createdtime >= ?");
       list.add(facilita.getStartTime());
       builder.append(" and sa.createdtime <= ?");
       list.add(facilita.getEndTime());
       builder.append(" and aap.name = ?");
       list.add(facilita.getName());
       builder.append(" GROUP BY aap.name,sab.taxAmount,aa.idinventoryclass,aa.productinfo,sab.id ) ss GROUP BY name )dd order by 返利 desc,订单总金额 desc");
       return sqlServerJdbcTemplate.queryForMap(builder.toString(),list.toArray());
   }

   public Long getFacilitatorGoldsInfoCount(Facilitator facilitator){
       List<Object> list = new ArrayList<>();
       StringBuilder builder = new StringBuilder("select count(1)");
       builder.append(" from SA_SaleDelivery sa ");
       builder.append(" left join SA_SaleDelivery_b sab on sa.id =sab.idSaleDeliveryDTO");
       builder.append(" left join AA_Inventory aa on sab.IDInventory = aa.id");
       builder.append(" left join AA_Partner aap on sa.IDCustomer = aap.id");
       builder.append(" where sa.pubuserdefnvc1 not in ('线上','限时购')");
       builder.append(" and aap.name =?");
       list.add(facilitator.getName());
       builder.append(" and sa.createdtime >=?");
       list.add(facilitator.getStartTime());
       builder.append(" and sa.createdtime <=?");
       list.add(facilitator.getEndTime());
       return sqlServerJdbcTemplate.queryForObject(builder.toString(), list.toArray(), Long.class);
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
