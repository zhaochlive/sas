package com.js.sas.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * 逾期统计表
 */
@Entity
@Data
public class OverdueEntity {
    @Id
    private String settlementName;
}
