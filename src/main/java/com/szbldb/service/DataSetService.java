package com.szbldb.service;

import com.szbldb.dao.DataSetMapper;
import com.szbldb.pojo.datasetPojo.DataSet;
import com.szbldb.pojo.datasetPojo.DataSetList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class DataSetService {

    @Autowired
    private DataSetMapper dataSetMapper;

    public DataSetList searchList(DataSet dataSet){
        List<DataSet> dataSets = dataSetMapper.searchLike(dataSet);
        return new DataSetList(dataSets.size(), dataSets);
    }

    public DataSetList searchAllLike(String word){
        List<DataSet> dataSets = dataSetMapper.searchGlobal(word);
        return new DataSetList(dataSets.size(), dataSets);
    }

    public DataSetList searchByCountry(String country){
        List<DataSet> dataSets = dataSetMapper.searchByCountry(country);
        return new DataSetList(dataSets.size(), dataSets);
    }
}
