package com.js.sas.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.poi.ss.formula.functions.T;

import java.io.Serializable;

/**
 * @ClassName Result
 * @Description 接口返回结果类
 * @Author zc
 * @Date 2019/6/13 10:40
 **/
@Data
@AllArgsConstructor
public class Result<T> implements Serializable {
    // 状态码
    private String code;
    // 状态描述
    private String message;
    // 数据
    private T data;

    public static <T> Result<T> getResult(ResultCode rc) {
        return new Result<T>(rc.getCode(), rc.name(), null);
    }

    public static <T> Result<T> getResult(ResultCode rc, T data) {
        return new Result<T>(rc.getCode(), rc.name(), data);
    }
}
