package com.js.sas.utils;

/**
 * @ClassName ResultUtils
 * @Description 接口返回结果工具类
 * @Author zc
 * @Date 2019/6/13 10:53
 **/
public class ResultUtils {
    public static Result getResult(ResultCode rc) {
        return new Result(rc.getCode(), rc.name(), null);
    }

    public static Result getResult(ResultCode rc, Object obj) {
        return new Result(rc.getCode(), rc.name(), obj);
    }
}
