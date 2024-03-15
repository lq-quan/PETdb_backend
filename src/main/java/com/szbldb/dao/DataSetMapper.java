package com.szbldb.dao;

import com.szbldb.pojo.datasetPojo.DataSet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DataSetMapper {

    @Select("select * from dataset where name like '%${name}%' and type like '%${type}%'" +
            " and uploader like '%${uploader}%' and country like '%${country}%'")
    List<DataSet> searchLike(DataSet dataSet);

    @Select("select * from dataset where name like '%${word}%' or type like '%${word}%' or uploader like '%${word}%'" +
            " or description like '%%${word}' collate utf8mb4_general_ci")
    List<DataSet> searchGlobal(String word);
}