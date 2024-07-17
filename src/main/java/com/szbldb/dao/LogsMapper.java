package com.szbldb.dao;

import com.szbldb.pojo.logPojo.Operation;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LogsMapper {
    @Select("select * from logs order by id desc limit #{limit} offset #{offset} ")
    List<Operation> getLogs(Integer offset, Integer limit);

    @Select("select count(*) from logs")
    Integer getLogNums();

    @Insert("insert into logs (time, operation, operator) value (#{time}, #{operation}, #{operator})")
    void insertLog(Operation operation);

    @Select("select count(username) from admins where binary username = #{username}")
    Integer checkIfAdmin(String username);
}
