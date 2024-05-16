package com.szbldb.dao;

import com.szbldb.pojo.datasetPojo.DataSet;
import com.szbldb.pojo.datasetPojo.DataSetLoc;
import com.szbldb.pojo.datasetPojo.File;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DataSetMapper {

    @Select("select * from dataset where name like '%${name}%' and type like '%${type}%'" +
            " and uploader like '%${uploader}%' and country like '%${country}%'")
    List<DataSet> searchLike(DataSet dataSet);

    @Select("select * from dataset where id = #{id}")
    DataSet getDatasetById(Integer id);

    @Select("select * from dataset where name = #{name} and type = #{type}")
    DataSet checkDatasetName(String name, String type);

    @Select("select * from dataset where name like '%${word}%' or type like '%${word}%' or uploader like '%${word}%'" +
            " or description like '%${word}%' collate utf8mb4_general_ci")
    List<DataSet> searchGlobal(String word);

    @Select("select * from dataset_loc d inner join files f where f.id = #{id} and d.id = f.datasetId")
    DataSetLoc searchLocByFileId(Integer id);

    @Select("select * from dataset_loc where id = #{id}")
    DataSetLoc searchLocByDatasetId(Integer id);

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into dataset (name, type, status, country, uploader, size, date, description) VALUE " +
            "(#{name}, #{type}, #{status}, #{country}, #{uploader}, #{size}, #{date}, #{description})")
    void insertDataset(DataSet dataSet);

    @Update("update dataset set name = #{name}, type = #{type}, status = #{status}, description = #{description}, " +
            "country = #{country}, uploader = #{uploader}, date = #{date} where id = #{id}")
    void updateDatasetById(DataSet dataSet);

    @Insert("insert into dataset_loc (id, bucketname, objectname) value (#{id}, #{bucketName}, #{objectName})")
    void insertLoc(DataSetLoc dataSetLoc);

    @Insert("insert into files (datasetId, name, type, date, size, md5) value (#{datasetId}, #{name}, #{type}, #{date}, #{size}, #{md5})")
    void insertFile(File file);

    @Update("update dataset set size = size + #{fileSize} where id = #{id}")
    void updateSize(Long fileSize, Integer id);

    @Select("select * from files where datasetId = #{datasetId}")
    List<File> getFilesByDatasetId(Integer datasetId);

    @Update("update dataset set downloads = downloads + 1 where id = #{id}")
    void updateDownloads(Integer id);

    @Select("select * from dataset inner join files f on dataset.id = f.datasetId and f.id = #{id}")
    DataSet getDatasetByFileId(Integer id);

    @Select("select count(*) from files where datasetId = #{datasetId}")
    Integer getFileNums(Integer datasetId);

    @Select("select * from files where id = #{id}")
    File getFileByFileId(Integer id);

    @Select("select * from files where name = #{filename} and datasetId = #{dataSetId}")
    File checkFilename(String filename, Integer dataSetId);

    @Select("select * from files where md5 = #{md5} limit 1")
    File checkAndGetMd5(String md5);

    @Delete("delete from files where id = #{fileId}")
    void deleteFile(Integer fileId);

    @Delete("delete from files where datasetId = #{id}")
    void deleteAllFilesOfDataset(Integer id);

    @Delete("delete from dataset_loc where id = #{id}")
    void deleteDatasetLoc(Integer id);

    @Delete("delete from dataset where id = #{id}")
    void deleteDataset(Integer id);

}