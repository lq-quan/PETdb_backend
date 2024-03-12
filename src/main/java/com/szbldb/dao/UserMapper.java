package com.szbldb.dao;

import com.szbldb.pojo.User;
import com.szbldb.pojo.userInfoPojo.UserInfo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper {
    @Select("select * from user")
    List<User> list();

    @Select("select * from user where username = #{username}")
    User getUserByUsername(String username);

    @Select("select id from user where username = #{username}")
    Integer getIdByName(String username);

    @Insert("insert into user (username, password, email) VALUE (#{username}, #{password}, #{email})")
    int insertUser(User user);

    @Select("select * from userinfo where id = #{id}")
    UserInfo getInfoById(int id);
}
