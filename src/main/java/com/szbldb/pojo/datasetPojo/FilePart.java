package com.szbldb.pojo.datasetPojo;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FilePart {
    private MultipartFile file;
    private String uid;
    private Integer chunk;
    private Integer id; //datasetId
    private Integer chunks;
    private String fileName;
    private Long fullSize;
    private String md5;

    @Override
    public String toString() {
        return "FilePart{" +
                "file=" + file +
                ", uid='" + uid + '\'' +
                ", chunk=" + chunk +
                ", id=" + id +
                ", chunks=" + chunks +
                ", fileName='" + fileName + '\'' +
                ", fullSize=" + fullSize +
                ", md5='" + md5 + '\'' +
                '}';
    }
}
