package com.szbldb.controller.datasetController;

import com.szbldb.pojo.Result;
import com.szbldb.pojo.datasetPojo.*;
import com.szbldb.service.datasetService.DataSetUploadService;
import com.szbldb.util.JWTHelper;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
public class DataSetUploadController {


    private final DataSetUploadService dataSetUploadService;

    public DataSetUploadController(@Autowired DataSetUploadService dataSetUploadService) {
        this.dataSetUploadService = dataSetUploadService;
    }

    @RequestMapping("/PETdatabase/dataset/uploadinfo")
    public Result uploadMeta(@RequestBody DataSet dataSet, @RequestHeader String token) throws Exception{
        System.out.println(dataSet);
        if(dataSet.getUploader() == null){
            String username = JWTHelper.getUsername(token);
            dataSet.setUploader(username);
        }
        if(dataSetUploadService.uploadMeta(dataSet)){
            return Result.success();
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
                //deadlockE.printStackTrace();
            }
        }
    }

    @RequestMapping("/PETdatabase/dataset/uploadLocal")
    public Result uploadLocal(){
        String url = dataSetUploadService.uploadLocal();
        if(url == null)
            return Result.error("Failed to access", 40010);
        return Result.success(url);
        /*System.out.println(file);
        while(true){
            try{
                try{
                    dataSetUploadService.uploadLocal(file);
                    return Result.success();
                }catch (IOException e){
                    e.printStackTrace();
                    return Result.error("Failed to upload, maybe there is the file with the same name.", 40010);
                }
            }catch (PessimisticLockingFailureException deadlockE){
                System.err.println("Caught DeadLock!");
                //deadlockE.printStackTrace();
            }catch (RuntimeException re){
                re.printStackTrace();
                return Result.error("上传出错，请重试", 40010);
            }
        }*/
    }

    @RequestMapping("/PETdatabase/dataset/uploadLocalVerify")
    public Result uploadLocalVerify(@RequestBody FilePart part){
        System.out.println(part);
        try{
            List<Integer> lack = dataSetUploadService.verifyFile(part);
            if(lack == null){
                return Result.success(new UploadLocalRes(0, null));
            }
            else if(lack.isEmpty()){
                return Result.error("MD5校验失败，文件已删除", 40010);
            }
            else if(lack.get(0) == -1){
                return Result.error("exist the file with the same name!", 40011);
            }
            else return Result.success(new UploadLocalRes(1, lack));
        }catch (IOException e){
            e.printStackTrace();
            return Result.error("上传出错，请重试", 40010);
        }
    }
}
