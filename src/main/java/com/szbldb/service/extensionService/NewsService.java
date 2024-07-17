package com.szbldb.service.extensionService;

import com.szbldb.dao.ExtensionMapper;
import com.szbldb.pojo.extensionPojo.News;
import com.szbldb.pojo.extensionPojo.NewsListRes;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class NewsService {
    private final ExtensionMapper extensionMapper;

    @Value("${minio.server.address}")
    private String ipAddress;
    @Autowired
    public NewsService(ExtensionMapper extensionMapper){
        this.extensionMapper = extensionMapper;
    }

    /**
     *
     * @Description 创建新闻图片
     * @param news 新闻图片信息
     * @return boolean
     * @author Quan Li 2024/7/13 17:22
     **/
    @Transactional(rollbackFor = Exception.class)
    public boolean createNews(News news){
        if(extensionMapper.getNewsCount() == 20) return false;
        MultipartFile image = news.getImageFile();
        String imageName = image.getOriginalFilename();
        long size = image.getSize();
        try(MinioClient client = MinioClient.builder()
                .endpoint("http://" + ipAddress + ":9000")
                .credentials("lqquan", "12345678")
                .build()){
            client.putObject(
                    PutObjectArgs.builder().bucket("test").object("news/" + imageName).stream(
                                    image.getInputStream(), size, -1)
                            .contentType(image.getContentType())
                            .build());
        }catch (Exception e){
            log.error("创建新闻图片失败");
            return false;
        }
        news.setImageSrc(imageName);
        extensionMapper.createNews(news);
        return true;
    }

    /**
     *
     * @Description 获取新闻图片列表
     * @param page （第）页数
     * @param limit 每页项数
     * @return com.szbldb.pojo.extensionPojo.NewsListRes
     * @author Quan Li 2024/7/15 15:41
     **/
    @Transactional
    public NewsListRes getNews(Integer page, Integer limit){
        List<News> list = extensionMapper.getNews((page - 1) * limit, limit);
        try(MinioClient client = MinioClient.builder()
                .endpoint("http://" + ipAddress + ":9000")
                .credentials("lqquan", "12345678")
                .build()) {
            for(News news : list){
                String imageSrc =client.getPresignedObjectUrl(
                        GetPresignedObjectUrlArgs.builder()
                                .method(Method.GET)
                                .bucket("test")
                                .object("news/" + news.getImageSrc())
                                .expiry(24, TimeUnit.HOURS)
                                .build());
                news.setImageSrc(imageSrc);
            }
        }catch (Exception e){
            log.error("获取新闻图片失败");
            return null;
        }
        NewsListRes res = new NewsListRes();
        res.setNews(list);
        res.setTotal(extensionMapper.getNewsCount());
        return res;
    }

    /**
     *
     * @Description 根据 news id 删除新闻图片
     * @param nid news id
     * @return boolean
     * @author Quan Li 2024/7/13 17:23
     **/
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteNews(Integer nid){
        String imageName = extensionMapper.getNewsSrcByNid(nid);
        try(MinioClient client = MinioClient.builder()
                .endpoint("http://" + ipAddress + ":9000")
                .credentials("lqquan", "12345678")
                .build()) {
            client.removeObject(
                    RemoveObjectArgs.builder().bucket("test").object("news/" + imageName).build());
        }catch (Exception e){
            log.error("删除新闻图片失败");
            return false;
        }
        extensionMapper.deleteNews(nid);
        return true;
    }
}
