package com.js.sas.entity;

import lombok.Data;

import java.util.Date;

/**
 * @Author: daniel
 * @date: 2020/5/28 0028 14:07
 * @Description:
 */
@Data
public class Facilitator {
    private Long memberid;
    private String name;
    private Date startTime;
    private Date endTime;
}
