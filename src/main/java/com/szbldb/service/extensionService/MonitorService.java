package com.szbldb.service.extensionService;

import com.szbldb.dao.ExtensionMapper;
import com.szbldb.pojo.extensionPojo.MonitorRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MonitorService {

    private final ExtensionMapper extensionMapper;

    @Autowired
    public MonitorService(ExtensionMapper extensionMapper){
        this.extensionMapper = extensionMapper;
    }

    /**
     *
     * @Description 无参方法，返回流量管理信息
     * @return com.szbldb.pojo.extensionPojo.MonitorRes
     * @author Quan Li 2024/7/5 15:54
     **/
    public MonitorRes getMonitorInfo(){
        MonitorRes res = new MonitorRes();
        res.setUsers(extensionMapper.getUsersNum());
        res.setRequests(extensionMapper.getRequestsNum());
        res.setDownloads(extensionMapper.getDownloads());
        res.setStorage(extensionMapper.getStorage());
        return res;
    }
}
