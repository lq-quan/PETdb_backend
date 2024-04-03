package com.szbldb.pojo.datasetPojo;

import lombok.Data;

@Data
public class DataSetLoc {
    private Integer id;
    private String endpoint;
    private String bucketName;
    private String objectName;
}
