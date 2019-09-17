package com.js.sas.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 业务员
 */
@Entity
@Table(name = "JS_Salesman")
@Data
public class SalesmanEntity {

    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "username")
    private String username;

    @Column(name = "name")
    private String name;

    @Column(name = "department_id")
    private String departmentId;
}
