package com.js.sas.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "system_user_role")
public class SystemUserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    @JoinColumn(name="user_id")
    private SystemUser user;
    @ManyToOne
    @JoinColumn(name="role_id")
    private SystemRole role;
}
