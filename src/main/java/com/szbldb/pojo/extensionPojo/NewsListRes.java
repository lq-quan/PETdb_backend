package com.szbldb.pojo.extensionPojo;

import lombok.Data;

import java.util.List;

@Data
public class NewsListRes {
    private Integer total;
    private List<News> news;
}
