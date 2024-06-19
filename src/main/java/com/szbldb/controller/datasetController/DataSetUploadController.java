package com.szbldb.controller.datasetController;

import com.szbldb.pojo.Result;
import com.szbldb.pojo.datasetPojo.*;
import com.szbldb.service.datasetService.DataSetUploadService;
import com.szbldb.util.JWTHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
public class DataSetUploadController {


    private final DataSetUploadService dataSetUploadService;

    public DataSetUploadController(@Autowired DataSetUploadService dataSetUploadService) {
        this.dataSetUploadService = dataSetUploadService;
    }

    @RequestMapping("/PETdatabase/dataset/uploadinfo")
    public Result uploadMeta(@RequestBody DataSet dataSet, @RequestHeader String token){
        System.out.println(dataSet);
        if(dataSet.getUploader() == null){
            String username = JWTHelper.getUsername(token);
            dataSet.setUploader(username);
        }
        try {
            if(dataSetUploadService.uploadMeta(dataSet)){
                return Result.success();
            }
        } catch (Exception e) {
            log.error("创建数据集失败", e);
        }
        return Result.error("Exist dataset with the same name!", 40004);
    }

    @RequestMapping("/PETdatabase/dataset/uploadtoken")
    public Result getStsToken(){
        StsTokenInfo tokenInfo = dataSetUploadService.datasetUpload();
        if(tokenInfo == null)
            return Result.error("Failed to access", 40010);
        return Result.success(tokenInfo);
    }

    @RequestMapping("/PETdatabase/dataset/update")
    public Result changeMeta(@RequestBody DataSet dataSet){
        System.out.println(dataSet);
        dataSetUploadService.changeMeta(dataSet);
        return Result.success();
    }

    @RequestMapping("/PETdatabase/dataset/callback")
    public Result uploadFile(Integer id, String name, String type, Long size){
        if(name != null && name.length() > 100) return Result.error("Name too long! (more than 100 characters)", 40012);
        File file = new File(id, size, name, type);
        while(true){
            try{
                if(dataSetUploadService.uploadFile(file)){
                    return Result.success();
                }
                else return Result.error("Exist file with the same name!", 40011);
            }catch (PessimisticLockingFailureException deadlockE){
                System.err.println("Caught DeadLock!");
                log.info("检测到死锁，即将尝试重新进行", deadlockE);
            }
        }
    }

    @RequestMapping("/PETdatabase/dataset/uploadLocal")
    public Result uploadLocal(@RequestBody FilePart part){
        List<String> urls = dataSetUploadService.uploadLocal(part);
        UploadLocalRes res = new UploadLocalRes();
        res.setChunkUploadedList(urls);
        return Result.success(res);
    }

    @RequestMapping("/PETdatabase/dataset/mergefile")
    public Result mergeFile(@RequestBody FilePart part){
        dataSetUploadService.mergeFile(part);
        UploadLocalRes res = new UploadLocalRes();
        res.setSuccess(true);
        return Result.success(res);
    }

    @RequestMapping("/PETdatabase/dataset/checkmd5")
    public Result checkMd5(FilePart part){
        UploadLocalRes res = new UploadLocalRes();
        List<Integer> uploaded = new ArrayList<>();
        Boolean isOk = dataSetUploadService.checkMd5(uploaded, part);
        if(isOk == null) return Result.error("上传失败，请检查文件名是否重复", 40010);
        else if(isOk){
            res.setStatus(1);
        }
        else if(uploaded.isEmpty()) res.setStatus(0);
        else res.setStatus(2);
        res.setUploadedList(uploaded);
        return Result.success(res);
    }
}
