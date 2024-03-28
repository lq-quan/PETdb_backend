package com.szbldb.service;


import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.OutputStream;

@Service
public class DataDownloadService {
    public void dataDownload(Integer id, HttpServletResponse response) throws IOException {
        File file = ResourceUtils.getFile("classpath:static/ziptest.zip");
        String filename = file.getName();
        try(FileInputStream inputStream = new FileInputStream(file);
            OutputStream outputStream = response.getOutputStream()){
            byte[] data = new byte[1024];
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            response.setHeader("Accept-Ranges", "bytes");
            int read;
            while((read = inputStream.read(data)) != -1){
                outputStream.write(data, 0, read);
            }
            outputStream.flush();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
