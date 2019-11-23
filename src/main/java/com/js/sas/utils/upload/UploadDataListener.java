package com.js.sas.utils.upload;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.js.sas.entity.DeptStaff;
import com.js.sas.repository.DeptStaffRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 模板读取类
 */
@Log4j2
public class UploadDataListener extends AnalysisEventListener<UploadData> {

    DeptStaffRepository deptStaffRepository;

    public UploadDataListener(DeptStaffRepository deptStaffRepository) {
        this.deptStaffRepository = deptStaffRepository;
    }

    private static final int BATCH_COUNT = 1000;

    List<DeptStaff> list = new ArrayList<>();

    /**
     * 这个每一条数据解析都会来调用
     *
     * @param data
     * @param context
     */
    @Override
    public void invoke(UploadData data, AnalysisContext context) {
        DeptStaff deptStaff = new DeptStaff();
        BeanUtils.copyProperties(data,deptStaff);
        list.add(deptStaff);
        // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
        if (list.size() >= BATCH_COUNT) {
            saveData();
            // 存储完成清理 list
            list.clear();
        }
    }

    /**
     * 所有数据解析完成了 都会来调用
     *
     * @param context
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 清空表数据
        deptStaffRepository.deleteAll();
        deptStaffRepository.flush();
        // 导入新数据
        saveData();
    }

    /**
     * 加上存储数据库
     */
    private void saveData() {
        deptStaffRepository.saveAll(list);
    }
}
