package com.szbldb.controller;

import com.szbldb.service.DataDownloadService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class DataDownloadController {

    @Autowired
    DataDownloadService dataDownloadService;

    @RequestMapping("/PETdatabase/dataset/download")
    public void dataDownload(Integer id, HttpServletResponse response){
        try{
            dataDownloadService.dataDownload(id, response);
        }catch (IOException e){
        }
    }
}
