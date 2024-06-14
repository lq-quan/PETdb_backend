package com.szbldb.controller.datasetController;

import com.szbldb.pojo.Result;
import com.szbldb.service.datasetService.DataDownloadService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.net.URL;
import java.util.List;


@RestController
public class DataDownloadController {

    private final DataDownloadService dataDownloadService;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public DataDownloadController(@Autowired DataDownloadService dataDownloadService) {
        this.dataDownloadService = dataDownloadService;
    }

    @RequestMapping("/PETdatabase/dataset/download")
    public Result dataDownload(Integer id){
        System.out.println("File id: " + id);
        URL url = dataDownloadService.dataDownload(id);
        if(url == null)
            return Result.error("Failed to get the file", 40009);
        return Result.success(url);
    }

    @RequestMapping("/PETdatabase/dataset/downloadLocal")
    public Result downloadLocal(@RequestParam Integer id) {
        if(id == null) return Result.error("Nothing Selected", 40009);
        String url = dataDownloadService.downloadLocal(id);
        if(url == null)
            return Result.error("Failed to get the file", 40009);
        return Result.success(url);
    }

    @RequestMapping("/PETdatabase/dataset/downloadZip")
    public ResponseEntity<StreamingResponseBody> downloadZip(@RequestParam("files") List<Integer> files,
                                                             HttpServletRequest request, HttpServletResponse response){
        System.out.println(files);
        if(files.isEmpty() || files.size() == 1){
            RequestDispatcher dispatcher;
            if(files.size() == 1) dispatcher = request.getRequestDispatcher("/PETdatabase/dataset/downloadLocal?id=" + files.get(0));
            else dispatcher = request.getRequestDispatcher("/PETdatabase/dataset/downloadLocal?id=");
            try {
                dispatcher.forward(request, response);
            } catch (Exception e) {
                log.error("failed to dispatch", e);
            }
        }
        return dataDownloadService.createZip(files);
    }
}
