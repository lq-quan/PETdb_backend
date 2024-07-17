package com.szbldb.service.datasetService;

import com.szbldb.dao.DataSetMapper;
import com.szbldb.exception.DataSetException;
import com.szbldb.pojo.datasetPojo.*;
import com.szbldb.pojo.datasetPojo.File;
import com.szbldb.service.logService.LogService;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class DataSetUploadService {
    private final DataSetMapper dataSetMapper;
    private final LogService logService;

    @Value("${minio.server.address}")
    private String ipAddress;
    private final String bucket = "test";

    public DataSetUploadService(@Autowired DataSetMapper dataSetMapper, @Autowired LogService logService) throws UnknownHostException {
        this.logService = logService;
        this.dataSetMapper = dataSetMapper;
    }

    /**
     * 
     * @Description 无参方法，获取STSToken，上传文件到 OSS
     * @return com.szbldb.pojo.datasetPojo.StsTokenInfo
     * @author Quan Li 2024/7/5 11:09
     **/
    /*public StsTokenInfo datasetUpload(){
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
            log.error("failed to access OSS", e);
        }
        return null;
    }*/

    /**
     * 
     * @Description 创建新的空数据集
     * @param dataSet 新建数据集信息
     * @return boolean
     * @author Quan Li 2024/7/5 11:11
     **/
    @Transactional(rollbackFor = Exception.class)
    public boolean uploadMeta(DataSet dataSet){
        if(dataSetMapper.checkDatasetName(dataSet.getName(), dataSet.getType()) != null) return false;
        dataSet.setDate(LocalDate.now());
        dataSet.setSize(0L);
        dataSetMapper.insertDataset(dataSet);
        DataSetLoc dataSetLoc = new DataSetLoc();
        dataSetLoc.setId(dataSet.getId());
        dataSetLoc.setObjectName(dataSet.getType() + '/' + dataSet.getName() + '/');
        dataSetLoc.setBucketName(bucket);
        dataSetMapper.insertLoc(dataSetLoc);
        logService.addLog("成功：创建数据集 " + dataSet.getName());
        return true;
        /*dataSetLoc.setBucketName("szbldb-test");
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
            log.error("访问OSS失败", oe);
            throw new DataSetException("访问OSS失败");
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return true;*/
    }

    /**
     * 
     * @Description 修改数据集信息
     * @param dataSet 新数据集信息
     * @author Quan Li 2024/7/5 11:14
     **/
    public void changeMeta(DataSet dataSet){
        dataSet.setDate(LocalDate.now());
        dataSetMapper.updateDatasetById(dataSet);
    }

    /**
     *
     * @Description 上传文件元数据
     * @param file 文件元数据信息
     * @return boolean
     * @author Quan Li 2024/7/5 11:14
     **/
    @Transactional(rollbackFor = Exception.class)
    public boolean uploadFile(File file) throws PessimisticLockingFailureException {
        if(dataSetMapper.checkFilename(file.getName(), file.getDatasetId()) != null) return false;
        file.setDate(LocalDate.now());
        dataSetMapper.insertFile(file);
        dataSetMapper.updateSize(file.getSize(), file.getDatasetId());
        return true;
    }

    /**
     *
     * @Description 检查上传文件的信息，返回值：false-文件未上传过但可以上传；true-文件已上传过并且 copy 成功；null-文件上传出错，无法上传
     * @param uploaded 用于存放已上传的文件分片列表
     * @param part 文件信息，文件名/MD5等
     * @return java.lang.Boolean
     * @author Quan Li 2024/7/5 11:16
     **/
    @Transactional(rollbackFor = Exception.class)
    public Boolean checkMd5(List<Integer> uploaded, FilePart part){
        String md5 = part.getFileMd5();
        File origin = dataSetMapper.checkAndGetMd5(md5);
        DataSet toDataset = dataSetMapper.getDatasetById(part.getId());
        if(dataSetMapper.checkFilename(part.getFileName(), part.getId()) != null) return null;
        //尝试寻找分片
        if(origin == null){
            String object = toDataset.getType() + "/" + toDataset.getName() + "/" + part.getFileMd5();
            try(MinioClient client = MinioClient.builder()
                    .endpoint("http://" + ipAddress + ":9000")
                    .credentials("lqquan", "12345678")
                    .build()){
                int i = 1;
                while(true){
                    try{
                        client.statObject(StatObjectArgs.builder().bucket(bucket).object(object + "." + i).build());
                        uploaded.add(i++);
                    }catch (ErrorResponseException ee){
                        break;
                    }
                }
            }catch (Exception e){
                log.error("上传文件失败", e);
            }
            return false;
        }
        else{//文件已在其他数据集存在，直接Copy
            DataSet dataSet = dataSetMapper.getDatasetById(origin.getDatasetId());
            String toObject = toDataset.getType() + "/" + toDataset.getName() + "/" + part.getFileName();
            String originFileObject = dataSet.getType() + "/" + dataSet.getName() + "/" + origin.getName();
            try(MinioClient client = MinioClient.builder()
                    .endpoint("http://" + ipAddress + ":9000")
                    .credentials("lqquan", "12345678")
                    .build()){
                client.copyObject(CopyObjectArgs.builder()
                        .bucket(bucket)
                        .object(toObject)
                        .source(
                                CopySource.builder()
                                        .bucket(bucket)
                                        .object(originFileObject)
                                        .build()
                        ).build());
            }catch (Exception e){
                log.error("上传文件失败", e);
                return null;
            }
        }
        origin.setDatasetId(part.getId());
        if(!uploadFile(origin)){
            return null;
        }
        logService.addLog("成功：向 " + toDataset.getName() + " 上传 " + part.getFileName());
        return true;
    }

    /**
     *
     * @Description 上传文件分片，返回上传链接列表
     * @param part 文件以及分片信息
     * @return java.util.List<java.lang.String>
     * @author Quan Li 2024/7/5 11:20
     **/
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
            for(int i = 1; i <= chunkCount; i++){
                try{
                    client.statObject(StatObjectArgs.builder().bucket(bucket).object(object + "." + i).build());
                }catch (ErrorResponseException e){
                    String urlI = client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucket)
                            .object(object + "." + i)
                            .expiry(1, TimeUnit.HOURS)
                            .build());
                    list.add(urlI);
                }
            }
        }catch (Exception e){
            log.error("获取上传文件链接失败", e);
        }
        return list;
    }

    /**
     *
     * @Description 合并文件
     * @param part 需要合并文件的信息，文件名/MD5等
     * @author Quan Li 2024/7/5 11:26
     **/
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
            int i = 1;
            while(true){
                try{
                    client.statObject(StatObjectArgs.builder().bucket(bucket).object(object + "." + i).build());
                    sources.add(ComposeSource.builder().bucket(bucket).object(object + "." + i).build());
                }catch (ErrorResponseException e){
                    break;
                }
                i++;
            }
            client.composeObject(ComposeObjectArgs.builder()
                    .bucket(bucket)
                    .object(dataSet.getType() + "/" + dataSet.getName() + "/" + part.getFileName())
                    .sources(sources)
                    .build());
            while(--i > 0){
                client.removeObject(RemoveObjectArgs.builder()
                        .bucket(bucket)
                        .object(object + "." + i)
                        .build());
            }
            object = dataSet.getType() + "/" + dataSet.getName() + "/" + part.getFileName();
            try(InputStream input = client.getObject(GetObjectArgs.builder().bucket(bucket).object(object).build())){
                md5 = DigestUtils.md5Hex(input);
                if(!md5.equals(part.getFileMd5())){
                    client.removeObject(RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(object)
                            .build());
                    throw new DataSetException("Md5对比失败");
                }
            }
            StatObjectResponse args = client.statObject(StatObjectArgs.builder().bucket(bucket).object(object).build());
            size = args.size();
            type = args.contentType();
        }catch (Exception e){
            logService.addLog("失败：向 " + dataSet.getName() + " 上传 " + part.getFileName());
            log.error("合并文件失败", e);
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
                log.info("检测到死锁，将尝试重新进行", pe);
            }
        }
    }
}
