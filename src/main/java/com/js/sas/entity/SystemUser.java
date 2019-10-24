package com.js.sas.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Collection;

/**
 * @author daniel
 * @description: 系统用户
 * @create: 2019-10-16 15:35
 */
@Entity
@Data
@Table(name ="system_user")
@ToString
public class SystemUser {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY )
    private Long id;
    @Column(name="user_name")
    private String userName;
    @Column(name = "password")
    private String password;
    @Column(name = "token")
    private String token;
    @Column(name = "salt")
    private String salt;
    @Column(name = "nick_name")
    private String nickName;
    @Column(name = "phone_num")
    private String phoneNum;
    @Column(name = "email")
    private String email;
    @Column(name = "last_login_time")
    private Timestamp lastLoginTime;
    @Column(name = "err_login")
    private Integer errLogin;
    @Column(name = "status")
    private Integer status;
    @Column(name = "code")
    private String code;
    @Column(name = "create_time")
    private Timestamp createTime;
//    @ManyToMany(cascade = {CascadeType.MERGE})
//    @JsonIgnore
//    @JoinTable(name = "system_user_role",
//            joinColumns = { @JoinColumn(name = "user_id", referencedColumnName = "user_id") },
//            inverseJoinColumns = { @JoinColumn(name = "role_id", referencedColumnName = "role_id") })
//    private Collection<SystemRole> roles;
}
