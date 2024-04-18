package com.szbldb.controller.datasetController;

import com.szbldb.pojo.Result;
import com.szbldb.service.datasetService.DataDownloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URL;


@RestController
public class DataDownloadController {

    @Autowired
    DataDownloadService dataDownloadService;

    @RequestMapping("/PETdatabase/dataset/download")
    public Result dataDownload(Integer id) throws Exception{
        System.out.println("File id: " + id);
        URL url = dataDownloadService.dataDownload(id);
        if(url == null)
            return Result.error("Failed to get the file", 50009);
        return Result.success(url);
    }
}
