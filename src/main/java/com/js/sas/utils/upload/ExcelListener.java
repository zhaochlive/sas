package com.js.sas.utils.upload;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ：zc
 * @date ：2019/11/22 17:43
 */
public class ExcelListener extends AnalysisEventListener {
    private List<Object> datas = new ArrayList<>();
    public void invoke(Object o, AnalysisContext analysisContext) {
        datas.add(o);
        doSomething(o);
    }

    private void doSomething(Object object) {
    }

    public List<Object> getDatas() {
        return datas;
    }

    public void setDatas(List<Object> datas) {
        this.datas = datas;
    }

    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
    }
}
