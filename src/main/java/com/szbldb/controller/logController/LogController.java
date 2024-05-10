package com.szbldb.controller.logController;

import com.szbldb.pojo.Result;
import com.szbldb.service.logService.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogController {
    private final LogService logService;

    public LogController(@Autowired LogService logService) {
        this.logService = logService;
    }

    @RequestMapping("/PETdatabase/logs")
    public Result getLogs(Integer page, Integer limit){
        if(!"admin".equals(logService.getUser())){
            return Result.error("Not admin", 52002);
        }
        return Result.success(logService.getLogs(page, limit));
    }
}
