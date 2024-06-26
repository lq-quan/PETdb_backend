package com.szbldb.controller.extensionController;

import com.szbldb.exception.ExtensionException;
import com.szbldb.pojo.Result;
import com.szbldb.pojo.extensionPojo.Collection;
import com.szbldb.pojo.extensionPojo.CollectionList;
import com.szbldb.service.extensionService.CollectionService;
import com.szbldb.util.JWTHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CollectionController {
    private final CollectionService collectionService;

    public CollectionController(@Autowired CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @RequestMapping("/PETdatabase/extended/collection/create")
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

    @RequestMapping("/PETdatabase/extended/collection/list")
    public Result getCollectionList(@RequestHeader String token){
        String username = JWTHelper.getUsername(token);
        CollectionList list = collectionService.getCollectionList(username);
        return Result.success(list);
    }

    @RequestMapping("/PETdatabase/extended/collection/detail")
    public Result getCollectionDetail(@RequestHeader String token, Integer collectionId){
        String username = JWTHelper.getUsername(token);
        try {
            Collection collection = collectionService.getCollectionDetail(collectionId, username);
            return Result.success(collection);
        } catch (ExtensionException e) {
            return Result.error("访问Collection失败", 60001);
        }
    }

    @RequestMapping("/PETdatabase/extended/collection/delete")
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

    @RequestMapping("/PETdatabase/extended/collection/add")
    public Result addDatasetToColl(@RequestHeader String token, Integer collectionId, Integer datasetId){
        String username = JWTHelper.getUsername(token);
        try {
            collectionService.addDatasetToCollection(datasetId, collectionId, username);
        } catch (ExtensionException e) {
            return Result.error("访问Collection失败", 60001);
        }
        return Result.success();
    }

    @RequestMapping("/PETdatabase/extended/collection/remove")
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
