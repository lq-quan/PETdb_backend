package com.szbldb.controller.datasetController;

import com.szbldb.exception.DataSetException;
import com.szbldb.pojo.Result;
import com.szbldb.pojo.datasetPojo.DataSet;
import com.szbldb.pojo.datasetPojo.DataSetList;
import com.szbldb.service.datasetService.DataSetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class DataSetController {

    private final DataSetService dataSetService;


    DataSetController(@Autowired DataSetService dataSetService){
        this.dataSetService = dataSetService;
    }

    /**
     *
     * @param dataInfo 待搜索的数据集信息
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/4 10:51
     **/
    @GetMapping("/PETdatabase/dataset/list")
    public Result searchList(DataSet dataInfo, Integer page, Integer limit, String sort, String word){
        System.out.println(dataInfo + " " + sort);
        DataSetList dataSetList = dataSetService.searchList(dataInfo, page, limit, sort, word);
        return Result.success(dataSetList);
    }

    /**
     *
     * @param word 待搜索的数据集关键词
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/4 10:52
     **/
    @GetMapping("/PETdatabase/dataset/list/searchGlobal")
    public Result globalSearch(String word){
        System.out.println("global search:" + word);
        DataSetList dataSetList = dataSetService.searchAllLike(word);
        return Result.success(dataSetList);
    }

    /**
     *
     * @param id 要查看的数据集 id
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/4 10:53
     **/
    @GetMapping("/PETdatabase/dataset/detail")
    public Result getDetail(Integer id){
        DataSet dataSet = dataSetService.getDetail(id);
        return Result.success(dataSet);
    }

    /**
     *
     *
     * @param id 待删除的文件 id
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/4 10:53
     **/
    @DeleteMapping("/PETdatabase/dataset/manage/deleteFile")
    public Result deleteFile(Integer id){
        try {
            dataSetService.deleteFile(id);
        } catch (Exception e) {
            return Result.error("failed to delete", 40013);
        }
        return Result.success();
    }

    /**
     *
     *
     * @param id 待删除的数据集 id
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/4 10:54
     **/
    @DeleteMapping("/PETdatabase/dataset/manage/delete")
    public Result deleteDataset(Integer id){
        try{
            dataSetService.deleteDataset(id);
        }catch (DataSetException e){
            return Result.error("failed to delete. The dataset might not be empty", 40009);
        }catch (Exception e){
            log.error("删除数据集失败", e);
            return Result.error("failed to delete", 40009);
        }
        return Result.success();
    }
}
