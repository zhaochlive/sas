package com.js.sas.utils.excel;

import com.alibaba.excel.event.WriteHandler;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import java.util.List;

/**
 * 导出用友对账单功能，重写导出样式
 */
public class StyleExcelHandler implements WriteHandler {

    private List<Integer> boldList;
    private List<Integer> borderList;
    private List<Integer> backgroundColorList;
    private List<Integer> centerList;
    private List<Integer> spacialBackgroundColorList;
    private List<Integer> centerDetailList;

    /**
     * 构造方法
     *
     * @param boldList 需要加粗的行数列表
     * @param borderList 需要加边框的行数列表
     */
    public StyleExcelHandler(List<Integer> boldList, List<Integer> borderList, List<Integer> backgroundColorList, List<Integer> centerList, List<Integer> spacialBackgroundColorList, List<Integer> centerDetailList) {
        this.boldList = boldList;
        this.borderList = borderList;
        this.backgroundColorList = backgroundColorList;
        this.centerList = centerList;
        this.spacialBackgroundColorList = spacialBackgroundColorList;
        this.centerDetailList = centerDetailList;
    }

    @Override
    public void sheet(int i, Sheet sheet) {
        sheet.setColumnWidth(0, 15*256);
        sheet.setColumnWidth(2, 15*256);
        sheet.setColumnWidth(3, 15*256);
        sheet.setColumnWidth(4, 15*256);
        sheet.setColumnWidth(5, 15*256);
        sheet.setColumnWidth(6, 15*256);
        sheet.setColumnWidth(7, 15*256);
    }

    @Override
    public void row(int i, Row row) {
    }

    @Override
    public void cell(int i, Cell cell) {
        // 从第二行开始设置格式，第一行是表头
        Workbook workbook = cell.getSheet().getWorkbook();
        CellStyle cellStyle = workbook.createCellStyle();

        // 加粗
        if (boldList.contains(cell.getRowIndex())) {
            Font font = workbook.createFont();
            font.setBold(true);
            cellStyle.setFont(font);
        }

        // 加边框
        if (borderList.contains(cell.getRowIndex())) {
            // 下边框
            cellStyle.setBorderBottom(BorderStyle.THIN);
            // 左边框
            cellStyle.setBorderLeft(BorderStyle.THIN);
            // 上边框
            cellStyle.setBorderTop(BorderStyle.THIN);
            // 右边框
            cellStyle.setBorderRight(BorderStyle.THIN);
        }

        // 背景色
        if (backgroundColorList.contains(cell.getRowIndex())) {
            cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }

        // 背景色特殊处理
        if (spacialBackgroundColorList.contains(cell.getRowIndex()) && cell.getColumnIndex() == 0 ) {
            cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }

        // 居中
        if (centerList.contains(cell.getRowIndex())) {
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
        }

        // 明细居中特殊处理
        if (centerDetailList.contains(cell.getRowIndex()) && cell.getColumnIndex() > 2 ) {
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
        }

        cell.getRow().getCell(i).setCellStyle(cellStyle);
    }

}
