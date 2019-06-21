package com.js.sas.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "order_product_back_info")
public class OrderProductBackInfo {
    @Id
    private Integer id;
    private String orderno;
    private Integer pdid;
    private String backno;
    private BigDecimal backnum;
    private int backtype;
    private int backstate;
    private Date backtime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOrderno() {
        return orderno;
    }

    public void setOrderno(String orderno) {
        this.orderno = orderno;
    }

    public Integer getPdid() {
        return pdid;
    }

    public void setPdid(Integer pdid) {
        this.pdid = pdid;
    }

    public String getBackno() {
        return backno;
    }

    public void setBackno(String backno) {
        this.backno = backno;
    }

    public BigDecimal getBacknum() {
        return backnum;
    }

    public void setBacknum(BigDecimal backnum) {
        this.backnum = backnum;
    }

    public int getBacktype() {
        return backtype;
    }

    public void setBacktype(int backtype) {
        this.backtype = backtype;
    }

    public void setBackstate(int backstate) {
        this.backstate = backstate;
    }


    public Date getBacktime() {
        return backtime;
    }

    public void setBacktime(Date backtime) {
        this.backtime = backtime;
    }

    @Override
    public String toString() {
        return "OrderProductBackInfo{" +
                "id=" + id +
                ", orderno='" + orderno + '\'' +
                ", pdid=" + pdid +
                ", backno='" + backno + '\'' +
                ", backnum=" + backnum +
                ", type=" + backtype +
                ", backstate=" + backstate +
                ", backtime=" + backtime +
                '}';
    }
}
