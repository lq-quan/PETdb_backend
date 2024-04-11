package com.szbldb.dao;

import com.szbldb.pojo.licensePojo.Submission;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LicenseMapper {

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into submission (firstname, lastname, degree, company, department, contact, purpose" +
            ", date, country, province, city, street, pscode) " +
            "value (#{firstname}, #{lastname}, #{degree}, #{company}, #{department}, #{contact}, #{purpose}, " +
            "#{date}, #{address.country}, #{address.province}, #{address.city}, #{address.street}, #{address.pscode})")
    void createApplication(Submission submission);

    @Select("select * from submission where id = #{id}")
    @Results({
            @Result(property = "address.country", column = "country"),
            @Result(property = "address.province", column = "province"),
            @Result(property = "address.city", column = "city"),
            @Result(property = "address.street", column = "street"),
            @Result(property = "address.pscode", column = "pscode")
    })
    Submission checkApplication(Integer id);

    @Insert("insert into user_appl select #{sid} as 'sid', id as 'uid', username from user where username = #{username}")
    void appendUserAppl(String username, Integer sid);

    @Select("select sid from user_appl where username = #{username}")
    Integer getSid(String username);

    @Select("select status from submission inner join user_appl ua on submission.id = ua.sid and ua.username = #{username}")
    String getStatusByUsername(String username);

    @Select("select status from submission where id = #{id}")
    String getStatusById(Integer id);

    @Select("select * from submission where firstname like '%${word}%' or lastname like '%${word}%' or company like '%${word}%'" +
            "limit #{limit} offset #{offset}")
    @Results({
            @Result(property = "address.country", column = "country"),
            @Result(property = "address.province", column = "province"),
            @Result(property = "address.city", column = "city"),
            @Result(property = "address.street", column = "street"),
            @Result(property = "address.pscode", column = "pscode")
    })
    List<Submission> searchSubmissions(String word, Integer offset, Integer limit);

    @Update("update submission set status = #{status}, reason = #{reason}, auditor = #{auditor} where id = #{id}")
    void updateSubmissionById(Integer id, String auditor, String status, String reason);

}
