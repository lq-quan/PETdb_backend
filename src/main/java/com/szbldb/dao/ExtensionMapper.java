package com.szbldb.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ExtensionMapper {

    @Select("select count(id) from user")
    Integer getUsersNum();

    @Select("select count(id) from submission")
    Integer getRequestsNum();

    @Select("select sum(downloads) from dataset")
    Integer getDownloads();

    @Select("select sum(size) from dataset")
    Long getStorage();
}
