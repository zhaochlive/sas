package com.js.sas.entity;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;

/**
 * @author daniel
 * @description: 系统菜单
 * @create: 2019-10-18 15:51
 */
@Entity
@Data
@Table(name ="system_menu")
@ToString
public class SystemMenu {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="menu_id")
    private Long menuId;
    @Column(name="pid")
    private Long pid;
    @Column(name="name")
    private String name;
    @Column(name="url")
    private String url;
    @Column(name="icon")
    private String  icon;
    @Column(name="sort")
    private Integer sort;
    @Column(name="id_column")
    private String idColumn;
    @Column(name = "status")
    private Integer status;
}
