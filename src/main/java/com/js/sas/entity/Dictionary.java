package com.js.sas.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @ClassName Dictionary
 * @Description 字典表
 * @Author zc
 * @Date 2019/8/3 15:32
 **/
@Entity
@Table(name = "dictionary")
@Data
public class Dictionary implements Serializable {

    private static final long serialVersionUID = 1L;

    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false, length = 11)
    private int id;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "value", nullable = false, length = 500)
    private String value;
}
