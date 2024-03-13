package com.szbldb.dao;

import com.szbldb.pojo.userPojo.User;
import com.szbldb.pojo.userInfoPojo.UserInfo;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {
    @Select("select * from user")
    List<User> list();

    @Select("select * from user where username = #{username}")
    User getUserByUsername(String username);

    @Select("select id from user where username = #{username}")
    Integer getIdByName(String username);

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into user (username, password, email) VALUE (#{username}, #{password}, #{email})")
    void insertUser(User user);

    @Select("select * from userinfo where id = #{id}")
    UserInfo getInfoById(int id);

    @Select("select * from userinfo inner join user u on userinfo.id = u.id and u.username = #{username}")
    UserInfo getInfoByUsername(String username);

    @Insert("insert into userinfo (id, name) value (#{id}, #{name})")
    void initUserInfo(UserInfo userInfo);

    @Update("update userinfo set name = #{name}, roles = #{name}, introduction = #{introduction}, avatar = #{avatar} where id = #{id}")
    void changeUserInfo(UserInfo userInfo);
}