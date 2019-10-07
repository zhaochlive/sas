package com.js.sas.utils;

import com.alibaba.excel.event.WriteHandler;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
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

    /**
     * 构造方法
     *
     * @param boldList 需要加粗的行数列表
     * @param borderList 需要加边框的行数列表
     */
    public StyleExcelHandler(List<Integer> boldList, List<Integer> borderList, List<Integer> backgroundColorList, List<Integer> centerList, List<Integer> spacialBackgroundColorList) {
        this.boldList = boldList;
        this.borderList = borderList;
        this.backgroundColorList = backgroundColorList;
        this.centerList = centerList;
        this.spacialBackgroundColorList = spacialBackgroundColorList;
    }

    @Override
    public void sheet(int i, Sheet sheet) {
    }

    @Override
    public void row(int i, Row row) {
    }

    @Override
    public void cell(int i, Cell cell) {
        // 从第二行开始设置格式，第一行是表头
        Workbook workbook = cell.getSheet().getWorkbook();
        CellStyle cellStyle = createStyle(workbook);

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

        cell.getRow().getCell(i).setCellStyle(cellStyle);
    }

    /**
     * 实际中如果直接获取原单元格的样式进行修改, 最后发现是改了整行的样式, 因此这里是新建一个样式
     */
    private CellStyle createStyle(Workbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();
        // 水平对齐方式
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        // 垂直对齐方式
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        return cellStyle;
    }
}
