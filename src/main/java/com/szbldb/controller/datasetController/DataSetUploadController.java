package com.szbldb.controller.datasetController;

import com.szbldb.pojo.Result;
import com.szbldb.pojo.datasetPojo.DataSet;
import com.szbldb.pojo.datasetPojo.StsTokenInfo;
import com.szbldb.service.datasetService.DataSetUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataSetUploadController {

    @Autowired
    private DataSetUploadService dataSetUploadService;
    @RequestMapping("/PETdatabase/dataset/uploadinfo")
    public Result uploadMeta(@RequestBody DataSet dataSet){
        dataSetUploadService.uploadMeta(dataSet);
        return Result.success();
    }

    @RequestMapping("/PETdatabase/dataset/uploadtoken")
    public Result getStsToken(){
        StsTokenInfo tokenInfo = dataSetUploadService.datasetUpload();
        if(tokenInfo == null)
            return Result.error("Failed to access", 50010);
        return Result.success(tokenInfo);
    }
}
