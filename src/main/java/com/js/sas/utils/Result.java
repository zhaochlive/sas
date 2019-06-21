package com.js.sas.utils;

import lombok.*;

import java.io.Serializable;

/**
 * @ClassName Result
 * @Description 接口返回结果类
 * @Author zc
 * @Date 2019/6/13 10:40
 **/
@Data
@AllArgsConstructor
public class Result implements Serializable {

    private static final long serialVersionUID = 1L;

    // 状态码
    private String code;
    // 状态描述
    private String message;
    // 数据
    private Object data;

}
