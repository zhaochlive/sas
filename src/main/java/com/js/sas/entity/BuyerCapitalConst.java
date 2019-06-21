package com.js.sas.entity;

public class BuyerCapitalConst {
    //消费
    public  static  final int CAPITALTYPE_CONSUM            =0;
    //充值
    public  static  final int CAPITALTYPE_RECHARGE          =1;
    //退款
    public  static  final int CAPITALTYPE_REFUND            =2;
    //提现
    public  static  final int CAPITALTYPE_WITHDRAWAL        =3;
    //授信
    public  static  final int CAPITALTYPE_CREDIT            =4;
    //授信还款
    public  static  final int CAPITALTYPE_CREDIT_REPAY      =5;
    //违约金(买家)
    public  static  final int CAPITALTYPE_PENALTY           =6;
    //远期定金
    public  static  final int CAPITALTYPE_DEPOSIT_LONG      =7;
    //远期余款
    public  static  final int CAPITALTYPE_SURPLUS_LONG      =8;
    //远期全款
    public  static  final int CAPITALTYPE_FULL_PAYMENT_LONG =9;
    //卖家违约金
    public  static  final int CAPITALTYPE_PENALTY_SELLER    =10;
    //授信未出账单还款
    public  static  final int CAPITALTYPE_CREDIT_NOT_PAID   =11;

    //支付宝支付
    public  static  final int PAYMETHOD_ALIPAY     =0;
    //微信支付
    public  static  final int PAYMETHOD_WEIXIN     =1;
    //银行卡支付
    public  static  final int PAYMETHOD_BANKCARD   =2;
    //余额支付
    public  static  final int PAYMETHOD_BALANCE    =3;
    //授信支付
    public  static  final int PAYMETHOD_CREDIT     =4;


    //拆零费
    public  static  final int STATE_15    =15;
    //拆零费退款
    public  static  final int STATE_16   =16;


}