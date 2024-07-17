package com.szbldb.service.datasetService;

import com.szbldb.dao.DataSetMapper;
import com.szbldb.exception.DataSetException;
import com.szbldb.pojo.datasetPojo.DataSet;
import com.szbldb.pojo.datasetPojo.DataSetList;
import com.szbldb.pojo.datasetPojo.DataSetLoc;
import com.szbldb.pojo.datasetPojo.File;
import com.szbldb.service.logService.LogService;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


@Slf4j
@Service
public class DataSetService {

    private final DataSetMapper dataSetMapper;
    private final LogService logService;


    @Value("${minio.server.address}")
    private String ipAddress;
    private final String bucket = "test";

    public DataSetService(@Autowired DataSetMapper dataSetMapper, @Autowired LogService logService){
        this.logService = logService;
        this.dataSetMapper = dataSetMapper;
    }

    /**
     *
     * @Description 通过数据集信息搜索数据集
     * @param dataSet 数据集信息
     * @param page （第）页数
     * @param limit 每页项数
     * @param sort 排序方式，+id 或 -id
     * @param word 关键词
     * @return com.szbldb.pojo.datasetPojo.DataSetList
     * @author Quan Li 2024/7/12 16:07
     **/
    @Transactional(rollbackFor = Exception.class)
    public DataSetList searchList(DataSet dataSet, Integer page, Integer limit, String sort, String word){
        if(word != null && !word.isEmpty()) return searchAllLike(word);
        if(page == null) page = 1;
        if(limit == null) limit = 20;
        List<DataSet> dataSets;
        if("-id".equals(sort)){
            dataSets = dataSetMapper.searchLikeDesc(dataSet, (page - 1) * limit, limit);
        }
        else{
            dataSets = dataSetMapper.searchLikeAsc(dataSet, (page - 1) * limit, limit);
        }
        for(DataSet ds : dataSets){
            ds.setFiles(dataSetMapper.getFilesByDatasetId(ds.getId()));
        }
        return new DataSetList(dataSetMapper.getDataSetCounts(), dataSets);
    }

    /**
     *
     * @Description 根据关键词搜索数据集列表
     * @param word 关键词
     * @return com.szbldb.pojo.datasetPojo.DataSetList
     * @author Quan Li 2024/7/5 11:00
     **/
    @Transactional(rollbackFor = Exception.class)
    public DataSetList searchAllLike(String word){
        List<DataSet> dataSets = dataSetMapper.searchGlobal(word);
        for(DataSet ds : dataSets){
            ds.setFiles(dataSetMapper.getFilesByDatasetId(ds.getId()));
        }
        return new DataSetList(dataSets.size(), dataSets);
    }

    /**
     *
     * @Description 获取数据集具体内容
     * @param id 数据集 id
     * @return com.szbldb.pojo.datasetPojo.DataSet
     * @author Quan Li 2024/7/5 11:00
     **/
    @Transactional(rollbackFor = Exception.class)
    public DataSet getDetail(Integer id){
        DataSet dataSet = dataSetMapper.getDatasetById(id);
        List<File> files = dataSetMapper.getFilesByDatasetId(dataSet.getId());
        dataSet.setFiles(files);
        return dataSet;
    }

    /**
     *
     * @Description 删除指定文件
     * @param fileId 文件 id
     * @author Quan Li 2024/7/5 11:01
     **/
    @Transactional(rollbackFor = Exception.class)// 设置isolation = Isolation.SERIALIZABLE，可阻止事务并行
    public void deleteFile(Integer fileId) throws DataSetException{
        File deletedFile = dataSetMapper.getFileByFileId(fileId);
        dataSetMapper.deleteFile(fileId);
        Long size = deletedFile.getSize();
        Integer datasetId = deletedFile.getDatasetId();
        dataSetMapper.updateSize(-size, datasetId);
        DataSet dataSet = dataSetMapper.getDatasetById(datasetId);
        String objectName = dataSet.getType() + "/" + dataSet.getName() + "/" + deletedFile.getName();
        try(MinioClient client = MinioClient.builder()
                .endpoint("http://" + ipAddress + ":9000")
                .credentials("lqquan", "12345678")
                .build()){
            client.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
        }catch (Exception e){
            logService.addLog("失败：删除 " + dataSet.getName() + " 中的 " + deletedFile.getName());
            log.error("删除文件失败", e);
            throw new DataSetException("删除文件失败");
        }
        logService.addLog("成功：删除 " + dataSet.getName() + " 中的 " + deletedFile.getName());
        /*String endpoint = "https://oss-cn-shenzhen.aliyuncs.com";
        EnvironmentVariableCredentialsProvider credentialsProvider;
        try {
            credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        } catch (com.aliyuncs.exceptions.ClientException e) {
            log.error("获取用于登录OSS的环境变量失败", e);
            throw new DataSetException("获取用于登录OSS的环境变量失败");
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
        }*/
    }

    /**
     *
     * @Description 删除指定非空数据集
     * @param id 数据集 id
     * @author Quan Li 2024/7/5 11:03
     **/
    @Transactional(rollbackFor = Exception.class)
    public void deleteDataset(Integer id) throws Exception{
        System.out.println("Deleted dataset: " + id);
        DataSet dataSet = dataSetMapper.getDatasetById(id);

        DataSetLoc loc = dataSetMapper.searchLocByDatasetId(id);
        Integer fileNums = dataSetMapper.getFileNums(id);
        if(fileNums > 0){
            logService.addLog("失败：删除数据集 " + dataSet.getName());
            throw new DataSetException("Dataset not empty!");
        }
        dataSetMapper.deleteAllFilesOfDataset(id);
        dataSetMapper.deleteDatasetLoc(id);
        dataSetMapper.deleteDataset(id);
        dataSetMapper.deleteCollDset(id);
        if("local".equals(dataSet.getStatus())){
            try(MinioClient client = MinioClient.builder()
                    .endpoint("http://" + ipAddress + ":9000")
                    .credentials("lqquan", "12345678")
                    .build()){
                client.removeObject(RemoveObjectArgs.builder()
                        .bucket(bucket)
                        .object(loc.getObjectName())
                        .build());
            }catch (Exception e){
                logService.addLog("失败：删除数据集 " + dataSet.getName());
                throw e;
            }
            logService.addLog("成功：删除数据集 " + dataSet.getName());
        }
        /*String endPoint = "https://oss-cn-shenzhen.aliyuncs.com";
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
        }*/
    }

}
