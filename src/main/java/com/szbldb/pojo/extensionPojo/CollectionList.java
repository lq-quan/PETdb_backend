package com.szbldb.pojo.extensionPojo;

import lombok.Data;

import java.util.List;

@Data
public class CollectionList {
    private Integer total;
    private List<Collection> items;
}
