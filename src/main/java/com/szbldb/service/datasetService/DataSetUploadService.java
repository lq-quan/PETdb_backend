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
import com.szbldb.pojo.datasetPojo.DataSet;
import com.szbldb.pojo.datasetPojo.DataSetLoc;
import com.szbldb.pojo.datasetPojo.File;
import com.szbldb.pojo.datasetPojo.StsTokenInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;

@Service
public class DataSetUploadService {
    private final DataSetMapper dataSetMapper;

    public DataSetUploadService(@Autowired DataSetMapper dataSetMapper) {
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
        dataSetLoc.setBucketName("szbldb-test");
        dataSetLoc.setObjectName(dataSet.getType() + '/' + dataSet.getName() + '/');
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
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
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

}
