package com.szbldb.dao;

import com.szbldb.pojo.userPojo.User;
import com.szbldb.pojo.userInfoPojo.UserInfo;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface UserMapper {
    @Select("select username from user where id = #{id}")
    String getUsernameById(Integer id);

    @Select("select * from user where binary username = #{username}")
    User getUserByUsername(String username);

    @Insert("insert into token_blacklist value (#{username}, #{token}, #{expireTime})")
    void logout(String username, String token, Date expireTime);

    @Select("select count(*) from token_blacklist where token = #{token}")
    Integer checkLogout(String token);

    @Select("select id from user where username = #{username}")
    Integer getIdByName(String username);

    @Update("update user set password = #{password} where binary username = #{username}")
    void modifyPassword(String username, String password);

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into user (username, password, email) value (#{username}, #{password}, #{email})")
    void insertUser(User user);

    @Select("select email from user where binary username = #{username}")
    String getEmail(String username);

    @Select("select * from userinfo where id = #{id}")
    UserInfo getInfoById(int id);

    @Select("select roles from userinfo inner join user u on userinfo.id = u.id and binary u.username = #{username}")
    String getRolesByUsername(String username);

    @Insert("insert into userinfo (id, name) value (#{id}, #{name})")
    void initUserInfo(UserInfo userInfo);

    @Update("update userinfo set name = #{name}, roles = #{roles}, introduction = #{introduction} where id = #{id}")
    void changeUserInfo(UserInfo userInfo);

    @Update("update userinfo set avatar = #{avatar} where id = #{id}")
    void changeAvatarById(Integer id, String avatar);

    @Update("update admins set ip_address = #{ipAddress}, cur_token = #{token}, expire_time = #{expireTime}" +
            " where username = #{username}")
    void updateAdmin(String username, String ipAddress, String token, Date expireTime);

    @Select("select ip_address from admins where binary username = #{username}")
    String checkIpAddrOfAdmin(String username);

    @Select("select cur_token from admins where binary username = #{username}")
    String checkTokenOfAdmin(String username);

    @Select("select us.id, username, email from user us inner join userinfo ui on us.id = ui.id and ui.roles = 'admin' " +
            "limit #{limit} offset #{offset}")
    List<User> getAdmins(Integer limit, Integer offset);

    @Insert("insert into admins (username) value (#{username})")
    void insertAdmin(String username);

    @Delete("delete from admins where username in (select username from user where id = #{id})")
    void deleteAdminById(Integer id);

    @Delete("delete from userinfo where id = #{id}")
    void deleteUserinfo(Integer id);

    @Delete("delete from user where id = #{id}")
    void deleteUser(Integer id);
}