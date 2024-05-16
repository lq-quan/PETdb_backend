package com.szbldb.controller.extensionController;

import com.szbldb.pojo.Result;
import com.szbldb.pojo.extensionPojo.MonitorRes;
import com.szbldb.service.extensionService.MonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MonitorController {

    private final MonitorService monitorService;

    @Autowired
    public MonitorController(MonitorService monitorService){
        this.monitorService = monitorService;
    }

    @RequestMapping("/PETdatabase/extended/admin/monitor")
    public Result monitor(){
        MonitorRes res = monitorService.getMonitorInfo();
        return Result.success(res);
    }
}
