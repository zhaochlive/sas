package com.js.sas;

import com.js.sas.dao.MapDao;
import com.js.sas.service.SalesPerformanceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SasApplicationTests {

    @Autowired
    @Qualifier(value = "secodJdbcTemplate")
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private SalesPerformanceService salesPerformanceService;
    @Resource
    private MapDao mapDao;
    @Test
    public void jdbc(){
        String sql ="select username,realname,createdate from member where id=? and username=?";
//        Pageable pageable = PageRequest.of(0,10);
//        Map<String, Object> hashMap = new HashMap<>();
//        hashMap.put("id",82);
//        Page<Map<String, Object>> mapPage = mapDao.findMapPage(sql, hashMap, pageable);
//        List<Map<String, Object>> content = mapPage.getContent();
        List<Map<String, Object>> content = jdbcTemplate.queryForList(sql, 82,"加油小猫");
////        jdbcTemplate.execute(sql, PreparedStatement::executeQuery)
        if(content!=null&&content.size()>0){
            for (Map<String, Object> map:content ) {
                System.out.println(map.get("username"));
                System.out.println(map.get("realname"));
                System.out.println(map.get("createdate"));
            }
        }



    }
    @Test
    public void contextLoads() {
        Map map =new HashMap<String,String>();
        map.put("waysalesman","王云杰");

        long list= salesPerformanceService.getCount(map);
        System.out.println("============================="+list);
//        for (int i = 0; i < list.size(); i++) {
////            for (String s:list.get(i).keySet() ) {
//                System.out.println(i+"=="+list.get(i).get("业务员"));
////            }
//        }
//        String sql =" SELECT *, CASE WHEN 下单月份 <= 6 THEN '1%' WHEN 下单月份 < 12 THEN '0.5%' ELSE '0%' END AS 比例 FROM " +
//                " (SELECT os.waysalesman AS \"业务员\",os.orderno AS \"订单号\",os.createtime AS \"下单时间\",os.membername AS \"买家账号\"," +
//                " bci.companyname AS \"公司名称\",os.totalprice AS \"订单总金额\",months_between ( " +
//                " (SELECT fot.firsttime FROM (SELECT MIN (os.createtime) AS firsttime,os.memberid FROM orders os GROUP BY os.memberid ) fot" +
//                " WHERE fot.memberid = os.memberid ) :: DATE, os.createtime :: DATE ) + 1 AS \"下单月份\" FROM orders os " +
//                " LEFT JOIN buyercompanyinfo bci ON bci.memberid = os.memberid WHERE 1=1 " +
//                " and os.createtime >= '2019-05-28' AND os.createtime < '2019-06-28') AS T;";
//        List<Map<String, Object>> maps = jdbcTemplate.queryForList(sql);
//        if(maps!=null&&maps.size()>0){
//            for (Map<String, Object> map:maps ) {
//                System.out.println(map.get("买家账号"));
////                System.out.println(map.get("realname"));
////                System.out.println(map.get("createdate"));
//            }
//        }
    }

}
