package com.szbldb.pojo.logPojo;

import lombok.Data;

import java.util.List;

@Data
public class LogRes {
    private Integer tot;
    private List<Operation> logs;
}
