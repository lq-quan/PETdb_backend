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
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DataSetUploadService {
    private final DataSetMapper dataSetMapper;

    private final String ipAddress = InetAddress.getLocalHost().getHostAddress();

    public DataSetUploadService(@Autowired DataSetMapper dataSetMapper) throws UnknownHostException {
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
    public String uploadLocal(){
        String url = null;
        try(MinioClient client = MinioClient.builder()
                .endpoint("http://" + ipAddress + ":9000")
                .credentials("lqquan", "12345678")
                .build()){
            url = client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket("test")
                    .object("listmode数据/本地测试目录/StorageExplorer-windows-x64.exe")
                    .expiry(2, TimeUnit.HOURS)
                    .build());
        }catch (Exception e){
            e.printStackTrace();
        }
        return url;
        /*Integer datasetId = part.getId();
        DataSet dataSet = dataSetMapper.getDatasetById(datasetId);
        if(dataSetMapper.checkFilename(part.getFileName(), datasetId) != null) {
            throw new IOException("文件上传失败！");
        }
        if(part.getChunk() == -1){
            MultipartFile file = part.getFile();
            String name = file.getOriginalFilename();
            Long size = file.getSize();
            String type = file.getContentType();
            File f = new File(datasetId, size, name, type);
            f.setDate(LocalDate.now());
            dataSetMapper.insertFile(f);
            dataSetMapper.updateSize(size, datasetId);
            String path = "D:/PETDatabase/" + dataSet.getType() + '/' + dataSet.getName() + '/' + name;
            file.transferTo(new java.io.File(path));
        }
        else{
            String dirPath = "D:/PETDatabase/" + dataSet.getType() + '/' + dataSet.getName() + '/' + part.getFileName() + "folder";
            if(part.getChunk() == 0){
                java.io.File d = new java.io.File(dirPath);
                if(!d.mkdir()) throw new RuntimeException();
                java.io.File f = new java.io.File(dirPath + '/' + part.getChunk());
                part.getFile().transferTo(f);
            }
            else{
                java.io.File f = new java.io.File(dirPath + '/' + part.getChunk());
                if(f.exists()) throw new RuntimeException("分片已存在！");
                part.getFile().transferTo(f);
            }
        }*/
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public List<Integer> verifyFile(FilePart part) throws IOException{
        Integer datasetId = part.getId();
        DataSet dataSet = dataSetMapper.getDatasetById(datasetId);
        if(dataSetMapper.checkFilename(part.getFileName(), datasetId) != null) {
            return List.of(-1);
        }
        String dirPath = "D:/PETDatabase/" + dataSet.getType() + '/' + dataSet.getName();
        java.io.File finalF = new java.io.File("D:/PETDatabase/" + dataSet.getType()
                + '/' + dataSet.getName() + '/' + part.getFileName());
        List<Integer> lack = new ArrayList<>();
        int count = part.getChunks();
        for(int i = 0; i < count; i++){
            java.io.File patch = new java.io.File(dirPath + '/' + part.getFileName() + "folder" + '/' + i);
            if(!patch.exists()){
                lack.add(i);
            }
        }
        if(lack.isEmpty()){
            //将每一个文件块的内容都追加到新文件中
            try(FileOutputStream fos = new FileOutputStream(finalF)){
                FileChannel out = fos.getChannel();
                for(int i = 0; i < count; i++){
                    java.io.File patch = new java.io.File(dirPath + '/' + part.getFileName() + "folder" + '/' + i);
                    try (FileInputStream in = new FileInputStream(patch)) {
                        FileChannel inc = in.getChannel();
                        inc.transferTo(0, inc.size(), out);
                    }
                    //追加完成后，删除文件块
                    Files.delete(patch.toPath());
                }

            }
            Files.delete(new java.io.File(dirPath + '/' + part.getFileName() + "folder").toPath());
        }
        else return lack;
        String md5 = DigestUtils.md5Hex(new FileInputStream(finalF));
        System.out.println(md5);
        if(!md5.equals(part.getMd5())){
            Files.delete(finalF.toPath());
            return lack;
        }
        File newF = new File(datasetId, finalF.length(), part.getFileName(), Files.probeContentType(finalF.toPath()));
        newF.setDate(LocalDate.now());
        dataSetMapper.insertFile(newF);
        dataSetMapper.updateSize(finalF.length(), datasetId);
        return null;
    }

}
