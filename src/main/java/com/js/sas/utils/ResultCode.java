package com.js.sas.utils;

/**
 * @ClassName ResultCode
 * @Description 状态码枚举类
 * @Author zc
 * @Date 2019/6/13 10:47
 **/
public enum ResultCode {
    成功("0000"),
    参数错误("1001"),
    系统异常("9999");

    //状态码
    private String code;

    ResultCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
