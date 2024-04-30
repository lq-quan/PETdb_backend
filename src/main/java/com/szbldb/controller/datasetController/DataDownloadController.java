package com.szbldb.controller.datasetController;

import com.szbldb.pojo.Result;
import com.szbldb.service.datasetService.DataDownloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URL;

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
}
