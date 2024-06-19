package com.szbldb.controller.datasetController;

import com.szbldb.pojo.Result;
import com.szbldb.pojo.datasetPojo.DataSet;
import com.szbldb.pojo.datasetPojo.DataSetList;
import com.szbldb.service.datasetService.DataSetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class DataSetController {

    private final DataSetService dataSetService;


    DataSetController(@Autowired DataSetService dataSetService){
        this.dataSetService = dataSetService;
    }

    @RequestMapping("/PETdatabase/dataset/list")
    public Result searchList(DataSet dataInfo){
        DataSetList dataSetList = dataSetService.searchList(dataInfo);
        return Result.success(dataSetList);
    }

    @RequestMapping("/PETdatabase/dataset/list/searchGlobal")
    public Result globalSearch(String word){
        System.out.println("global search:" + word);
        DataSetList dataSetList = dataSetService.searchAllLike(word);
        return Result.success(dataSetList);
    }

    @RequestMapping("/PETdatabase/dataset/detail")
    public Result getDetail(Integer id){
        DataSet dataSet = dataSetService.getDetail(id);
        return Result.success(dataSet);
    }

    @RequestMapping("/PETdatabase/dataset/deleteFile")
    public Result deleteFile(Integer id){
        try {
            dataSetService.deleteFile(id);
        } catch (Exception e) {
            log.error("删除文件失败", e);
        }
        return Result.success();
    }

    @RequestMapping("/PETdatabase/dataset/delete")
    public Result deleteDataset(Integer id){
        try{
            dataSetService.deleteDataset(id);
        }catch (RuntimeException e){
            return Result.error("failed to delete. The dataset might not be empty", 40009);
        }catch (Exception e){
            log.error("删除数据集失败", e);
            return Result.error("failed to delete", 40009);
        }
        return Result.success();
    }
}
