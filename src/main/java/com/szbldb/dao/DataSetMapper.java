package com.szbldb.dao;

import com.szbldb.pojo.datasetPojo.DataSet;
import com.szbldb.pojo.datasetPojo.DataSetLoc;
import org.apache.ibatis.annotations.Insert;
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

    @Select("select * from dataset_loc where id = #{id}")
    DataSetLoc searchLoc(Integer id);

    @Select("select type from dataset where id = #{id}")
    String getType(Integer id);

    @Insert("insert into dataset (name, type, status, country, uploader, size, date, description) VALUE " +
            "(#{name}, #{type}, #{status}, #{country}, #{uploader}, #{size}, #{date}, #{description})")
    void insertDataset(DataSet dataSet);
}