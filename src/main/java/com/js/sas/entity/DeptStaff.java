package com.js.sas.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * @author ：zc
 * @date ：2019/11/22 09:31
 */
@Entity
@Table(name = "dept_staff")
@Data
public class DeptStaff {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // 姓名
    @Column(name = "name", nullable = false)
    private String name;

    // 部门
    @Column(name = "department", nullable = false)
    private String department;
}
