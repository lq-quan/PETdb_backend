package com.szbldb.service.extensionService;

import com.szbldb.dao.ExtensionMapper;
import com.szbldb.exception.ExtensionException;
import com.szbldb.pojo.datasetPojo.DataSet;
import com.szbldb.pojo.extensionPojo.Collection;
import com.szbldb.pojo.extensionPojo.CollectionList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class CollectionService {
    private final ExtensionMapper extensionMapper;

    public CollectionService(@Autowired ExtensionMapper extensionMapper){
        this.extensionMapper = extensionMapper;
    }

    @Transactional
    public Integer createCollection(String username, Collection collection){
        collection.setCreateTime(LocalDateTime.now());
        if(extensionMapper.checkCollectionCount(username) == 10 ||
                extensionMapper.checkIfExisted(username, collection.getName()) == 1){
            throw new ExtensionException("数量超出限制或名称重复");
        }
        extensionMapper.createCollection(username, collection);
        return collection.getId();
    }

    public CollectionList getCollectionList(String username){
        List<Collection> items = extensionMapper.getCollectionList(username);
        CollectionList list = new CollectionList();
        list.setItems(items);
        list.setTotal(items.size());
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    public Collection getCollectionDetail(Integer cid, String username) throws ExtensionException {
        if(extensionMapper.checkIfUserColl(username, cid) != 1){
            ExtensionException exception = new ExtensionException("用户试图获取非本人Collection");
            log.warn("用户试图获取非本人Collection", exception);
            throw exception;
        }
        List<DataSet> dataSets = extensionMapper.getDatasetInColl(cid);
        Collection collection = new Collection();
        collection.setList(dataSets);
        collection.setTotal(dataSets.size());
        return collection;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCollection(Integer cid, String username) throws ExtensionException {
        if(extensionMapper.checkIfUserColl(username, cid) != 1){
            ExtensionException exception = new ExtensionException("用户试图删除非本人Collection");
            log.warn("用户试图删除非本人Collection", exception);
            throw exception;
        }
        if(extensionMapper.getDSetCountInColl(cid) > 0) return false;
        extensionMapper.deleteColl(cid, username);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public void addDatasetToCollection(Integer did, Integer cid, String username) throws ExtensionException {
        if(extensionMapper.checkIfUserColl(username, cid) != 1){
            ExtensionException exception = new ExtensionException("用户试图修改非本人Collection");
            log.warn("用户试图修改非本人Collection", exception);
            throw exception;
        }
        extensionMapper.insertDatasetToColl(did, cid);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteDatasetFromCollection(Integer did, Integer cid, String username) throws ExtensionException {
        if(extensionMapper.checkIfUserColl(username, cid) != 1){
            ExtensionException exception = new ExtensionException("用户试图修改非本人Collection");
            log.warn("用户试图修改非本人Collection", exception);
            throw exception;
        }
        extensionMapper.deleteDatasetFromColl(did, cid);
    }
}
