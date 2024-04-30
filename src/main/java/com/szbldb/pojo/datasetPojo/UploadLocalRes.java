package com.szbldb.pojo.datasetPojo;

import lombok.Data;

import java.util.List;

@Data
public class UploadLocalRes {
    private Integer status;
    private List<Integer> error_file;

    public UploadLocalRes(Integer status, List<Integer> error_file) {
        this.status = status;
        this.error_file = error_file;
    }
}
