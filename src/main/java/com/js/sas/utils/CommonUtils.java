package com.js.sas.utils;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.BaseRowModel;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import org.springframework.boot.configurationprocessor.json.JSONArray;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
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
     * @param fileName 导出文件名，目前sheet页是相同名称
     * @param clazz    对应导出Entity，需要继承BaseRowModel
     * @throws IOException @Description
     */
    public static void export(HttpServletResponse response, List<? extends BaseRowModel> dataList, String fileName, BaseRowModel clazz) throws IOException {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");

        Sheet sheet1 = new Sheet(1, 0, clazz.getClass());
        sheet1.setSheetName(fileName);

        fileName = fileName + df.format(new Date());
        ServletOutputStream out = response.getOutputStream();
        response.setContentType("multipart/form-data");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment;filename*= UTF-8''" + URLEncoder.encode(fileName, "UTF-8") + ".xlsx");
        ExcelWriter writer = new ExcelWriter(out, ExcelTypeEnum.XLSX, true);

        writer.write(dataList, sheet1);
        writer.finish();
        out.flush();
        out.close();
    }

    /**
     * 自定义导出Excel
     *
     * @param response HttpServletResponse
     * @param columnNameList 导出列名List
     * @param dataList 导出数据List
     * @param fileName 导出文件名，目前sheet页是相同名称
     * @throws IOException @Description
     */
    public static void exportByList(HttpServletResponse response, List<String> columnNameList, List<List<Object>> dataList, String fileName) throws IOException {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
        fileName = fileName + df.format(new Date());
        ServletOutputStream out = response.getOutputStream();
        response.setContentType("multipart/form-data");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment;filename*= UTF-8''" + URLEncoder.encode(fileName, "UTF-8") + ".xlsx");

        Sheet sheet1 = new Sheet(1, 0);
        sheet1.setSheetName(fileName);
        sheet1.setAutoWidth(Boolean.TRUE);

        // 设置列名
        if(columnNameList != null){
            List<List<String>> list = new ArrayList<>();
            columnNameList.forEach(h -> list.add(Collections.singletonList(h)));
            sheet1.setHead(list);
        }

        ExcelWriter writer = new ExcelWriter(out, ExcelTypeEnum.XLSX, true);
        writer.write1(dataList, sheet1);
        writer.finish();
        out.flush();
        out.close();

    }

    /**
     * 把ResultSet集合转换成JsonArray数组。
     *
     * @param rs
     * @param withColumns 是否包含列名数据
     * @return 符合Bootstrap Table格式的数据
     * @throws SQLException
     */
    public static Map<String, Object> formatRsToMap(ResultSet rs, boolean withColumns) throws SQLException {

        Map<String, Object> resultMap = new LinkedHashMap<String, Object>();

        ResultSetMetaData rsmd = rs.getMetaData();
        // 数据列数
        int count = rsmd.getColumnCount();

        if(withColumns) {
            // 列名数据
            List<Map<String, String>> columnsList = new ArrayList<Map<String, String>>();
            for (int i = 1; i <= count; i++) {
                Map<String, String> columnsMap = new LinkedHashMap<String, String>();
                columnsMap.put("field", rsmd.getColumnName(i));
                columnsMap.put("title", rsmd.getColumnName(i));
                columnsMap.put("align", "center");
                columnsMap.put("valign", "middle");
                columnsList.add(columnsMap);
            }
            resultMap.put("columns", columnsList);
        }

        JSONArray array = new JSONArray();
        // 数据
        List<Map<String, String>> rowsList = new ArrayList<Map<String, String>>();
        while (rs.next()) {
            Map<String, String> rowsMap = new LinkedHashMap<String, String>();
            for (int i = 1; i <= count; i++) {
                rowsMap.put(rsmd.getColumnName(i), rs.getString(i));
            }
            rowsList.add(rowsMap);
        }
        resultMap.put("rows", rowsList);

        return resultMap;
    }

    /**
     * 把ResultSet集合转换成List。
     *
     * @param rs
     * @param withColumns 是否包含列名数据
     * @return 符合Bootstrap Table格式的数据
     * @throws SQLException
     */
    public static Map<String, List> formatRsToListMap(ResultSet rs, boolean withColumns) throws SQLException {

        Map<String, List> resultMap = new LinkedHashMap();

        ResultSetMetaData rsmd = rs.getMetaData();
        // 数据列数
        int count = rsmd.getColumnCount();

        if(withColumns) {
            // 列名数据
            List<String> columnsList = new ArrayList<String>();
            for (int i = 1; i <= count; i++) {
                columnsList.add(rsmd.getColumnName(i));
            }
            resultMap.put("columnsList", columnsList);
        }

        // 数据
        List<List<Object>> rowsList = new ArrayList();
        while (rs.next()) {
            List<Object> dataList = new ArrayList();
            for (int i = 1; i <= count; i++) {
                dataList.add(rs.getString(i));
            }
            rowsList.add(dataList);
        }
        resultMap.put("rowList", rowsList);

        return resultMap;
    }

    /**
     * 账期客户计算逾期应减去的计算周期数
     *
     * 例如：账期月 1，账期日 20
     * 如果当前日期小于20日，则计算截止到上上个结算周期的应收；
     * 如果当前日期大于等于20日，则计算截止到上个结算周期的应收；
     *
     * @param month 账期月，0为当月
     * @param day 账期日
     * @return 从当前月份起，计算逾期应减去的计算周期数
     */
    public static int overdueMonth(int month, int day) {
        Calendar cal = Calendar.getInstance();
        if (cal.get(cal.DATE) < day) {
            month = month + 1;
        }
        return month;
    }

    /**
     * 账期客户计算逾期补零数量
     *
     * @param month 账期月，0为当月
     * @param day 账期日
     * @return 从当前月份起，计算逾期应减去的计算周期数
     */
    public static int overdueZero(int month, int day) {
        Calendar cal = Calendar.getInstance();
        if (cal.get(cal.DATE) >= day) {
            month = month - 1;
        }
        return month;
    }

}
