package com.szbldb.pojo.datasetPojo;

import lombok.Data;

import java.util.List;

@Data
public class UploadLocalRes {
    private Integer status;
    private List<Integer> error_file;
    private List<String> chunkUploadedList;
    private String url;
    private List<Integer> uploadedList;
    private boolean success;
}
