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

    /**
     *
     * @Description 创建 Collection
     * @param username 用户名
     * @param collection Collection 信息
     * @return java.lang.Integer
     * @author Quan Li 2024/7/5 15:49
     **/
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

    /**
     *
     * @Description 获取 Collection 列表
     * @param username 用户名
     * @return com.szbldb.pojo.extensionPojo.CollectionList
     * @author Quan Li 2024/7/5 15:50
     **/
    public CollectionList getCollectionList(String username){
        List<Collection> items = extensionMapper.getCollectionList(username);
        CollectionList list = new CollectionList();
        list.setItems(items);
        list.setTotal(items.size());
        return list;
    }

    /**
     *
     * @Description 获取指定 Collection 内容
     * @param cid Collection id
     * @param username 用户名
     * @return com.szbldb.pojo.extensionPojo.Collection
     * @author Quan Li 2024/7/5 15:50
     **/
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

    /**
     *
     * @Description 删除指定 Collection
     * @param cid Collection id
     * @param username 用户名
     * @return boolean
     * @author Quan Li 2024/7/5 15:52
     **/
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

    /**
     *
     * @Description 在指定 Collection 中加入指定数据集，返回值 0 - 添加成功；1 -数据集不存在；2 -数据集已添加过；3 -Collection已满
     * @param did 数据集 id
     * @param cid Collection id
     * @param username 用户名
     * @author Quan Li 2024/7/5 15:52
     **/
    @Transactional(rollbackFor = Exception.class)
    public Integer addDatasetToCollection(Integer did, Integer cid, String username) throws ExtensionException {
        DataSet dataset = extensionMapper.checkDataset(did);
        if(dataset == null) return 1;
        if(extensionMapper.checkIfUserColl(username, cid) != 1){
            ExtensionException exception = new ExtensionException("用户试图修改非本人Collection");
            log.warn("用户试图修改非本人Collection", exception);
            throw exception;
        }
        if(extensionMapper.checkIfDsetInColl(did, cid) == 1) return 2;
        if(extensionMapper.getDSetCountInColl(cid) == 50) return 3;
        extensionMapper.insertDatasetToColl(did, cid, dataset.getStatus(), dataset.getName());
        return 0;
    }

    /**
     *
     * @Description 删除指定 Collection 中的指定数据集
     * @param did 数据集 id
     * @param cid Collection id
     * @param username 用户名
     * @author Quan Li 2024/7/5 15:53
     **/
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
