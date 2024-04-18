package com.szbldb.service.datasetService;

import com.szbldb.dao.DataSetMapper;
import com.szbldb.pojo.datasetPojo.DataSet;
import com.szbldb.pojo.datasetPojo.DataSetList;
import com.szbldb.pojo.datasetPojo.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class DataSetService {

    @Autowired
    private DataSetMapper dataSetMapper;

    @Transactional(rollbackFor = Exception.class)
    public DataSetList searchList(DataSet dataSet){
        List<DataSet> dataSets = dataSetMapper.searchLike(dataSet);
        for(DataSet ds : dataSets){
            ds.setFiles(dataSetMapper.getFilesByDatasetId(ds.getId()));
        }
        return new DataSetList(dataSets.size(), dataSets);
    }

    public DataSetList searchAllLike(String word){
        List<DataSet> dataSets = dataSetMapper.searchGlobal(word);
        for(DataSet ds : dataSets){
            ds.setFiles(dataSetMapper.getFilesByDatasetId(ds.getId()));
        }
        return new DataSetList(dataSets.size(), dataSets);
    }

    @Transactional(rollbackFor = Exception.class)
    public DataSet getDetail(Integer id){
        DataSet dataSet = dataSetMapper.getDatasetById(id);
        List<File> files = dataSetMapper.getFilesByDatasetId(dataSet.getId());
        dataSet.setFiles(files);
        return dataSet;
    }
}
