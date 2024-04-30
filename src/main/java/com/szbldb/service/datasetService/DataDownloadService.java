package com.szbldb.service.datasetService;


import com.aliyun.oss.*;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.szbldb.dao.DataSetMapper;
import com.szbldb.pojo.datasetPojo.DataSetLoc;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Service
public class DataDownloadService {

    private final String ipAddress = InetAddress.getLocalHost().getHostAddress();

    private final DataSetMapper dataSetMapper;

    public DataDownloadService(@Autowired DataSetMapper dataSetMapper) throws UnknownHostException {
        this.dataSetMapper = dataSetMapper;
    }

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

    public String downloadLocal(Integer id){
        DataSetLoc loc = dataSetMapper.searchLocByFileId(id);
        String filename = dataSetMapper.getFileByFileId(id).getName();
        if(loc == null) return null;
        String url = null;
        try(MinioClient client = MinioClient.builder()
                .endpoint("http://" + ipAddress + ":9000")
                .credentials("lqquan", "12345678")
                .build()){
            Map<String, String> reqParams = new HashMap<>();
            reqParams.put("response-content-type", "application/x-msdownload");
            url = client.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(loc.getBucketName())
                            .object(loc.getObjectName() + filename)
                            .expiry(2, TimeUnit.HOURS)
                            .extraQueryParams(reqParams)
                            .build());
        }catch (Exception e){
            e.printStackTrace();
        }
        return url;
    }
}
