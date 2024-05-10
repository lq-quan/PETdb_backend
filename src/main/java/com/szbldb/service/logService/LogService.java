package com.szbldb.service.logService;

import com.szbldb.dao.LogsMapper;
import com.szbldb.pojo.logPojo.LogRes;
import com.szbldb.pojo.logPojo.Operation;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LogService {
    private final LogsMapper logsMapper;
    @Getter
    @Setter
    private String user;

    public LogService(@Autowired LogsMapper logsMapper) {
        this.logsMapper = logsMapper;
    }

    public void addLog(String operation){
        Operation op = new Operation(LocalDateTime.now(), operation);
        op.setOperator(user);
        logsMapper.insertLog(op);
    }

    public LogRes getLogs(Integer page, Integer limit){
        LogRes res = new LogRes();
        res.setLogs(logsMapper.getLogs((page - 1) * limit, limit));
        res.setTot(logsMapper.getLogNums());
        return res;
    }
}
