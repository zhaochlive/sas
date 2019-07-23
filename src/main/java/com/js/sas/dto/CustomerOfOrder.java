package com.js.sas.dto;

import java.util.Date;

public class CustomerOfOrder {
    private Long memberid;//会员ID
    private Date firsttime;//首次下单时间
    private String username;//会员名称
    private String companyname;//企业名称
    private String city;//所在省市
    private String address;//地址
    private String realname;//联系人
    private String mobile;//手机
    private String telephone;//座机
    private String waysalesman;//业务员
    private String totalprice;//总金额

    public Long getMemberid() {
        return memberid;
    }

    public void setMemberid(Long memberid) {
        this.memberid = memberid;
    }

    public Date getFirsttime() {
        return firsttime;
    }

    public void setFirsttime(Date firsttime) {
        this.firsttime = firsttime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCompanyname() {
        return companyname;
    }

    public void setCompanyname(String companyname) {
        this.companyname = companyname;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getWaysalesman() {
        return waysalesman;
    }

    public void setWaysalesman(String waysalesman) {
        this.waysalesman = waysalesman;
    }

    public String getTotalprice() {
        return totalprice;
    }

    public void setTotalprice(String totalprice) {
        this.totalprice = totalprice;
    }

    @Override
    public String toString() {
        return "CustomerOfOrder{" +
                "memberid=" + memberid +
                ", firsttime=" + firsttime +
                ", username='" + username + '\'' +
                ", companyname='" + companyname + '\'' +
                ", city='" + city + '\'' +
                ", address='" + address + '\'' +
                ", realname='" + realname + '\'' +
                ", mobile='" + mobile + '\'' +
                ", telephone='" + telephone + '\'' +
                ", waysalesman='" + waysalesman + '\'' +
                ", totalprice='" + totalprice + '\'' +
                '}';
    }
}
