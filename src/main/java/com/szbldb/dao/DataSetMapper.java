package com.szbldb.dao;

import com.szbldb.pojo.datasetPojo.DataSet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DataSetMapper {

    @Select("select * from dataset where name like '%${name}%' and type like '%${type}%' and uploader like '%${uploader}%'")
    List<DataSet> searchLike(DataSet dataSet);

    @Select("select * from dataset where name like '%${word}%' or type like '%${word}%' or uploader like '%${word}%'")
    List<DataSet> searchGlobal(String word);

    @Select("select * from dataset where country like '%${country}%'")
    List<DataSet> searchByCountry(String country);
}