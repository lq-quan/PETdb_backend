package com.szbldb.service;


import com.aliyun.oss.*;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Date;


@Service
public class DataDownloadService {
    public URL dataDownload(Integer id) throws Exception{
        String endpoint = "https://oss-cn-shenzhen.aliyuncs.com";
        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        String bucketName = "szbldb-test";
        String objectName = "test/体验商务英语综合教程.pdf";
        OSS ossClient = new OSSClientBuilder().build(endpoint, credentialsProvider);
        URL signedUrl;
        try{
            Date expiration = new Date(new Date().getTime() + 3600 * 1000L);
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectName, HttpMethod.GET);
            request.setExpiration(expiration);
            signedUrl = ossClient.generatePresignedUrl(request);
            return signedUrl;
        }catch (OSSException oe){
            System.out.println("Caught an OSSException");
        }catch (ClientException ce){
            System.out.println("Caught a ClientException");
        }
        return null;
    }
}
