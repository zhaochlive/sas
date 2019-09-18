package com.js.sas.service;

import com.js.sas.utils.DateTimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

/**
 * @author daniel
 */
@Service
public class StoreDetailService {

    @Autowired
    @Qualifier(value = "secodJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    /**
     * 店铺详情
     * @param params
     * @return
     */
    public List<Map<String, Object>> getPage(Map<String, String> params) {
        if (params != null) {
            if (!params.containsKey("startDate")||!params.containsKey("endDate")){
                return null;
            }
            String startDate = params.get("startDate");
            String endDate = params.get("endDate");
            Date StartDate = DateTimeUtils.convert(startDate);
            Date EndDate = DateTimeUtils.convert(endDate);

            Timestamp lastMonthStartDate = DateTimeUtils.addTime(StartDate, -1, DateTimeUtils.MONTH);
            Timestamp lastMonthEndDate = DateTimeUtils.addTime(EndDate, -1, DateTimeUtils.MONTH);
            Timestamp lastYearStartDate = DateTimeUtils.addTime(StartDate, -1, DateTimeUtils.YEAR);
            Timestamp lastYearEndDate = DateTimeUtils.addTime(EndDate, -1, DateTimeUtils.YEAR);

            List<Object> list = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ss.shopname,SUM (付款订单数量) 付款订单数量,sum(非关闭订单) 非关闭订单,SUM (发货订单数量) 发货订单数量,SUM (ss.下单金额) 下单金额,");
            sb.append(" round((SUM(ss.下单金额)/Lastmonth.下单金额-1)* 100,2) 下单金额环比,Lastmonth.下单金额 上月下单金额,Lastyear.下单金额 去年下单金额,");
            sb.append(" round((SUM(ss.下单金额)/Lastyear.下单金额-1)* 100,2)  下单金额同比,");
            sb.append(" COUNT(ss.memberid) 下单人数, mi.首次下单金额,mi.首次下单人数,round(avg(ss.规格),4) 平均规格,sum(ss.当日发货) 当日发货量,");
            sb.append(" (CASE WHEN COUNT(ss.memberid) = 0 THEN 0 ELSE round(SUM (ss.下单金额)/COUNT(ss.memberid),2) END ) 客单价,");
            sb.append(" (CASE WHEN Lastmonth.下单人数 = 0 or COUNT(ss.memberid) = 0 or SUM (ss.下单金额) =0 THEN 0 ELSE ");
            sb.append(" round(((Lastmonth.下单金额/Lastmonth.下单人数)/(SUM (ss.下单金额)/COUNT(ss.memberid))-1)*100,2) END ) 环比客单价,");
            sb.append(" (CASE WHEN Lastyear.下单人数 = 0 or COUNT(ss.memberid) = 0 or SUM (ss.下单金额) =0 THEN 0 ELSE ");
            sb.append(" round(((Lastyear.下单金额/Lastyear.下单人数)/(SUM (ss.下单金额)/COUNT(ss.memberid))-1)*100,2) END ) 同比客单价,");
            sb.append(" (CASE WHEN SUM (ss.付款订单数量) = 0 THEN 0 ELSE round(SUM (ss.发货订单数量) / SUM (ss.付款订单数量) * 100, 2 ) END ) 店铺发货率,");
            sb.append(" (CASE WHEN SUM (ss.付款订单数量) = 0 THEN 0 ELSE round(SUM (ss.卖家违约订单) / SUM (ss.付款订单数量) * 100, 2 ) END ) 店铺违约率,");
            sb.append(" (CASE WHEN SUM (ss.部分发货订单) = 0 THEN 0 ELSE round( SUM (ss.部分发货订单) / SUM (ss.所有订单) * 100, 2 ) END ) 店铺部分发货比例,");
            sb.append(" (CASE WHEN SUM (ss.所有订单) = 0 THEN 0 ELSE round( SUM (ss.未付款超时取消订单) / SUM (ss.所有订单) * 100, 2 ) END ) 未付款超时取消订单,");
            sb.append(" (CASE WHEN SUM (ss.子订单总数) = 0 THEN 0 ELSE round( SUM (ss.退货订单数) / SUM (ss.子订单总数) * 100, 2 ) END ) 店铺退货率 ,");
            sb.append(" (CASE WHEN SUM (发货订单数量) = 0 THEN 0 ELSE round(sum(ss.当日发货)/SUM (发货订单数量) * 100,2) END ) 当日发货 ,");
            sb.append(" (CASE WHEN SUM (发货订单数量) = 0 THEN 0 ELSE round(sum(ss.超时一天发货)/SUM (发货订单数量) * 100,2) END ) 超时一天发货 ,");
            sb.append(" (CASE WHEN SUM (发货订单数量) = 0 THEN 0 ELSE round(sum(ss.超时两天发货)/SUM (发货订单数量) * 100,2) END ) 超时两天发货 ,");
            sb.append(" (CASE WHEN SUM (发货订单数量) = 0 THEN 0 ELSE round(sum(ss.超时三天)/SUM (发货订单数量) * 100,2) END ) 超时三天 ,");
            sb.append(" (CASE WHEN SUM (付款订单数量) = 0 THEN 0 ELSE round(sum(in_500)/SUM (付款订单数量) * 100,2) END ) 低于500元订单 ,");
            sb.append(" (CASE WHEN SUM (付款订单数量) = 0 THEN 0 ELSE round(sum(in_500_800)/SUM (付款订单数量) * 100,2) END ) 在500_800元订单 ,");
            sb.append(" (CASE WHEN SUM (付款订单数量) = 0 THEN 0 ELSE round(sum(in_800_1200)/SUM (付款订单数量) * 100,2) END ) 在800_1200订单 ,");
            sb.append(" (CASE WHEN SUM (付款订单数量) = 0 THEN 0 ELSE round(sum(in_1200_1500)/SUM (付款订单数量) * 100,2) END ) 在1200_1500元订单 ,");
            sb.append(" (CASE WHEN SUM (付款订单数量) = 0 THEN 0 ELSE round(sum(in_1500_2000)/SUM (付款订单数量) * 100,2) END ) 在1500_2000元订单 ,");
            sb.append(" (CASE WHEN SUM (付款订单数量) = 0 THEN 0 ELSE round(sum(in_2000_5000)/SUM (付款订单数量) * 100,2) END ) 在2000_5000元订单 ,");
            sb.append(" (CASE WHEN SUM (付款订单数量) = 0 THEN 0 ELSE round(sum(more_5000)/SUM (付款订单数量) * 100,2) END ) 在5000元以上订单 ");
            sb.append(" FROM (SELECT  os.shopname,");
            sb.append(" SUM ( CASE WHEN os.orderstatus IN ( 0, 1, 3, 4, 5, 8, 9, 10 ) THEN 1 ELSE 0 END ) 非关闭订单,");
            sb.append(" SUM ( CASE WHEN os.orderstatus IN ( 1, 3, 4, 5, 8, 9, 10 ) THEN 1 ELSE 0 END ) 付款订单数量,");
            sb.append(" SUM ( CASE WHEN os.orderstatus IN ( 1, 3, 4, 5 ) THEN 1 ELSE 0 END ) 发货订单数量,");
            sb.append(" SUM (CASE WHEN os.orderstatus IN ( 1, 3, 4, 5, 8, 9, 10 ) THEN os.totalprice ELSE 0 END ) 下单金额,");
            sb.append(" SUM ( CASE WHEN os.orderstatus = 10 THEN 1 ELSE 0 END ) 部分发货订单,");
            sb.append(" SUM ( CASE WHEN os.orderstatus = 7 THEN 1 ELSE 0 END ) 未付款超时取消订单,");
            sb.append(" SUM ( CASE WHEN os.orderstatus = 7 and os.reason = '卖家取消订单' THEN 1 ELSE 0 END ) 卖家违约订单,");
            sb.append(" SUM ( CASE WHEN os.orderstatus IN ( 1, 3, 4, 5, 8, 9, 10 ) and os.totalprice BETWEEN 0 and 499.99 THEN 1 ELSE 0 END ) in_500,");
            sb.append(" SUM ( CASE WHEN os.orderstatus IN ( 1, 3, 4, 5, 8, 9, 10 ) and os.totalprice BETWEEN 500.00 and 799.99 THEN 1 ELSE 0 END ) in_500_800,");
            sb.append(" SUM ( CASE WHEN os.orderstatus IN ( 1, 3, 4, 5, 8, 9, 10 ) and os.totalprice BETWEEN 800 and 1199.99 THEN 1 ELSE 0 END ) in_800_1200,");
            sb.append(" SUM ( CASE WHEN os.orderstatus IN ( 1, 3, 4, 5, 8, 9, 10 ) and os.totalprice BETWEEN 1200 and 1499.99 THEN 1 ELSE 0 END ) in_1200_1500,");
            sb.append(" SUM ( CASE WHEN os.orderstatus IN ( 1, 3, 4, 5, 8, 9, 10 ) and os.totalprice BETWEEN 1500 and 1999.99 THEN 1 ELSE 0 END ) in_1500_2000,");
            sb.append(" SUM ( CASE WHEN os.orderstatus IN ( 1, 3, 4, 5, 8, 9, 10 ) and os.totalprice BETWEEN 2000 and 4999.99 THEN 1 ELSE 0 END ) in_2000_5000,");
            sb.append(" SUM ( CASE WHEN os.orderstatus IN ( 1, 3, 4, 5, 8, 9, 10 ) and os.totalprice > 5000 THEN 1 ELSE 0 END ) more_5000,");
            sb.append(" sum(case when (os.paymenttime::text < DATE(os.paymenttime)||' 16:00:00' and os.sellerdeliverytime::text <= DATE(os.paymenttime)||' 23:59:59'");
            sb.append(" and os.orderstatus not in (0,7)) or(os.paymenttime::text > DATE(os.paymenttime)||' 16:00:00' ");
            sb.append(" and os.sellerdeliverytime::text <= DATE(os.paymenttime+interval'1day')||' 16:00:00' ");
            sb.append(" and os.orderstatus not in (0,7)) then 1 else 0 end) 当日发货,");
            sb.append(" sum(case when (os.paymenttime::text < DATE(os.paymenttime)||' 16:00:00' " +
                    " and os.sellerdeliverytime::text >= DATE(os.paymenttime)||' 23:59:59' " +
                    " and os.sellerdeliverytime::text <= DATE(os.paymenttime+interval'1day')||' 16:00:00'" +
                    " and os.orderstatus not in (0,7)) or(os.paymenttime::text < DATE(os.paymenttime)||' 16:00:00' " +
                    " and os.sellerdeliverytime::text >= DATE(os.paymenttime+interval'1day')||' 16:00:00' " +
                    " and os.sellerdeliverytime::text <= DATE(os.paymenttime+interval'2day')||' 16:00:00' " +
                    " and os.orderstatus not in (0,7)) then 1 else 0 end) 超时一天发货,");
            sb.append(" sum(case when (os.paymenttime::text < DATE(os.paymenttime)||' 16:00:00' " +
                    " and os.sellerdeliverytime::text >= DATE(os.paymenttime+interval'1day')||' 23:59:59' " +
                    " and os.sellerdeliverytime::text <= DATE(os.paymenttime+interval'2day')||' 16:00:00'" +
                    " and os.orderstatus not in (0,7)) or(os.paymenttime::text < DATE(os.paymenttime)||' 16:00:00' " +
                    " and os.sellerdeliverytime::text >= DATE(os.paymenttime+interval'2day')||' 16:00:00' " +
                    " and os.sellerdeliverytime::text <= DATE(os.paymenttime+interval'3day')||' 16:00:00'" +
                    " and os.orderstatus not in (0,7)) then 1 else 0 end) 超时两天发货,");
            sb.append(" sum(case when (os.paymenttime::text < DATE(os.paymenttime)||' 16:00:00' " +
                    " and os.sellerdeliverytime::text >= DATE(os.paymenttime+interval'2day')||' 23:59:59' " +
                    " and os.orderstatus not in (0,7)) or(os.paymenttime::text < DATE(os.paymenttime)||' 16:00:00' " +
                    " and os.sellerdeliverytime::text >= DATE(os.paymenttime+interval'3day')||' 16:00:00' " +
                    " and os.orderstatus not in (0,7)) then 1 else 0 end) 超时三天,");
            sb.append(" COUNT (os.ID) 所有订单,os.memberid,sum( standardn ) 规格,SUM(子订单总数) 子订单总数,SUM(退货订单数) 退货订单数");
            sb.append(" FROM orders os LEFT JOIN ( SELECT orderno, COUNT(DISTINCT (standard)) standardn,COUNT ( 1 ) 子订单总数,");
            sb.append(" SUM ( CASE WHEN backstate in (1,2,3) THEN 1 ELSE 0 END ) 退货订单数 FROM orderproduct GROUP BY orderno ) od ON od.orderno = os.orderno ");
            sb.append(" where createtime>='"+startDate+"' and createtime <='"+endDate+"'");
            if(params.containsKey("shopname")){
                sb.append( " and os.shopname ='"+params.get("shopname").trim()+"'");
            }
            sb.append(" GROUP BY os.shopname,os.memberid ) ss ");
            //同比数据
            sb.append(" LEFT JOIN (select sum (下单金额) 下单金额,COUNT(memberid )下单人数,shopname from ( ");
            sb.append(" SELECT SUM(totalprice) 下单金额,memberid, COUNT(1) ordernum ,shopname FROM orders ");
            sb.append(" where createtime>='"+lastMonthStartDate+"' and createtime <='"+lastMonthEndDate+"' and orderstatus IN ( 1, 3, 4, 5, 8, 9, 10 ) ");
            sb.append(" GROUP BY memberid,shopname )ss GROUP BY shopname) Lastmonth ON Lastmonth.shopname = ss.shopname  ");
            //环比数据
            sb.append(" LEFT JOIN (select sum (下单金额) 下单金额,COUNT(memberid )下单人数,shopname from (");
            sb.append(" SELECT SUM(totalprice) 下单金额, memberid, COUNT(1) ordernum ,shopname FROM orders  ");
            sb.append(" where createtime>='"+lastYearStartDate+"' and createtime <='"+lastYearEndDate+"' and orderstatus IN ( 1, 3, 4, 5, 8, 9, 10 ) ");
            sb.append(" GROUP BY memberid,shopname )ss GROUP BY shopname) Lastyear ON Lastyear.shopname = ss.shopname ");
            // 首次下单信息
            sb.append(" LEFT JOIN (select count(1) 首次下单人数,shopname,sum(totalprice) 首次下单金额 from ( SELECT ID, totalprice, memberid, shopname, createtime firstTime, orderstatus  ");
            sb.append(" FROM orders WHERE ID IN ( SELECT MIN ( ID ) ID FROM orders GROUP BY memberid )  ");
            sb.append(" and createtime>='"+startDate+"' and createtime <='"+endDate+"')ss GROUP BY shopname) mi ON mi.shopname = ss.shopname ");
            sb.append(" GROUP BY ss.shopname,Lastmonth.下单人数,Lastyear.下单人数,Lastmonth.下单金额,Lastyear.下单金额,mi.首次下单人数,mi.首次下单金额 ");
            sb.append(" ORDER BY 付款订单数量 DESC ");
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
            List<Map<String, Object>> maps = jdbcTemplate.queryForList(sb.toString(),list.toArray());
            System.out.println(sb.toString());
            return maps;
        }
        return null;
    }


    /**
     *
     * @param params
     * @return
     */
    public Long getCount(Map<String ,String > params){

        StringBuilder sb = new StringBuilder("SELECT count(1) from ( SELECT shopname FROM");
        sb.append(" (SELECT os.shopname,os.memberid FROM orders os where 1=1");
        sb.append(" and os.createtime>='"+params.get("startDate")+"' and os.createtime <='"+params.get("endDate")+"'");
        if(params.containsKey("shopname")){
            sb.append( " and os.shopname ='"+params.get("shopname").trim()+"'");
        }
        sb.append(" GROUP BY os.shopname, os.memberid )ss GROUP BY shopname) ss");
        return jdbcTemplate.queryForObject(sb.toString(),Long.class);
    }

    public Map<String, Object> getTotal(Map<String, String> params) {
        if (params != null) {
            if (!params.containsKey("startDate")||!params.containsKey("endDate")){
                return null;
            }
            String startDate = params.get("startDate");
            String endDate = params.get("endDate");
            Date StartDate = DateTimeUtils.convert(startDate);
            Date EndDate = DateTimeUtils.convert(endDate);
            Timestamp lastMonthStartDate = DateTimeUtils.addTime(StartDate, -1, DateTimeUtils.MONTH);
            Timestamp lastMonthEndDate = DateTimeUtils.addTime(EndDate, -1, DateTimeUtils.MONTH);
            Timestamp lastYearStartDate = DateTimeUtils.addTime(StartDate, -1, DateTimeUtils.YEAR);
            Timestamp lastYearEndDate = DateTimeUtils.addTime(EndDate, -1, DateTimeUtils.YEAR);

            StringBuilder sb = new StringBuilder("select  round((sum(total.下单金额)/sum(total.去年下单金额) -1)* 100 ,2)||'%' 同比金额,");
            sb.append(" round((sum(total.下单金额)/sum(total.上月下单金额) -1)* 100 ,2)||'%' 环比金额,round(sum(total.下单金额)/sum(total.下单人数) ,2) 总客单价,");
            sb.append(" round(((sum(total.上月下单金额)/sum(total.上月下单人数))/(sum(total.下单金额)/sum(total.下单人数)) -1)*100,2)||'%' 环比客单价,");
            sb.append(" round(((sum(total.去年下单金额)/sum(total.去年下单人数))/(sum(total.下单金额)/sum(total.下单人数)) -1)*100,2)||'%' 同比客单价,");
            sb.append(" round(sum(total.发货订单数量)/sum(付款订单数量)*100,2)||'%' 总店铺发货率,");
            sb.append(" round(sum(total.部分发货订单)/sum(总订单)*100,2)||'%' 店铺部分发货比,");
            sb.append(" round(sum(total.卖家违约订单总数)/sum(付款订单数量)*100,2)||'%' 总店铺违约率,");
            sb.append(" round(sum(total.退货订单总数)/sum(付款订单数量)*100,2)||'%' 总店铺退货率,");
            sb.append(" round(sum(total.超时取消订单总数)/sum(付款订单数量)*100,2)||'%' 总未付款超时取消订单,");
            sb.append(" round(sum(total.当日发货总数)/sum(发货订单数量)*100,2)||'%' 总当日发货总数率,");
            sb.append(" sum(total.卖家违约订单总数),sum(total.付款订单数量) 付款订单总数,sum(total.发货订单数量) 发货订单总数,sum(total.下单人数)下单总人数, ");
            sb.append(" sum(total.首次下单人数) 新用户下单总人数,sum(total.首次下单金额) 新用户下单总金额 ");

            sb.append(" from ( SELECT ss.shopname,SUM (付款订单数量) 付款订单数量,SUM (发货订单数量) 发货订单数量,SUM (ss.下单金额) 下单金额,");
            sb.append(" SUM(ss.所有订单) 总订单, SUM(ss.退货订单数) 退货订单总数, sum(ss.当日发货) 当日发货总数, Lastmonth.下单人数 上月下单人数,  ");
            sb.append(" Lastyear.下单人数 去年下单人数,sum(ss.部分发货订单) 部分发货订单,sum(ss.卖家违约订单) 卖家违约订单总数, sum(ss.未付款超时取消订单) 超时取消订单总数, ");
            sb.append(" round((SUM(ss.下单金额)/Lastmonth.下单金额-1)* 100,2) 下单金额环比,Lastmonth.下单金额 上月下单金额,Lastyear.下单金额 去年下单金额,");
            sb.append(" round((SUM(ss.下单金额)/Lastyear.下单金额-1)* 100,2)  下单金额同比,");
            sb.append(" COUNT(ss.memberid) 下单人数, mi.首次下单金额,mi.首次下单人数,round(avg(ss.规格),4) 平均规格,sum(ss.当日发货) 当日发货量,");
            sb.append(" (CASE WHEN COUNT(ss.memberid) = 0 THEN 0 ELSE round(SUM (ss.下单金额)/COUNT(ss.memberid),2) END ) 客单价,");
            sb.append(" (CASE WHEN Lastmonth.下单人数 = 0 or COUNT(ss.memberid) = 0 or SUM (ss.下单金额) =0 THEN 0 ELSE ");
            sb.append(" round(((Lastmonth.下单金额/Lastmonth.下单人数)/(SUM (ss.下单金额)/COUNT(ss.memberid))-1)*100,2) END ) 环比客单价,");
            sb.append(" (CASE WHEN Lastyear.下单人数 = 0 or COUNT(ss.memberid) = 0 or SUM (ss.下单金额) =0 THEN 0 ELSE ");
            sb.append(" round(((Lastyear.下单金额/Lastyear.下单人数)/(SUM (ss.下单金额)/COUNT(ss.memberid))-1)*100,2) END ) 同比客单价,");
            sb.append(" (CASE WHEN SUM (ss.付款订单数量) = 0 THEN 0 ELSE round(SUM (ss.发货订单数量) / SUM (ss.付款订单数量) * 100, 2 ) END ) 店铺发货率,");
            sb.append(" (CASE WHEN SUM (ss.付款订单数量) = 0 THEN 0 ELSE round(SUM (ss.卖家违约订单) / SUM (ss.付款订单数量) * 100, 2 ) END ) 店铺违约率,");
            sb.append(" (CASE WHEN SUM (ss.部分发货订单) = 0 THEN 0 ELSE round( SUM (ss.部分发货订单) / SUM (ss.所有订单) * 100, 2 ) END ) 店铺部分发货比例,");
            sb.append(" (CASE WHEN SUM (ss.所有订单) = 0 THEN 0 ELSE round( SUM (ss.未付款超时取消订单) / SUM (ss.所有订单) * 100, 2 ) END ) 未付款超时取消订单,");
            sb.append(" (CASE WHEN SUM (ss.子订单总数) = 0 THEN 0 ELSE round( SUM (ss.退货订单数) / SUM (ss.子订单总数) * 100, 2 ) END ) 店铺退货率 ,");
            sb.append(" (CASE WHEN SUM (发货订单数量) = 0 THEN 0 ELSE round(sum(ss.当日发货)/SUM (发货订单数量) * 100,2) END ) 当日发货 ,");
            sb.append(" (CASE WHEN SUM (发货订单数量) = 0 THEN 0 ELSE round(sum(ss.超时一天发货)/SUM (发货订单数量) * 100,2) END ) 超时一天发货 ,");
            sb.append(" (CASE WHEN SUM (发货订单数量) = 0 THEN 0 ELSE round(sum(ss.超时两天发货)/SUM (发货订单数量) * 100,2) END ) 超时两天发货 ,");
            sb.append(" (CASE WHEN SUM (发货订单数量) = 0 THEN 0 ELSE round(sum(ss.超时三天)/SUM (发货订单数量) * 100,2) END ) 超时三天 ,");
            sb.append(" (CASE WHEN SUM (付款订单数量) = 0 THEN 0 ELSE round(sum(in_500)/SUM (付款订单数量) * 100,2) END ) 低于500元订单 ,");
            sb.append(" (CASE WHEN SUM (付款订单数量) = 0 THEN 0 ELSE round(sum(in_500_800)/SUM (付款订单数量) * 100,2) END ) 在500_800元订单 ,");
            sb.append(" (CASE WHEN SUM (付款订单数量) = 0 THEN 0 ELSE round(sum(in_800_1200)/SUM (付款订单数量) * 100,2) END ) 在800_1200订单 ,");
            sb.append(" (CASE WHEN SUM (付款订单数量) = 0 THEN 0 ELSE round(sum(in_1200_1500)/SUM (付款订单数量) * 100,2) END ) 在1200_1500元订单 ,");
            sb.append(" (CASE WHEN SUM (付款订单数量) = 0 THEN 0 ELSE round(sum(in_1500_2000)/SUM (付款订单数量) * 100,2) END ) 在1500_2000元订单 ,");
            sb.append(" (CASE WHEN SUM (付款订单数量) = 0 THEN 0 ELSE round(sum(in_2000_5000)/SUM (付款订单数量) * 100,2) END ) 在2000_5000元订单 ,");
            sb.append(" (CASE WHEN SUM (付款订单数量) = 0 THEN 0 ELSE round(sum(more_5000)/SUM (付款订单数量) * 100,2) END ) 在5000元以上订单 ");
            sb.append(" FROM (SELECT  os.shopname,");
            sb.append(" SUM ( CASE WHEN os.orderstatus IN ( 0, 1, 3, 4, 5, 8, 9, 10 ) THEN 1 ELSE 0 END ) 非关闭订单,");
            sb.append(" SUM ( CASE WHEN os.orderstatus IN ( 1, 3, 4, 5, 8, 9, 10 ) THEN 1 ELSE 0 END ) 付款订单数量,");
            sb.append(" SUM ( CASE WHEN os.orderstatus IN ( 1, 3, 4, 5 ) THEN 1 ELSE 0 END ) 发货订单数量,");
            sb.append(" SUM (CASE WHEN os.orderstatus IN ( 1, 3, 4, 5, 8, 9, 10 ) THEN os.totalprice ELSE 0 END ) 下单金额,");
            sb.append(" SUM ( CASE WHEN os.orderstatus = 10 THEN 1 ELSE 0 END ) 部分发货订单,");
            sb.append(" SUM ( CASE WHEN os.orderstatus = 7 THEN 1 ELSE 0 END ) 未付款超时取消订单,");
            sb.append(" SUM ( CASE WHEN os.orderstatus = 7 and os.reason = '卖家取消订单' THEN 1 ELSE 0 END ) 卖家违约订单,");
            sb.append(" SUM ( CASE WHEN os.orderstatus IN ( 1, 3, 4, 5, 8, 9, 10 ) and os.totalprice BETWEEN 0 and 499.99 THEN 1 ELSE 0 END ) in_500,");
            sb.append(" SUM ( CASE WHEN os.orderstatus IN ( 1, 3, 4, 5, 8, 9, 10 ) and os.totalprice BETWEEN 500.00 and 799.99 THEN 1 ELSE 0 END ) in_500_800,");
            sb.append(" SUM ( CASE WHEN os.orderstatus IN ( 1, 3, 4, 5, 8, 9, 10 ) and os.totalprice BETWEEN 800 and 1199.99 THEN 1 ELSE 0 END ) in_800_1200,");
            sb.append(" SUM ( CASE WHEN os.orderstatus IN ( 1, 3, 4, 5, 8, 9, 10 ) and os.totalprice BETWEEN 1200 and 1499.99 THEN 1 ELSE 0 END ) in_1200_1500,");
            sb.append(" SUM ( CASE WHEN os.orderstatus IN ( 1, 3, 4, 5, 8, 9, 10 ) and os.totalprice BETWEEN 1500 and 1999.99 THEN 1 ELSE 0 END ) in_1500_2000,");
            sb.append(" SUM ( CASE WHEN os.orderstatus IN ( 1, 3, 4, 5, 8, 9, 10 ) and os.totalprice BETWEEN 2000 and 4999.99 THEN 1 ELSE 0 END ) in_2000_5000,");
            sb.append(" SUM ( CASE WHEN os.orderstatus IN ( 1, 3, 4, 5, 8, 9, 10 ) and os.totalprice > 5000 THEN 1 ELSE 0 END ) more_5000,");
            sb.append(" sum(case when (os.paymenttime::text < DATE(os.paymenttime)||' 16:00:00' and os.sellerdeliverytime::text <= DATE(os.paymenttime)||' 23:59:59'");
            sb.append(" and os.orderstatus not in (0,7)) or(os.paymenttime::text > DATE(os.paymenttime)||' 16:00:00' ");
            sb.append(" and os.sellerdeliverytime::text <= DATE(os.paymenttime+interval'1day')||' 16:00:00' ");
            sb.append(" and os.orderstatus not in (0,7)) then 1 else 0 end) 当日发货,");
            sb.append(" sum(case when (os.paymenttime::text < DATE(os.paymenttime)||' 16:00:00' " +
                    " and os.sellerdeliverytime::text >= DATE(os.paymenttime)||' 23:59:59' " +
                    " and os.sellerdeliverytime::text <= DATE(os.paymenttime+interval'1day')||' 16:00:00'" +
                    " and os.orderstatus not in (0,7)) or(os.paymenttime::text < DATE(os.paymenttime)||' 16:00:00' " +
                    " and os.sellerdeliverytime::text >= DATE(os.paymenttime+interval'1day')||' 16:00:00' " +
                    " and os.sellerdeliverytime::text <= DATE(os.paymenttime+interval'2day')||' 16:00:00' " +
                    " and os.orderstatus not in (0,7)) then 1 else 0 end) 超时一天发货,");
            sb.append(" sum(case when (os.paymenttime::text < DATE(os.paymenttime)||' 16:00:00' " +
                    " and os.sellerdeliverytime::text >= DATE(os.paymenttime+interval'1day')||' 23:59:59' " +
                    " and os.sellerdeliverytime::text <= DATE(os.paymenttime+interval'2day')||' 16:00:00'" +
                    " and os.orderstatus not in (0,7)) or(os.paymenttime::text < DATE(os.paymenttime)||' 16:00:00' " +
                    " and os.sellerdeliverytime::text >= DATE(os.paymenttime+interval'2day')||' 16:00:00' " +
                    " and os.sellerdeliverytime::text <= DATE(os.paymenttime+interval'3day')||' 16:00:00'" +
                    " and os.orderstatus not in (0,7)) then 1 else 0 end) 超时两天发货,");
            sb.append(" sum(case when (os.paymenttime::text < DATE(os.paymenttime)||' 16:00:00' " +
                    " and os.sellerdeliverytime::text >= DATE(os.paymenttime+interval'2day')||' 23:59:59' " +
                    " and os.orderstatus not in (0,7)) or(os.paymenttime::text < DATE(os.paymenttime)||' 16:00:00' " +
                    " and os.sellerdeliverytime::text >= DATE(os.paymenttime+interval'3day')||' 16:00:00' " +
                    " and os.orderstatus not in (0,7)) then 1 else 0 end) 超时三天,");
            sb.append(" COUNT (os.ID) 所有订单,os.memberid,COUNT ( standardn ) 规格,SUM(子订单总数) 子订单总数,SUM(退货订单数) 退货订单数");
            sb.append(" FROM orders os LEFT JOIN ( SELECT orderno, COUNT(DISTINCT (standard)) standardn,COUNT ( 1 ) 子订单总数,");
            sb.append(" SUM ( CASE WHEN backstate != 0 THEN 1 ELSE 0 END ) 退货订单数 FROM orderproduct GROUP BY orderno ) od ON od.orderno = os.orderno ");
            sb.append(" where createtime>='"+startDate+"' and createtime <='"+endDate+"'  ");
            if(params.containsKey("shopname")){
                sb.append( "and os.shopname ='"+params.get("shopname").trim()+"'");
            }
            sb.append(" GROUP BY os.shopname,os.memberid ) ss ");
            //同比数据
            sb.append(" LEFT JOIN (select sum (下单金额) 下单金额,COUNT(memberid )下单人数,shopname from ( ");
            sb.append(" SELECT SUM(totalprice) 下单金额,memberid, COUNT(1) ordernum ,shopname FROM orders ");
            sb.append(" where createtime>='"+lastMonthStartDate+"' and createtime <='"+lastMonthEndDate+"' and orderstatus IN ( 1, 3, 4, 5, 8, 9, 10 ) ");
            sb.append(" GROUP BY memberid,shopname )ss GROUP BY shopname) Lastmonth ON Lastmonth.shopname = ss.shopname  ");
            //环比数据
            sb.append(" LEFT JOIN (select sum (下单金额) 下单金额,COUNT(memberid )下单人数,shopname from (");
            sb.append(" SELECT SUM(totalprice) 下单金额, memberid, COUNT(1) ordernum ,shopname FROM orders  ");
            sb.append(" where createtime>='"+lastYearStartDate+"' and createtime <='"+lastYearEndDate+"' and orderstatus IN ( 1, 3, 4, 5, 8, 9, 10 ) ");
            sb.append(" GROUP BY memberid,shopname )ss GROUP BY shopname) Lastyear ON Lastyear.shopname = ss.shopname ");
            // 首次下单信息
            sb.append(" LEFT JOIN (select count(1) 首次下单人数,shopname,sum(totalprice) 首次下单金额 from ( SELECT ID, totalprice, memberid, shopname, createtime firstTime, orderstatus  ");
            sb.append(" FROM orders WHERE ID IN ( SELECT MIN ( ID ) ID FROM orders GROUP BY memberid )  ");
            sb.append(" and createtime>='"+startDate+"' and createtime <='"+endDate+"')ss GROUP BY shopname) mi ON mi.shopname = ss.shopname ");
            sb.append(" GROUP BY ss.shopname,Lastmonth.下单人数,Lastyear.下单人数,Lastmonth.下单金额,Lastyear.下单金额,mi.首次下单人数,mi.首次下单金额 ");
            sb.append(" ORDER BY 付款订单数量 DESC )total; ");

            Map<String, Object> maps = jdbcTemplate.queryForMap(sb.toString());
            Map<String, Object> result = new HashMap<>();
            result.put("allPayedOrders", maps.get("付款订单总数"));
            result.put("allSentOrders", maps.get("发货订单总数"));
            result.put("allPeoples", maps.get("下单总人数"));
            result.put("amountRate",  maps.get("环比金额")+"|"+maps.get("同比金额"));
            result.put("amountOfNew",  maps.get("新用户下单总金额"));
            result.put("peopleOfNew",  maps.get("新用户下单总人数"));
            result.put("sendRate",  maps.get("总店铺发货率"));
            result.put("unitPrice",  maps.get("总客单价"));
            result.put("unitPriceRate",  maps.get("环比客单价")+"|"+maps.get("同比客单价"));
            result.put("partSendRate", maps.get("店铺部分发货比") );
            result.put("violateRate",  maps.get("总店铺违约率"));
            result.put("ordersOfCancelRate", maps.get("总未付款超时取消订单") );
            result.put("backRate", maps.get("总店铺退货率"));
            result.put("sentInDay", maps.get("总当日发货总数率"));
            return result;
        }
        return null;
    }
}