package com.szbldb.controller.extensionController;

import com.szbldb.pojo.Result;
import com.szbldb.pojo.extensionPojo.News;
import com.szbldb.service.extensionService.NewsService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class NewsController {
    private final NewsService newsService;

    public NewsController(NewsService newsService){
        this.newsService = newsService;
    }

    /**
     *
     * @Description 创建新闻图片
     * @param news 新闻图片内容
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/13 17:23
     **/
    @PostMapping("/PETdatabase/extended/news/admin/uploadImage")
    public Result createNews(News news){
        if(newsService.createNews(news)){
            return Result.success();
        }
        return Result.error("failed to upload", 60006);
    }

    /**
     *
     * @Description 获取新闻图片列表
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/13 17:24
     **/
    @GetMapping("/PETdatabase/extended/news/list")
    public Result getNews(){
        List<News> list = newsService.getNews();
        if(list == null) return Result.error("failed to get news images", 60007);
        return Result.success(list);
    }

    /**
     *
     * @Description 根据 news id 删除对应新闻图片
     * @param id news id
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/13 17:24
     **/
    @DeleteMapping("/PETdatabase/extended/news/admin/deleteImage")
    public Result deleteNews(Integer id){
        if(newsService.deleteNews(id)) return Result.success();
        return Result.error("failed to delete", 60008);
    }
}
