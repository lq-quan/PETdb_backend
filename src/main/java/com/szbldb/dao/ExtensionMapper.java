package com.szbldb.dao;

import com.szbldb.pojo.datasetPojo.DataSet;
import com.szbldb.pojo.extensionPojo.Collection;
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
            " where username = #{username}")
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

    @Results({
            @Result(property = "createTime", column = "create_time")
    })
    @Select("select * from collection inner join user on collection.uid = user.id and user.username = #{username}")
    List<Collection> getCollectionList(String username);

    @Select("select d.id, d.name, type from dataset d inner join coll_dset c on c.did = d.id and c.cid = #{cid}")
    List<DataSet> getDatasetInColl(Integer cid);

    @Select("select count(*) from coll_dset where cid = #{cid}")
    Integer getDSetCountInColl(Integer cid);

    @Delete("delete from collection where id = #{cid} " +
            "and uid in (select id as uid from user where username = #{username})")
    void deleteColl(Integer cid, String username);

    @Insert("insert into coll_dset (cid, did) value (#{cid}, #{did})")
    void insertDatasetToColl(Integer did, Integer cid);


    @Delete("delete from coll_dset where cid = #{cid} and did = #{did}")
    void deleteDatasetFromColl(Integer did, Integer cid);
}
