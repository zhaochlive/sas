package com.js.sas.utils.upload;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.Data;

/**
 * @author ：zc
 * @date ：2019/11/22 10:48
 */
@Data
public class UploadData extends BaseRowModel {
    @ExcelProperty(index = 0)
    private String name;

    @ExcelProperty(index = 1)
    private String department;
}
