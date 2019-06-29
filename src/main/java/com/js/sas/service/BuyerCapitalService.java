package com.js.sas.service;

import com.js.sas.entity.AccountsPayable;
import com.js.sas.entity.BuyerCapital;
import com.js.sas.entity.BuyerCapitalConst;
import com.js.sas.entity.OrderProductBackInfo;
import com.js.sas.repository.BuyerCapitalRepository;
import com.js.sas.repository.OrderProductBackInfoRepository;
import com.js.sas.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

@Service
@Slf4j
public class BuyerCapitalService {

    @Autowired
    private BuyerCapitalRepository buyerCapitalRepository;

    @Autowired
    private OrderProductBackInfoRepository orderProductBackInfoRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Map<String, Object> getAccountsPayable(Map<String, String> params){
        Map<String, Object> result = new LinkedHashMap();
        long count = 0L;
        List<AccountsPayable> accountsPayables = null;
        if(params.containsKey("invoiceName")||params.containsKey("userNo")||params.containsKey("userName")
                ||params.containsKey("seller")||params.containsKey("startDate")||params.containsKey("endDate")
                ||params.containsKey("page")||params.containsKey("limit")||params.containsKey("companyname")
                ||params.containsKey("offset")) {
            accountsPayables = new ArrayList<>();
            BigDecimal Receivableaccount = new BigDecimal(0.00);//应结账单
            BigDecimal InvoiceBalance = new BigDecimal(0.00);//发票结余


            //查询总数 count
            String countSql = getCountSql(params);
            Map<String, Object> cutmap = jdbcTemplate.queryForMap(countSql.substring(0,countSql.indexOf(" Order By")));
            if (cutmap.containsKey("cut")){
                count = cutmap.get("cut").equals("0")?0l:Long.parseLong(cutmap.get("cut").toString());
            }
            //查询当期结算
            Map<String, Object> settlement = getSettlement(params);
            result.putAll(settlement);

            //第一页不需要查询 '之前页结余'
            if(params.containsKey("page")&&params.get("page")!=null){
                int offset = Integer.parseInt(params.get("page"));
                if(offset>1) {
                    //查询当前页之前的数据
                    Map<String, Object> maps = jdbcTemplate.queryForMap(getAfterSql(params));
                    Receivableaccount = CommonUtils.getBigDecimal(maps.get("Receivableaccount") == null ? 0.00 : maps.get("Receivableaccount"));
                    InvoiceBalance = CommonUtils.getBigDecimal(maps.get("InvoiceBalance") == null ? 0.00 : maps.get("InvoiceBalance"));
                }
            }

            //查询当前页的数据
            List<BuyerCapital> buycapitals = getBuycapitals(params);



            if (buycapitals != null && buycapitals.size() > 0) {
                for (BuyerCapital buyerCapital : buycapitals) {
                    //拆零费的账单不显示
                    if (buyerCapital.getCapitalType() == BuyerCapitalConst.STATE_15 || buyerCapital.getCapitalType() == BuyerCapitalConst.STATE_16) {
                        continue;
                    }
                    //根据拆零费计算
                    if (buyerCapital.getScattered()!=null&&buyerCapital.getScattered()==1){
                        buyerCapital.setCapital(buyerCapital.getCapital().add(buyerCapital.getScatteredcapital()));
                    }
                    AccountsPayable accountsPayable = null;
                    //根据capitalType分组计算
                    switch (buyerCapital.getCapitalType()) {
                        case BuyerCapitalConst.CAPITALTYPE_CONSUM: {   // 0 消费
                            if (buyerCapital.getPayType() == BuyerCapitalConst.PAYMETHOD_ALIPAY
                                    || buyerCapital.getPayType() == BuyerCapitalConst.PAYMETHOD_WEIXIN
                                    || buyerCapital.getPayType() == BuyerCapitalConst.PAYMETHOD_BANKCARD) {
                                //当记录为网络支付时，先添加一条收款记录，【应收账款】减钱
                                accountsPayable = transitionToAccountsPayable(buyerCapital);
//                                accountsPayable.setDeliveryAmount(null);
//                                accountsPayable.setOtherAmount(null);
                                accountsPayable.setCapitalTypeName("收款");
//                                Receivableaccount = Receivableaccount.subtract(buyerCapital.getCapital());
//                                accountsPayable.setReceivableAccount(Receivableaccount);
//                                accountsPayable.setInvoicebalance(InvoiceBalance);
//                                accountsPayable.setRemark(buyerCapital.getInvoiceHeadUp() == null ? "" : buyerCapital.getInvoiceHeadUp()
//                                        + "\r\n" + buyerCapital.getMemberId() + "\r\n" + buyerCapital.getMemberUserName());
//                                accountsPayables.add(accountsPayable);
                                //然后再添加一条发货记录，【应收账款】加钱
//                                accountsPayable = transitionToAccountsPayable(buyerCapital);
                                accountsPayable.setCapitalTypeName("收款/发货");
//                                accountsPayable.setReceivingAmount(null);
                                accountsPayable.setOtherAmount(null);
//                                Receivableaccount = Receivableaccount.add(buyerCapital.getCapital());
                                accountsPayable.setReceivableAccount(Receivableaccount);
                                InvoiceBalance = InvoiceBalance.add(buyerCapital.getCapital());
                                accountsPayable.setInvoicebalance(InvoiceBalance);
                                accountsPayable.setRemark(buyerCapital.getInvoiceHeadUp() == null ? "" : buyerCapital.getInvoiceHeadUp()
                                        + "\r\n" + buyerCapital.getMemberId() + "\r\n" + buyerCapital.getMemberUserName());
                                accountsPayables.add(accountsPayable);
                            } else if (buyerCapital.getPayType() == BuyerCapitalConst.PAYMETHOD_BALANCE
                                    || buyerCapital.getPayType() == BuyerCapitalConst.PAYMETHOD_CREDIT) {
                                accountsPayable = transitionToAccountsPayable(buyerCapital);
                                accountsPayable.setReceivingAmount(null);
                                accountsPayable.setOtherAmount(null);
                                accountsPayable.setCapitalTypeName("发货");
                                Receivableaccount = Receivableaccount.add(buyerCapital.getCapital());
                                accountsPayable.setReceivableAccount(Receivableaccount);
                                InvoiceBalance = InvoiceBalance.add(buyerCapital.getCapital());
                                accountsPayable.setInvoicebalance(InvoiceBalance);
                                accountsPayable.setRemark(buyerCapital.getInvoiceHeadUp() == null ? "" : buyerCapital.getInvoiceHeadUp()
                                        + "\r\n" + buyerCapital.getMemberId() + "\r\n" + buyerCapital.getMemberUserName());
                                accountsPayables.add(accountsPayable);
                            }
                            break;
                        }
                        //1 充值
                        case BuyerCapitalConst.CAPITALTYPE_RECHARGE: {
                            accountsPayable = transitionToAccountsPayable(buyerCapital);
                            if (accountsPayable.getRechargestate() == 1) {
                                accountsPayable.setOrderno(buyerCapital.getRechargeNumber());
                                accountsPayable.setDeliveryAmount(null);
                                accountsPayable.setOtherAmount(null);
                                accountsPayable.setCapitalTypeName("充值");
                                Receivableaccount = Receivableaccount.subtract(buyerCapital.getCapital());
                                accountsPayable.setReceivableAccount(Receivableaccount);
                                accountsPayable.setInvoicebalance(InvoiceBalance);
                                accountsPayables.add(accountsPayable);
                            }
                            break;

                        }
                        //6 //买家违约，金额归为其他，进行相加
                        case BuyerCapitalConst.CAPITALTYPE_PENALTY: {
                            accountsPayable = transitionToAccountsPayable(buyerCapital);
                            accountsPayable.setDeliveryAmount(null);
                            accountsPayable.setReceivingAmount(null);
                            accountsPayable.setCapitalTypeName("违约金");
                            Receivableaccount = Receivableaccount.add(buyerCapital.getCapital());
                            accountsPayable.setReceivableAccount(Receivableaccount);
                            accountsPayable.setInvoicebalance(InvoiceBalance);
                            accountsPayable.setPaytype(5);
                            accountsPayable.setRemark("买家违约");


                            //查询出这笔违约金是否有对应的退货记录存在，有的话，则插入一条退货记录
                            List<OrderProductBackInfo> productBackInfoByOrderno = orderProductBackInfoRepository.findOrderProductBackInfoByOrderno(buyerCapital.getOrderNo());
                            if (productBackInfoByOrderno != null && productBackInfoByOrderno.size() > 0) {
//                                accountsPayable = transitionToAccountsPayable(buyerCapital);
                                accountsPayable.setDeliveryAmount(buyerCapital.getCapital().multiply(new BigDecimal(-1)));
//                                accountsPayable.setOtherAmount(null);
//                                accountsPayable.setReceivingAmount(null);
                                accountsPayable.setCapitalTypeName("违约金/退货");
                                Receivableaccount = Receivableaccount.add(accountsPayable.getDeliveryAmount());
                                accountsPayable.setReceivableAccount(Receivableaccount);
                                InvoiceBalance = InvoiceBalance.add(accountsPayable.getDeliveryAmount());
                                accountsPayable.setInvoicebalance(InvoiceBalance);
                                accountsPayable.setPaytype(5);
                                accountsPayable.setRemark(buyerCapital.getInvoiceHeadUp() == null ? "" : buyerCapital.getInvoiceHeadUp()
                                        + "\r\n" + buyerCapital.getMemberId() + "\r\n" + buyerCapital.getMemberUserName());
//                                accountsPayables.add(accountsPayable);
                            }
                            accountsPayables.add(accountsPayable);
                            break;
                        }
                        //10 卖家违约金
                        case BuyerCapitalConst.CAPITALTYPE_PENALTY_SELLER: {
                            accountsPayable = transitionToAccountsPayable(buyerCapital);
                            accountsPayable.setDeliveryAmount(null);
                            accountsPayable.setReceivingAmount(null);
                            accountsPayable.setCapitalTypeName("违约金");
                            accountsPayable.setOtherAmount(accountsPayable.getOtherAmount().multiply(new BigDecimal(-1)));
                            Receivableaccount = Receivableaccount.add(buyerCapital.getCapital());
                            accountsPayable.setReceivableAccount(Receivableaccount);
                            accountsPayable.setInvoicebalance(InvoiceBalance);
                            accountsPayable.setPaytype(5);
                            accountsPayable.setRemark("卖家违约");
                            accountsPayables.add(accountsPayable);

                            break;
                        }
                        //2 退款、提现，收款金额变成负数，进行相减
                        case BuyerCapitalConst.CAPITALTYPE_REFUND: {
                            if (buyerCapital.getPayType() == BuyerCapitalConst.PAYMETHOD_ALIPAY
                                    || buyerCapital.getPayType() == BuyerCapitalConst.PAYMETHOD_WEIXIN
                                    || buyerCapital.getPayType() == BuyerCapitalConst.PAYMETHOD_BANKCARD) {
                                accountsPayable = transitionToAccountsPayable(buyerCapital);
                                accountsPayable.setDeliveryAmount(accountsPayable.getDeliveryAmount().multiply(new BigDecimal(-1)));
                                accountsPayable.setOtherAmount(null);
                                accountsPayable.setCapitalTypeName("退货/退款");

//                                accountsPayable.setReceivingAmount(null);
                                Receivableaccount = Receivableaccount.add(accountsPayable.getDeliveryAmount());
//                                accountsPayable.setReceivableAccount(Receivableaccount);
                                InvoiceBalance = InvoiceBalance.add(accountsPayable.getDeliveryAmount());
//                                accountsPayable.setInvoicebalance(InvoiceBalance);
                                accountsPayable.setPaytype(5);
//                                accountsPayable.setRemark(buyerCapital.getInvoiceHeadUp() != null ? "" : buyerCapital.getInvoiceHeadUp()
//                                        + "\r\n" + buyerCapital.getMemberId() + "\r\n" + buyerCapital.getMemberUserName());
//                                accountsPayables.add(accountsPayable);
                                //再添加一条记录
//                                accountsPayable = transitionToAccountsPayable(buyerCapital);
//                                accountsPayable.setOtherAmount(null);
//                                accountsPayable.setDeliveryAmount(null);
//                                accountsPayable.setCapitalTypeName("退款");
                                accountsPayable.setReceivingAmount(accountsPayable.getReceivingAmount().multiply(new BigDecimal(-1)));
                                Receivableaccount = Receivableaccount.subtract(accountsPayable.getReceivingAmount());
                                accountsPayable.setReceivableAccount(Receivableaccount);
                                accountsPayable.setInvoicebalance(InvoiceBalance);
                                accountsPayable.setRemark(buyerCapital.getInvoiceHeadUp() == null ? "" : buyerCapital.getInvoiceHeadUp()
                                        + "\r\n" + buyerCapital.getMemberId() + "\r\n" + buyerCapital.getMemberUserName());
                                accountsPayables.add(accountsPayable);

                            } else {
                                accountsPayable = transitionToAccountsPayable(buyerCapital);
                                accountsPayable.setDeliveryAmount(accountsPayable.getDeliveryAmount().multiply(new BigDecimal(-1)));
                                accountsPayable.setOtherAmount(null);
                                accountsPayable.setCapitalTypeName("退货");
                                accountsPayable.setReceivingAmount(null);
                                Receivableaccount = Receivableaccount.add(accountsPayable.getDeliveryAmount());
                                accountsPayable.setReceivableAccount(Receivableaccount);
                                InvoiceBalance = InvoiceBalance.add(accountsPayable.getDeliveryAmount());
                                accountsPayable.setInvoicebalance(InvoiceBalance);
                                accountsPayable.setRemark(buyerCapital.getInvoiceHeadUp() == null ? "" : buyerCapital.getInvoiceHeadUp()
                                        + "\r\n" + buyerCapital.getMemberId() + "\r\n" + buyerCapital.getMemberUserName());
                                accountsPayables.add(accountsPayable);
                            }
                            break;
                        }
                        //3 提现
                        case BuyerCapitalConst.PAYMETHOD_BALANCE: {
                            accountsPayable = transitionToAccountsPayable(buyerCapital);
                            if (accountsPayable.getRechargestate() == 1) {
                                accountsPayable.setDeliveryAmount(null);
                                accountsPayable.setOtherAmount(null);
                                accountsPayable.setCapitalTypeName(buyerCapital.getCapitalType() == BuyerCapitalConst.PAYMETHOD_BANKCARD ? "退款" : "提现");
                                accountsPayable.setReceivingAmount(accountsPayable.getReceivingAmount().multiply(new BigDecimal(-1)));
                                Receivableaccount = Receivableaccount.subtract(buyerCapital.getCapital());
                                accountsPayable.setReceivableAccount(Receivableaccount);
                                accountsPayable.setInvoicebalance(InvoiceBalance);
                                accountsPayables.add(accountsPayable);
                            }
                            break;
                        }
                        default:
                    }

                }

            }else {
                accountsPayables = null;
            }
        }
        result.put("total",count);
        result.put("rows",accountsPayables);
        return result;
    }




    public List<BuyerCapital> getBuycapitals(Map<String, String> params){


        List<BuyerCapital> buyerCapitals = jdbcTemplate.query(getResultSql(params), new RowMapper() {
                    @Override
                    public BuyerCapital mapRow(ResultSet rs, int rowNum) throws SQLException {
//                        log.info("orderno:{},Timestamp:{},Date:{},String{}",rs.getString("tradeno"),rs.getTimestamp("tradeTime").toString(),rs.getDate("tradeTime").toString(),rs.getString("tradeTime"));
                        BuyerCapital buyerCapital = new BuyerCapital();
                        buyerCapital.setId(rs.getInt("id"));
                        buyerCapital.setTradeTime(Timestamp.valueOf(rs.getString("tradeTime")));
                        buyerCapital.setTradeNo(rs.getString("tradeno"));
                        buyerCapital.setOrderNo(rs.getString("orderno"));
                        buyerCapital.setPayType(rs.getInt("paytype"));
                        buyerCapital.setTransactionId(rs.getString("transactionid"));
                        buyerCapital.setCapital(rs.getBigDecimal("capital"));
                        buyerCapital.setCapitalType(rs.getInt("capitaltype"));
                        buyerCapital.setRechargeState(rs.getInt("rechargestate"));
                        buyerCapital.setInvoiceHeadUp(rs.getString("invoiceheadup"));
                        buyerCapital.setMemberUserName(rs.getString("member_username"));
                        buyerCapital.setRechargePerform(rs.getInt("rechargeperform"));
                        buyerCapital.setRechargeNumber(rs.getString("rechargenumber"));
                        buyerCapital.setMemberId(rs.getInt("memberid"));
                        buyerCapital.setOperation(rs.getString("operation"));
                        buyerCapital.setVerify(rs.getString("verify"));
                        buyerCapital.setScatteredcapital(rs.getBigDecimal("scatteredcapital"));
                        buyerCapital.setScattered(rs.getInt("scattered"));
                        return buyerCapital;
                    }
                }
        );
        return buyerCapitals;
    }


    /**
     * 返回结果集之前获取当前页参数页下的 上期结转
     * @param params
     * @return
     */

    private String getAfterSql(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        sb.append(" SELECT SUM(CASE WHEN bc.capitaltype = 0 AND bc.paytype IN ( 3, 4 ) THEN if (bc.scattered = 1 ,bc.capital+bc.scatteredcapital ,bc.capital ) " +
                " WHEN bc.capitaltype = 2 AND bc.paytype NOT IN ( 0, 1, 2 ) THEN if (bc.scattered = 1 ,- (bc.capital+bc.scatteredcapital) ,- bc.capital ) " +
                " WHEN bc.capitaltype = 1 AND bc.rechargestate = 1 THEN if (bc.scattered = 1 ,- (bc.capital+bc.scatteredcapital) ,- bc.capital) " +
                " WHEN bc.capitaltype = 3 AND bc.rechargestate = 1 THEN if (bc.scattered = 1 , (bc.capital+bc.scatteredcapital) , bc.capital) " +
                " WHEN bc.capitaltype = 6 THEN " +
                " IF(( SELECT COUNT(1) FROM order_product_back_info WHERE orderno = bc.orderno ) > 0, 0," +
                " if (bc.scattered = 1 ,bc.capital +bc.scatteredcapital,bc.capital)) " +
                " WHEN bc.capitaltype = 10 THEN if (bc.scattered = 1 , - (bc.capital +bc.scatteredcapital), -bc.capital) " +
                " END ) AS Receivableaccount, " +
                " SUM(CASE WHEN bc.capitaltype = 0 AND bc.paytype IN (0,1,2,3,4) THEN if(bc.scattered = 1 ,bc.capital+bc.scatteredcapital ,bc.capital) " +
                " WHEN bc.capitaltype = 6 THEN  IF(( SELECT COUNT( 1 ) FROM order_product_back_info WHERE orderno = bc.orderno ) > 0," +
                " -if(bc.scattered = 1 ,bc.capital+bc.scatteredcapital ,bc.capital), 0 ) " +
                " WHEN bc.capitaltype = 2 THEN -if(bc.scattered = 1 ,bc.capital+bc.scatteredcapital ,bc.capital) " +
                " END) AS InvoiceBalance  FROM (SELECT ca.capitaltype,ca.capital,ca.tradetime,ca.paytype,ca.orderno,ca.id,ca.rechargestate,ca.tradeno,ca.scattered,ca.scatteredcapital " +
                " FROM buyer_capital ca WHERE 1 = 1 AND ca.capitaltype in (0,1,2,3,6,10) ");

        if(params!=null){
            if (params.containsKey("invoiceName")&& StringUtils.isNotBlank(params.get("invoiceName"))){
                sb.append(" and ca.invoiceheadup ='"+params.get("invoiceName").trim() +"'");
            }
            if (params.containsKey("userNo")&&StringUtils.isNotBlank(params.get("userNo"))){
                sb.append(" and ca.memberid ='"+params.get("userNo").trim()+"' ");
            }
            if (params.containsKey("companyname")&&StringUtils.isNotBlank(params.get("companyname"))){
                sb.append(" and ca.companyname ='"+params.get("companyname").trim()+"'");
            }
            if (params.containsKey("userName")&&StringUtils.isNotBlank(params.get("userName"))){
                sb.append(" and ca.member_username ='"+params.get("userName").trim()+"' ");
            }
//            if (params.get("seller")!=null&&StringUtils.isNotBlank(params.get("seller"))){
//                sb.append(" and ca.sellerid ='"+params.get("seller")+"' ");
//            }
            if (params.containsKey("startDate")&&StringUtils.isNotBlank(params.get("startDate"))){
                sb.append(" and ca.tradetime >='"+params.get("startDate")+"' ");
            }
            if (params.containsKey("endDate")&&StringUtils.isNotBlank(params.get("endDate"))){
                sb.append(" and ca.tradetime <='"+params.get("endDate")+"' ");
            }
            int offset = 0;//起始位置

            if (params.containsKey("offset")&&StringUtils.isNotBlank(params.get("offset"))){
                offset = Integer.parseInt(params.get("offset"));
            }

            if(offset!=0){
                sb.append(" Order By ca.tradetime limit 0,"+ offset +") bc ");
            }

        }else{
            sb.append(" Order By ca.tradetime limit 0,20 ) bc " );
        }
        log.info("returnCapitalAccount.getAfterSql :{}",sb.toString());
        return sb.toString();
    }

    /**
     * 获取用户对账单基础表数据
     * @param params
     * @return
     */
    private String getResultSql(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT bc.id,bc.tradetime,bc.tradeno,bc.orderno,bc.capitaltype,bc.paytype,bc.transactionid,bc.capital,bc.rechargestate,bc.invoiceheadup" +
                ",bc.member_username,bc.rechargeperform,bc.memberid,bc.operation,bc.verify,bc.rechargenumber,bc.scatteredcapital,bc.scattered");
        return getString(params, sb);
    }

    private AccountsPayable transitionToAccountsPayable(BuyerCapital capital) {
        AccountsPayable accountsPayable = new AccountsPayable();
        accountsPayable.setTradetime(capital.getTradeTime());
        accountsPayable.setOrderno(capital.getOrderNo()==null?capital.getRechargeNumber():capital.getOrderNo());
        accountsPayable.setCapitaltype(capital.getCapitalType());
        accountsPayable.setReceivingAmount(capital.getCapital());
        accountsPayable.setDeliveryAmount(capital.getCapital());
        accountsPayable.setOtherAmount(capital.getCapital());
        accountsPayable.setPaytype(capital.getPayType());
        accountsPayable.setPayno(capital.getTransactionId());
        accountsPayable.setRemark("操作人: "+(capital.getOperation()==null?"":capital.getOperation())+"\r\n"+"审核人: "+(capital.getVerify()==null?"":capital.getVerify())+"\r\n"+capital.getMemberId()+"\r\n"+capital.getMemberUserName());
        accountsPayable.setRechargestate(capital.getRechargeState());
        accountsPayable.setUsername(capital.getMemberUserName());
        accountsPayable.setRechargeperform(capital.getRechargePerform());
        return accountsPayable;
    }

    private String getCountSql(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("select count(1) cut");
        return getString(params, sb);
    }

    private String getString(Map<String, String> params, StringBuilder sb) {
        sb.append(" from buyer_capital bc where  bc.capitaltype in (0,1,2,3,6,10)");
        if(params!=null){
            if (params.containsKey("invoiceName")&& StringUtils.isNotBlank(params.get("invoiceName"))){
                sb.append(" and bc.invoiceheadup ='"+params.get("invoiceName").trim()+"'");
            }
            if (params.containsKey("userNo")&&StringUtils.isNotBlank(params.get("userNo"))){
                sb.append(" and bc.memberid ='"+params.get("userNo").trim()+"' ");
            }
            if (params.containsKey("userName")&&StringUtils.isNotBlank(params.get("userName"))){
                sb.append(" and bc.member_username ='"+params.get("userName").trim()+"' ");
            }
            if (params.containsKey("companyname")&&StringUtils.isNotBlank(params.get("companyname"))){
                sb.append(" and bc.companyname ='"+params.get("companyname").trim()+"'");
            }
//            if (params.containsKey("seller")&&StringUtils.isNotBlank(params.get("seller"))){
//                sb.append(" and bc.sellerid ='"+params.get("seller")+"' ");
//            }
            if (params.containsKey("startDate")&&StringUtils.isNotBlank(params.get("startDate"))){
                sb.append(" and bc.tradetime >='"+params.get("startDate")+" 00:00:00' ");
            }
            if (params.containsKey("endDate")&&StringUtils.isNotBlank(params.get("endDate"))){
                sb.append(" and bc.tradetime <='"+params.get("endDate")+" 23:59:59' ");
            }

            Integer pageSize = 20;
            Integer offset = 0;
            if (params.containsKey("limit")||StringUtils.isNotBlank(params.get("limit"))){
                pageSize = Integer.parseInt(params.get("limit"));
            }
            if (params.containsKey("offset")||StringUtils.isNotBlank(params.get("offset"))){
                offset = Integer.parseInt(params.get("offset"));
            }
            sb.append(" Order By bc.tradetime  limit "+offset+","+ pageSize  +";");

        }else{
            sb.append(" Order By bc.tradetime limit 0,20" );
        }
        log.info("returnCapitalAccount.getResultSql :{}",sb.toString());
        return sb.toString();
    }


    private String getSettlementSql(Map<String, String> params) {
        StringBuilder builder = new StringBuilder();
        builder.append(" SELECT SUM(CASE WHEN bc.capitaltype = 0 AND bc.paytype IN ( 3, 4 ) THEN if (bc.scattered = 1 ,bc.capital+bc.scatteredcapital ,bc.capital ) ");
        builder.append(" WHEN bc.capitaltype = 2 AND bc.paytype NOT IN ( 0, 1, 2 ) THEN if (bc.scattered = 1 ,- (bc.capital+bc.scatteredcapital) ,- bc.capital )");
        builder.append(" WHEN bc.capitaltype = 1 AND bc.rechargestate = 1 THEN if (bc.scattered = 1 ,- (bc.capital+bc.scatteredcapital) ,- bc.capital)");
        builder.append(" WHEN bc.capitaltype = 3 AND bc.rechargestate = 1 THEN if (bc.scattered = 1 , (bc.capital+bc.scatteredcapital) , bc.capital)");
        builder.append(" WHEN bc.capitaltype = 6 THEN IF(( SELECT COUNT(1) FROM order_product_back_info WHERE orderno = bc.orderno ) > 0, 0, ");
        builder.append(" if (bc.scattered = 1 ,bc.capital +bc.scatteredcapital,bc.capital)) ");
        builder.append(" WHEN bc.capitaltype = 10 THEN if (bc.scattered = 1 , - (bc.capital +bc.scatteredcapital), -bc.capital) ");
        builder.append(" END ) AS Receivableaccount, SUM(CASE WHEN bc.capitaltype = 0 AND bc.paytype IN (0,1,2,3,4) THEN if(bc.scattered = 1 ,bc.capital+bc.scatteredcapital ,bc.capital)");
        builder.append(" WHEN bc.capitaltype = 6 THEN IF(( SELECT COUNT( 1 ) FROM order_product_back_info WHERE orderno = bc.orderno ) > 0, -if(bc.scattered = 1 ,bc.capital+bc.scatteredcapital ,bc.capital), 0 ) ");
        builder.append(" WHEN bc.capitaltype = 2 THEN -if(bc.scattered = 1 ,bc.capital+bc.scatteredcapital ,bc.capital) END) AS InvoiceBalance ,");
        builder.append(" SUM(case WHEN bc.capitaltype =0 and bc.paytype IN (0,1,2,3,4) THEN if (bc.scattered = 1 ,bc.capital+bc.scatteredcapital ,bc.capital ) ");
        builder.append(" WHEN bc.capitaltype = 6 THEN IF(( SELECT COUNT(1) FROM order_product_back_info WHERE orderno = bc.orderno ) > 0, -if(bc.scattered = 1 ,bc.capital +bc.scatteredcapital,bc.capital),0)");
        builder.append(" WHEN bc.capitaltype = 2 THEN -if(bc.scattered = 1 ,bc.capital+bc.scatteredcapital ,bc.capital ) end) Deliveryamount,");
        builder.append(" SUM(CASE WHEN bc.capitaltype = 0 AND bc.paytype IN (0,1,2) THEN if (bc.scattered = 1 ,bc.capital+bc.scatteredcapital ,bc.capital )");
        builder.append(" WHEN bc.capitaltype = 1 AND bc.rechargestate = 1 THEN if (bc.scattered = 1 ,bc.capital+bc.scatteredcapital ,bc.capital )");
        builder.append(" WHEN bc.capitaltype = 2 AND bc.paytype IN ( 0, 1, 2 ) THEN if (bc.scattered = 1 ,-(bc.capital+bc.scatteredcapital),-bc.capital )");
        builder.append(" WHEN bc.capitaltype = 3 AND bc.rechargestate = 1 THEN if (bc.scattered = 1 ,-(bc.capital+bc.scatteredcapital),-bc.capital )END) Receiptamount,");
        builder.append(" SUM(CASE WHEN bc.capitaltype = 10 THEN if (bc.scattered = 1 ,- (bc.capital+bc.scatteredcapital) ,- bc.capital )");
        builder.append(" WHEN bc.capitaltype = 6 THEN if (bc.scattered = 1 ,bc.capital+bc.scatteredcapital ,bc.capital ) END) OtherAmount");
        builder.append(" FROM (SELECT ca.capitaltype,ca.capital,ca.tradetime,ca.paytype,ca.orderno,ca.id,ca.rechargestate,ca.tradeno,ca.scattered,ca.scatteredcapital");
        builder.append(" FROM buyer_capital ca WHERE 1 = 1 AND ca.capitaltype in (0,1,2,3,6,10) ");
        if(params!=null){
            if (params.containsKey("invoiceName")&& StringUtils.isNotBlank(params.get("invoiceName"))){
                builder.append(" and ca.invoiceheadup ='"+params.get("invoiceName").trim()+"'");
            }
            if (params.containsKey("userNo")&&StringUtils.isNotBlank(params.get("userNo"))){
                builder.append(" and ca.memberid ='"+params.get("userNo").trim()+"' ");
            }
            if (params.containsKey("companyname")&&StringUtils.isNotBlank(params.get("companyname"))){
                builder.append(" and ca.companyname ='"+params.get("companyname").trim()+"'");
            }
            if (params.containsKey("userName")&&StringUtils.isNotBlank(params.get("userName"))){
                builder.append(" and ca.member_username ='"+params.get("userName").trim()+"' ");
            }
//            if (params.containsKey("startDate")&&StringUtils.isNotBlank(params.get("startDate"))){
//                builder.append(" and ca.tradetime >='"+params.get("startDate")+"' ");
//            }
            if (params.containsKey("endDate")&&StringUtils.isNotBlank(params.get("endDate"))){
                builder.append(" and ca.tradetime <='"+params.get("endDate")+"' ");
            }
                builder.append(" Order By ca.tradetime  )bc ");

        }else{
            builder.append(" Order By ca.tradetime  ) bc " );
        }
        log.info("returnCapitalAccount.getSettlementSql :{}",builder.toString());

        return  builder.toString();
    }

    /**
     * //查询当期结算
     * @param params
     * @return
     */
    public Map<String,Object> getSettlement(Map<String ,String > params){

        Map<String, Object> map = new HashMap<>();
        String settlementSql = getSettlementSql(params);
        BigDecimal Deliveryamount = new BigDecimal(0);
        BigDecimal Receiptamount = new BigDecimal(0);
        BigDecimal OtherAmount = new BigDecimal(0);
        BigDecimal Invoice = new BigDecimal(0);
        BigDecimal Receivable = new BigDecimal(0);

        Map<String, Object> settlement = jdbcTemplate.queryForMap(settlementSql);//Deliveryamount Receiptamount OtherAmount
        if (settlement.containsKey("Deliveryamount")){
            Deliveryamount = CommonUtils.getBigDecimal(settlement.get("Deliveryamount") == null ? 0.00 : settlement.get("Deliveryamount"));
        }
        if (settlement.containsKey("Receiptamount")){
            Receiptamount = CommonUtils.getBigDecimal(settlement.get("Receiptamount") == null ? 0.00 : settlement.get("Receiptamount"));
        }
        if (settlement.containsKey("OtherAmount")){
            OtherAmount = CommonUtils.getBigDecimal(settlement.get("OtherAmount") == null ? 0.00 : settlement.get("OtherAmount"));
        }
        if (settlement.containsKey("InvoiceBalance")){
            Invoice = CommonUtils.getBigDecimal(settlement.get("InvoiceBalance") == null ? 0.00 : settlement.get("InvoiceBalance"));
        }
        if (settlement.containsKey("Receivableaccount")){
            Receivable = CommonUtils.getBigDecimal(settlement.get("Receivableaccount") == null ? 0.00 : settlement.get("Receivableaccount"));
        }
        map.put("DeliveryAmount",Deliveryamount);
        map.put("ReceiptAmount",Receiptamount);
        map.put("OtherAmount",OtherAmount);
        map.put("Invoice",Invoice);
        map.put("Receivable",Receivable);

        return map;
    }
}
