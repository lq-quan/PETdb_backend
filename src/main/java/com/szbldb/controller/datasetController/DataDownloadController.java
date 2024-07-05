package com.szbldb.controller.datasetController;

import com.szbldb.pojo.Result;
import com.szbldb.service.datasetService.DataDownloadService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.net.URL;
import java.util.List;

@Slf4j
@RestController
public class DataDownloadController {

    private final DataDownloadService dataDownloadService;

    /**
     *
     * @param dataDownloadService 数据下载服务组件
     * @author Quan Li 2024/7/3 17:55
     **/
    public DataDownloadController(@Autowired DataDownloadService dataDownloadService) {
        this.dataDownloadService = dataDownloadService;
    }

    /**
     * 
     * @param id 文件id
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/3 17:56
     **/
    @RequestMapping("/PETdatabase/dataset/download")
    public Result dataDownload(Integer id){
        System.out.println("File id: " + id);
        URL url = dataDownloadService.dataDownload(id);
        if(url == null)
            return Result.error("Failed to get the file", 40009);
        return Result.success(url);
    }

    /**
     * 
     *
     * @param id 文件id
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/3 17:59
     **/
    @RequestMapping("/PETdatabase/dataset/downloadLocal")
    public Result downloadLocal(@RequestParam Integer id) {
        if(id == null) return Result.error("Nothing Selected", 40009);
        String url = dataDownloadService.downloadLocal(id);
        if(url == null)
            return Result.error("Failed to get the file", 40009);
        return Result.success(url);
    }

    /**
     * 
     *
     * @param files 文件 id 列表（需要属于同一数据集）
     * @param request 请求体，用于转发
     * @param response 响应体，用于写入数据
     * @return org.springframework.http.ResponseEntity<org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody>
     * @author Quan Li 2024/7/3 18:00
     **/
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
