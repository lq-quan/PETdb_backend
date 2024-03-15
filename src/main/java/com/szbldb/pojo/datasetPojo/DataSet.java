package com.szbldb.pojo.datasetPojo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DataSet {
    private Integer id;
    private String name;
    private String type;
    private String status;
    private String country;
    private String uploader;
    private Long size;
    private LocalDate date;
    private String description;
}
