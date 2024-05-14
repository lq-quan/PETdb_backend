package com.szbldb.service.datasetService;


import com.aliyun.oss.*;
import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.szbldb.dao.DataSetMapper;
import com.szbldb.pojo.datasetPojo.DataSet;
import com.szbldb.pojo.datasetPojo.DataSetLoc;
import com.szbldb.pojo.datasetPojo.File;
import com.szbldb.service.logService.LogService;
import io.minio.*;
import io.minio.http.Method;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;


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
        DataSet dataSet = dataSetMapper.getDatasetByFileId(id);
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

    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<StreamingResponseBody> createZip(List<Integer> fileIDs){
        DataSet dataSet = dataSetMapper.getDatasetByFileId(fileIDs.get(0));
        String md5 = DigestUtils.md5Hex(fileIDs.toString());
        String zipName = md5 + ".zip";
        List<InputStream> streams = new ArrayList<>();
        DataSetLoc loc = dataSetMapper.searchLocByDatasetId(dataSet.getId());
        try(MinioClient client = MinioClient.builder()
                .endpoint("http://" + ipAddress + ":9000")
                .credentials("lqquan", "12345678")
                .build()){
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
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.attachment().filename(zipName).build());
            StreamingResponseBody streamingResponseBody = outputStream -> {
                try (outputStream; ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {
                    int idx = 0;
                    for (InputStream input : streams) {
                        ZipEntry entry = new ZipEntry(names[idx++]);
                        zipOut.putNextEntry(entry);
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = input.read(buffer)) > 0) {
                            zipOut.write(buffer, 0, len);
                        }
                        zipOut.closeEntry();
                        input.close();
                    }
                }
            };
            return ResponseEntity.ok().headers(headers).body(streamingResponseBody);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.noContent().build();
        }
    }
}
