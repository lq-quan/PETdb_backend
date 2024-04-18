package com.szbldb.pojo.datasetPojo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class File {
    private Integer id;
    private Integer datasetId;
    private Integer size;
    private String name;
    private String type;
    private LocalDate date;

    public File(Integer datasetId, Integer size, String name, String type) {
        this.datasetId = datasetId;
        this.size = size;
        this.name = name;
        this.type = type;
    }
}
