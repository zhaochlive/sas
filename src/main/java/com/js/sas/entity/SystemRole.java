package com.js.sas.entity;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.util.Collection;

/**
 * @author daniel
 * @description: 系统菜单
 * @create: 2019-10-18 15:51
 */
@Entity
@Data
@Table(name ="system_role")
@ToString
public class SystemRole {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="role_id")
    private Long roleId;
    @Column(name="name")
    private String name;
    @Column(name = "status")
    private Integer status;
    @Column(name = "description")
    private String description;
//    @ManyToMany(mappedBy = "roles")
//    @Basic(fetch = FetchType.LAZY)
//    private Collection<SystemUser> users;
}
