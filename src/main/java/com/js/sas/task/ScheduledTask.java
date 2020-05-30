package com.js.sas.task;

import com.js.sas.entity.OrderProductBackInfo;
import com.js.sas.service.OrderProductBackInfoService;
import com.js.sas.utils.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;


/**
 * @Author: daniel
 * @date: 2019/12/7 0007 16:53
 * @Description:
 */
@Component
@Slf4j
public class ScheduledTask {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier(value = "secodJdbcTemplate")
    private JdbcTemplate jinshangJdbcTemplate;

    @Autowired
    private OrderProductBackInfoService orderProductBackInfoService;

    /**
     * 更新分析系统拆零费
     */
    @Scheduled(cron = "0 20 4 1-31 * ?")
    public void updateBuyerCapital(){
        long time1 = new Date().getTime();
        int[] ints = update();
        long time2 = new Date().getTime();
        log.info("时间{}执行了一次更新拆零费,共计执行时长{}秒，执行数量"+ints[0],DateTimeUtils.convert(new Date()),(time2-time1)/1000);
    }

    /**
     * 更新退单数据到分析系统
     */
    @Scheduled(cron = "0 0 4 1-31 * ?")
    public void saveAllOrderProductBackInfo(){

        Date datetime = DateTimeUtils.getStartDatetime(new Date());
        Timestamp timestamp = DateTimeUtils.addTime(datetime, -1, DateTimeUtils.DAY);
        String sql ="select id,orderno,pdid,backno,backnum,backtype,backstate,backtime from orderproductbackinfo where backtime > ?";
        List<Map<String, Object>> maps = jinshangJdbcTemplate.queryForList(sql, timestamp);
        List<OrderProductBackInfo> infos = new ArrayList<>();
        if (maps!=null&&maps.size()>1) {
            for (Map<String, Object> map : maps) {
                OrderProductBackInfo info = new OrderProductBackInfo();
                info.setId((Long) map.get("id"));
                info.setOrderno(map.get("orderno").toString());
                info.setPdid((Long) map.get("pdid"));
                info.setBackno(map.get("backno").toString());
                info.setBacknum(map.get("backnum") == null ? new BigDecimal(0) : new BigDecimal(map.get("backnum").toString()));
                info.setBacktype(map.get("backtype") == null ? 0 : (Integer) map.get("backtype"));
                info.setBackstate(map.get("backstate") == null ? 0 : (Integer) map.get("backstate"));
                Date backtime = DateTimeUtils.convert(map.get("backtime").toString(),DateTimeUtils.DATE_TIME_FORMAT);
                info.setBacktime(backtime);
                OrderProductBackInfo byId = orderProductBackInfoService.findById((Long) map.get("id"));
                if (byId == null) {
                    infos.add(info);
                } else {
                    log.info("已存在数据:" + byId.toString());
                }
            }
        }
        orderProductBackInfoService.saveAll(infos);
        log.info("时间{}执行了一次更新退货单语句",DateTimeUtils.convert(new Date()));
    }


    @Transactional
    public int[] update(){
        int[] ints = new int[0];
        try {
            String sql = "update buyer_capital bb inner join ( " +
                    " select bc.id bid,ca.id cid,ca.capital from buyer_capital ca " +
                    " inner JOIN (select * from buyer_capital WHERE capitaltype not in (15,16)) bc  " +
                    " WHERE ca.capitaltype in (15,16) and ca.tradeno = bc.tradeno and ca.orderno = bc.orderno and bc.tradetime = ca.tradetime " +
                    " ) aa on aa.bid = bb.id set bb.scattered = 1 , bb.scatteredcapital = aa.capital ;";
            ints = jdbcTemplate.batchUpdate(sql);
        } catch (DataAccessException e) {
            log.info("执行更新拆零费/违约金异常");
            e.printStackTrace();
        }
        return ints;
    }


//    @Scheduled(cron = "0 50 10 1-31 * ? ")
//    public void demo(){
//        log.info("时间{}执行了一次定时任务",DateTimeUtils.convert(new Date()));
//    }
}
