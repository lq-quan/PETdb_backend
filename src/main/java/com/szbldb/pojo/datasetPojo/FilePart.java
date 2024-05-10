package com.szbldb.pojo.datasetPojo;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FilePart {
    private Integer chunkSize;
    private Integer id; //datasetId
    private String fileName;
    private Long fileSize;
    private String fileMd5;
    private String contentType;

    @Override
    public String toString() {
        return "FilePart{" +
                "chunkSize=" + chunkSize +
                ", id=" + id +
                ", fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", fileMd5='" + fileMd5 + '\'' +
                ", contentType='" + contentType + '\'' +
                '}';
    }
}
