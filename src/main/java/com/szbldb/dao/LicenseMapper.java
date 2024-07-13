package com.szbldb.dao;

import com.szbldb.pojo.licensePojo.Submission;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface LicenseMapper {

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into submission (firstname, lastname, degree, company, department, contact, purpose" +
            ", date, country, province, city, street, pscode) " +
            "value (#{firstname}, #{lastname}, #{degree}, #{company}, #{department}, #{contact}, #{purpose}, " +
            "#{date}, #{address.country}, #{address.province}, #{address.city}, #{address.street}, #{address.pscode})")
    void createApplication(Submission submission);

    @Update("update submission inner join user_appl ua set firstname = #{firstname}, lastname = #{lastname}, " +
            "degree = #{degree}, company = #{company}, department = #{department}, " +
            "contact = #{contact}, purpose = #{purpose}, date = #{date}, country = #{address.country}, " +
            "province = #{address.province}, city = #{address.city}, street = #{address.street}, pscode = #{address.pscode} " +
            "where id = ua.sid and ua.username = #{username}")
    void updateApplicationByUsername(Submission submission, String username);

    @Select("select * from submission where id = #{sid}")
    @Results({
            @Result(property = "address.country", column = "country"),
            @Result(property = "address.province", column = "province"),
            @Result(property = "address.city", column = "city"),
            @Result(property = "address.street", column = "street"),
            @Result(property = "address.pscode", column = "pscode")
    })
    Submission checkApplication(Integer sid);

    @Insert("insert into email_status select id, email, #{expireTime} as 'expire_time' from user where username = #{username}")
    void insertValidEmail(String username, Date expireTime);

    @Select("select email from user where binary username = #{username}")
    String getEmailByUsername(String username);

    @Select("select count(*) from email_status e inner join user u on u.id = e.id and u.username = #{username}")
    Integer checkIfVerifiedByUsername(String username);

    @Select("select count(username) from admins where binary username = #{username}")
    Integer checkIfAdmin(String username);

    @Insert("insert into user_appl select #{sid} as 'sid', id as 'uid', username from user where binary username = #{username}")
    void appendUserAppl(String username, Integer sid);

    @Select("select sid from user_appl where binary username = #{username}")
    Integer getSid(String username);

    @Select("select status from submission inner join user_appl ua on submission.id = ua.sid and ua.username = #{username}")
    String getStatusByUsername(String username);


    @Select("select * from submission where firstname like '%${name}%' or lastname like '%${name}%' and status = #{status} " +
            "limit #{limit} offset #{offset}")
    List<Submission> searchSubmissions(String name, String status, Integer offset, Integer limit);


    @Update("update submission set status = #{status}, reason = #{reason}, auditor = #{auditor} where id = #{id}")
    void updateSubmissionById(Integer id, String auditor, String status, String reason);

}
