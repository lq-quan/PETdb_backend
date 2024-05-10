package com.szbldb.service.datasetService;


import com.aliyun.oss.*;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.szbldb.dao.DataSetMapper;
import com.szbldb.pojo.datasetPojo.DataSet;
import com.szbldb.pojo.datasetPojo.DataSetLoc;
import com.szbldb.pojo.datasetPojo.File;
import com.szbldb.service.logService.LogService;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Service
public class DataDownloadService {

    private final String ipAddress = InetAddress.getLocalHost().getHostAddress();

    private final DataSetMapper dataSetMapper;
    private final LogService logService;

    public DataDownloadService(@Autowired DataSetMapper dataSetMapper, @Autowired LogService logService) throws UnknownHostException {
        this.logService = logService;
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
        DataSet dataSet = dataSetMapper.getDatasetById(id);
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
            logService.addLog("失败：获取 " + dataSet.getName() + " 中文件 " + filename + " 的下载链接");
            e.printStackTrace();
        }
        logService.addLog("成功：获取 " + dataSet.getName() + " 中文件 " + filename + " 的下载链接");
        return url;
    }

    public String downloadLocalFiles(List<Integer> fileIDs){
        if(fileIDs.isEmpty()) return null;
        else if(fileIDs.size() == 1) return downloadLocal(fileIDs.get(0));
        String zipName = DigestUtils.md5Hex(fileIDs.toString()) + ".zip";
        List<InputStream> streams = new ArrayList<>();
        DataSet dataSet = dataSetMapper.getDatasetByFileId(fileIDs.get(0));
        DataSetLoc loc = dataSetMapper.searchLocByDatasetId(dataSet.getId());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String url;
        try(MinioClient client = MinioClient.builder()
                .endpoint("http://" + ipAddress + ":9000")
                .credentials("lqquan", "12345678")
                .build(); ZipOutputStream zipOut = new ZipOutputStream(baos)){
            Map<String, String> reqParams = new HashMap<>();
            reqParams.put("response-content-type", "application/x-msdownload");
            try{
                client.statObject(StatObjectArgs.builder()
                        .bucket("test")
                        .object("zip/" + zipName)
                        .build());
                url = client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket("test")
                        .object("zip/" + zipName)
                        .expiry(2, TimeUnit.HOURS)
                        .extraQueryParams(reqParams)
                        .build());
                logService.addLog("成功：获取 " + dataSet.getName() + " 中已存在zip文件的下载链接");
                return url;
            }catch (ErrorResponseException ere){
                logService.addLog("消息：即将创建新的zip文件，请及时清理");
            }
            String[] names = new String[fileIDs.size()];
            int i = 0;
            for(Integer fileId : fileIDs){
                File file = dataSetMapper.getFileByFileId(fileId);
                String object = loc.getObjectName() + file.getName();
                names[i] = file.getName();
                InputStream stream = client.getObject(GetObjectArgs.builder()
                        .bucket("test")
                        .object(object).build());
                streams.add(stream);
                i++;
            }
            for(i = 0; i < streams.size(); i++){
                zipOut.putNextEntry(new ZipEntry(names[i]));
                copyStream(streams.get(i), zipOut);
                zipOut.closeEntry();
            }
            InputStream zip = new ByteArrayInputStream(baos.toByteArray());
            client.putObject(PutObjectArgs.builder()
                    .bucket("test")
                    .object("zip/" + zipName)
                    .stream(zip, -1, 10485760)
                    .contentType("application/zip")
                    .build());
            url = client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket("test")
                    .object("zip/" + zipName)
                    .expiry(2, TimeUnit.HOURS)
                    .extraQueryParams(reqParams)
                    .build());
            logService.addLog("成功：获取 " + dataSet.getName() + " 中部分文件的下载链接");
            return url;
        }catch (Exception e){
            e.printStackTrace();
            logService.addLog("失败：下载 " + dataSet.getName() + " 中部分文件");
            return null;
        }
    }

    private void copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }
}
