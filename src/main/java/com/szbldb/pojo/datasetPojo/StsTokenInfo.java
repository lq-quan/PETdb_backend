package com.szbldb.pojo.datasetPojo;

import lombok.Data;

@Data
public class StsTokenInfo {
    private String accessKeyId;
    private String accessKeySecret;
    private String STStoken;
    private String bucket;
    private String region;
}
