package com.szbldb.dao;

import com.szbldb.pojo.datasetPojo.DataSet;
import com.szbldb.pojo.extensionPojo.Collection;
import com.szbldb.pojo.extensionPojo.News;
import org.apache.ibatis.annotations.*;


import java.util.List;

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

    @Options(useGeneratedKeys = true, keyProperty = "collection.id")
    @Insert("insert into collection (uid, description, create_time, name)" +
            " select id, #{collection.description}, #{collection.createTime}, #{collection.name}" +
            " from user" +
            " where binary username = #{username}")
    void createCollection(String username, Collection collection);

    @Select("select count(*) from collection " +
            "inner join user on user.username = #{username} and collection.uid = user.id")
    Integer checkCollectionCount(String username);

    @Select("select count(*) from collection " +
            "inner join user on collection.uid = user.id " +
            "where user.username = #{username} and collection.name = #{collName}")
    Integer checkIfExisted(String username, String collName);

    @Select("select count(*) from collection " +
            "inner join user on collection.uid = user.id " +
            "where user.username = #{username} and collection.id = #{cid}")
    Integer checkIfUserColl(String username, Integer cid);

    @Select("select count(cid) from coll_dset where cid = #{cid} and did = #{did}")
    Integer checkIfDsetInColl(Integer did, Integer cid);

    @Results({
            @Result(property = "createTime", column = "create_time")
    })
    @Select("select * from collection inner join user on collection.uid = user.id and user.username = #{username}")
    List<Collection> getCollectionList(String username);

    @Results({
            @Result(column = "did", property = "id"),
            @Result(column = "dataset_name", property = "name"),
            @Result(column = "status", property = "status")
    })
    @Select("select did, dataset_name, status from coll_dset where cid = #{cid}")
    List<DataSet> getDatasetInColl(Integer cid);

    @Select("select count(*) from coll_dset where cid = #{cid}")
    Integer getDSetCountInColl(Integer cid);

    @Select("select status, name from dataset where id = #{did}")
    DataSet checkDataset(Integer did);

    @Delete("delete from collection where id = #{cid} " +
            "and uid in (select id as uid from user where binary username = #{username})")
    void deleteColl(Integer cid, String username);

    @Insert("insert into coll_dset (cid, did, status, dataset_name) value (#{cid}, #{did}, #{status}, #{datasetName})")
    void insertDatasetToColl(Integer did, Integer cid, String status, String datasetName);


    @Delete("delete from coll_dset where cid = #{cid} and did = #{did}")
    void deleteDatasetFromColl(Integer did, Integer cid);

    @Insert("insert into news (content, image_src, link) value (#{news.content}, #{news.imageSrc}, #{news.link})")
    void createNews(News news);

    @Results({
            @Result(column = "image_src", property = "imageSrc")
    })
    @Select("select nid, image_src, content, link from news")
    List<News> getNews();

    @Select("select image_src from news where nid = #{nid}")
    String getNewsSrcByNid(Integer nid);

    @Delete("delete from news where nid = #{nid}")
    void deleteNews(Integer nid);

}
