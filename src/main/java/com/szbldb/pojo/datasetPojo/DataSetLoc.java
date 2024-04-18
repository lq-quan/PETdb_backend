package com.szbldb.pojo.datasetPojo;

import lombok.Data;

@Data
public class DataSetLoc {
    private Integer id;
    private String endpoint;
    private String bucketName;
    private String objectName;

    @Override
    public String toString() {
        return "DataSetLoc{" +
                "id=" + id +
                ", endpoint='" + endpoint + '\'' +
                ", bucketName='" + bucketName + '\'' +
                ", objectName='" + objectName + '\'' +
                '}';
    }
}
