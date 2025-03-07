package com.szbldb.pojo.datasetPojo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class File {
    private Integer id;
    private Integer datasetId;
    private Long size;
    private String name;
    private String type;
    private LocalDate date;
    private String md5;

    public File(Integer datasetId, Long size, String name, String type) {
        this.datasetId = datasetId;
        this.size = size;
        this.name = name;
        this.type = type;
    }
}
