package com.szbldb.service.logService;

import com.szbldb.dao.LogsMapper;
import com.szbldb.pojo.logPojo.LogRes;
import com.szbldb.pojo.logPojo.Operation;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LogService {
    private final LogsMapper logsMapper;
    @Getter
    @Setter
    private String user;

    public LogService(@Autowired LogsMapper logsMapper) {
        this.logsMapper = logsMapper;
    }

    /**
     *
     * @Description 添加操作记录
     * @param operation 操作内容
     * @author Quan Li 2024/7/5 16:20
     **/
    public void addLog(String operation){
        Operation op = new Operation(LocalDateTime.now(), operation);
        op.setOperator(user);
        logsMapper.insertLog(op);
    }

    /**
     *
     * @Description 获取操作记录列表
     * @param page （第）页数
     * @param limit 每页项数
     * @return com.szbldb.pojo.logPojo.LogRes
     * @author Quan Li 2024/7/5 16:20
     **/
    public LogRes getLogs(Integer page, Integer limit){
        LogRes res = new LogRes();
        res.setLogs(logsMapper.getLogs((page - 1) * limit, limit));
        res.setTot(logsMapper.getLogNums());
        return res;
    }
}
