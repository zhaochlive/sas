package com.js.sas.utils.excel;

import com.alibaba.excel.event.WriteHandler;
import org.apache.poi.ss.usermodel.*;

import java.util.List;

/**
 * 导出用友对账单功能，重写导出样式
 */
public class SalesOverdueStyleExcelHandler implements WriteHandler {

    private List<Integer> backgroundColorList;
    private List<Integer> boldList;
    private List<Integer> highList;

    /**
     * 构造方法
     */
    public SalesOverdueStyleExcelHandler(List<Integer> backgroundColorList, List<Integer> boldList, List<Integer> highList) {
        this.backgroundColorList = backgroundColorList;
        this.boldList = boldList;
        this.highList = highList;
    }

    @Override
    public void sheet(int i, Sheet sheet) {
    }

    @Override
    public void row(int i, Row row) {
        if (highList != null) {
            if (highList.contains(i)) {
                row.setHeight((short) 500);
            }
        }
    }

    @Override
    public void cell(int i, Cell cell) {
        Workbook workbook = cell.getSheet().getWorkbook();
        CellStyle cellStyle = cell.getCellStyle();
        //CellStyle cellStyle = workbook.createCellStyle();
        // 前两行标题加边框
        if (cell.getRowIndex() < 2) {
            // 下边框
            cellStyle.setBorderBottom(BorderStyle.THIN);
            // 左边框
            cellStyle.setBorderLeft(BorderStyle.THIN);
            // 上边框
            cellStyle.setBorderTop(BorderStyle.THIN);
            // 右边框
            cellStyle.setBorderRight(BorderStyle.THIN);
            // 居中
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
        } else {
            // 单元格位置
            if (i < 3) {
                // 靠左
                cellStyle.setAlignment(HorizontalAlignment.LEFT);
            } else if (i < 5) {
                // 靠右
                cellStyle.setAlignment(HorizontalAlignment.RIGHT);
            } else if (i == 5) {
                // 靠左
                cellStyle.setAlignment(HorizontalAlignment.LEFT);
            } else {
                // 靠右
                cellStyle.setAlignment(HorizontalAlignment.RIGHT);
            }
        }
        if (boldList != null) {
            // 加粗
            if (boldList.contains(cell.getRowIndex())) {
                Font font = workbook.createFont();
                font.setBold(true);
                cellStyle.setFont(font);
            }
        }
        if (backgroundColorList != null) {
            // 背景色
            if (backgroundColorList.contains(cell.getRowIndex())) {
                if (cell.getRowIndex() > 1) {
                    cellStyle = workbook.createCellStyle();
                }
                cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            }
        }
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cell.getRow().getCell(i).setCellStyle(cellStyle);
    }

}