package com.szbldb.controller.datasetController;

import com.szbldb.pojo.Result;
import com.szbldb.service.datasetService.DataDownloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.net.URL;
import java.util.List;


@RestController
public class DataDownloadController {

    DataDownloadService dataDownloadService;

    public DataDownloadController(@Autowired DataDownloadService dataDownloadService) {
        this.dataDownloadService = dataDownloadService;
    }

    @RequestMapping("/PETdatabase/dataset/download")
    public Result dataDownload(Integer id) throws Exception{
        System.out.println("File id: " + id);
        URL url = dataDownloadService.dataDownload(id);
        if(url == null)
            return Result.error("Failed to get the file", 40009);
        return Result.success(url);
    }

    @RequestMapping("/PETdatabase/dataset/downloadLocal")
    public Result downloadLocal(Integer id) {
        String url = dataDownloadService.downloadLocal(id);
        if(url == null)
            return Result.error("Failed to get the file", 40009);
        return Result.success(url);
    }

    @RequestMapping("/PETdatabase/dataset/downloadZip")
    public Result downloadZip(@RequestParam("files") List<Integer> files){
        System.out.println(files);
        if(files.isEmpty()) return Result.success();
        String url = dataDownloadService.getZipUrl(files);
        if(url == null){
            return Result.success("Creating zip file, please wait");
        }
        else if("null".equals(url)){
            dataDownloadService.createZip(files);
            return Result.success("Creating zip file, please wait");
        }
        else if("false".equals(url)){
            return Result.error("Failed to get the files", 40009);
        }
        else return Result.success(url);
    }
}
