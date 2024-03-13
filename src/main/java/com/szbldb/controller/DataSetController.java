package com.szbldb.controller;

import com.szbldb.pojo.Result;
import com.szbldb.pojo.datasetPojo.DataSet;
import com.szbldb.pojo.datasetPojo.DataSetList;
import com.szbldb.service.DataSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataSetController {

    @Autowired
    private DataSetService dataSetService;

    @RequestMapping("/PETdatabase/dataset/list/searchBy")
    public Result searchList(DataSet dataInfo){
        DataSetList dataSetList = dataSetService.searchList(dataInfo);
        return Result.success(dataSetList);
    }

    @RequestMapping("/PETdatabase/dataset/list/searchGlobal")
    public Result globalSearch(String word){
        DataSetList dataSetList = dataSetService.searchAllLike(word);
        return Result.success(dataSetList);
    }

    @RequestMapping("/PETdatabase/dataset/list/searchCountry")
    public Result searchCountry(String country){
        DataSetList dataSetList = dataSetService.searchByCountry(country);
        return Result.success(dataSetList);
    }
}
