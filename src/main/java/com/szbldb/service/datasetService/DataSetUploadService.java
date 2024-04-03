package com.szbldb.service.datasetService;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.auth.sts.AssumeRoleRequest;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.szbldb.dao.DataSetMapper;
import com.szbldb.pojo.datasetPojo.DataSet;
import com.szbldb.pojo.datasetPojo.StsTokenInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataSetUploadService {
    @Autowired
    private DataSetMapper dataSetMapper;
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
                                "acs:oss:*:*:szbldb-test/test",
                                "acs:oss:*:*:szbldb-test/test/*"
                            ]
                        }
                    ]
                }
                """;
        Long durationSeconds = 3600L;
        StsTokenInfo tokenInfo = new StsTokenInfo();
        tokenInfo.setBucket("szbldb-test");
        tokenInfo.setRegion("cn-oss-shenzhen");
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

    public void uploadMeta(DataSet dataSet){
        dataSetMapper.insertDataset(dataSet);
    }

}
