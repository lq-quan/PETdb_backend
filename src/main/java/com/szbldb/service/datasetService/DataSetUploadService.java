package com.szbldb.service.datasetService;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.auth.sts.AssumeRoleRequest;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.szbldb.dao.DataSetMapper;
import com.szbldb.pojo.datasetPojo.*;
import com.szbldb.pojo.datasetPojo.File;
import com.szbldb.service.logService.LogService;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DataSetUploadService {
    private final DataSetMapper dataSetMapper;
    private final LogService logService;

    private final String ipAddress = InetAddress.getLocalHost().getHostAddress();

    public DataSetUploadService(@Autowired DataSetMapper dataSetMapper, @Autowired LogService logService) throws UnknownHostException {
        this.logService = logService;
        this.dataSetMapper = dataSetMapper;
    }

    public StsTokenInfo datasetUpload(){
        String endpoint = "sts.cn-shenzhen.aliyuncs.com";
        String accessKeyId = System.getenv("OSS_ACCESS_KEY_ID");
        String accessKeySecret = System.getenv("OSS_ACCESS_KEY_SECRET");
        String roleArn = System.getenv("OSS_STS_ROLE_ARN");
        String roleSessionName = "putSessionTest";
        String policy = """
                {
                    "Version": "1",
                    "Statement": [
                        {
                            "Effect": "Allow",
                            "Action": "oss:PutObject",
                            "Resource": [
                                "acs:oss:*:*:szbldb-test/*",
                                "acs:oss:*:*:szbldb-test/test/*"
                            ]
                        }
                    ]
                }
                """;
        Long durationSeconds = 900L;
        StsTokenInfo tokenInfo = new StsTokenInfo();
        tokenInfo.setBucket("szbldb-test");
        tokenInfo.setRegion("oss-cn-shenzhen");
        try{
            String regionId = "cn-shenzhen";
            DefaultProfile.addEndpoint(regionId, "Sts", endpoint);
            IClientProfile profile = DefaultProfile.getProfile(regionId, accessKeyId, accessKeySecret);
            DefaultAcsClient client = new DefaultAcsClient(profile);
            final AssumeRoleRequest request = new AssumeRoleRequest();
            request.setSysMethod(MethodType.POST);
            request.setRoleArn(roleArn);
            request.setRoleSessionName(roleSessionName);
            request.setPolicy(policy);
            request.setDurationSeconds(durationSeconds);
            final AssumeRoleResponse response = client.getAcsResponse(request);
            tokenInfo.setAccessKeyId(response.getCredentials().getAccessKeyId());
            tokenInfo.setAccessKeySecret(response.getCredentials().getAccessKeySecret());
            tokenInfo.setSTStoken(response.getCredentials().getSecurityToken());
            return tokenInfo;
        }catch (ClientException e){
            System.out.println("Error Message: " + e.getErrMsg());
            System.out.println("Error code: " + e.getErrCode());
        }
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean uploadMeta(DataSet dataSet) throws Exception{
        if(dataSetMapper.checkDatasetName(dataSet.getName(), dataSet.getType()) != null) return false;
        dataSet.setDate(LocalDate.now());
        dataSet.setSize(0L);
        dataSetMapper.insertDataset(dataSet);
        DataSetLoc dataSetLoc = new DataSetLoc();
        dataSetLoc.setId(dataSet.getId());
        dataSetLoc.setObjectName(dataSet.getType() + '/' + dataSet.getName() + '/');
        if("local".equals(dataSet.getStatus())){
            dataSetLoc.setBucketName("test");
            dataSetMapper.insertLoc(dataSetLoc);
            return true;
        }
        dataSetLoc.setBucketName("szbldb-test");
        dataSetMapper.insertLoc(dataSetLoc);
        //创建OSS目录
        String endpoint = "https://oss-cn-shenzhen.aliyuncs.com";
        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        String bucketName = "szbldb-test";
        String objectName = dataSetLoc.getObjectName();
        System.out.println(dataSetLoc);
        OSS ossClient = new OSSClientBuilder().build(endpoint, credentialsProvider);
        try{
            String content = "";
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, new ByteArrayInputStream(content.getBytes()));
            ossClient.putObject(putObjectRequest);
        }catch (OSSException oe) {
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
            throw oe;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return true;
    }

    public void changeMeta(DataSet dataSet){
        dataSet.setDate(LocalDate.now());
        dataSetMapper.updateDatasetById(dataSet);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean uploadFile(File file) throws PessimisticLockingFailureException {
        if(dataSetMapper.checkFilename(file.getName(), file.getDatasetId()) != null) return false;
        file.setDate(LocalDate.now());
        dataSetMapper.insertFile(file);
        dataSetMapper.updateSize(file.getSize(), file.getDatasetId());
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean checkMd5(List<Integer> uploaded, FilePart part){
        String md5 = part.getFileMd5();
        File origin = dataSetMapper.checkAndGetMd5(md5);
        DataSet toDataset = dataSetMapper.getDatasetById(part.getId());
        if(dataSetMapper.checkFilename(part.getFileName(), part.getId()) != null) return null;
        //尝试寻找分片
        int flag = 0;
        if(origin == null){
            String object = toDataset.getType() + "/" + toDataset.getName() + "/" + part.getFileMd5();
            try(MinioClient client = MinioClient.builder()
                    .endpoint("http://" + ipAddress + ":9000")
                    .credentials("lqquan", "12345678")
                    .build()){
                int i = 0;
                while(true){
                    try{
                        client.statObject(StatObjectArgs.builder().bucket("test").object(object + "." + i).build());
                        uploaded.add(i++);
                    }catch (ErrorResponseException ee){
                        if(flag++ == 2) break;
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return false;
        }
        else{
            DataSet dataSet = dataSetMapper.getDatasetById(origin.getDatasetId());
            String toObject = toDataset.getType() + "/" + toDataset.getName() + "/" + part.getFileName();
            String originFileObject = dataSet.getType() + "/" + dataSet.getName() + "/" + origin.getName();
            try(MinioClient client = MinioClient.builder()
                    .endpoint("http://" + ipAddress + ":9000")
                    .credentials("lqquan", "12345678")
                    .build()){
                client.copyObject(CopyObjectArgs.builder()
                        .bucket("test")
                        .object(toObject)
                        .source(
                                CopySource.builder()
                                        .bucket("test")
                                        .object(originFileObject)
                                        .build()
                        ).build());
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }
        origin.setDatasetId(part.getId());
        uploadFile(origin);
        logService.addLog("成功：向 " + toDataset.getName() + " 上传 " + part.getFileName());
        return true;
    }

    //@Transactional(rollbackFor = Exception.class)
    public List<String> uploadLocal(FilePart part){
        System.out.println(part);
        List<String> list = new ArrayList<>();
        DataSet dataSet = dataSetMapper.getDatasetById(part.getId());
        String object = dataSet.getType() + "/" + dataSet.getName() + "/" + part.getFileMd5();
        try(MinioClient client = MinioClient.builder()
                .endpoint("http://" + ipAddress + ":9000")
                .credentials("lqquan", "12345678")
                .build()){
            long chunkCount = part.getFileSize() / part.getChunkSize() + 1;
            if(part.getFileSize() % part.getChunkSize() == 0) chunkCount--;
            for(int i = 0; i < chunkCount; i++){
                try{
                    client.statObject(StatObjectArgs.builder().bucket("test").object(object + "." + i).build());
                }catch (ErrorResponseException e){
                    String urlI = client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket("test")
                            .object(object + "." + i)
                            .expiry(1, TimeUnit.HOURS)
                            .build());
                    list.add(urlI);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }

    @Async
    @Transactional(rollbackFor = Exception.class)
    public void mergeFile(FilePart part){
        DataSet dataSet = dataSetMapper.getDatasetById(part.getId());
        String object = dataSet.getType() + "/" + dataSet.getName() + "/" + part.getFileMd5();
        String type, md5;
        long size;
        try(MinioClient client = MinioClient.builder()
                .endpoint("http://" + ipAddress + ":9000")
                .credentials("lqquan", "12345678")
                .build()){
            List<ComposeSource> sources = new ArrayList<>();
            int i = 0;
            while(true){
                try{
                    client.statObject(StatObjectArgs.builder().bucket("test").object(object + "." + i).build());
                    sources.add(ComposeSource.builder().bucket("test").object(object + "." + i).build());
                }catch (ErrorResponseException e){
                    break;
                }
                i++;
            }
            client.composeObject(ComposeObjectArgs.builder()
                    .bucket("test")
                    .object(dataSet.getType() + "/" + dataSet.getName() + "/" + part.getFileName())
                    .sources(sources)
                    .build());
            while(--i >= 0){
                client.removeObject(RemoveObjectArgs.builder()
                        .bucket("test")
                        .object(object + "." + i)
                        .build());
            }
            object = dataSet.getType() + "/" + dataSet.getName() + "/" + part.getFileName();
            try(InputStream input = client.getObject(GetObjectArgs.builder().bucket("test").object(object).build())){
                md5 = DigestUtils.md5Hex(input);
                if(!md5.equals(part.getFileMd5())){
                    client.removeObject(RemoveObjectArgs.builder()
                            .bucket("test")
                            .object(object)
                            .build());
                    throw new RuntimeException("Md5对比失败");
                }
            }
            StatObjectResponse args = client.statObject(StatObjectArgs.builder().bucket("test").object(object).build());
            size = args.size();
            type = args.contentType();
        }catch (Exception e){
            logService.addLog("失败：向 " + dataSet.getName() + " 上传 " + part.getFileName());
            e.printStackTrace();
            return;
        }
        File file = new File(part.getId(), size, part.getFileName(), type);
        file.setMd5(md5);
        while(true){
            try{
                uploadFile(file);
                logService.addLog("成功：向 " + dataSet.getName() + " 上传 " + part.getFileName());
                return;
            }catch (PessimisticLockingFailureException pe){
                System.out.println("Caught deadlock!");
            }
        }
    }
}
