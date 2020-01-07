package com.js.sas.config;

import com.js.sas.utils.Result;
import com.js.sas.utils.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@Slf4j
public class GlobalException {
    /**
     * 未知异常处理
     *
     * @param e 异常
     * @return 结果
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result<T> handleException(Exception e) {
        log.error(e.getMessage(), e);
        return Result.getResult(ResultCode.系统异常);
    }

}
