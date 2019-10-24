package com.js.sas.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "system_role_menu")
public class SystemRoleMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    @JoinColumn(name="menu_id")
    private SystemMenu menu;
    @ManyToOne
    @JoinColumn(name="role_id")
    private SystemRole role;
}
