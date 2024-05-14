package com.szbldb.controller.datasetController;

import com.szbldb.pojo.Result;
import com.szbldb.pojo.datasetPojo.DataSet;
import com.szbldb.pojo.datasetPojo.DataSetList;
import com.szbldb.service.datasetService.DataSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public Result deleteFile(Integer id) throws Exception{
        dataSetService.deleteFile(id);
        return Result.success();
    }

    @RequestMapping("/PETdatabase/dataset/delete")
    public Result deleteDataset(Integer id) throws Exception{
        try{
            dataSetService.deleteDataset(id);
        }catch (RuntimeException e){
            e.printStackTrace();
            return Result.error("failed to delete. The dataset might not be empty", 40009);
        }
        return Result.success();
    }
}
