package com.szbldb.pojo.datasetPojo;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

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
    private Integer downloads;
    private List<File> files;

    @Override
    public String toString() {
        return "DataSet{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                ", country='" + country + '\'' +
                ", uploader='" + uploader + '\'' +
                ", size=" + size +
                ", date=" + date +
                ", description='" + description + '\'' +
                '}';
    }
}
