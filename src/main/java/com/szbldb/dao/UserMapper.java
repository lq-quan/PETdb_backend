package com.szbldb.dao;

import com.szbldb.pojo.userPojo.User;
import com.szbldb.pojo.userInfoPojo.UserInfo;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface UserMapper {
    @Select("select * from user")
    List<User> list();

    @Select("select * from user where username = #{username}")
    User getUserByUsername(String username);

    @Insert("insert into token_blacklist value (#{username}, #{token}, #{expireTime})")
    void logout(String username, String token, Date expireTime);

    @Select("select count(*) from token_blacklist where token = #{token}")
    Integer checkLogout(String token);

    @Select("select id from user where username = #{username}")
    Integer getIdByName(String username);

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into user (username, password, email) value (#{username}, #{password}, #{email})")
    void insertUser(User user);

    @Select("select email from user where username = #{username}")
    String getEmail(String username);

    @Select("select * from userinfo where id = #{id}")
    UserInfo getInfoById(int id);

    @Select("select roles from userinfo inner join user u on userinfo.id = u.id and u.username = #{username}")
    String getRolesByUsername(String username);

    @Insert("insert into userinfo (id, name) value (#{id}, #{name})")
    void initUserInfo(UserInfo userInfo);

    @Update("update userinfo set name = #{name}, roles = #{roles}, introduction = #{introduction} where id = #{id}")
    void changeUserInfo(UserInfo userInfo);

    @Update("update userinfo set avatar = #{avatarFilename} where id = #{id}")
    void changeAvatarById(Integer id, String avatarFilename);
}