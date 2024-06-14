package com.szbldb.service.datasetService;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.szbldb.dao.DataSetMapper;
import com.szbldb.pojo.datasetPojo.DataSet;
import com.szbldb.pojo.datasetPojo.DataSetList;
import com.szbldb.pojo.datasetPojo.DataSetLoc;
import com.szbldb.pojo.datasetPojo.File;
import com.szbldb.service.logService.LogService;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.aliyun.oss.common.auth.*;
import com.aliyun.oss.model.*;

import java.net.InetAddress;
import java.net.URLDecoder;

import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


@Service
public class DataSetService {

    private final DataSetMapper dataSetMapper;
    private final LogService logService;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final String ipAddress = InetAddress.getLocalHost().getHostAddress();

    public DataSetService(@Autowired DataSetMapper dataSetMapper, @Autowired LogService logService) throws UnknownHostException {
        this.logService = logService;
        this.dataSetMapper = dataSetMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public DataSetList searchList(DataSet dataSet){
        List<DataSet> dataSets = dataSetMapper.searchLike(dataSet);
        for(DataSet ds : dataSets){
            ds.setFiles(dataSetMapper.getFilesByDatasetId(ds.getId()));
        }
        return new DataSetList(dataSets.size(), dataSets);
    }

    @Transactional(rollbackFor = Exception.class)
    public DataSetList searchAllLike(String word){
        List<DataSet> dataSets = dataSetMapper.searchGlobal(word);
        for(DataSet ds : dataSets){
            ds.setFiles(dataSetMapper.getFilesByDatasetId(ds.getId()));
        }
        return new DataSetList(dataSets.size(), dataSets);
    }

    @Transactional(rollbackFor = Exception.class)
    public DataSet getDetail(Integer id){
        DataSet dataSet = dataSetMapper.getDatasetById(id);
        List<File> files = dataSetMapper.getFilesByDatasetId(dataSet.getId());
        dataSet.setFiles(files);
        return dataSet;
    }

    @Transactional(rollbackFor = Exception.class)// 设置isolation = Isolation.SERIALIZABLE，可阻止事务并行
    public void deleteFile(Integer fileId) throws Exception{
        File deletedFile = dataSetMapper.getFileByFileId(fileId);
        dataSetMapper.deleteFile(fileId);
        Long size = deletedFile.getSize();
        Integer datasetId = deletedFile.getDatasetId();
        dataSetMapper.updateSize(-size, datasetId);
        DataSet dataSet = dataSetMapper.getDatasetById(datasetId);
        String objectName = dataSet.getType() + "/" + dataSet.getName() + "/" + deletedFile.getName();
        if("local".equals(dataSet.getStatus())){
            try(MinioClient client = MinioClient.builder()
                    .endpoint("http://" + ipAddress + ":9000")
                    .credentials("lqquan", "12345678")
                    .build()){
                client.removeObject(RemoveObjectArgs.builder()
                        .bucket("test")
                        .object(objectName)
                        .build());
            }catch (Exception e){
                logService.addLog("失败：删除 " + dataSet.getName() + " 中的 " + deletedFile.getName());
                log.error("删除文件失败", e);
            }
            logService.addLog("成功：删除 " + dataSet.getName() + " 中的 " + deletedFile.getName());
            return;
        }
        String endpoint = "https://oss-cn-shenzhen.aliyuncs.com";
        EnvironmentVariableCredentialsProvider credentialsProvider;
        try {
            credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        } catch (com.aliyuncs.exceptions.ClientException e) {
            log.error("获取用于登录OSS的环境变量失败", e);
            throw e;
        }
        String bucketName = "szbldb-test";
        OSS ossClient = new OSSClientBuilder().build(endpoint, credentialsProvider);
        try {
            ossClient.deleteObject(bucketName, objectName);
        } catch (OSSException oe) {
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
            throw oe;
        } catch (ClientException ce) {
            System.out.println("Error Message:" + ce.getMessage());
            throw ce;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteDataset(Integer id) throws Exception{
        System.out.println("Deleted dataset: " + id);
        DataSet dataSet = dataSetMapper.getDatasetById(id);

        DataSetLoc loc = dataSetMapper.searchLocByDatasetId(id);
        Integer fileNums = dataSetMapper.getFileNums(id);
        if(fileNums > 0){
            logService.addLog("失败：删除数据集 " + dataSet.getName());
            throw new RuntimeException("Dataset not empty!");
        }
        dataSetMapper.deleteAllFilesOfDataset(id);
        dataSetMapper.deleteDatasetLoc(id);
        dataSetMapper.deleteDataset(id);
        if("local".equals(dataSet.getStatus())){
            try(MinioClient client = MinioClient.builder()
                    .endpoint("http://" + ipAddress + ":9000")
                    .credentials("lqquan", "12345678")
                    .build()){
                client.removeObject(RemoveObjectArgs.builder()
                        .bucket("test")
                        .object(loc.getObjectName())
                        .build());
            }catch (Exception e){
                logService.addLog("失败：删除数据集 " + dataSet.getName());
                throw e;
            }
            logService.addLog("成功：删除数据集 " + dataSet.getName());
            return;
        }

        String endPoint = "https://oss-cn-shenzhen.aliyuncs.com";
        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        String bucketName = "szbldb-test";
        // 待删除目录的完整路径，完整路径中不包含Bucket名称。
        final String prefix = loc.getObjectName();

        // 创建OSSClient实例
        OSS ossClient = new OSSClientBuilder().build(endPoint, credentialsProvider);

        try {
            // 删除目录及目录下的所有文件。
            String nextMarker = null;
            ObjectListing objectListing;
            do {
                ListObjectsRequest listObjectsRequest = new ListObjectsRequest(bucketName)
                        .withPrefix(prefix)
                        .withMarker(nextMarker);

                objectListing = ossClient.listObjects(listObjectsRequest);
                if (!objectListing.getObjectSummaries().isEmpty()) {
                    List<String> keys = new ArrayList<>();
                    for (OSSObjectSummary s : objectListing.getObjectSummaries()) {
                        System.out.println("key name: " + s.getKey());
                        keys.add(s.getKey());
                    }
                    DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucketName).withKeys(keys).withEncodingType("url");
                    DeleteObjectsResult deleteObjectsResult = ossClient.deleteObjects(deleteObjectsRequest);
                    List<String> deletedObjects = deleteObjectsResult.getDeletedObjects();
                    for(String obj : deletedObjects) {
                        String deleteObj =  URLDecoder.decode(obj, StandardCharsets.UTF_8);
                        System.out.println(deleteObj);
                    }
                }

                nextMarker = objectListing.getNextMarker();
            } while (objectListing.isTruncated());
        } catch (OSSException oe) {
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
            throw oe;
        } catch (ClientException ce) {
            System.out.println("Error Message:" + ce.getMessage());
            throw ce;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

}
