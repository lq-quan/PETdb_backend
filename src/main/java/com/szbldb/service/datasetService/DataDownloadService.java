package com.szbldb.service.datasetService;


import com.aliyun.oss.*;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.szbldb.dao.DataSetMapper;
import com.szbldb.pojo.datasetPojo.DataSetLoc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.util.Date;


@Service
public class DataDownloadService {

    @Autowired
    private DataSetMapper dataSetMapper;

    @Transactional(rollbackFor = Exception.class)
    public URL dataDownload(Integer fileId) throws Exception{
        DataSetLoc dataSetLoc = dataSetMapper.searchLocByFileId(fileId);
        if(dataSetLoc == null) return null;
        String endpoint = dataSetLoc.getEndpoint();
        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        String bucketName = dataSetLoc.getBucketName();
        String objectName = dataSetLoc.getObjectName() + dataSetMapper.getFileByFileId(fileId).getName();
        //System.out.println(objectName);
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
