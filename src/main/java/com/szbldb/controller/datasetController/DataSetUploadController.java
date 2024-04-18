package com.szbldb.controller.datasetController;

import com.szbldb.pojo.Result;
import com.szbldb.pojo.datasetPojo.DataSet;
import com.szbldb.pojo.datasetPojo.File;
import com.szbldb.pojo.datasetPojo.StsTokenInfo;
import com.szbldb.service.datasetService.DataSetUploadService;
import com.szbldb.util.JWTHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataSetUploadController {

    @Autowired
    private DataSetUploadService dataSetUploadService;
    @RequestMapping("/PETdatabase/dataset/uploadinfo")
    public Result uploadMeta(@RequestBody DataSet dataSet, @RequestHeader String token) throws Exception{
        System.out.println(dataSet);
        if(dataSet.getUploader() == null){
            String username = JWTHelper.getUsername(token);
            dataSet.setUploader(username);
        }
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

    @RequestMapping("/PETdatabase/dataset/update")
    public Result changeMeta(@RequestBody DataSet dataSet){
        System.out.println(dataSet);
        dataSetUploadService.changeMeta(dataSet);
        return Result.success();
    }

    @RequestMapping("/PETdatabase/dataset/callback")
    public Result uploadFile(Integer id, String name, String type, Integer size){
        File file = new File(id, size, name, type);
        if(dataSetUploadService.uploadFile(file)){
            return Result.success();
        }
        else return Result.error("Exist file with the same name!", 50011);
    }
}
