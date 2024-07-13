package com.szbldb.controller.extensionController;

import com.szbldb.exception.ExtensionException;
import com.szbldb.pojo.Result;
import com.szbldb.pojo.extensionPojo.Collection;
import com.szbldb.pojo.extensionPojo.CollectionList;
import com.szbldb.service.extensionService.CollectionService;
import com.szbldb.util.JWTHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class CollectionController {
    private final CollectionService collectionService;

    public CollectionController(@Autowired CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    /**
     *
     * @Description 创建 Collection
     * @param token 用户令牌
     * @param collection 新建 Collection 信息
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/4 15:26
     **/
    @PostMapping("/PETdatabase/extended/collection/create")
    public Result createCollection(@RequestHeader String token, @RequestBody Collection collection){
        String username = JWTHelper.getUsername(token);
        Integer cid;
        try {
            cid = collectionService.createCollection(username, collection);
        } catch (ExtensionException e) {
            return Result.error("数量超出限制或名称重复", 60002);
        }
        return Result.success(cid);
    }

    /**
     *
     * @Description 列出用户创建的 Collection
     * @param token 用户令牌
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/4 15:26
     **/
    @GetMapping("/PETdatabase/extended/collection/list")
    public Result getCollectionList(@RequestHeader(required = false) String token){
        if(token == null){
            return Result.success("not_login");
        }
        String username = JWTHelper.getUsername(token);
        CollectionList list = collectionService.getCollectionList(username);
        return Result.success(list);
    }

    /**
     *
     * @Description 获取 Collection 具体内容
     * @param token 用户令牌
     * @param collectionId Collection id
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/4 15:27
     **/
    @GetMapping("/PETdatabase/extended/collection/detail")
    public Result getCollectionDetail(@RequestHeader String token, Integer collectionId){
        String username = JWTHelper.getUsername(token);
        try {
            Collection collection = collectionService.getCollectionDetail(collectionId, username);
            return Result.success(collection);
        } catch (ExtensionException e) {
            return Result.error("访问Collection失败", 60001);
        }
    }

    /**
     *
     * @Description 删除 Collection
     * @param token 用户令牌
     * @param collectionId Collection id
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/4 15:28
     **/
    @DeleteMapping("/PETdatabase/extended/collection/delete")
    public Result deleteColl(@RequestHeader String token, Integer collectionId){
        String username = JWTHelper.getUsername(token);
        try {
            if(!collectionService.deleteCollection(collectionId, username)){
                return Result.error("无法删除非空Collection", 60001);
            }
        } catch (ExtensionException e) {
            return Result.error("访问Collection失败", 60001);
        }
        return Result.success();
    }

    /**
     *
     * @Description 添加数据集到 Collection
     * @param token 用户令牌
     * @param collectionId Collection id
     * @param datasetId 数据集 id
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/4 15:29
     **/
    @PostMapping("/PETdatabase/extended/collection/add")
    public Result addDatasetToColl(@RequestHeader String token, Integer collectionId, Integer datasetId){
        String username = JWTHelper.getUsername(token);
        try {
            Integer add = collectionService.addDatasetToCollection(datasetId, collectionId, username);
            return switch (add) {
                case 0 -> Result.success();
                case 1 -> Result.error("数据集不存在", 60005);
                case 2 -> Result.error("数据集已添加过", 60005);
                default -> Result.error("Collection已满", 60004);
            };
        } catch (ExtensionException e) {
            return Result.error("访问Collection失败", 60001);
        }
    }

    /**
     *
     * @Description 从 Collection 中移除数据集
     * @param token 用户令牌
     * @param collectionId Collection id
     * @param datasetId 数据集 id
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/4 15:29
     **/
    @DeleteMapping("/PETdatabase/extended/collection/remove")
    public Result deleteDatasetFromColl(@RequestHeader String token, Integer collectionId, Integer datasetId){
        String username = JWTHelper.getUsername(token);
        try {
            collectionService.deleteDatasetFromCollection(datasetId, collectionId, username);
        } catch (ExtensionException e) {
            return Result.error("访问Collection失败", 60001);
        }
        return Result.success();
    }
}
