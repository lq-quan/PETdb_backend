package com.szbldb.pojo.extensionPojo;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class News {
    private Integer nid;
    private String content;
    private String imageSrc;
    private String link;
    private MultipartFile imageFile;
}
