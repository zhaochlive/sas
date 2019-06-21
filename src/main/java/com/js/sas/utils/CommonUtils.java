package com.js.sas.utils;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.BaseRowModel;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import org.springframework.validation.BindingResult;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName CommonUtils
 * @Description
 * @Author zc
 * @Date 2019/6/12 15:07
 **/
public class CommonUtils {

    public static BigDecimal getBigDecimal(Object value ) {
        BigDecimal ret = null;
        if( value != null ) {
            if( value instanceof BigDecimal ) {
                ret = (BigDecimal) value;
            } else if( value instanceof String ) {
                ret = new BigDecimal( (String) value );
            } else if( value instanceof BigInteger) {
                ret = new BigDecimal( (BigInteger) value );
            } else if( value instanceof Number ) {
                ret = new BigDecimal( ((Number)value).doubleValue() );
            } else {
                throw new ClassCastException("Not possible to coerce ["+value+"] from class "+value.getClass()+" into a BigDecimal.");
            }
        }
        return ret;
    }

    /**
     * 导出Excel
     *
     * @param response HttpServletResponse
     * @param dataList 导出类List
     * @param fileName 导出文件名，目前sheet页是相同名称。
     * @throws IOException
     */
    public static void export(HttpServletResponse response, List dataList, String fileName, BaseRowModel clazz) throws IOException {
        fileName = new String((fileName + new Date().getTime()).getBytes(), "ISO8859-1");
        ServletOutputStream out = response.getOutputStream();
        response.setContentType("multipart/form-data");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        ExcelWriter writer = new ExcelWriter(out, ExcelTypeEnum.XLSX, true);
        Sheet sheet1 = new Sheet(1, 0, clazz.getClass());
        sheet1.setSheetName(fileName);
        writer.write(dataList, sheet1);
        writer.finish();
        out.flush();
        out.close();
    }

}
