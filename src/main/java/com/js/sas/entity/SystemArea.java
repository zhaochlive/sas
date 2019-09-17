package com.js.sas.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "system_area")
public class SystemArea {

    @Id
    @Column(name = "id")
    private int id;
    @Column(name = "code")
    private String code;
    @Column(name = "post_code")
    private String postcode;
    @Column(name = "name")
    private String name;
    @Column(name = "fid")
    private int fid;
    @Column(name = "is_Province")
    private int isProvince;
    @Column(name = "is_City")
    private int isCity;
    @Column(name = "is_Area")
    private int isArea;

    @Override
    public String toString() {
        return "Area{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", postcode='" + postcode + '\'' +
                ", name='" + name + '\'' +
                ", fid=" + fid +
                ", isProvince=" + isProvince +
                ", isCity=" + isCity +
                ", isArea=" + isArea +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public int getFid() {
        return fid;
    }

    public void setFid(int fid) {
        this.fid = fid;
    }

    public int getIsProvince() {
        return isProvince;
    }

    public void setIsProvince(int isProvince) {
        this.isProvince = isProvince;
    }

    public int getIsCity() {
        return isCity;
    }

    public void setIsCity(int isCity) {
        this.isCity = isCity;
    }

    public int getIsArea() {
        return isArea;
    }

    public void setIsArea(int isArea) {
        this.isArea = isArea;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}