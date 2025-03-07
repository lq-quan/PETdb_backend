package com.szbldb.service.datasetService;



import com.szbldb.dao.DataSetMapper;
import com.szbldb.exception.DataSetException;
import com.szbldb.pojo.datasetPojo.DataSet;
import com.szbldb.pojo.datasetPojo.DataSetLoc;
import com.szbldb.pojo.datasetPojo.File;
import com.szbldb.service.logService.LogService;
import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;


import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Slf4j
@Service
public class DataDownloadService {


    @Value("${minio.server.address}")
    private String ipAddress;

    @Value("${minio.access-key}")
    private String accessKey;
    @Value("${minio.secret-key}")
    private String secretKey;
    @Value("${minio.bucket}")
    private String bucket;

    private final DataSetMapper dataSetMapper;
    private final LogService logService;

    public DataDownloadService(@Autowired DataSetMapper dataSetMapper, @Autowired LogService logService){
        this.logService = logService;
        this.dataSetMapper = dataSetMapper;
    }


    /*@Transactional(rollbackFor = Exception.class)
    public URL dataDownload(Integer fileId){
        DataSetLoc dataSetLoc = dataSetMapper.searchLocByFileId(fileId);
        if(dataSetLoc == null) return null;
        String endpoint = dataSetLoc.getEndpoint();
        EnvironmentVariableCredentialsProvider credentialsProvider;
        try {
            credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        } catch (com.aliyuncs.exceptions.ClientException e) {
            log.error("获取用于登录OSS的环境变量失败", e);
            return null;
        }
        String bucketName = dataSetLoc.getBucketName();
        String objectName = dataSetLoc.getObjectName() + dataSetMapper.getFileByFileId(fileId).getName();
        OSS ossClient = new OSSClientBuilder().build(endpoint, credentialsProvider);
        URL signedUrl;
        try{
            Date expiration = new Date(new Date().getTime() + 3600 * 1000L);
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectName, HttpMethod.GET);
            request.setExpiration(expiration);
            signedUrl = ossClient.generatePresignedUrl(request);
            return signedUrl;
        }catch (Exception e){
            log.error("获取OSS文件失败", e);
        }
        return null;
    }*/

    /**
     *
     * @Description 返回本地文件下载链接
     * @param id 文件 id
     * @param username 用户名，用于检测权限
     * @return java.lang.String
     * @author Quan Li 2024/7/5 10:56
     **/
    @Transactional(rollbackFor = Exception.class)
    public String downloadLocal(Integer id, String username){
        DataSetLoc loc = dataSetMapper.searchLocByFileId(id);
        String filename = dataSetMapper.getFileByFileId(id).getName();
        DataSet dataSet = dataSetMapper.getDatasetByFileId(id);
        if("private".equals(dataSet.getStatus())){
            if(!"admin".equals(dataSetMapper.checkRole(username))) return null;
        }
        if(loc == null) return null;
        String url = null;
        try(MinioClient client = MinioClient.builder()
                .endpoint("https://" + ipAddress)
                .credentials(accessKey, secretKey)
                .build()){
            Map<String, String> reqParams = new HashMap<>();
            reqParams.put("response-content-type", "application/x-msdownload");
            url = client.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(loc.getObjectName() + filename)
                            .expiry(2, TimeUnit.HOURS)
                            .extraQueryParams(reqParams)
                            .build());
        }catch (Exception e){
            logService.addLog("失败：获取 " + dataSet.getName() + " 中文件 " + filename + " 的下载链接");
            log.error("获取下载链接失败", e);
        }
        dataSetMapper.updateDownloads(dataSet.getId());
        logService.addLog("成功：获取 " + dataSet.getName() + " 中文件 " + filename + " 的下载链接");
        return url;
    }

    /**
     *
     * @Description 返回多个文件压缩包
     * @param fileIDs 文件 id 列表
     * @param username 用户名，用于检测权限
     * @return org.springframework.http.ResponseEntity<org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody>
     * @author Quan Li 2024/7/5 10:57
     **/
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<StreamingResponseBody> createZip(List<Integer> fileIDs, String username){
        DataSet dataSet = dataSetMapper.getDatasetByFileId(fileIDs.get(0));
        String md5 = DigestUtils.md5Hex(fileIDs.toString());
        String zipName = md5 + ".zip";
        List<InputStream> streams = new ArrayList<>();
        DataSetLoc loc = dataSetMapper.searchLocByDatasetId(dataSet.getId());
        try(MinioClient client = MinioClient.builder()
                .endpoint("https://" + ipAddress)
                .credentials(accessKey, secretKey)
                .build()){
            if("private".equals(dataSet.getStatus())){
                if(!"admin".equals(dataSetMapper.checkRole(username))) throw new DataSetException("数据集非公开");
            }
            String[] names = new String[fileIDs.size()];
            int i = 0;
            for(Integer fileId : fileIDs){
                File file = dataSetMapper.getFileByFileId(fileId);
                if(file.getDatasetId() - dataSet.getId() != 0) throw new DataSetException("Files不属于同一数据集");
                String object = loc.getObjectName() + file.getName();
                names[i] = file.getName();
                InputStream stream = client.getObject(GetObjectArgs.builder()
                        .bucket(bucket)
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
            dataSetMapper.updateDownloads(dataSet.getId());
            return ResponseEntity.ok().headers(headers).body(streamingResponseBody);
        }catch (Exception e) {
            log.error("流式返回压缩包文件失败", e);
            for(InputStream stream : streams){
                try{
                    stream.close();
                }catch (IOException ioe){
                    log.warn("failed to close InputStream from Minio");
                }
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
