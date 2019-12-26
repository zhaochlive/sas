package com.js.sas.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * @Author: daniel
 * @date: 2019/12/26 0026 17:23
 * @Description:
 */
@Entity
@Table(name = "YY_Member_Saleman")
@Data
public class MemberSalesman {

    // 姓名
    @Id
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "top_saleman")
    private String topSaleman;

    @Column(name = "second_saleman")
    private String secondSaleman;
}
