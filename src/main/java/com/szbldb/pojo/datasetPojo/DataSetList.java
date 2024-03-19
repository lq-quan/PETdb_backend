package com.szbldb.pojo.datasetPojo;

import lombok.Data;

import java.util.List;

@Data
public class DataSetList {
    private Integer total;
    private List<DataSet> setList;

    public DataSetList(Integer total, List<DataSet> setList) {
        this.total = total;
        this.setList = setList;
    }

    public DataSetList() {
    }
}
