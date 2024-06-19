package com.szbldb.pojo.extensionPojo;

import com.szbldb.pojo.datasetPojo.DataSet;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Collection {
    private Integer id;
    private Integer uid;
    private String name;
    private String description;
    private Integer total;
    private List<DataSet> list;
    private LocalDateTime createTime;
}
