package com.szbldb.pojo.extensionPojo;

import lombok.Data;

@Data
public class MonitorRes {
    private Integer users;
    private Integer requests;
    private Integer downloads;
    private Long storage;
}
